package com.example.lume.components;

public class Metadata {
    private String fileName;
    private String filePath;
    private String fileExtension;
    private String author;
    private String title;
    private String description;
    private String coverImagePath;
    private String coverImageExtension;

    public Metadata(String fileName, String filePath, String fileExtension, String author, String title, String description, String coverImagePath, String coverImageExtension) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileExtension = fileExtension;
        this.author = author;
        this.title = title;
        this.description = description;
        this.coverImagePath = coverImagePath;
        this.coverImageExtension = coverImageExtension;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCoverImagePath() {
        return coverImagePath;
    }

    public void setCoverImagePath(String coverImagePath) {
        this.coverImagePath = coverImagePath;
    }

    public String getCoverImageExtension() {
        return coverImageExtension;
    }

    public void setCoverImageExtension(String coverImageExtension) {
        this.coverImageExtension = coverImageExtension;
    }
}
