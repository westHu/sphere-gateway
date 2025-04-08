package com.sphere.api.vo;

import com.sphere.api.vo.BaseResult;
import com.sphere.common.constants.GatewayConstant;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 分页响应结果
 * 用于封装分页查询的响应数据，包含总记录数和分页数据列表
 * 继承自BaseResult，提供分页相关的特定功能
 *
 * @author sphere
 * @since 1.0.0
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
public class PageResult<T> extends BaseResult {

    /**
     * 总记录数
     * 表示符合条件的总数据条数
     * 用于前端分页计算和展示
     */
    private Long total;

    /**
     * 分页数据列表
     * 当前页的数据内容
     * 泛型T表示具体的数据类型
     */
    private List<T> data;

    /**
     * 创建分页成功响应
     * 使用默认成功消息
     *
     * @param total 总记录数
     * @param data 分页数据列表
     * @param <T> 数据类型
     * @return 分页响应结果
     */
    public static <T> PageResult<T> success(Long total, List<T> data) {
        log.debug("创建分页响应 - 总记录数: {}, 数据条数: {}", total, data != null ? data.size() : 0);
        PageResult<T> result = new PageResult<>();
        result.setCode(GatewayConstant.SUCCESS);
        result.setMessage("success");
        result.setTotal(total);
        result.setData(data);
        return result;
    }

    /**
     * 创建分页成功响应
     * 使用自定义成功消息
     *
     * @param total 总记录数
     * @param data 分页数据列表
     * @param message 自定义成功消息
     * @param <T> 数据类型
     * @return 分页响应结果
     */
    public static <T> PageResult<T> success(Long total, List<T> data, String message) {
        log.debug("创建分页响应 - 总记录数: {}, 数据条数: {}, 消息: {}", 
            total, data != null ? data.size() : 0, message);
        PageResult<T> result = new PageResult<>();
        result.setCode(GatewayConstant.SUCCESS);
        result.setMessage(message);
        result.setTotal(total);
        result.setData(data);
        return result;
    }
}
