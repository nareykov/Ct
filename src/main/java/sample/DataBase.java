package sample;

import java.io.File;
import java.sql.*;
import java.util.Calendar;
import org.apache.log4j.Logger;

/**
 * Класс содержит функции для работы с базой данных
 */
public class DataBase {
    private Connection c = null;

    private Statement stmt = null;

    private final long LIMIT = 10 * 1024 * 1024;

    private final String salt = "eshgfure";

    private static final Logger log = Logger.getLogger(DataBase.class);

    /** Устанавливает соединение с базой данных.
     * Перед подключением к базе данных производим проверку на её существование.
     * В зависимости от результата производим открытие базы данных или её восстановление
     */
    public void connectToDataBase() {
        if(!new File("database.db").exists()){
            if (!this.restoreDataBase()) {
                log.error("Tables not created");
                System.out.println("Tables not created");
            }
        } else {
            this.openDataBase();
        }
    }

    /**
     * Востановление базы данных.
     * Создается файл базы данных и таблицы.
     * @return false - файл или таблица не создались, true - успех)
     */
    private boolean restoreDataBase() {
        if (this.openDataBase()) {
            if (!this.createUsers() || !this.createFileBase()) {
                return false;
            } else {
                return true;
            }
        } else {
            log.error("Restore database failed");
            System.out.println("Restore database failed");
            return false;
        }
    }

