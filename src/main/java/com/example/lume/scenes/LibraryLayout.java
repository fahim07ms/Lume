package com.example.lume.scenes;

import com.example.lume.components.*;
import com.example.lume.layouts.BaseLayout;
import com.example.lume.layouts.BookSelf;
import com.example.lume.layouts.TitleBar;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.fasterxml.jackson.databind.ObjectMapper;

public class LibraryLayout extends BaseLayout {
    private final String allBooksSVGPath = "M9 3v15h3V3zm3 2l4 13l3-1l-4-13zM5 5v13h3V5zM3 19v2h18v-2z";
    private final String catalogSVGPath = "m12 14l-2-2l2-2l2 2zM9.875 8.471L8.183 6.78l2.68-2.681q.243-.242.54-.354q.299-.111.597-.111t.596.111t.54.354l2.681 2.68l-1.692 1.693L12 6.346zm-3.096 7.346l-2.681-2.68q-.242-.243-.354-.54q-.111-.299-.111-.597t.111-.596t.354-.54l2.68-2.681l1.693 1.692L6.346 12l2.125 2.125zm10.442 0l-1.692-1.692L17.654 12l-2.125-2.125l1.692-1.692l2.681 2.68q.242.243.354.54q.111.299.111.597t-.111.596t-.354.54zm-6.357 4.085l-2.681-2.68l1.692-1.693L12 17.654l2.125-2.125l1.692 1.692l-2.68 2.681q-.243.242-.54.354q-.299.111-.597.111t-.596-.111t-.54-.354";
    public Label titleText = new Label("Lume");
    public SubTitleVBox subTitleLibrary = new SubTitleVBox(400, 40, "Library");
    public SidePanelOption optionAllBooks = new SidePanelOption(
            400,
            50,
            allBooksSVGPath,
            "All Books");
    public SubTitleVBox subTitleCatalog = new SubTitleVBox(400, 40, "Catalog");

    public static LumeMetadata lumeMetadata;
    Stage stage;
    public static BookSelf bookSelf = null;

    public  LibraryLayout(Stage stage) {
        super();
        this.stage = stage;

        try {
            File file = new File("src/main/resources/com/example/lume/styles.css");
            this.getStylesheets().add("file:////" + file.getAbsolutePath().replace("\\", "/"));
        } catch (NullPointerException e) {
            System.out.println(e.getMessage());
        }

        // Title of Left side panel
        titleText.getStyleClass().add("title-text");
        titleText.setPadding(new Insets(0, 0, 20, 0));
        titleText.setStyle("""
                    -fx-font-size: 20px;
                    -fx-font-weight: 700;
                """);

        // Sub Title VBox - Library
        leftSidePanel.getChildren().addAll(titleText, subTitleLibrary, optionAllBooks);
        optionAllBooks.getIconButton().setStyle("-fx-background-color: rgba(54, 54, 56, 0.93);");

        optionAllBooks.getIconButton().setOnAction(e -> {
            stage.setScene(this.getScene());
        });


        // Sub Title Component - Catalog
        subTitleCatalog.setPadding(new Insets(30, 0, 10, 0));

        // Add Menu Option
//        SidePanelOption optionGutenberg = new SidePanelOption(400, 50, catalogSVGPath, "Gutenberg");
//        SidePanelOption optionStandardEbooks = new SidePanelOption(400, 50, catalogSVGPath, "Standard Ebooks");
//        SidePanelOption optionFeedBooks = new SidePanelOption(400, 50, catalogSVGPath, "Feedbooks");

//        leftSidePanel.getChildren().addAll(subTitleCatalog, optionGutenberg, optionStandardEbooks);

        TitleBar titleBar = new TitleBar(this.getRightSidePanelWidth(), 100, stage);;
        try {
            titleBar.setTitleBarText(subTitleLibrary.getSubTitleText());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        // Add title bar to right side panel
        rightSidePanel.getChildren().add(titleBar);

        rightSidePanel.setPadding(new Insets(0, 25, 0, 25));

        // Add Plus Icon to title bar (On opening shows the library)
        titleBar.setLeftBtn("M8 4a.5.5 0 0 1 .5.5v3h3a.5.5 0 0 1 0 1h-3v3a.5.5 0 0 1-1 0v-3h-3a.5.5 0 0 1 0-1h3v-3A.5.5 0 0 1 8 4", "Open a Book");
        titleBar.getLeftBtn().setOnAction(e -> showBookViewScene());

        loadLibrary();
        System.out.println(lumeMetadata);
    }

    private String fileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("EPUB Files", "*.epub")
        );

