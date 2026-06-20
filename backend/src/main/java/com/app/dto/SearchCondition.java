package com.app.dto;

/**
 * 综合查询单条件
 */
public class SearchCondition {
    private String field;
    private String operator;  // eq, ne, like, gt, lt, ge, le
    private String value;

    public SearchCondition() {}

    public SearchCondition(String field, String operator, String value) {
        this.field = field;
        this.operator = operator;
        this.value = value;
    }

    public String getField() { return field; }
    public void setField(String field) { this.field = field; }
    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    public boolean isValid() {
        return field != null && !field.isEmpty() && operator != null && value != null && !value.isEmpty();
    }
}