    /**
     * Открытие базы данных или, создание и открытие.
     * @return false - возникло исключение при создании файла БД, true - в случае успеха
     */
    private boolean openDataBase() {
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:database.db");
        } catch ( Exception e ) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
        log.info("Opened database successfully");
        System.out.println("Opened database successfully");
        return true;
    }

    /**
     * Создание таблицы пользователей.
     * @return true - таблица успешно создана, false - исключение
     */
    private boolean createUsers() {
        try {
            stmt = c.createStatement();
            String sql = "CREATE TABLE Users " +
                    "(Email      TEXT           NOT NULL," +
                    " Pass       TEXT           NOT NULL," +
                    " MB         TEXT           NOT NULL," +
                    " Day       TEXT           NOT NULL," +
                    " Month     TEXT           NOT NULL," +
                    " Year     TEXT           NOT NULL)";
            stmt.executeUpdate(sql);
            stmt.close();
        } catch ( Exception e ) {
            log.error(e.getClass().getName() + ": " + e.getMessage());
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
        log.info("Table Users created successfully");
        System.out.println("Table Users created successfully");
        return true;
    }

    /**
     * Создание таблицы файлов.
     * @return true - таблица успешно создана, false - исключение
     */
    private boolean createFileBase() {
        try {
            stmt = c.createStatement();
            String sql = "CREATE TABLE FileBase " +
                    "(Name       TEXT                NOT NULL," +
                    " Path       TEXT                NOT NULL)";
            stmt.executeUpdate(sql);
            stmt.close();
        } catch ( Exception e ) {
            log.error(e.getClass().getName() + ": " + e.getMessage());
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
        log.info("Table FileBase created successfully");
        System.out.println("Table FileBase created successfully");
        return true;
    }

    /**
     * Закрывает базу данных.
     */
    public void closeDataBase() {
        try {
            c.close();
        } catch (SQLException e) {
            log.error(e.getClass().getName() + ": " + e.getMessage());
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(e.getErrorCode());
        }
        log.info("Database closed successfully");
        System.out.println("Database closed successfully");
    }

    /**
     * Запись в таблицу пользователей логина и пароля, а также отставшееся кол-во байт
     * и дату последнего добавления файлов.
     * @param email Мыло пользователя
     * @param pass Пароль
     */
    public void insertIntoUsers(String email, String pass) {
        try {
            stmt = c.createStatement();
            Calendar calendar = Calendar.getInstance();
            String sql = "INSERT INTO Users (Email, Pass, MB, Day, Month, Year) " +
                    "VALUES ('" + email + "', '" + pass + salt + "', '" + String.valueOf(LIMIT) + "', '" + String.valueOf(calendar.get(Calendar.DATE)) + "'," +
                    " '" +  String.valueOf(calendar.get(Calendar.MONTH)) + "', '" +  String.valueOf(calendar.get(Calendar.YEAR)) + "');";
            stmt.executeUpdate(sql);

            stmt.close();
        } catch ( Exception e ) {
            log.error(e.getClass().getName() + ": " + e.getMessage());
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        log.info("Recorded into Users successfully");
        System.out.println("Recorded into Users successfully");
    }

    /**
     * Запись в таблицу файлов имени файла и полного пути
     * @param name имя файла
     * @param path Полный путь
     */
    public void insertIntoFileBase(String name, String path) {
        try {
            stmt = c.createStatement();
            String sql = "INSERT INTO FileBase (Name, Path) " +
                    "VALUES ('" + name + "', '" + path + "');";
            stmt.executeUpdate(sql);

            stmt.close();
        } catch ( Exception e ) {
            log.error(e.getClass().getName() + ": " + e.getMessage());
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        log.info("Recorded into FileBase successfully");
        System.out.println("Recorded into FileBase successfully");
    }

    /**
     * Проверка логина и пароля пользователя при нажатии на Enter в окне входа.
     * @param email Мыло пользователя
     * @param pass Пароль
     * @return true - верный логин и пароль, false - неверные логин или пароль
     */
    public boolean enter(String email, String pass) {
        try {
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT * FROM Users WHERE Email = '" + email + "';" );
            if ((pass + salt).equals(rs.getString("Pass"))) {
                rs.close();
                stmt.close();
                return true;
            }
            rs.close();
            stmt.close();
        } catch ( Exception e ) {
            log.info("Incorrect login or password");
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
        return false;
    }

    /**
     * ищет в базе данных файл по имени и, в случае успеха, возвращает полный путь.
     * Если не найден, возвращает null.
     * @param name имя файла
     * @return Возвращает полный путь искомого файла
     */
    public String searchInFileBase(String name) {
        try {
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT * FROM FileBase WHERE Name = '" + name + "';" );
            if (rs.getString("Name") != null) {
                String path = rs.getString("Path");
                rs.close();
                stmt.close();
                return path;
            }
            rs.close();
            stmt.close();
        } catch ( Exception e ) {
            return null;
        }
        return null;
    }

    /**
     * Проверяет был ли зарегистрирован пользователь ранее.
     * @param email Мыло пользователя
     * @return Если уже зарегистрирован - true, если нет - false.
     */
    public boolean isRegistered(String email) {
        try {
            stmt = c.createStatement();
            ResultSet rs = stmt.executeQuery( "SELECT * FROM Users WHERE Email = '" + email + "';" );
            if (rs.getString("Email") != null) {
                rs.close();
                stmt.close();
                return true;
            }
            rs.close();
            stmt.close();
        } catch ( Exception e ) {
            return false;
        }
        return false;
    }

    /**
     * Достает из базы данных и возвращает кол-во оставшихся байт у текущего пользователя.
     * @param currUser Мыло текущего польвателя
     * @return Возвращает кол-во доступных пользователю байт
     */
    public long getMB(String currUser) {
        try {
            stmt = c.createStatement();

            Calendar calendar = Calendar.getInstance();
            int currDay = calendar.get(Calendar.DATE);
            int currMonth = calendar.get(Calendar.MONTH);
            int currYear = calendar.get(Calendar.YEAR);

            ResultSet rs = stmt.executeQuery( "SELECT * FROM Users WHERE Email = '" + currUser + "';" );
            int day = Integer.parseInt(rs.getString("Day"));
            int month = Integer.parseInt(rs.getString("Month"));
            int year = Integer.parseInt(rs.getString("Year"));

            if (currDay == day && currMonth == month && currYear == year) {
                long mb = Long.parseLong(rs.getString("MB"));
                rs.close();
                stmt.close();
                return mb;
            } else {
                String sql = "UPDATE Users SET MB = " + LIMIT + ", Day = " + currDay + ", Month = " + currMonth + ", Year= " + currYear + "  WHERE Email = '" + currUser + "';";
                stmt.executeUpdate(sql);
                rs.close();
                stmt.close();
                return LIMIT;
            }

        } catch ( Exception e ) {
            log.error("User not found");
            System.out.println("User not found");
            return 0;
        }
    }

    /**
     * Устанавливает количество байт оставшихся у текущего пользователя
     * @param currUser Мыло текущего польвателя
     * @param mb Устанавливаемое значение
     */
    public void setMB(String currUser, long mb) {
        try {
            stmt = c.createStatement();

            String sql = "UPDATE Users SET MB = " + mb + " WHERE Email = '" + currUser + "';";
            stmt.executeUpdate(sql);

        } catch ( Exception e ) {
            log.error("User not found");
            System.out.println("User not found");
            return;
        }
    }

    /**
     * Удаляет из базы данных файл/папку
     * @param path путь
     */
    public void removeFromFileBase(String path) {
        try {
            stmt = c.createStatement();

            String sql = "DELETE FROM FileBase WHERE Path = '" + path + "';";
            stmt.executeUpdate(sql);

        } catch ( Exception e ) {
            log.error("File " + path + " not removed from FileBase");
            System.out.println("File " + path + " not removed from FileBase");
            return;
        }
    }
}
