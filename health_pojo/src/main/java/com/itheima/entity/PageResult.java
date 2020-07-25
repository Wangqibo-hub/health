package com.itheima.entity;

import java.io.Serializable;
import java.util.List;

/**
 * 分页结果封装对象
 */
public class PageResult implements Serializable{
    private Integer currentPage;//页码 1
    private Long total;//总记录数
    private List rows;//当前页结果


    public PageResult(Long total, List rows) {
        super();
        this.total = total;
        this.rows = rows;
    }

    public PageResult(Long total, List rows , Integer currentPage) {
        this.currentPage = currentPage;
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
