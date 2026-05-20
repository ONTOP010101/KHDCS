package com.app.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public class PageQuery {

    private long current = 1;
    private long size = 10;
    private String keyword;

    public long getCurrent() { return current; }
    public void setCurrent(long current) { this.current = current; }
    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }

    public <T> IPage<T> toPage() {
        return new Page<>(current, size);
    }
}
