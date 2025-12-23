import java.sql.*;

public class SimpleTest {
    public static void main(String[] args) {
        System.out.println("Testing AIDSYNC components...");
        
        // Test SQLite (should work without external dependencies)
        try {
            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection("jdbc:sqlite:test.db");
            System.out.println("SQLite connection: SUCCESS");
            conn.close();
        } catch (Exception e) {
            System.out.println("SQLite connection: FAILED - " + e.getMessage());
        }
        
        System.out.println("\nTo run the full JavaFX application:");
        System.out.println("1. Download JavaFX 21 SDK");
        System.out.println("2. Add JavaFX JARs to classpath");
        System.out.println("3. Add BCrypt JAR to lib folder");
        System.out.println("4. Use VM options: --module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.fxml");
    }
}