        try {
            File file = fileChooser.showOpenDialog(stage);
            Path path = file.toPath();
            return path.toString();
        } catch (NullPointerException err) {
            System.out.println(err.getMessage());
            return null;
        }
    }

    private void loadLibrary() {
//        if (System.getProperty("os.name").toLowerCase().contains("windows")) {}
//        else if (System.getProperty("os.name").toLowerCase().contains("linux")) {


        // Create lume folder if already doesn't exists
        Path lumeFolderPath = Paths.get(String.format("%s/.lume", System.getProperty("user.home")));
        if (!Files.exists(lumeFolderPath)) {
            File lumeDirectory = new File(lumeFolderPath.toString());
            boolean created = lumeDirectory.mkdirs();

            if (created) {
                System.out.println("Created Lume directory");
            } else {
                System.out.println("Failed to create Lume directory");
                System.exit(-1);
            }
        }

        // Create metadata file if already doesn't exist
        Path metaFilePath = lumeFolderPath.resolve("metadata.json");
        if (!Files.exists(metaFilePath)) {
            // Ask user if he/she wants to load epub files from device automatically
            try {
                Files.createFile(metaFilePath);
                System.out.println("Created metadata.json file in " + lumeFolderPath);

                System.out.println("Searching for epub files...");

                Path startDir = Paths.get(System.getProperty("user.home"));

//                    List<Path> epubFiles = new ArrayList<>();
//
//                    Files.walk(startDir)
//                            .filter(path -> {
//                                try {
//                                    if (!Files.isReadable(path)) return false;
//
//                                    return path.toString().endsWith(".epub");
//                                } catch (Exception e) {
//                                    System.out.println("Access denied: " + path);
//                                    return false;
//                                }
//                            })
//                            .forEach(epubFiles::add);
//
//                    lumeMetadata = loadMetadataFromEpubFiles(epubFiles);

                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.writeValue(metaFilePath.toFile(), lumeMetadata);

            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            lumeMetadata = objectMapper.readValue(metaFilePath.toFile(), LumeMetadata.class);

            if (lumeMetadata == null) {
                lumeMetadata = new LumeMetadata();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        if (lumeMetadata == null || lumeMetadata.getBookMetaDataMap().isEmpty()) {
            showEmptyLibraryRightSidePanel();
        } else {
            showLibraryWithBooks();
        }
    }

    private void showLibraryWithBooks() {
        if (bookSelf == null) {
            bookSelf = new BookSelf();
            lumeMetadata.getBookMetaDataMap().forEach((id, bookMetaData) -> {
                BookDisplay bookDisplay = new BookDisplay(stage, id, bookMetaData);
                bookSelf.addBookDisplay(bookDisplay);
            });

            bookSelf.setPadding(new Insets(10, 10, 10, 10));

            rightSidePanel.getChildren().add(bookSelf);
        }
    }

    private void showEmptyLibraryRightSidePanel() {
        // Show in the library that user's library is empty
        VBox emptyLibraryBox = new VBox();
        emptyLibraryBox.setAlignment(Pos.CENTER);
        emptyLibraryBox.setPadding(new Insets(10, 10, 10, 10));
        emptyLibraryBox.setPrefSize(this.getRightSidePanelWidth(), this.getRightSidePanelHeight() - 100);

        Label emptyLibraryHeading = new Label("Library is empty");
        emptyLibraryHeading.getStyleClass().add("empty-library-heading");
        emptyLibraryHeading.setPadding(new Insets(0, 0, 20, 0));
        emptyLibraryHeading.setStyle("""
                            -fx-font-size: 36px;
                            -fx-font-weight: 800;
                        """);

        Label emptyLibrarySubHeading = new Label("Open a book from device to start reading");
        emptyLibrarySubHeading.getStyleClass().add("empty-library-subheading");
        emptyLibrarySubHeading.setPadding(new Insets(0, 0, 20, 0));
        emptyLibrarySubHeading.setStyle("""
                            -fx-font-size: 20px;
                            -fx-font-weight: 500;
                        """);

        ButtonIcon openBookBtn = new ButtonIcon("");
        openBookBtn.setText("Open Book");
        openBookBtn.setAlignment(Pos.CENTER);
        openBookBtn.setStyle("""
                     -fx-font-weight: 600;
                """);
        openBookBtn.setMaxSize(180, 50);
        openBookBtn.setOnAction(e -> {
            showBookViewScene();
        });

        VBox.getVgrow(emptyLibraryBox);
        emptyLibraryBox.getChildren().addAll(emptyLibraryHeading, emptyLibrarySubHeading, openBookBtn);
        rightSidePanel.getChildren().add(emptyLibraryBox);
    }

//    private LumeMetadata loadMetadataFromEpubFiles(List<Path> epubFiles) {
//        LumeMetadata lumeMetadata = new LumeMetadata();
//
//        for (Path epubFile : epubFiles) {
//            try {
//                EpubReader epubReader = new EpubReader();
//                Book book = epubReader.readEpub(new FileInputStream(epubFile.toFile()));
//
//                List<String> authors = new ArrayList<>();
//                for (Author author : book.getMetadata().getAuthors()) {
//                    authors.add(author.getFirstname() + " " + author.getLastname());
//                }
//
//                BookMetadata bookMetadata = new BookMetadata(
//                        book.getTitle(),
//                        authors,
//                        -1,
//                        0,
//                        epubFile.toString(),
//                        "unknown");
//
//                String bookUUID = UUID.randomUUID().toString();
//                lumeMetadata.addBookMetadata(bookUUID, bookMetadata);
//                lumeMetadata.addCategory("unknown", bookUUID);
//                System.out.println("Added " +  epubFile.toString());
//            } catch (Exception e) {
//                System.out.println(e.getMessage());
//                System.out.println("Cannot open load epub file: " + epubFile.toString());
//            }
//        }
//
//        return lumeMetadata;
//    }

//    private void loadGutenbergData() {
//        rightSidePanel.getChildren().clear();
//
//        HttpResponse<String> response = null;
//        try {
//            HttpClient client = HttpClient.newBuilder()
//                    .followRedirects(HttpClient.Redirect.NORMAL)
//                    .build();
//            HttpRequest request = HttpRequest.newBuilder()
//                    .uri(URI.create("https://gutendex.com/books?page=1&languages=en&mime_type=text%2Fhtml&"))
//                    .build();
//            response = client.send(request, HttpResponse.BodyHandlers.ofString());
//
//            ObjectMapper mapper = new ObjectMapper();
//
//            // read the json strings and convert it into JsonNode
//            JsonNode node = mapper.readTree(response.body());
//
//            JsonNode results = node.get("results");
//            for (JsonNode result : results) {
//                JsonNode titleText = result.get("title");
//                JsonNode authorsText = result.get("authors");
//                JsonNode summaries = result.get("summaries");
//                JsonNode coverImage = result.get("formats").get("image/jpeg");
//                JsonNode downloadUrl = result.get("formats").get("application/epub+zip");
//
//                System.out.println(coverImage.toString());
//
////                VBox bookDetails = new VBox();
////                bookDetails.setPrefWidth(250);
////                bookDetails.setSpacing(5);
////                Image coverImg = new Image(coverImage.toString());
////                if (coverImg != null) {
////                    ImageView coverImageView = new ImageView();
////                    coverImageView.setImage(coverImg);
////                    coverImageView.getStyleClass().add("book-display-cover-image");
////                    bookDetails.getChildren().add(coverImageView);
////                    coverImageView.setPreserveRatio(true);
////                    coverImageView.setFitWidth(250);
////                }
////
////                HBox titleThreeDot = new HBox();
////                Region spacer = new Region();
////                HBox.setHgrow(spacer, Priority.ALWAYS);
////                titleThreeDot.setPrefWidth(250);
////                titleThreeDot.setAlignment(Pos.CENTER);
////
////                titleThreeDot.setPadding(new Insets(10, 0, 0, 0));
////
////                Label title = new Label(titleText.toString());
////                title.setStyle("""
////                 -fx-font-weight: 700;
////                 -fx-font-size: 20px;
////                """);
////
////
////
////                titleThreeDot.getChildren().addAll(title);
////
////                Label authors = new Label(authorsText.toString());
////                authors.setStyle("-fx-font-size: 16px;");
////
////
////                bookDetails.getChildren().addAll(titleThreeDot, authors);
////
////                rightSidePanel.getChildren().add(bookDetails);
//            }
//        } catch (IOException | InterruptedException e) {
//            System.out.println(e.getMessage());
//            e.printStackTrace();
//        }
//    }

    private void showBookViewScene() {
        Scene bookViewScene;
        try {
            String filePath = fileChooser();
            bookViewScene = new Scene(new BookViewScene(stage, stage.getScene(), filePath, lumeMetadata), this.getHomeLayoutWidth(), this.getHomeLayoutHeight());
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return;
        }

        try {
            File file = new File("src/main/resources/com/example/lume/styles.css");
            bookViewScene.getStylesheets().add("file:////" + file.getAbsolutePath().replace("\\", "/"));
        } catch (NullPointerException e) {
            System.out.println(e.getMessage());
        }
        stage.setScene(bookViewScene);
    }
}
