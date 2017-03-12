package sample;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class Main extends Application {

    private static String currUser = "";

    private static final String ADMIN_EMAIL = "admin";
    private static final String ADMIN_PASS = "admin";

    private static String rootPath;

    private static String currPath;

    private TableView<TableItem> table = new TableView<>();

    private final static ObservableList<TableItem> data = FXCollections.observableArrayList();

    private static Text info = new Text(currPath);

    private static int index = -1;

    DataBase db = new DataBase();

    /**
     * Метод отрисовывающий интерфейс
     * @param primaryStage Главный стэйдж
     */
    @Override
    public void start(Stage primaryStage) {

        Stage regStage = new Stage();

        Files.createRoot();
        Files.showFiles(currPath);

        primaryStage.setTitle("Catalog");
        primaryStage.setResizable(true);
        primaryStage.setMinWidth(300);
        primaryStage.setMinHeight(250);

        VBox vertical = new VBox();
        vertical.setAlignment(Pos.TOP_CENTER);

        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");
        fileMenu.setVisible(false);
        MenuItem addFile = new MenuItem("Add file");
        addFile.setOnAction((event -> {
            Files.addFile(primaryStage, currUser);
        }));
        MenuItem createFolder = new MenuItem("Create folder");
        createFolder.setOnAction(event -> {
            Files.createFolder(currPath);
        });
        MenuItem deleteFile = new MenuItem("Delete");
        deleteFile.setOnAction(event -> {
            if (index != -1) {
                String name = table.getSelectionModel().getSelectedItem().getName();
                Files.deleteFile(currPath + "\\" + name);
                data.remove(index);
            } else {

            }
        });
        fileMenu.getItems().addAll(addFile, createFolder, deleteFile);
        Menu exitMenu = new Menu("Exit");
        MenuItem exit = new MenuItem("Exit");
        exitMenu.getItems().add(exit);
        exit.setOnAction(event -> {
            primaryStage.close();
            regStage.show();
        });
        menuBar.getMenus().addAll(fileMenu, exitMenu);
        vertical.getChildren().add(menuBar);

        AnchorPane APath = new AnchorPane();
        HBox HPath = new HBox();
        TextField path = new TextField();
        Button openBtn = new Button("Search");
        openBtn.setOnAction(event -> {
            Files.searchFile(path.getText());
            path.clear();
        });
        openBtn.setMinWidth(70);
        path.setPrefWidth(2000);
        HPath.getChildren().addAll(path, openBtn);
        APath.getChildren().addAll(HPath);
        AnchorPane.setRightAnchor(HPath, 0.0);
        AnchorPane.setLeftAnchor(HPath, 0.0);
        vertical.getChildren().add(APath);

        AnchorPane ATable = new AnchorPane(table);
        table.setPrefSize(2000, 2000);

        TableColumn<TableItem, String> typeCol = new TableColumn<>("Type");
        typeCol.setMinWidth(55);
        typeCol.setCellValueFactory(
                new PropertyValueFactory<>("type"));

        TableColumn<TableItem, String> nameCol = new TableColumn<>("Name");
        nameCol.setMinWidth(400);
        nameCol.setCellValueFactory(
                new PropertyValueFactory<>("name"));

        TableColumn<TableItem, String> sizeCol = new TableColumn<>("Size");
        sizeCol.setMinWidth(100);
        sizeCol.setCellValueFactory(
                new PropertyValueFactory<>("size"));

        table.setItems(data);
        table.getColumns().addAll(typeCol, nameCol, sizeCol);

        table.setOnMouseClicked((event -> {
            index = table.getSelectionModel().getSelectedIndex();
            if (event.getClickCount() == 2) {
                String fileName = table.getSelectionModel().getSelectedItem().getName();
                if (fileName != null) {
                    Files.openFile(currPath + "\\" + fileName);
                }
            }
        }));

        AnchorPane.setBottomAnchor(table, 0.0);
        AnchorPane.setTopAnchor(table, 0.0);
        AnchorPane.setRightAnchor(table, 0.0);
        AnchorPane.setLeftAnchor(table, 0.0);
        vertical.getChildren().add(table);

        AnchorPane AInfo = new AnchorPane();
        AInfo.getChildren().add(info);
        AnchorPane.setBottomAnchor(info, 0.0);
        AnchorPane.setLeftAnchor(info, 0.0);
        vertical.getChildren().add(AInfo);


        AnchorPane AVertical = new AnchorPane(vertical);
        AnchorPane.setBottomAnchor(vertical, 0.0);
        AnchorPane.setTopAnchor(vertical, 0.0);
        AnchorPane.setRightAnchor(vertical, 0.0);
        AnchorPane.setLeftAnchor(vertical, 0.0);

        primaryStage.setScene(new Scene(AVertical, 700, 500));

        // Окно входа и регистрации

        GridPane loginGrid = new GridPane();
        loginGrid.setPadding(new Insets(25));
        loginGrid.setVgap(10);
        loginGrid.setHgap(10);
        Text emailLabel = new Text("Email: ");
        loginGrid.add(emailLabel, 0, 0);
        TextField emailLogin = new TextField();
        loginGrid.add(emailLogin, 1, 0);
        Text passLabel = new Text("Password: ");
        loginGrid.add(passLabel, 0, 1);
        PasswordField passLogin = new PasswordField();
        loginGrid.add(passLogin, 1, 1);
        Button guestBtn = new Button("Guest");
        guestBtn.setOnAction(event -> {
            regStage.close();
            primaryStage.show();
        });
        guestBtn.setPrefSize(80, 20);
        Text infoLogin = new Text("");
        loginGrid.add(infoLogin, 1, 3);
        Button enterBtn = new Button("Enter");
        enterBtn.setOnAction(event -> {
            if (emailLogin.getText().equals(ADMIN_EMAIL) && passLogin.getText().equals(ADMIN_PASS)) {
                regStage.close();
                primaryStage.show();
                Main.currUser = "admin";
                fileMenu.setVisible(true);
            } else if (!(emailLogin.getText().equals("") || passLogin.getText().equals(""))) {
                db.connectToDataBase();
                if (db.enter(emailLogin.getText(), passLogin.getText())) {
                    regStage.close();
                    primaryStage.show();
                    Main.currUser = emailLogin.getText();
                    fileMenu.setVisible(true);
                } else {
                    infoLogin.setText("Incorrect login or password");
                    infoLogin.setFill(Color.RED);
                }
                db.closeDataBase();
            } else {
                infoLogin.setText("Empty field");
                infoLogin.setFill(Color.RED);
            }
        });
        enterBtn.setPrefSize(80,20);
        HBox hBox = new HBox(guestBtn, enterBtn);
        hBox.setSpacing(5);
        hBox.setAlignment(Pos.BOTTOM_RIGHT);
        loginGrid.add(hBox, 1, 2);

        Tab login = new Tab("Login", loginGrid);
        login.setClosable(false);

        GridPane registerGrid = new GridPane();
        registerGrid.setPadding(new Insets(25));
        registerGrid.setVgap(10);
        registerGrid.setHgap(10);
        Text emailLabelR = new Text("Email: ");
        registerGrid.add(emailLabelR, 0, 0);
        TextField emailReg = new TextField();
        registerGrid.add(emailReg, 1, 0);
        Text passLabelR = new Text("Password: ");
        registerGrid.add(passLabelR, 0, 1);
        PasswordField passReg = new PasswordField();
        registerGrid.add(passReg, 1, 1);
        Text confirmLabel = new Text("Confirm\npassword");
        registerGrid.add(confirmLabel,0, 3);
        PasswordField confirmReg = new PasswordField();
        registerGrid.add(confirmReg, 1, 3);
        Text infoReg = new Text();
        registerGrid.add(infoReg, 1, 5);
        Button registerBtn = new Button("Register");
        registerBtn.setOnAction(event -> {
            if (!(emailReg.getText().equals("") || passReg.getText().equals("") || confirmReg.getText().equals(""))) {
                if (passReg.getText().equals(confirmReg.getText())) {
                    db.connectToDataBase();
                    if (!db.isRegistered(emailReg.getText())) {
                        db.insertIntoUsers(emailReg.getText(), passReg.getText());
                        infoReg.setText("Success");
                        infoReg.setFill(Color.GREEN);
                    } else {
                        infoReg.setText("User is registered");
                        infoReg.setFill(Color.RED);
                    }
                    db.closeDataBase();
                } else {
                    infoReg.setText("Password not confirmed");
                    infoReg.setFill(Color.RED);
                }
            } else {
                infoReg.setText("Empty field");
                infoReg.setFill(Color.RED);
            }
        });
        registerBtn.setPrefSize(80,20);
        registerGrid.add(registerBtn,1,4);
        Tab register = new Tab("Register", registerGrid);
        register.setClosable(false);

        TabPane tabPane = new TabPane(login, register);
        regStage.setScene(new Scene(tabPane, 300, 250));
        regStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static String getCurrPath() {
        return currPath;
    }

    public static void setCurrPath(String currPath) {
        Main.currPath = currPath;
        Main.info.setText(currPath);
    }

    public static ObservableList<TableItem> getData() {
        return data;
    }

    public static void setRootPath(String rootPath) {
        Main.rootPath = rootPath;
    }

    public static String getRootPath() {
        return rootPath;
    }
}