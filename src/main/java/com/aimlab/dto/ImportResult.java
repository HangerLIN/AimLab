package com.aimlab.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 导入结果
 */
public class ImportResult {
    private int successCount;
    private final List<String> errors = new ArrayList<>();

    public int getSuccessCount() {
        return successCount;
    }

    public void incrementSuccess() {
        this.successCount++;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void addError(String error) {
        this.errors.add(error);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}
