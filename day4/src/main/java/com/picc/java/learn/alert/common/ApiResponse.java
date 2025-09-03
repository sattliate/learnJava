package com.picc.java.learn.alert.common;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * 统一API响应类
 * 
 * @author learn-java
 * @since 2025-08-30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    /**
     * 响应是否成功
     */
    private Boolean success;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 响应时间戳
     */
    private Long timestamp;

    /**
     * 错误代码（失败时使用）
     */
    private String errorCode;

    /**
     * 分页信息（分页查询时使用）
     */
    private PageInfo pageInfo;

    /**
     * 创建成功响应
     * 
     * @param data 响应数据
     * @param <T> 数据类型
     * @return API响应
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "操作成功", data, System.currentTimeMillis(), null, null);
    }

    /**
     * 创建成功响应
     * 
     * @param data 响应数据
     * @param message 响应消息
     * @param <T> 数据类型
     * @return API响应
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, message, data, System.currentTimeMillis(), null, null);
    }

    /**
     * 创建成功响应（无数据）
     * 
     * @param message 响应消息
     * @return API响应
     */
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message, null, System.currentTimeMillis(), null, null);
    }

    /**
     * 创建失败响应
     * 
     * @param message 错误消息
     * @return API响应
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, System.currentTimeMillis(), null, null);
    }

    /**
     * 创建失败响应
     * 
     * @param message 错误消息
     * @param errorCode 错误代码
     * @return API响应
     */
    public static <T> ApiResponse<T> error(String message, String errorCode) {
        return new ApiResponse<>(false, message, null, System.currentTimeMillis(), errorCode, null);
    }

    /**
     * 创建分页响应
     * 
     * @param data 分页数据
     * @param pageInfo 分页信息
     * @param <T> 数据类型
     * @return API响应
     */
    public static <T> ApiResponse<List<T>> page(List<T> data, PageInfo pageInfo) {
        return new ApiResponse<>(true, "查询成功", data, System.currentTimeMillis(), null, pageInfo);
    }

    /**
     * 分页信息类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageInfo {
        /**
         * 当前页码
         */
        private Integer pageNum;

        /**
         * 每页大小
         */
        private Integer pageSize;

        /**
         * 总记录数
         */
        private Long total;

        /**
         * 总页数
         */
        private Integer totalPages;

        /**
         * 是否有下一页
         */
        private Boolean hasNext;

        /**
         * 是否有上一页
         */
        private Boolean hasPrevious;

        /**
         * 创建分页信息
         * 
         * @param pageNum 当前页码
         * @param pageSize 每页大小
         * @param total 总记录数
         * @return 分页信息
         */
        public static PageInfo of(Integer pageNum, Integer pageSize, Long total) {
            int totalPages = (int) Math.ceil((double) total / pageSize);
            return new PageInfo(
                pageNum,
                pageSize,
                total,
                totalPages,
                pageNum < totalPages,
                pageNum > 1
            );
        }
    }
}
