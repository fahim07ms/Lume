package com.example.lume.layouts;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Screen;

public class BaseLayout extends Parent {
    
    public HBox homeLayout;
    public VBox leftSidePanel;
    public VBox rightSidePanel;
    
    static double homeLayoutWidth = Screen.getPrimary().getVisualBounds().getWidth();
    static double homeLayoutHeight = Screen.getPrimary().getVisualBounds().getHeight();
    static double leftSidePanelWidth = 400;
    static double leftSidePanelHeight = homeLayoutHeight;
    static double rightSidePanelWidth = homeLayoutWidth - leftSidePanelWidth;
    static double rightSidePanelHeight = homeLayoutHeight;
    
    public BaseLayout() {
        super();
        // Set up Home Layout width, height and className
        homeLayout = new HBox();
        homeLayout.setMaxSize(homeLayoutWidth, homeLayoutHeight);
        homeLayout.getStyleClass().add("home-layout");
        
        // Set up left side panel width, height, className, position and padding
        leftSidePanel = new VBox();
        leftSidePanel.getStyleClass().add("left-side-panel");
        leftSidePanel.setAlignment(Pos.TOP_CENTER);
        leftSidePanel.setPrefSize(leftSidePanelWidth, leftSidePanelHeight);
        leftSidePanel.setPadding(new Insets(10, 15, 20, 15));
        leftSidePanel.setBorder(new Border(new BorderStroke(Color.rgb(87, 87, 87, 0.94), BorderStrokeStyle.SOLID, null, new BorderWidths(0, 0.5, 0, 0))));

        ScrollPane leftScrollPane = new ScrollPane(leftSidePanel);
        leftScrollPane.setFitToWidth(true);
        leftScrollPane.setFitToHeight(true);
        leftScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        leftScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        leftScrollPane.setPrefSize(leftSidePanelWidth, leftSidePanelHeight);

        
        // Set up right side panel width, height, className, position
        rightSidePanel = new VBox();
        rightSidePanel.getStyleClass().add("right-side-panel");
        rightSidePanel.setAlignment(Pos.TOP_CENTER);
        rightSidePanel.setPrefSize(rightSidePanelWidth, rightSidePanelHeight);

        ScrollPane rightScrollPane = new ScrollPane(rightSidePanel);
        rightScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        rightScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        homeLayout.getChildren().addAll(leftScrollPane, rightScrollPane);

        this.getChildren().add(homeLayout);
    }

//    public void setHomeLayoutWidth(double width) {
//        homeLayoutWidth = width;
//    }
//
//    public void setHomeLayoutHeight(double height) {
//        homeLayoutHeight = height;
//    }
//
//    public void setLeftSidePanelWidth(double width) {
//        leftSidePanelWidth = width;
//    }
//
//    public void setLeftSidePanelHeight(double height) {
//        leftSidePanelHeight = height;
//    }
//
//    public void setRightSidePanelWidth(double width) {
//        rightSidePanelWidth = width;
//    }
//
//    public void setRightSidePanelHeight(double height) {
//        rightSidePanelHeight = height;
//    }

    public double getHomeLayoutHeight() {
        return homeLayoutHeight;
    }

    public double getHomeLayoutWidth() {
        return homeLayoutWidth;
    }

    public double getLeftSidePanelHeight() {
        return leftSidePanelHeight;
    }

    public double getLeftSidePanelWidth() {
        return leftSidePanelWidth;
    }

    public double getRightSidePanelHeight() {
        return rightSidePanelHeight;
    }

    public double getRightSidePanelWidth() {
        return rightSidePanelWidth;
    }
}

