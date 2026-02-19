import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    // Database credentials
    private static final String URL = "jdbc:mysql://localhost:3306/tetris?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";     
    private static final String PASSWORD = "";    

    // Method to get connection
    public static Connection getConnection() {
        Connection connection = null;

        try {

            Class.forName("com.mysql.cj.jdbc.Driver");

            connection = DriverManager.getConnection(URL, USER, PASSWORD);

            System.out.println("Database connected successfully!");

        } catch (ClassNotFoundException e) {
            System.out.println("MySQL JDBC Driver not found.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("Connection failed.");
            e.printStackTrace();
        }

        return connection;
    }
}
