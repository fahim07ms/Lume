package com.example.lume.scenes;

import com.example.lume.components.ButtonIcon;
import com.example.lume.components.SidePanelOption;
import com.example.lume.components.SubTitleVBox;
import com.example.lume.layouts.BaseLayout;
import com.example.lume.layouts.TitleBar;
import io.documentnode.epub4j.domain.*;
import io.documentnode.epub4j.epub.EpubReader;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class BookViewScene extends BaseLayout {

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

    // Classes to inject
    private static final String TITLE_CLASS = "lume-title";
    private static final String CHAPTER_CLASS = "lume-chapter";
    private static final String PARAGRAPH_CLASS = "lume-paragraph";
    private static final String NAVIGATION_CLASS = "lume-navigation";
    private static final String SECTION_CLASS = "lume-section";
    private static final String SUBTITLE_CLASS = "lume-subtitle";

    // List to hold the Fragment ID's
    Set<String> listOfFragmentId = new LinkedHashSet<>();

    // List to hold the href's
    Set<String> listOfHref = new LinkedHashSet<>();

    // List to hold the img data with key as href
    HashMap<String, String> imgResource = new HashMap<String, String>();

    List<String> titleList = new ArrayList<>();

    // Cover Image Data
    String coverImg;

    // Body Content that would be shown
    private String fullBookContent = "";

    // EPub Book (epublib)
    Book book;

    Scene prevScene;

    public BookViewScene(Stage stage, Scene prevScene, String filePath) throws IOException {
        super();

        this.prevScene = prevScene;

        // Load the epub book
        EpubReader epubReader = new EpubReader();
        book = epubReader.readEpub(new FileInputStream(filePath));

        // Set total width of the webview as right side panel width
        totalWidth = this.getRightSidePanelWidth();

        // Set each page/column width and height
        pageWidth = (totalWidth - 20) / 2;
        pageHeight = this.getRightSidePanelHeight() - 100;

        // Set title of the right side panel as book title
        // If can't then set default
        TitleBar titleBar = new TitleBar(this.getRightSidePanelWidth(), 100, stage);
        try {
            titleBar.setTitleBarText(book.getTitle());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            titleBar.setTitleBarText("Lume Ebook");
        }

        // Add title bar to right side panel
        rightSidePanel.getChildren().add(titleBar);

        tocPanelTopBar(stage);
        bookShortDetails();

        // Show book in single web view
        showBookInWebView();
        setupKeyboardNavigation();
    }

    private void tocPanelTopBar(Stage stage) {
        HBox topBar = new HBox();
        ButtonIcon backBtn = new ButtonIcon("M11.354 1.646a.5.5 0 0 1 0 .708L5.707 8l5.647 5.646a.5.5 0 0 1-.708.708l-6-6a.5.5 0 0 1 0-.708l6-6a.5.5 0 0 1 .708 0");
        backBtn.setOnAction(e -> {
            stage.setScene(prevScene);
        });
        backBtn.setMaxSize(10, 10);


        topBar.setPadding(new Insets(0, 5, 5, 5));
        topBar.setSpacing(10);
        topBar.setAlignment(Pos.TOP_LEFT);
        topBar.getStyleClass().add("toc-panel-top-bar");
        topBar.getChildren().addAll(backBtn);

        leftSidePanel.getChildren().add(topBar);
    }

    private void bookShortDetails() throws IOException {
        BorderPane shortBookDetails = new BorderPane();
        shortBookDetails.getStyleClass().add("book-short-details");
        shortBookDetails.setPadding(new Insets(20, 5, 30, 5));
        shortBookDetails.setPrefSize(this.getRightSidePanelWidth(), 200);

        // Show cover image
        Image smallCoverImg = new Image(new ByteArrayInputStream(book.getCoverImage().getData()));
        ImageView smallCoverImgView = new ImageView();
        smallCoverImgView.setImage(smallCoverImg);
        smallCoverImgView.getStyleClass().add("book-short-details-img");

        smallCoverImgView.setPreserveRatio(true);
        smallCoverImgView.setFitWidth(100);

        // Show title and author
        VBox titleAndAuthor = new VBox();
        titleAndAuthor.setSpacing(10);
        titleAndAuthor.setAlignment(Pos.TOP_LEFT);
        titleAndAuthor.getStyleClass().add("book-short-details-title-author");

        Label title = new Label(book.getTitle());
        title.getStyleClass().add("book-short-details-title");
        titleAndAuthor.getChildren().addAll(title);
        title.setStyle("""
                    -fx-spacing: 100px;
                    -fx-font-weight: 700;
                """);

        for (Author author : book.getMetadata().getAuthors()) {
            Label authorName = new Label(author.getFirstname() + " " + author.getLastname()) ;
            authorName.getStyleClass().add("book-short-details-author");
            titleAndAuthor.getChildren().addAll(authorName);
        }

        shortBookDetails.setCenter(titleAndAuthor);
        shortBookDetails.setLeft(smallCoverImgView);

        leftSidePanel.getChildren().add(shortBookDetails);
    }

    public void showBookInWebView() {
        try {
            // Create WebView and WebEngine
            // Set style class, width and height
            bookWebView = new WebView();
            bookWebEngine = bookWebView.getEngine();
            bookWebView.getStyleClass().addAll("epub-view", "book-web-view");
            bookWebView.setPrefSize(totalWidth, pageHeight);

            // Add the webview to the rightSidePanel
            rightSidePanel.getChildren().add(bookWebView);

            // Load EPUB content
            loadBook();

            // Add navigation controls
            addNavigationControls(rightSidePanel);

        } catch (Exception e) {
            System.out.println("Error in showBookInWebView: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadBook() {
        try {
            // Load OCF in Jsoup format
            String ocfRef = new String(
                    book.getOpfResource().getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8
            );
            Document docOCF = Jsoup.parse(ocfRef);

            // Load image data in the imgResource
            loadBookImageData(docOCF);

            // Get the coverImage data by the cover image href
            coverImg = imgResource.get(book.getCoverImage().getHref());

            // Build the Table of Contents from the NCX
            buildTOC();

            // Build the complete book content
            StringBuilder bookContent = new StringBuilder();

            for (int i = 0; i < listOfFragmentId.size(); i++) {
                // Href and title
                String fragmentId = listOfFragmentId.toArray(new String[0])[i];
                String href = listOfHref.toArray(new String[0])[i];

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
                        element.addClass("lume-img");
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

    private void loadBookImageData(Document docOCF) {
        // Select the `item` tag
        // For each get the hyper reference
        // If it is an image get the base64 and set it to proper data value and add it
        docOCF.select("item").forEach(item -> {
            String href = item.attribute("href").getValue();
            if (item.attribute("media-type").getValue().contains("image/")) {
                Resource res = book.getResources().getByHref(href);
                try {
                    byte[] imageData = res.getData();
                    String base64Data = Base64.getEncoder().encodeToString(imageData);

                    String imgData = "data:" + item.attribute("media-type").getValue() + ";base64," + base64Data;

                    imgResource.put(href, imgData);

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void buildTOC() throws IOException {
        // Generate Jsoup format of the Navigation Control file for XML
        String ncx = new String(book.getNcxResource().getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        Document docNcx = Jsoup.parse(ncx);

        // Add the title of the left side panel
        leftSidePanel.getChildren().add(new SubTitleVBox(this.getRightSidePanelWidth(), 20, "Table of Contents"));

        // Get the NavMap of the NCX
        Element navMap = docNcx.select("navMap").first();
        if (navMap != null) {
            // For each NavPoint in the NavMap
            navMap.select("> navPoint").forEach(navPoint -> {
                // Get its child elements (Grandchildren also comes here)
                Elements childNavPoints = navPoint.select("navPoint");

                // Get the child count
                int childCount = childNavPoints.size();

                // If only one child then show that in the left side panel
                // Track the href and fragment id's also
                if (childCount == 1) {
                    SidePanelOption parentSidePanelOption = createSidePanelOption(childNavPoints.getFirst());
                }

                // Otherwise take care of the parent first
                // And then show the childrens
                else if (childCount > 1) {
                    SidePanelOption parentSidePanelOption = createSidePanelOption(childNavPoints.getFirst());
                    for (int i = 1; i < childCount; i++) {
                        SidePanelOption childSidePanelOption = createSidePanelOption(childNavPoints.get(i));
                        // Add left padding for the children's for good visibility
                        childSidePanelOption.setPadding(new Insets(5, 10, 5, 25));
                    }
                }
            });
        }
    }

    private SidePanelOption createSidePanelOption(Element element) {
        // Get the href, fragmentId and title
        String href = element.select("content").attr("src");
        String fragmentId = href.contains("#") ? href.split("#")[1] : href;
        String title = element.select("text").text();

        // If href is already present don't add it, otherwide add it
        if (!listOfHref.contains(href.split("#")[0])) {
            listOfHref.add(href.split("#")[0]);
            listOfFragmentId.add(fragmentId);
            titleList.add(title);
        }

        // Build the side panel option for the given element
        SidePanelOption sidePanelOption = new SidePanelOption(this.getRightSidePanelWidth(), 20, "", title);
        sidePanelOption.getIconButton().setOnAction(e -> scrollToSection(fragmentId)); // Check for click action so that it goes to the proper fragment
        sidePanelOption.setPadding(new Insets(5, 0, 5, -10));

        // Add side panel option to the left side panel
        leftSidePanel.getChildren().add(sidePanelOption);

        return sidePanelOption;
    }

    // Scrolling to proper page section when some button clicked in TOC
    private void scrollToSection(String fragmentId) {
        String script = """
                (() => {
                    const el = document.getElementById('%s');
                    const container = document.querySelector('.book-container');
                    if (!el || !container) {
                        return -1; // Element or container not found
                    }

                    // Single spread width
                    const spreadWidth = window.innerWidth;

                    // The container's left position
                    const containerLeft = container.getBoundingClientRect().left;

                    // The element's left position relative to the viewport
                    const elementLeftInViewport = el.getBoundingClientRect().left;

                    // Calculate the element's true position from the start of the container's content
                    const absoluteElementLeft = elementLeftInViewport - containerLeft;

                    // Calculate the spread index based on the absolute position
                    const spreadIndex = Math.floor(absoluteElementLeft / spreadWidth);

                    return spreadIndex;
                })();
                """.formatted(fragmentId);

        Object result = bookWebEngine.executeScript(script);

        // Check if the we get proper spread index or not
        // If so move to that page
        if (result instanceof Number && ((Number) result).intValue() != -1) {
            currentSpreadIndex = ((Number) result).intValue();
            displayCurrentSpread();
            updatePageInfo();
        }
    }

    // Display the full book content with proper css
    private void displayBook() throws IOException {
        // Load css
        File bookCssFile = new File("src/main/resources/com/example/lume/bookView.css");
        String css = Files.readString(Paths.get(bookCssFile.getAbsolutePath()));

        // Structure the HTML
        String html = String.format(
                "<html><head><style>%s</style></head><body><div class='book-container'><img src='%s' class='lume-img'>" +
                        "%s</div></body></html>",
                css, coverImg, fullBookContent
        );

        // Load content
        bookWebEngine.loadContent(html);

        // When content fully loads
        bookWebEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                Platform.runLater(() -> {
                    try {
                        // Small delay to ensure rendering is complete
                        Thread.sleep(100);

                        // Calculate the spread and display the current one
                        calculateTotalSpreads();
                        displayCurrentSpread();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
        });
    }

    // Inject proper CSS classes to beautify the content
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
            String script = String.format("""
                    (function() {
                    var container = document.querySelector('.book-container');
                    if (container) {
                        // Get the container width, column width and gap
                        var totalWidth = container.scrollWidth;
                        var columnWidth = %.0f;
                        var columnGap = 40;
                        var pageWidth = columnWidth + columnGap;
                        // Calculate the total number of columns we get
                        var totalColumns = Math.ceil(totalWidth / pageWidth);
                        // Calculate the spread number
                        var spreads = Math.ceil(totalColumns / 2);
                        return spreads;
                    }
                    return 1;
                })();
                """, pageWidth - 40
            );

            Object result = bookWebEngine.executeScript(script);

            // The number would be between 1 to the result
            if (result instanceof Number) {
                totalSpreads = Math.max(1, ((Number) result).intValue());
            } else {
                totalSpreads = 1;
            }

//            System.out.println("Total spreads calculated: " + totalSpreads);
            updatePageInfo();

        } catch (Exception e) {
            System.out.println("Error calculating spreads: " + e.getMessage());
            totalSpreads = 1;
        }
    }

    private void displayCurrentSpread() {
        try {
            // Calculate the offset width to be given on transform
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

        ButtonIcon prevButton = new ButtonIcon("M11.354 1.646a.5.5 0 0 1 0 .708L5.707 8l5.647 5.646a.5.5 0 0 1-.708.708l-6-6a.5.5 0 0 1 0-.708l6-6a.5.5 0 0 1 .708 0");
        prevButton.setOnAction(e -> {
            if (currentSpreadIndex > 0) {
                currentSpreadIndex--;
                displayCurrentSpread();
                updatePageInfo();
            }
        });

        ButtonIcon nextButton = new ButtonIcon("M4.646 1.646a.5.5 0 0 1 .708 0l6 6a.5.5 0 0 1 0 .708l-6 6a.5.5 0 0 1-.708-.708L10.293 8 4.646 2.354a.5.5 0 0 1 0-.708");
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
