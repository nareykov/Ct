package sample;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.log4j.Logger;

import java.awt.*;
import java.io.*;

/**
 * Класс содержащий функции для работы с файлами
 */
public class Files {

    private static DataBase db = new DataBase();

    private static final Logger log = Logger.getLogger(DataBase.class);

    /**
     * Функция проверяющая наличие корневой папки каталога. и в случае ее
     * отсутствия создает корневую папку.
     */
    public static void createRoot() {
        File dir = new File("Root");
        if(dir.mkdir()) {
            log.info("Directory successfully created");
            System.out.println("Directory successfully created");
        }
        Main.setRootPath(dir.getAbsolutePath());
        Main.setCurrPath(dir.getAbsolutePath());


        //В базу данных пихать путь на Root. Потому что при последующих запусках будет выводится это окно
        /*FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName("Root");
        fileChooser.setTitle("Saving root directory");
        File dir = fileChooser.showSaveDialog(primaryStage);
        if(dir.mkdir()) {
            System.out.println("Directory successfully created");
        }
        Main.setRootPath(dir.getAbsolutePath());
        Main.setCurrPath(dir.getAbsolutePath());*/
    }

    /**
     * Функция отображает в таблице все файлы расположенные в директории по пути path
     * @param path Путь к директории
     */
    public static void showFiles(String path) {
        Main.getData().clear();
        if (!Main.getCurrPath().equals(Main.getRootPath())){
            Main.getData().add(new TableItem());
        }
        File dir = new File(path);
        System.out.println(dir.getAbsolutePath());
        File[] filesList = dir.listFiles();

        for (int i = 0; i < filesList.length; i++) {
            Main.getData().add(new TableItem(filesList[i]));
        }
    }

