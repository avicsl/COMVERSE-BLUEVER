package UserControllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import Class.Student;
import Main.DatabaseHandler;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class UserLoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Button signup;

    private DatabaseHandler databaseHandler;

    public static Student loggedInUser; // Store the logged-in user for access

    public void initialize() {
        databaseHandler = DatabaseHandler.getInstance();
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String email = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert("Login Error", "Missing Fields", "Please enter both email and password.", Alert.AlertType.WARNING);
            return;
        }

        Student student = authenticateUser(email, password);
        if (student != null) {
            loggedInUser = student; // Save the logged-in student

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/UserUI/user.fxml"));
                Parent root = loader.load();

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Navigation Error", "Failed to load the homepage.", e.getMessage(), Alert.AlertType.ERROR);
            }
        }
        // Error messages are now handled within authenticateUser()
    }

    private Student authenticateUser(String email, String password) {
        String fullQuery = "SELECT * FROM users WHERE email = ? AND password = ?";
        String emailCheckQuery = "SELECT * FROM users WHERE email = ?";

        try (Connection conn = DatabaseHandler.getDBConnection()) {

            // First check: valid email & password combination
            try (PreparedStatement fullStmt = conn.prepareStatement(fullQuery)) {
                fullStmt.setString(1, email);
                fullStmt.setString(2, password);
                ResultSet fullResult = fullStmt.executeQuery();

                if (fullResult.next()) {
                    return new Student(
                        fullResult.getString("student_number"),
                        fullResult.getString("email"),
                        fullResult.getString("password"),
                        fullResult.getString("first_name"),
                        fullResult.getString("last_name"),
                        fullResult.getString("department"),
                        fullResult.getString("course")
                    );
                }
            }

            // Second check: does the email exist?
            try (PreparedStatement emailStmt = conn.prepareStatement(emailCheckQuery)) {
                emailStmt.setString(1, email);
                ResultSet emailResult = emailStmt.executeQuery();

                if (!emailResult.next()) {
                    showAlert("Login Failed", "No Account Found", "You are not registered in the system.", Alert.AlertType.ERROR);
                } else {
                    showAlert("Login Failed", null, "Invalid email or password.", Alert.AlertType.ERROR);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", null, "An error occurred while accessing the database.", Alert.AlertType.ERROR);
        }

        return null;
    }

    private void showAlert(String title, String header, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    public void signup(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/UserUI/usersignup.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    public void back(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/Main/start.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}
