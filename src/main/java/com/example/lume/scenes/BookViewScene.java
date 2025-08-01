package com.example.lume.scenes;

import com.example.lume.Main;
import com.example.lume.components.*;
import com.example.lume.layouts.BaseLayout;
import com.example.lume.components.ChatPane;
import com.example.lume.layouts.TitleBar;
import com.example.lume.networking.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.documentnode.epub4j.domain.*;
import io.documentnode.epub4j.epub.EpubReader;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
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

    private final String BOOKMARK_ICON = "M2 2a2 2 0 0 1 2-2h8a2 2 0 0 1 2 2v13.5a.5.5 0 0 1-.777.416L8 13.101l-5.223 2.815A.5.5 0 0 1 2 15.5zm2-1a1 1 0 0 0-1 1v12.566l4.723-2.482a.5.5 0 0 1 .554 0L13 14.566V2a1 1 0 0 0-1-1z";
    private final String LIST_ICON = "M2.5 12a.5.5 0 0 1 .5-.5h10a.5.5 0 0 1 0 1H3a.5.5 0 0 1-.5-.5m0-4a.5.5 0 0 1 .5-.5h10a.5.5 0 0 1 0 1H3a.5.5 0 0 1-.5-.5m0-4a.5.5 0 0 1 .5-.5h10a.5.5 0 0 1 0 1H3a.5.5 0 0 1-.5-.5";
    private final String SEND_ICON = "M15.854.146a.5.5 0 0 1 .11.54l-5.819 14.547a.75.75 0 0 1-1.329.124l-3.178-4.995L.643 7.184a.75.75 0 0 1 .124-1.33L15.314.037a.5.5 0 0 1 .54.11ZM6.636 10.07l2.761 4.338L14.13 2.576zm6.787-8.201L1.591 6.602l4.339 2.76z";

    Set<String> listOfFragmentId = new LinkedHashSet<>(); // List to hold the Fragment ID's
    Set<String> listOfHref = new LinkedHashSet<>(); // List to hold the href's
    HashMap<String, String> imgResource = new HashMap<String, String>(); // List to hold the img data with key as href
    List<String> titleList = new ArrayList<>(); // List to hold the titles of the TOC

    List<Annotation> annotations = new ArrayList<>(); // List of annotations

    List<Integer> bookmarkedSpreads = new ArrayList<>(); // List of bookmarked spreads

    String coverImg; // Cover Image Data
    private String fullBookContent = ""; // Body Content that would be shown

    // File path of the book that is opened
    String filePath;

    // Metadata holding object
    LumeMetadata lumeMetadata;

    Stage stage; // Stage
    Scene prevScene; // Previous scene

    MenuItem bookmarkBtn = null;

    // EPub Book (epublib)
    Book book;
    private String bookId = null;
    StringBuilder fullText = new StringBuilder();


    public BookViewScene(Stage stage, Scene prevScene, String filePath, LumeMetadata lumeMetadata) throws IOException {
        super();
        this.prevScene = prevScene;
        this.filePath = filePath;
        this.lumeMetadata = lumeMetadata;
        this.stage = stage;

        // Check if file was opened previously then change annotations, currentSpreadIndex and totalSpreads
        lumeMetadata.getBookMetaDataMap().forEach((k, v) -> {
            if (v.getFilePath().equals(filePath)) {
                annotations = v.getAnnotations();
                currentSpreadIndex = v.getCurrentSpread();
                totalSpreads = v.getTotalSpread();
                bookmarkedSpreads = v.getBookmarkedSpreads();
                bookId = k;
            }
        });

        if (bookId == null) {
            bookId = UUID.randomUUID().toString();
        }

        // Load the epub book
        EpubReader epubReader = new EpubReader();
        book = epubReader.readEpub(new FileInputStream(filePath));

        // Set total width of the webview as right side panel width
        totalWidth = this.getRightSidePanelWidth();

        // Set each page/column width and height
        pageWidth = (totalWidth - 20) / 2;
        pageHeight = this.getRightSidePanelHeight() - 100;

        setBookViewTitleBar();

        tocPanelTopBar(stage);
        bookShortDetails();

        // Show book in single web view
        showBookInWebView();
        setupKeyboardNavigation();
        setContextMenu();

        if (Main.networkManager != null) {
            // Tell the network manager what to do when a message comes in
            Main.networkManager.setOnMessageReceived(this::applyRemoteAnnotation);
        }
    }

    private void setBookViewTitleBar() {
        // Set title of the right side panel as book title
        // If can't then set default
        TitleBar titleBar = new TitleBar(this.getRightSidePanelWidth(), 100, stage);
        try {
            titleBar.setTitleBarText(book.getTitle());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            titleBar.setTitleBarText("Lume Ebook");
        }

        titleBar.setLeftBtn(LIST_ICON, "More");

        setListBtnContextMenu(titleBar.getLeftBtn());

        // On closing save the book data
        EventHandler currentCloseBtnAction = titleBar.getCloseBtnIcon().getOnAction();
        titleBar.getCloseBtnIcon().setOnAction(e -> {
            storeBookData();
            currentCloseBtnAction.handle(e);
            Main.networkManager.stop();
        });

        // Add title bar to right side panel
        rightSidePanel.getChildren().add(titleBar);
    }

    private void setListBtnContextMenu(Button  button) {
        ContextMenu listBtnContextMenu = new ContextMenu();
        listBtnContextMenu.setAutoHide(true);
        listBtnContextMenu.setHideOnEscape(true);
        listBtnContextMenu.setMinWidth(250);
        listBtnContextMenu.getStyleClass().add("list-btn-context-menu");

        MenuItem saveBtn = new MenuItem("Save    ");
       // MenuItem sharedReadingBtn = new MenuItem("Start Shared Reading");

        saveBtn.getStyleClass().add("savebtn");
        saveBtn.setOnAction(e -> {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                storeBookData();
                objectMapper.writeValue(new File(System.getProperty("user.home") + "/.lume/metadata.json"), LibraryLayout.lumeMetadata);
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        });
        saveBtn.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));

        MenuItem aiBtn = new MenuItem("Ask Lume AI");
        AtomicReference<AiChat> aiChat = new AtomicReference<>();
        Stage newStage = new Stage();
        newStage.setResizable(false);
        newStage.setWidth(500);
        newStage.setHeight(this.getHomeLayoutHeight() - 100);
        newStage.initStyle(StageStyle.UNDECORATED);

        TitleBar titleBar = new TitleBar(500, 100, newStage);
        titleBar.setTitleBarText("Ask Lume AI about " + book.getTitle());

        BorderPane rootPane = new BorderPane();
        ChatPane chatPane = new ChatPane();
        HBox inputBox = new HBox();
        TextArea textArea = new TextArea();
        ButtonIcon sendButton = new ButtonIcon(SEND_ICON);

        rootPane.getStyleClass().add("ai-root-pane-box");
        rootPane.setTop(titleBar);

        rootPane.setCenter(chatPane);
        chatPane.setPadding(new Insets(10, 10, 10, 10));

        inputBox.getStyleClass().add("ai-input-box");
        inputBox.setAlignment(Pos.CENTER);
        inputBox.setPrefHeight(120);
        inputBox.setSpacing(10);
        textArea.maxWidth(400);
        textArea.setBorder(new Border(new BorderStroke(Color.rgb(87, 87, 87, 0.94), BorderStrokeStyle.SOLID, null, new BorderWidths(0.5, 0.5, 0.5, 0.5))));
        textArea.setWrapText(true);
        textArea.setPromptText("Ask a question...");

        textArea.getStyleClass().add("ai-box-textarea");
        HBox.setHgrow(textArea, Priority.ALWAYS);
        textArea.setStyle("-fx-font-size: 18px;");

        final boolean[] notAdded = {true};

        aiBtn.setOnAction(e -> {
            listBtnContextMenu.hide();


            if (aiChat.get() == null) {
                aiChat.set(new AiChat(book.getTitle(), fullText.toString(), Arrays.toString(book.getMetadata().getAuthors().toArray())));
            }

            sendButton.setOnAction(e1 -> {
                String query = textArea.getText().trim();
                if (query.isEmpty()) {
                    return;
                }
                textArea.clear();

                chatPane.addBubble(new ChatBox(query, ChatBox.Sender.USER));

                ChatBox aiThinkingBubble = new ChatBox("Lume is thinking...", ChatBox.Sender.AI);
                chatPane.addBubble(aiThinkingBubble);

                Task<String> getResponseTask = new Task<>() {
                    @Override
                    protected String call() throws Exception {
                        return aiChat.get().getResponse(query);
                    }
                };

                getResponseTask.setOnSucceeded(event -> {
                    String response = getResponseTask.getValue();
                    aiThinkingBubble.setText(response);
                });

                getResponseTask.setOnFailed(event -> {
                    aiThinkingBubble.setText("Sorry, I encountered an error. Please try again.");
                    getResponseTask.getException().printStackTrace();
                });

                new Thread(getResponseTask).start();
            });

            if (notAdded[0]) {
                inputBox.getChildren().addAll(textArea, sendButton);
                rootPane.setBottom(inputBox);

                Scene aiScene = new Scene(rootPane, 500, 600); // Adjusted height
                try {
                    File file = new File("src/main/resources/com/example/lume/styles.css");
                    aiScene.getStylesheets().add("file:///" + file.getAbsolutePath().replace("\\", "/"));
                } catch (Exception ex) {
                    System.out.println("Could not load stylesheet for AI chat: " + ex.getMessage());
                }

                newStage.setScene(aiScene);

                notAdded[0] = false;
            }

            newStage.show();
        });

        MenuItem settingsBtn = new MenuItem("Edit View");
        settingsBtn.setOnAction(e -> {
            listBtnContextMenu.hide();
            launchViewSettingsWindow();
        });


        listBtnContextMenu.getItems().addAll(saveBtn, aiBtn, settingsBtn);

        button.setOnContextMenuRequested(event -> {
            listBtnContextMenu.show(button, event.getScreenX(), event.getScreenY());
        });
    }

    private void launchViewSettingsWindow() {
        Stage settingsStage = new Stage();
        settingsStage.initModality(Modality.APPLICATION_MODAL); // Blocks interaction with the main window
        settingsStage.initOwner(this.getScene().getWindow());
        settingsStage.setResizable(false);
        settingsStage.setTitle("Edit View");

        VBox rootPane = new VBox();
        rootPane.getStyleClass().add("settings-pane");

        Label fontLabel = new Label("Font");
        fontLabel.getStyleClass().add("setting-section-title");

        ComboBox<String> fontComboBox = new ComboBox<>();
        fontComboBox.getSelectionModel().selectFirst();
        fontComboBox.getSelectionModel().select(lumeMetadata.getSettings().getFont().split("px")[0]);
        Settings.FONTS.keySet().forEach(fontName -> {
            fontComboBox.getItems().add(fontName);
        });

        fontComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            lumeMetadata.getSettings().setFont(Settings.FONTS.get(newVal));
            System.out.println(Settings.FONTS.get(newVal));
            System.out.println(newVal);
            bookWebEngine.executeScript("document.body.style.fontFamily = '%s';".formatted(lumeMetadata.getSettings().getFont()));
        });

        fontComboBox.getStyleClass().add("font-combo-box");
        HBox.setHgrow(fontComboBox, Priority.ALWAYS);

        HBox fontBox = new HBox(fontComboBox);
        fontBox.getStyleClass().add("setting-control-box");

        Label fontSizeLabel = new Label("Font Size");
        fontSizeLabel.getStyleClass().add("setting-section-title");

        Slider fontSizeSlider = new Slider(16, 32, (int) Integer.parseInt(lumeMetadata.getSettings().getFontSize().split("px")[0]));
        fontSizeSlider.setMajorTickUnit(2);
        fontSizeSlider.setMinorTickCount(1);
        fontSizeSlider.setShowTickMarks(true);
        fontSizeSlider.setShowTickLabels(true);

        Label sizeIndicator = new Label(String.format("%.0fpt", fontSizeSlider.getValue()));
        sizeIndicator.getStyleClass().add("slider-indicator");

        fontSizeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            newVal = newVal.intValue();
            sizeIndicator.setText(String.format("%dpt", newVal));
            lumeMetadata.getSettings().setFontSize(String.format("%dpx", newVal));
            bookWebEngine.executeScript("document.body.style.fontSize = '%spx';".formatted(newVal));
            fontSizeSlider.setValue(newVal.doubleValue());
        });

        GridPane sliderPane = new GridPane();
        sliderPane.getStyleClass().add("setting-control-box");
        sliderPane.add(fontSizeSlider, 0, 0);
        sliderPane.add(sizeIndicator, 1, 0);
        GridPane.setHgrow(fontSizeSlider, Priority.ALWAYS);

        // Add all sections to the root pane
        rootPane.getChildren().addAll(
                createSettingSection(fontLabel, fontBox),
                createSettingSection(fontSizeLabel, sliderPane)
        );

        Scene scene = new Scene(rootPane, 350, -1);
        try {
            File file = new File("src/main/resources/com/example/lume/styles.css");
            scene.getStylesheets().add("file:///" + file.getAbsolutePath().replace("\\", "/"));
        } catch (Exception ex) {
            System.out.println("Could not load stylesheet for settings: " + ex.getMessage());
        }

        settingsStage.setScene(scene);
        settingsStage.show();
    }

    private VBox createSettingSection(Label title, Node control) {
        VBox section = new VBox(5); // 5px spacing
        section.getStyleClass().add("setting-section");
        section.getChildren().addAll(title, control);
        return section;
    }

    private void applyRemoteAnnotation(Message message) {
        String msgBookId = message.getBookId();
        Annotation msgAnnotation = message.getAnnotation();

        boolean alreadyExist = annotations.stream()
                .anyMatch(a -> a.getAnnotationId().equals(msgAnnotation.getAnnotationId()));

        if (!alreadyExist) {
            ObjectMapper objectMapper = new ObjectMapper();
            if (msgBookId.equals(bookId)) {
                String annotationStr = null;
                try {
                    annotationStr = objectMapper.writeValueAsString(msgAnnotation);

                    // Execute and check result
                    Object result = bookWebEngine.executeScript("restoreAnnotation(" + annotationStr + ")");
                    annotations.add(msgAnnotation);
                } catch (JsonProcessingException e) {
                    System.out.println("Error in displaying annotations: " + e.getMessage());
                }
            }
        }
    }

    private void setContextMenu() {
        // Context menu for showing annotation button
        ContextMenu annotateMenu = new ContextMenu();
        annotateMenu.setAutoHide(true);
        annotateMenu.setHideOnEscape(true);
        annotateMenu.setPrefSize(100, 300);

        annotateMenu.getStyleClass().add("highlighter-menu");

        // Context menu items
        MenuItem red = new MenuItem("Red");
        MenuItem green = new MenuItem("Green");
        MenuItem yellow = new MenuItem("Yellow");
        MenuItem cyan = new MenuItem("Cyan");

        red.setOnAction(e -> setAnnotations("red"));
        green.setOnAction(e -> setAnnotations("green"));
        yellow.setOnAction(e -> setAnnotations("yellow"));
        cyan.setOnAction(e -> setAnnotations("cyan"));
        updateBookMark();

        annotateMenu.getItems().addAll(red, green, yellow, cyan, bookmarkBtn);

        // Enable context menu on right clicking the WebView
        bookWebView.setContextMenuEnabled(false);
        bookWebView.setOnContextMenuRequested(event -> {
            annotateMenu.show(bookWebView, event.getSceneX(), event.getSceneY());
        });
    }

    private void updateBookMark() {

        if (bookmarkBtn == null) {
            if (bookmarkedSpreads.contains(currentSpreadIndex)) {
                bookmarkBtn = new MenuItem("Remove Bookmark");
            } else {
                bookmarkBtn = new MenuItem("Bookmark");
            }
        } else {
            if (bookmarkedSpreads.contains(currentSpreadIndex)) {
                bookmarkBtn.setText("Remove Bookmark");
            } else {
                bookmarkBtn.setText("Bookmark");
            }
        }

        MenuItem finalBookmarkBtn = bookmarkBtn;
        bookmarkBtn.setOnAction(e -> {
            if (bookmarkedSpreads.contains(currentSpreadIndex)) {
                bookmarkedSpreads.removeIf(bookmarkedSpread -> bookmarkedSpread == currentSpreadIndex);
                leftSidePanel.getChildren().removeIf(node -> {
                    if (node.getId() != null && node.getId().equals("bookmark-%d".formatted(currentSpreadIndex + 1))) {
                        return true;
                    }
                    return false;
                });
                finalBookmarkBtn.setText("Bookmark");
            } else {
                bookmarkedSpreads.add(currentSpreadIndex);
                finalBookmarkBtn.setText("Remove Bookmark");
                setBookMarkOption(currentSpreadIndex);
            }
        });
    }

    private void setAnnotations(String color) {
        String annotationId = UUID.randomUUID().toString();
        String annotationLoc = (String) bookWebEngine.executeScript("annotate('%s', '%s')".formatted(color, annotationId));
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Annotation annotation = objectMapper.readValue(annotationLoc, Annotation.class);
            annotations.add(annotation);

            Message message = new Message(bookId, annotation);
            Main.networkManager.sendMessage(message);

        } catch (Exception exc) {
            System.out.println("Exception while reading annotation: " + exc.getMessage());
            exc.printStackTrace();
        }
    }

    private void tocPanelTopBar(Stage stage) {
        HBox topBar = new HBox();
        ButtonIcon backBtn = new ButtonIcon("M11.354 1.646a.5.5 0 0 1 0 .708L5.707 8l5.647 5.646a.5.5 0 0 1-.708.708l-6-6a.5.5 0 0 1 0-.708l6-6a.5.5 0 0 1 .708 0");
        backBtn.setOnAction(e -> {
            storeBookData();
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

    private void storeBookData() {
        AtomicBoolean fileExists = new AtomicBoolean(false);

        lumeMetadata.getBookMetaDataMap().forEach((k, v) -> {
            if (v.getFilePath().equals(filePath)) {
                fileExists.set(true);
                v.setAnnotations(annotations);
                v.setCurrentSpread(currentSpreadIndex);
            }
        });

        if (!fileExists.get()) {
            List<String> authors = new ArrayList<>();
            for (Author author : book.getMetadata().getAuthors()) {
                authors.add(author.getFirstname() + " " + author.getLastname());
            }

            BookMetadata metadata = new BookMetadata(
                    book.getTitle(),
                    authors,
                    totalSpreads,
                    currentSpreadIndex,
                    filePath,
                    "last_read",
                    annotations,
                    bookmarkedSpreads
            );

            LibraryLayout.lumeMetadata.addBookMetadata(bookId, metadata);
            LibraryLayout.bookSelf.addBookDisplay(new BookDisplay(stage, bookId, metadata));
        }
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

        // Show author and title vertically
        VBox titleAndAuthor = new VBox();
        titleAndAuthor.setSpacing(10);
        titleAndAuthor.setAlignment(Pos.TOP_LEFT);
        titleAndAuthor.getStyleClass().add("book-short-details-title-author");

        // Title
        Label title = new Label(book.getTitle());
        title.getStyleClass().add("book-short-details-title");
        titleAndAuthor.getChildren().addAll(title);
        title.setStyle("""
                    -fx-spacing: 100px;
                    -fx-font-weight: 700;
                """);

        // Author
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

            updatePageInfo();

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

                fullText.append(body.text());

                String htmlContent = null;
                if (body != null) {
                    processContent(body);

                    body.select("img").forEach(element -> {
                        Set<String> keys = imgResource
                                                .keySet()
                                                .stream()
                                                .filter(s -> element.attr("src")
                                                                          .contains(s)).collect(Collectors.toSet());

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

        leftSidePanel.getChildren().add(new SubTitleVBox(this.getRightSidePanelWidth(), 20, "Bookmarks"));
        bookmarkedSpreads.forEach(bookmarkedSpread -> {
            setBookMarkOption((int) bookmarkedSpread);
        });

    }

    private void setBookMarkOption(int bookmarkedSpread) {
        SidePanelOption bookmarkOption = new SidePanelOption(this.getLeftSidePanelWidth(), 20, BOOKMARK_ICON, "Spread %d".formatted(bookmarkedSpread + 1));
        bookmarkOption.setId("bookmark-%d".formatted(bookmarkedSpread + 1));
        bookmarkOption.getIconButton().setOnAction(e -> {
            currentSpreadIndex = bookmarkedSpread;
            displayCurrentSpread();
            updatePageInfo();
        });

        leftSidePanel.getChildren().add(bookmarkOption);
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
        SidePanelOption sidePanelOption = new SidePanelOption(this.getLeftSidePanelWidth(), 20, "", title);
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

        File annotateJSFile = new File("src/main/resources/com/example/lume/annotate.js");
        String annotateJS = Files.readString(Paths.get(annotateJSFile.getAbsolutePath()));

        // Structure the HTML
        String html = String.format("""
                   <html>
                        <head>
                            <style>%s</style>
                        </head>
                        <body>
                            <div class='book-container'><img src='%s' class='lume-img'>%s</div>
                        </body>
                        <script>
                            %s
                        </script>
                   </html>""",
                css, coverImg, fullBookContent, annotateJS
        );

        // Load content
        bookWebEngine.loadContent(html);
        bookWebEngine.setJavaScriptEnabled(true);

        // When content fully loads
        bookWebEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                Platform.runLater(() -> {
                    try {
                        // Small delay to ensure rendering is complete
                        Thread.sleep(100);

                        // Calculate the spread and display the current one
                        if (totalSpreads == 0) calculateTotalSpreads();
                        displayCurrentSpread();

                        bookWebEngine.executeScript(String.format("""
                            document.body.style.fontSize = '%s';
                            document.body.style.fontFamily = '%s';
                            """, lumeMetadata.getSettings().getFontSize(), lumeMetadata.getSettings().getFont()));


                        if (!annotations.isEmpty()) {
                            ObjectMapper objectMapper = new ObjectMapper();
                            annotations.forEach(annotation -> {
                                String annotationStr = null;
                                try {
                                    annotationStr = objectMapper.writeValueAsString(annotation);

                                    // Execute and check result
                                    Object result = bookWebEngine.executeScript("restoreAnnotation(" + annotationStr + ")");
                                } catch (JsonProcessingException e) {
                                    System.out.println("Error in displaying annotations: " + e.getMessage());
                                }
                            });
                        }

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
            updateBookMark();

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
        prevButton.setTooltip(new Tooltip("ALT + Left-Arrow"));

        ButtonIcon nextButton = new ButtonIcon("M4.646 1.646a.5.5 0 0 1 .708 0l6 6a.5.5 0 0 1 0 .708l-6 6a.5.5 0 0 1-.708-.708L10.293 8 4.646 2.354a.5.5 0 0 1 0-.708");
        nextButton.setOnAction(e -> {
            if (currentSpreadIndex < totalSpreads - 1) {
                currentSpreadIndex++;
                displayCurrentSpread();
                updatePageInfo();
            }
        });
        nextButton.setTooltip(new Tooltip("ALT + Right-Arrow"));

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
