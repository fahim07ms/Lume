package com.example.lume.components;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class BookMetadata {
    private String title;
    private List<String> authors;
    private int totalSpread;
    private int currentSpread;
    private String filePath;
    private String category;
    private List<Annotation> annotations;
    private List<Integer> bookmarkedSpreads;

    @JsonCreator
    public BookMetadata(
            @JsonProperty("title") String title,
            @JsonProperty("authors") List<String> authors,
            @JsonProperty("totalSpread") int totalSpread,
            @JsonProperty("currentSpread") int currentSpread,
            @JsonProperty("filePath") String filePath,
            @JsonProperty("category") String category,
            @JsonProperty("annotations") List<Annotation> annotations,
            @JsonProperty("bookmarkedSpreads") List<Integer> bookmarkedSpreads
    ) {
        this.title = title;
        this.authors = authors;
        this.totalSpread = totalSpread;
        this.currentSpread = currentSpread;
        this.filePath = filePath;
        this.category = category;
        this.annotations = annotations != null ? annotations : new ArrayList<>();
        this.bookmarkedSpreads = bookmarkedSpreads != null ? bookmarkedSpreads : new ArrayList<>();
    }

    public String getTitle() { return title; }

    public List<String> getAuthors() { return authors; }

    public int getTotalSpread() { return totalSpread; }

    public int getCurrentSpread() { return currentSpread; }

    public String getFilePath() { return filePath; }

    public String getCategory() { return category; }

    public List<Annotation> getAnnotations() { return annotations; }

    public List<Integer> getBookmarkedSpreads() { return bookmarkedSpreads; }

    public void setTitle(String title) { this.title = title; }

    public void setAuthors(List<String> authors) { this.authors = authors; }

    public void setTotalSpread(int totalSpread) { this.totalSpread = totalSpread; }

    public void setCurrentSpread(int currentSpread) { this.currentSpread = currentSpread; }

    public void setFilePath(String filePath) { this.filePath = filePath; }

    public void setCategory(String category) { this.category = category; }

    public void setAnnotations(List<Annotation> annotations) { this.annotations = annotations; }

    public void setBookmarkedSpreads(List<Integer> bookmarkedSpreads) { this.bookmarkedSpreads = bookmarkedSpreads; }
}