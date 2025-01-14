package com.project.findsimilartracks.dto;

public class FilePathResponse {
    private String filePath;

    // Конструктор
    public FilePathResponse(String filePath) {
        this.filePath = filePath;
    }

    // Геттер и сеттер
    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}