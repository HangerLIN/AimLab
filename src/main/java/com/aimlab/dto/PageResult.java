package com.aimlab.dto;

import java.util.List;

/**
 * 通用分页结果
 */
public class PageResult<T> {
    private long total;
    private List<T> items;

    public PageResult(long total, List<T> items) {
        this.total = total;
        this.items = items;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }
}
