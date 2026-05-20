package com.app.dto;

import java.util.List;
import java.util.Map;

public class ImportResult {

    private int totalCount;
    private int successCount;
    private int failCount;
    private List<Map<String, String>> failedRows;

    public int getTotalCount() { return totalCount; }
    public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
    public int getSuccessCount() { return successCount; }
    public void setSuccessCount(int successCount) { this.successCount = successCount; }
    public int getFailCount() { return failCount; }
    public void setFailCount(int failCount) { this.failCount = failCount; }
    public List<Map<String, String>> getFailedRows() { return failedRows; }
    public void setFailedRows(List<Map<String, String>> failedRows) { this.failedRows = failedRows; }
}
