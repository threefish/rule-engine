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

package cn.xjbpm.rule.engine;

import cn.xjbpm.rule.common.utils.JsonUtils;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiRobotSendRequest;
import com.dingtalk.api.response.OapiRobotSendResponse;
import com.dingtalk.open.app.api.OpenDingTalkClient;
import com.dingtalk.open.app.api.OpenDingTalkStreamClientBuilder;
import com.dingtalk.open.app.api.callback.OpenDingTalkCallbackListener;
import com.dingtalk.open.app.api.security.AuthClientCredential;
import com.taobao.api.ApiException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Map;


/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2025/11/20
 */
@Slf4j
public class Main {

    public static void main(String[] args) throws Exception {

        OpenDingTalkClient client = OpenDingTalkStreamClientBuilder
                .custom()
                .credential(new AuthClientCredential("ding0gmtosk95ghgqprj", "g7oQb89orRuPV5MSDvubhtazleH_nganlCqnKdanKScYG3MITN1886F-ZIQDGQB-"))
                .registerCallbackListener("/v1.0/im/bot/messages/get", new RobotMsgCallbackConsumer())
                .build();
        client.start();
    }

    public static class RobotMsgCallbackConsumer implements OpenDingTalkCallbackListener<Map, Map> {


        @SneakyThrows
        @Override
        public Map execute(Map request) {
            System.out.println(JsonUtils.obj2Json(request));
            sendMessageWebhook(String.valueOf(request.get("sessionWebhook")),String.valueOf(request.get("senderStaffId")));
            return request;
        }
    }

    public static void sendMessageWebhook(String sessionWebhook,String senderStaffId) throws ApiException {
        DingTalkClient client = new DefaultDingTalkClient(sessionWebhook);
        OapiRobotSendRequest request = new OapiRobotSendRequest();
        request.setMsgtype("text");
        OapiRobotSendRequest.Text text = new OapiRobotSendRequest.Text();
        text.setContent("测试文本消息");
        request.setText(text);
        OapiRobotSendRequest.At at = new OapiRobotSendRequest.At();
        at.setAtUserIds(Arrays.asList(senderStaffId));
        at.setIsAtAll(false);
        request.setAt(at);
        OapiRobotSendResponse response = client.execute(request);
        System.out.println(response.getBody());
    }
}