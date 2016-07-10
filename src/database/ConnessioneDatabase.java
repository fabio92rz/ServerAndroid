package database;

import java.sql.*;

public class ConnessioneDatabase {

    static public Statement cmd;
    static public Connection connessione;
    static String db_name = "basket_time";
    static String user_name = "root";
    static String user_pass = "emperor92";
    static String driver = "com.mysql.jdbc.Driver";
    static String url = "jdbc:mysql://localhost/";

    public static void Connetti() {
        try {
            Class.forName(driver);
            connessione = DriverManager.getConnection(url + db_name, user_name, user_pass);
            cmd = connessione.createStatement();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void disconnetti() {
        try {
            connessione.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


