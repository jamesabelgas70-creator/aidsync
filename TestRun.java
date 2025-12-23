public class TestRun {
    public static void main(String[] args) {
        System.out.println("Java is working!");
        System.out.println("Java version: " + System.getProperty("java.version"));
        
        try {
            Class.forName("javafx.application.Application");
            System.out.println("JavaFX is available");
        } catch (ClassNotFoundException e) {
            System.out.println("JavaFX NOT found - need to add to classpath");
        }
        
        try {
            Class.forName("at.favre.lib.crypto.bcrypt.BCrypt");
            System.out.println("BCrypt is available");
        } catch (ClassNotFoundException e) {
            System.out.println("BCrypt NOT found - need to add JAR to lib folder");
        }
    }
}