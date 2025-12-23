import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class SimpleAIDSYNC extends JFrame {
    public SimpleAIDSYNC() {
        setTitle("AIDSYNC 2.0 - Simple Version");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        panel.add(new JLabel("Username:"));
        JTextField usernameField = new JTextField("admin");
        panel.add(usernameField);
        
        panel.add(new JLabel("Password:"));
        JPasswordField passwordField = new JPasswordField("password");
        panel.add(passwordField);
        
        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            
            if ("admin".equals(username) && "password".equals(password)) {
                JOptionPane.showMessageDialog(this, "Login Successful!\nAIDSYNC 2.0 is working!");
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials!");
            }
        });
        
        panel.add(loginButton);
        panel.add(new JLabel(""));
        
        add(panel);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeel());
            } catch (Exception e) {
                // Use default look and feel
            }
            new SimpleAIDSYNC().setVisible(true);
        });
    }
}