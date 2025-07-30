package com.example.lume.components;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class Annotation implements Serializable {
    private String parentId;
    private int start;
    private int end;
    private String color;
    private String annotationId;

    @JsonCreator
    public Annotation(
            @JsonProperty("parentId") String parentId,
            @JsonProperty("start") int start,
            @JsonProperty("end") int end,
            @JsonProperty("color") String color,
            @JsonProperty("annotationId") String annotationId
    ) {
        this.parentId = parentId;
        this.start = start;
        this.end = end;
        this.color = color;
        this.annotationId = annotationId;
    }

    public String getParentId() { return parentId; }
    public int getStart() { return start; }
    public int getEnd() { return end; }
    public String getColor() { return color; }
    public String getAnnotationId() { return annotationId; }

    public void setParentId(String parentId) { this.parentId = parentId; }
    public void setStart(String start) { this.start = Integer.parseInt(start); }
    public void setEnd(String end) { this.end = Integer.parseInt(end); }
    public void setColor(String color) { this.color = color; }
    public void setAnnotationId(String annotationId) { this.annotationId = annotationId; }

    @Override
    public String toString() {
        return "Annotation{" +
                "parentId='" + parentId + '\'' +
                ", start=" + start +
                ", end=" + end +
                ", color='" + color + '\'' +
                ", annotationId='" + annotationId + '\'' +
                '}';
    }
}
