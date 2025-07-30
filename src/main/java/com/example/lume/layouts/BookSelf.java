package com.example.lume.layouts;


import com.example.lume.components.BookDisplay;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;

public class BookSelf extends GridPane {
    private final int MAX_COL = 4;

    private int curRow = 0;
    private int curCol = 0;

    public BookSelf() {
        super();
        this.getStyleClass().add("book-self");

        this.setHgap(15);
        this.setVgap(15);
    }

    public void addBookDisplay(BookDisplay bookDisplay) {
        this.add(bookDisplay, curCol, curRow);
        if (curCol == MAX_COL) {
            curCol = 0;
            curRow++;
        } else curCol++;
    }

    public void removeBookDisplay(BookDisplay bookDisplay) {
        this.getChildren().remove(bookDisplay);
        if (curCol == 0) {
            curCol = MAX_COL;
            curRow--;
        } else curCol--;
    }

    public boolean isEmpty() {
        return (curCol == 0 && curRow == 0);
    }
}
