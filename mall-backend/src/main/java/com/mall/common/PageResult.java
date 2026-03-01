package com.mall.common;

import com.github.pagehelper.PageInfo;
import lombok.Data;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Data
public class PageResult<T> {
    private int pageNum;
    private int pageSize;
    private long total;
    private int totalPages;
    private List<T> list;

    /**
     * 普通封装：直接将 PageInfo 转为 PageResult (不涉及类型转换)
     */
    public static <T> PageResult<T> create(PageInfo<T> pageInfo) {
        PageResult<T> result = new PageResult<>();
        result.setPageNum(pageInfo.getPageNum());
        result.setPageSize(pageInfo.getPageSize());
        result.setTotal(pageInfo.getTotal());
        result.setTotalPages(pageInfo.getPages());
        result.setList(pageInfo.getList());
        return result;
    }

    /**
     * 高级封装：支持 PO -> VO 的转换
     * @param sourcePageInfo 原始查询的分页结果 (PO)
     * @param converter 转换逻辑 (Lambda表达式)
     * @param <T> 原始类型 (PO)
     * @param <R> 目标类型 (VO)
     */
    public static <T, R> PageResult<R> convert(PageInfo<T> sourcePageInfo, Function<T, R> converter) {
        PageResult<R> result = new PageResult<>();
        result.setPageNum(sourcePageInfo.getPageNum());
        result.setPageSize(sourcePageInfo.getPageSize());
        result.setTotal(sourcePageInfo.getTotal());
        result.setTotalPages(sourcePageInfo.getPages());
        // 执行 List 转换
        List<R> convertedList = sourcePageInfo.getList().stream().map(converter).collect(Collectors.toList());
        result.setList(convertedList);
        return result;
    }
}