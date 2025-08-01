package com.example.lume.components;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LumeMetadata {
    @JsonProperty
    private Map<String, BookMetadata> bookMetaDataMap;

    @JsonProperty
    private Map<String, List<String>> categoryMap;

    @JsonProperty
    private Settings settings;

    @JsonCreator
    public LumeMetadata(
            @JsonProperty("bookMetaDataMap") Map<String, BookMetadata> bookMetaDataMap,
            @JsonProperty("categoryMap") Map<String, List<String>> categoryMap,
            @JsonProperty("settings") Settings settings
    ) {
        this.bookMetaDataMap =  bookMetaDataMap;
        this.categoryMap = categoryMap;
        this.settings = settings;
    }

    public Map<String, BookMetadata> getBookMetaDataMap() { return bookMetaDataMap; }
    public Map<String, List<String>> getCategoryMap() { return categoryMap; }
    public Settings getSettings() { return settings; }

    public LumeMetadata() {
        this.bookMetaDataMap = new HashMap<String, BookMetadata>();
        this.categoryMap = new HashMap<String, List<String>>();
        this.settings = new Settings("dark", "Alice", "20px");
    }

    public void addBookMetadata(String uuid, BookMetadata bookMetadata) {
        bookMetaDataMap.put(uuid, bookMetadata);
    }

    public void addCategory(String category, String bookUUID) {
        if (categoryMap.containsKey(category)) {
            categoryMap.get(category).add(bookUUID);
        } else {
            List<String> bookUUIDList = new ArrayList<>(List.of(bookUUID));
            categoryMap.put(category, bookUUIDList);
        }
    }

    public void setSettings(Settings settings) { this.settings = settings; }
}
