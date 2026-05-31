package com.app.dto;

import java.util.List;
import java.util.Map;

public class ImportResult {

    private int totalCount;
    private int successCount;
    private int failCount;
    private int duplicateCount;
    private int updatedCount;
    private List<Map<String, String>> failedRows;

    public int getTotalCount() { return totalCount; }
    public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
    public int getSuccessCount() { return successCount; }
    public void setSuccessCount(int successCount) { this.successCount = successCount; }
    public int getFailCount() { return failCount; }
    public void setFailCount(int failCount) { this.failCount = failCount; }
    public int getDuplicateCount() { return duplicateCount; }
    public void setDuplicateCount(int duplicateCount) { this.duplicateCount = duplicateCount; }
    public int getUpdatedCount() { return updatedCount; }
    public void setUpdatedCount(int updatedCount) { this.updatedCount = updatedCount; }
    public List<Map<String, String>> getFailedRows() { return failedRows; }
    public void setFailedRows(List<Map<String, String>> failedRows) { this.failedRows = failedRows; }
}
