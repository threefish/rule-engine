package cn.xjbpm.rule.vo.common;

import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * @author 黄川 huchuc@vip.qq.com
 * date: 2025/12/2
 */
@Data
public class PageVO<T> {

    private Long totalElements;
    private int totalPages;
    private int number;
    private int size;
    private List<T> content;


    public static <T> PageVO<T> of(Page<T> page) {
        PageVO<T> vo = new PageVO<>();
        vo.setTotalElements(page.getTotalElements());
        vo.setTotalPages(page.getTotalPages());
        vo.setContent(page.getContent());
        vo.setNumber(page.getNumber());
        vo.setSize(page.getSize());
        return vo;
    }

    public static <T> PageVO<T> of(Page page, List<T> content) {
        PageVO<T> vo = new PageVO<>();
        vo.setTotalElements(page.getTotalElements());
        vo.setTotalPages(page.getTotalPages());
        vo.setContent(content);
        vo.setNumber(page.getNumber());
        vo.setSize(page.getSize());
        return vo;
    }

}
