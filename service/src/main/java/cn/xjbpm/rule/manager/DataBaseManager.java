/**
 * Copyright 2025 threefish.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.xjbpm.rule.manager;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.zaxxer.hikari.HikariDataSource;
import org.nutz.dao.Dao;
import org.nutz.dao.impl.NutDao;
import org.nutz.lang.Lang;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author 黄川 huchuc@vip.qq.com
 */
@Service
public class DataBaseManager implements DisposableBean {

    /**
     * 构建 Guava Cache
     * 1. expireAfterAccess(30, Minutes): 典型的 LRU 策略，30分钟未被访问则过期
     * 2. removalListener: 当缓存失效（过期或被剔除）时，关闭数据源
     */
    private final Cache<String, Dao> daoCache = CacheBuilder.newBuilder()
            .expireAfterAccess(30, TimeUnit.MINUTES)
            .removalListener((RemovalListener<String, Dao>) notification -> {
                Dao dao = notification.getValue();
                if (dao instanceof NutDao) {
                    NutDao nutDao = (NutDao) dao;
                    if (nutDao.getDataSource() instanceof HikariDataSource) {
                        ((HikariDataSource) nutDao.getDataSource()).close();
                    }
                }
            })
            .build();

    public Dao getOrCreateDao(Map<String, String> authData) {
        // Key 确保同一库地址+账号能命中缓存
        String cacheKey = Lang.md5(authData.get("url") + authData.get("username"));
        try {
            return daoCache.get(cacheKey, () -> {
                HikariDataSource dataSource = new HikariDataSource();
                dataSource.setJdbcUrl(authData.get("url"));
                dataSource.setUsername(authData.get("username"));
                dataSource.setPassword(authData.get("password"));
                dataSource.setDriverClassName(authData.get("driverClassName"));

                // 针对动态创建数据源的连接池调优
                dataSource.setMinimumIdle(1);
                dataSource.setMaximumPoolSize(5); // 限制单池大小，防止动态源过多撑爆数据库连接数
                dataSource.setPoolName("Hikari-Pool-" + cacheKey.substring(0, 8));

                return createNutDao(dataSource);
            });
        } catch (ExecutionException e) {
            throw new RuntimeException("Failed to initialize Dao for: " + authData.get("url"), e);
        }
    }

    private NutDao createNutDao(HikariDataSource dataSource) {
        NutDao nutDao = new NutDao(dataSource);
        nutDao.setRunner((ds, callback) -> {
            // 与 Spring 事务同步
            Connection connection = DataSourceUtils.getConnection(ds);
            try {
                callback.invoke(connection);
            } catch (Exception e) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                } else {
                    throw new RuntimeException(e);
                }
            } finally {
                DataSourceUtils.releaseConnection(connection, ds);
            }
        });
        return nutDao;
    }


    @Override
    public void destroy() {
        daoCache.invalidateAll();
    }
}