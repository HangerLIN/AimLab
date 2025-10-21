package com.aimlab.dto;

/**
 * 文件导出结果
 */
public class ExportFile {
    private final String fileName;
    private final String contentType;
    private final byte[] data;

    public ExportFile(String fileName, String contentType, byte[] data) {
        this.fileName = fileName;
        this.contentType = contentType;
        this.data = data;
    }

    public String getFileName() {
        return fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public byte[] getData() {
        return data;
    }
}