    /**
     * Функция окрывает папку или файл по пути path
     * @param path Путь к папке или файлу
     */
    public static void openFile(String path) {
        try {
            File file = new File(path);
            if (file.getName().equals("BACK")) {
                openFile(new File(file.getParent()).getParent());
            } else {
                if (file.isDirectory()) {
                    Main.setCurrPath(path);
                    showFiles(path);
                } else if (file.isFile()) {
                    Desktop desktop = Desktop.getDesktop();
                    desktop.open(file);
                }
            }
        } catch (IOException e) {
            log.error(e.getClass().getName() + ": " + e.getMessage());
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

    /**
     * Функция выбирает файл при помощи FileChooser и сохраняет его в текущую папку,
     * накладывая ограничения на обычных пользователей (10МБ в день)
     * @param primaryStage Основной стэйдж
     * @param currUser Текущей пользователь
     */
    public static void addFile(Stage primaryStage, String currUser) {
        try {
            final FileChooser fileChooser = new FileChooser();
            final File originalFile = fileChooser.showOpenDialog(primaryStage);

            db.connectToDataBase();
            if (currUser.equals("admin")) {
                File createdFile = new File((Main.getCurrPath() + "\\" + originalFile.getName()));
                copyFileUsingStream(originalFile, createdFile);
                createdFile.createNewFile();
                showFiles(Main.getCurrPath());
                db.insertIntoFileBase(createdFile.getName(), createdFile.getAbsolutePath());
                db.closeDataBase();
                return;
            }

            long mb = db.getMB(currUser);
            if (originalFile.length() <= mb) {
                File createdFile = new File((Main.getCurrPath() + "\\" + originalFile.getName()));
                copyFileUsingStream(originalFile, createdFile);
                createdFile.createNewFile();
                showFiles(Main.getCurrPath());
                db.insertIntoFileBase(createdFile.getName(), createdFile.getAbsolutePath());
                System.out.println("File length: " + originalFile.length());
                db.setMB(currUser ,mb - originalFile.length());
            } else {
                Stage stage = new Stage();
                stage.setTitle("MB is limited");

                HBox hBox = new HBox();
                db.connectToDataBase();
                Text msg = new Text("You have only: " + db.getMB(currUser) / 1024 + "KB");
                db.closeDataBase();
                hBox.getChildren().add(msg);
                hBox.setPadding(new Insets(20));

                stage.setResizable(false);
                stage.setScene(new Scene(hBox));
                stage.initModality(Modality.WINDOW_MODAL);
                stage.show();
            }
            db.closeDataBase();
        } catch (IOException e) {
            log.error(e.getClass().getName() + ": " + e.getMessage());
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

    /**
     * Поиск файла или папки по переданному имени и открытие его
     * @param name имя файла
     */
    public static void searchFile(String name) {
        db.connectToDataBase();
        String path = db.searchInFileBase(name);
        if (path == null) {
            Stage stage = new Stage();
            stage.setTitle("Not found");

            HBox hBox = new HBox();
            Text text = new Text("NOT FOUND");
            text.setFont(javafx.scene.text.Font.font(72));
            text.setFill(Color.RED);
            hBox.getChildren().add(text);

            stage.setResizable(false);
            stage.setScene(new Scene(hBox));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.show();
        } else {
            openFile(path);
        }
        db.closeDataBase();
    }

    /**
     * Создание папки в текущей директории (currPath)
     * @param currPath Путь к текущей директории
     */
    public static void createFolder(String currPath) {
        Stage stage = new Stage();
        stage.setTitle("Create folder");

        GridPane gridPane = new GridPane();
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(20));
        Text text = new Text("Enter the name: ");
        gridPane.add(text, 0, 0);
        TextField name = new TextField();
        gridPane.add(name, 1, 0);
        Button createBtn = new Button("Create");
        createBtn.setOnAction(event -> {
            File dir = new File(currPath + "\\" +  name.getText());
            if(dir.mkdir()) {
                log.info("Directory successfully created");
                System.out.println("Directory successfully created");
                Files.showFiles(currPath);
                stage.close();

                db.connectToDataBase();
                db.insertIntoFileBase(dir.getName(), dir.getAbsolutePath());
                db.closeDataBase();
            } else {
                log.error("Creation failed");
                System.out.println("Creation failed");
            }
        });
        createBtn.setPrefSize(70, 20);
        HBox hBox = new HBox(createBtn);
        hBox.setAlignment(Pos.BOTTOM_RIGHT);
        gridPane.add(hBox, 1, 1);

        stage.setResizable(false);
        stage.setScene(new Scene(gridPane));
        stage.initModality(Modality.WINDOW_MODAL);
        stage.show();
    }

    /**
     * Функция копирования потоков, при помощи потоков
     * @param source Копируемый файл
     * @param dest Копия
     * @throws IOException
     */
    private static void copyFileUsingStream(File source, File dest) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            is.close();
            os.close();
        }
    }

    /**
     * Функция удаления файла/папки
     * @param path путь
     */
    public static void deleteFile(String path) {
        File file = new File(path);
        if (deleteDirectory(file)) {
            System.out.println("File " + path + " deleted successfully");
            log.info("File " + path + " deleted successfully");
        } else {
            System.out.println("File " + path + " delete error");
            log.error("File " + path + " delete error");
        }
    }

    /**
     * Рекурсивная функция удаления
     * @param directory Объект удаляемого файла/папки
     * @return true - в случае успеха
     */
    public static boolean deleteDirectory(File directory) {
        db.connectToDataBase();
        if(directory.exists()){
            File[] files = directory.listFiles();
            if(null!=files){
                for(int i=0; i<files.length; i++) {
                    if(files[i].isDirectory()) {
                        deleteDirectory(files[i]);
                    }
                    else {
                        db.removeFromFileBase(files[i].getAbsolutePath());
                        files[i].delete();
                    }
                }
            }
        }
        db.removeFromFileBase(directory.getAbsolutePath());
        db.closeDataBase();
        return directory.delete();
    }
}