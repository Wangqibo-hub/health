package com.itheima.entity;

import java.io.Serializable;
import java.util.List;

/**
 * 分页结果封装对象
 */
public class PageResult implements Serializable{
    private Long total;//总记录数
    private List rows;//当前页结果
    private Integer currentPage;//页码 1

    public PageResult(Long total, List rows, Integer currentPage) {
        super();
        this.total = total;
        this.rows = rows;
        this.currentPage = currentPage;
    }

    public PageResult(Long total, List rows) {
        super();
        this.total = total;
        this.rows = rows;
    }

    public Integer getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }

    public Long getTotal() {
        return total;
    }
    public void setTotal(Long total) {
        this.total = total;
    }
    public List getRows() {
        return rows;
    }
    public void setRows(List rows) {
        this.rows = rows;
    }
}
