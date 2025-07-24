package com.example.lume.components;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class BookMetadata {
    private String title;
    private List<String> authors;
    private int totalSpread;
    private int currentSpread;
    private String filePath;
    private String category;

    @JsonCreator
    public BookMetadata(
            @JsonProperty("title") String title,
            @JsonProperty("authors") List<String> authors,
            @JsonProperty("totalSpread") int totalSpread,
            @JsonProperty("currentSpread") int currentSpread,
            @JsonProperty("filePath") String filePath,
            @JsonProperty("category") String category
    ) {
        this.title = title;
        this.authors = authors;
        this.totalSpread = totalSpread;
        this.currentSpread = currentSpread;
        this.filePath = filePath;
        this.category = category;
    }

    public String getTitle() { return title; }

    public List<String> getAuthors() { return authors; }

    public int getTotalSpread() { return totalSpread; }

    public int getCurrentSpread() { return currentSpread; }

    public String getFilePath() { return filePath; }

    public String getCategory() { return category; }

    public void setTitle(String title) { this.title = title; }

    public void setAuthors(List<String> authors) { this.authors = authors; }

    public void setTotalSpread(int totalSpread) { this.totalSpread = totalSpread; }

    public void setCurrentSpread(int currentSpread) { this.currentSpread = currentSpread; }

    public void setFilePath(String filePath) { this.filePath = filePath; }

    public void setCategory(String category) { this.category = category; }
}