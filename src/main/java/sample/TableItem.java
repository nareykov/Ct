package sample;

import javafx.beans.property.SimpleStringProperty;
import java.io.File;

/**
 * Класс, элементы которого заносятся в таблицу главного окна
 */
public class TableItem {

    private final SimpleStringProperty type;
    private final SimpleStringProperty name;
    private final SimpleStringProperty size;

    /**
     * Создает BACK элемент в таблице
     */
    public TableItem() {
        this.type = new SimpleStringProperty("...");
        this.name = new SimpleStringProperty("BACK");
        this.size = new SimpleStringProperty("...");
    }

    /**
     * информацию из полученного файла записывает в новый объект
     * @param f Файл заносимый в таблицу
     */
    public TableItem(File f) {
        if (f.isDirectory()) {
            this.type = new SimpleStringProperty("<folder>");
        } else {
            this.type = new SimpleStringProperty("<file>");
        }
        this.name = new SimpleStringProperty(f.getName());
        this.size = new SimpleStringProperty((f.length() / 1024) + "KB");
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public void setType(String type) {
        this.type.set(type);
    }

    public void setSize(String size) {
        this.size.set(size);
    }

    public String getSize() {
        return size.get();
    }

    public String getType() {
        return type.get();
    }
}
