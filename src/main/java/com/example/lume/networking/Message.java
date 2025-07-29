package com.example.lume.networking;

import com.example.lume.components.Annotation;

public class Message {
    private String bookId;
    private Annotation annotation;

    public Message(String bookId, Annotation annotation) {
        this.bookId = bookId;
        this.annotation = annotation;
    }

    public void setBookId(String bookId) { this.bookId = bookId; }
    public void setAnnotation(Annotation annotation) { this.annotation = annotation; }
    public String getBookId() { return bookId; }
    public Annotation getAnnotation() { return annotation; }
}
