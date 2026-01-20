package adfdev.erp.demo.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class database {
    private static final String url="jdbc:mysql://localhost:3306/erp?useSSL=false&serverTimezone=UTC";
    private static final String username="root";
    private static final String password="";

    public static Connection getConection() throws SQLException {
        return DriverManager.getConnection(url,username,password);
    }
}
