package com.example.lume.components;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Settings {
    private String theme;
    private String font;
    private String fontSize;

    @JsonCreator
    public Settings(
            @JsonProperty("theme") String theme,
            @JsonProperty("font") String font,
            @JsonProperty("fontSize") String fontSize) {
        this.theme = theme;
        this.font = font;
        this.fontSize = fontSize;
    }

    public String getTheme() { return theme; }
    public String getFont() { return font; }
    public String getFontSize() { return fontSize; }

    public void setTheme(String theme) { this.theme = theme; }
    public void setFont(String font) { this.font = font; }
    public void setFontSize(String fontSize) { this.fontSize = fontSize; }
}
