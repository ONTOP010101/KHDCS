package com.app.dto;

import java.util.List;

/**
 * 综合查询请求
 */
public class AdvancedSearchRequest {
    private List<SearchCondition> conditions;

    public List<SearchCondition> getConditions() { return conditions; }
    public void setConditions(List<SearchCondition> conditions) { this.conditions = conditions; }
}
