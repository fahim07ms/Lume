package com.example.lume.scenes;

import com.example.lume.components.ButtonIcon;
import com.example.lume.components.SidePanelOption;
import com.example.lume.layouts.BaseLayout;
import com.example.lume.layouts.TitleBar;
import io.documentnode.epub4j.domain.*;
import io.documentnode.epub4j.epub.EpubReader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class BookViewScene2 extends BaseLayout {

    // Book view
    private WebView bookWebView;
    private WebEngine bookWebEngine;

    // Index of total pages
    private int currentSpreadIndex = 0;
    private int totalSpreads = 0;

    // Page width,height
    private double pageWidth;
    private double pageHeight;
    private double totalWidth;

    private static final String TITLE_CLASS = "lume-title";
    private static final String CHAPTER_CLASS = "lume-chapter";
    private static final String PARAGRAPH_CLASS = "lume-paragraph";
    private static final String NAVIGATION_CLASS = "lume-navigation";
    private static final String SECTION_CLASS = "lume-section";
    private static final String SUBTITLE_CLASS = "lume-subtitle";

    Set<String> listOfFragmentId = new LinkedHashSet<>();
    Set<String> listOfHref = new LinkedHashSet<>();

    HashMap<String, String> imgResource = new HashMap<String, String>();

    List<String> titleList = new ArrayList<>();
    String coverImg;

    private String fullBookContent = "";

    Book book;

    public BookViewScene2(Stage stage) throws IOException {
        super();

        EpubReader epubReader = new EpubReader();
        book = epubReader.readEpub(new FileInputStream("/home/fahim07/Documents/Books/theodore-roosevelt_through-the-brazilian-wilderness.epub"));


        // Set total width of the webview
        totalWidth = this.getRightSidePanelWidth();

        // Set each page/column width and height
        pageWidth = (totalWidth - 20) / 2;
        pageHeight = this.getRightSidePanelHeight() - 100;

        TitleBar titleBar = new TitleBar(this.getRightSidePanelWidth(), 100, stage);
        try {
            titleBar.setTitleBarText(book.getTitle());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        rightSidePanel.getChildren().add(titleBar);

        showBookInWebView();
        setupKeyboardNavigation();
    }

    public void showBookInWebView() {
        try {
            // Create WebView
            bookWebView = new WebView();
            bookWebEngine = bookWebView.getEngine();
            bookWebView.getStyleClass().addAll("epub-view", "book-web-view");
            bookWebView.setPrefSize(totalWidth, pageHeight);

            rightSidePanel.getChildren().add(bookWebView);

            // Load EPUB content
            loadBook();

            // Add navigation controls
            addNavigationControls(rightSidePanel);

            setupKeyboardNavigation();

        } catch (Exception e) {
            System.out.println("Error in showBookInWebView: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadBook() {
        try {
            String ocpReference = new String(
                    book.getOpfResource().getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8
            );

            Document docOCP = Jsoup.parse(ocpReference);

            docOCP.select("item").forEach(item -> {
                String href = item.attribute("href").getValue();
                if (item.attribute("media-type").getValue().contains("image/")) {
                    Resource res = book.getResources().getByHref(href);
                    try {
                        byte[] imageData = res.getData();

                        // Convert to Base64
                        String base64Data = Base64.getEncoder().encodeToString(imageData);

                        String imgData = "data:" + item.attribute("media-type").getValue() + ";base64," + base64Data;

                        imgResource.put(href, imgData);

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });

            coverImg = imgResource.get(book.getCoverImage().getHref());
            getDataListsFromNCX();


            // Build the complete book content
            StringBuilder bookContent = new StringBuilder();

            for (int i = 0; i < listOfFragmentId.size(); i++) {
                // Href and title
                String fragmentId = listOfFragmentId.toArray(new String[0])[i];
                String href = listOfHref.toArray(new String[0])[i];
                String title = titleList.toArray(new String[0])[i];

                // Add title button for the chapter menu
                SidePanelOption titleBtn = new SidePanelOption(400, 40, "", title);
                titleBtn.getStyleClass().add("title-btn");
                titleBtn.getIconButton().setOnAction(e -> scrollToSection(fragmentId));

                leftSidePanel.getChildren().add(titleBtn);

                // Get resource by href
                Resource resource = book.getResources().getByHref(href);
                String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                Document doc = Jsoup.parse(content);
                Element body = doc.body();

                String htmlContent = null;
                if (body != null) {
                    processContent(body);

                    body.select("img").forEach(element -> {
                        Set<String> keys = imgResource.keySet().stream().filter(s -> element.attr("src").contains(s)).collect(Collectors.toSet());

                        element.attr("src", imgResource.get(keys.toArray()[0]));
                        element.addClass("epub-img");
                    });

                    htmlContent = String.format("""
                                <div class="chapter-break" id="%s">%s</div>
                            """, fragmentId, body);
                }



                bookContent.append(htmlContent);
            }

            fullBookContent = bookContent.toString();

            // Display the book
            displayBook();

        } catch (Exception e) {
            System.out.println("Error loading EPUB: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void getDataListsFromNCX() throws IOException {
        String ncx = new String(book.getNcxResource().getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        Document docNcx = Jsoup.parse(ncx);

        docNcx.select("navPoint").forEach(navPoint -> {
            String href = navPoint.select("content").attr("src");
            String fragmentId = href.contains("#") ? href.split("#")[1] : href;
            String title = navPoint.select("text").text();

            // If href is already present don't add it
            if (!listOfHref.contains(href.split("#")[0])) {
                listOfHref.add(href.split("#")[0]);
                listOfFragmentId.add(fragmentId);
                titleList.add(title);
            }
        });
    }

    private void scrollToSection(String fragmentId) {
        String script = """
                        (() => {
                            const el = document.getElementById('%s');
                            if (!el) return -1;
                        
                            const currentPageIndex = %d;
                            const pageWidth = window.innerWidth;
                            const x = el.getBoundingClientRect().left + window.scrollX;
                            const pageIndex = Math.floor(x / pageWidth);
                            if (currentPageIndex >= pageIndex) { return currentPageIndex + pageIndex; }
                            else return pageIndex;
                        })();
                        """.formatted(fragmentId, currentSpreadIndex);

        Object result = bookWebEngine.executeScript(script);
        if (result instanceof Number) {
            currentSpreadIndex = ((Number) result).intValue();
            displayCurrentSpread();
            updatePageInfo();
        }
    }

    private void displayBook() throws IOException {
        File bookCssFile = new File("src/main/resources/com/example/lume/bookView.css");
        String css = Files.readString(Paths.get(bookCssFile.getAbsolutePath()));
        String html = String.format(
                "<html><head><style>%s</style></head><body><div class='book-container'><img src='%s' class='epub-img'>" +
                        "%s</div></body></html>",
                css, coverImg, fullBookContent
        );

        // Load content
        bookWebEngine.loadContent(html);

        // Calculate total spreads after content loads
        bookWebEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                // Small delay to ensure rendering is complete
                javafx.application.Platform.runLater(() -> {
                    try {
                        Thread.sleep(100);
                        calculateTotalSpreads();
                        displayCurrentSpread();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
        });
    }

    private void processContent(Element elem) {
        elem.select("h1, hgroup").forEach(el -> {
            addLumeClass(el, TITLE_CLASS);

            if (el.text().toLowerCase().contains("chapter")) {
                addLumeClass(el, CHAPTER_CLASS);
            }
        });
        elem.select("h2, h3, h4, h5, h6").forEach(el -> addLumeClass(el, SUBTITLE_CLASS));
        elem.select("p").forEach(el -> {
            addLumeClass(el, PARAGRAPH_CLASS);

            if (el.text().trim().isEmpty()) {
                addLumeClass(el, "spacer-paragraph");
            } else if (el.hasClass("continued") || el.hasClass("no-indent")) {
                addLumeClass(el, "special-paragraph");
            }
        });
        elem.select("nav, .navigation").forEach(el -> addLumeClass(el, NAVIGATION_CLASS));
        elem.select("div, section, article").forEach(el -> {
            addLumeClass(el, SECTION_CLASS);
        });
    }

    private void addLumeClass(Element el, String className) {
        if (!el.hasClass(className)) {
            el.addClass(className);
        }
    }

    private void calculateTotalSpreads() {
        try {
            // Use JavaScript to get the actual rendered width
            String script = String.format("""
                    (function() {
                    var container = document.querySelector('.book-container');
                    if (container) {
                        var totalWidth = container.scrollWidth;
                        var columnWidth = %.0f;
                        var columnGap = 40;
                        var pageWidth = columnWidth + columnGap;
                        var totalColumns = Math.ceil(totalWidth / pageWidth);
                        var spreads = Math.ceil(totalColumns / 2);
                        return spreads;
                    }
                    return 1;
                })();
                """, pageWidth - 40
            );

            Object result = bookWebEngine.executeScript(script);

            if (result instanceof Number) {
                totalSpreads = Math.max(1, ((Number) result).intValue());
            } else {
                totalSpreads = 1;
            }

            System.out.println("Total spreads calculated: " + totalSpreads);
            updatePageInfo();

        } catch (Exception e) {
            System.out.println("Error calculating spreads: " + e.getMessage());
            totalSpreads = 1;
        }
    }

    private void displayCurrentSpread() {
        try {
            double spreadWidth = totalWidth;
            double offset = currentSpreadIndex * spreadWidth;

            String script = String.format(
                    "document.querySelector('.book-container').style.transform = 'translateX(-%.0fpx)';",
                    offset
            );

            bookWebEngine.executeScript(script);

        } catch (Exception e) {
            System.out.println("Error displaying spread: " + e.getMessage());
        }
    }

    private void addNavigationControls(VBox container) {
        HBox controls = new HBox();
        controls.setSpacing(10);
        controls.setPadding(new Insets(10));
        controls.setAlignment(Pos.BOTTOM_CENTER);

        ButtonIcon prevButton = new ButtonIcon("M1 8a7 7 0 1 0 14 0A7 7 0 0 0 1 8m15 0A8 8 0 1 1 0 8a8 8 0 0 1 16 0m-4.5-.5a.5.5 0 0 1 0 1H5.707l2.147 2.146a.5.5 0 0 1-.708.708l-3-3a.5.5 0 0 1 0-.708l3-3a.5.5 0 1 1 .708.708L5.707 7.5z");
        prevButton.setOnAction(e -> {
            if (currentSpreadIndex > 0) {
                currentSpreadIndex--;
                displayCurrentSpread();
                updatePageInfo();
            }
        });

        ButtonIcon nextButton = new ButtonIcon("M1 8a7 7 0 1 0 14 0A7 7 0 0 0 1 8m15 0A8 8 0 1 1 0 8a8 8 0 0 1 16 0M4.5 7.5a.5.5 0 0 0 0 1h5.793l-2.147 2.146a.5.5 0 0 0 .708.708l3-3a.5.5 0 0 0 0-.708l-3-3a.5.5 0 1 0-.708.708L10.293 7.5z");
        nextButton.setOnAction(e -> {
            if (currentSpreadIndex < totalSpreads - 1) {
                currentSpreadIndex++;
                displayCurrentSpread();
                updatePageInfo();
            }
        });

        Label pageInfo = new Label("Spread 1 of " + totalSpreads);
        pageInfo.setId("page-info");
        pageInfo.getStyleClass().add("page-info");

        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);

        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        controls.getChildren().addAll(prevButton, spacer1, pageInfo, spacer2, nextButton);
        container.getChildren().add(controls);
    }

    private void updatePageInfo() {
        Label pageInfo = (Label) rightSidePanel.lookup("#page-info");
        if (pageInfo != null) {
            pageInfo.setText(String.format("Spread %d of %d", currentSpreadIndex + 1, totalSpreads));
        }
    }

    private void setupKeyboardNavigation() {
        this.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case LEFT:
                    if (currentSpreadIndex > 0) {
                        currentSpreadIndex--;
                        displayCurrentSpread();
                        updatePageInfo();
                    }
                    break;
                case RIGHT:
                    if (currentSpreadIndex < totalSpreads - 1) {
                        currentSpreadIndex++;
                        displayCurrentSpread();
                        updatePageInfo();
                    }
                    break;
            }
        });


        bookWebView.setFocusTraversable(true);
    }
}
