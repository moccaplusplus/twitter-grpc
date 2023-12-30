package srpr.grpc.twitter.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import srpr.grpc.twitter.Keycloak;

import java.util.function.Consumer;

import static srpr.grpc.twitter.gui.Utils.initComponent;

public class LoginPanel extends BorderPane {
    private static final String LAYOUT = "/login_panel.fxml";

    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passwdField;
    @FXML
    private Button loginButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Label statusLabel;
    private final Consumer<Keycloak.Session> listener;

    public LoginPanel(String status, Consumer<Keycloak.Session> listener) {
        initComponent(this, LAYOUT);
        this.listener = listener;
        loginField.textProperty().addListener((observable, oldValue, newValue) -> onFieldEdit());
        passwdField.textProperty().addListener((observable, oldValue, newValue) -> onFieldEdit());
        loginButton.setOnAction(this::loginClicked);
        cancelButton.setOnAction(event -> listener.accept(null));
        onFieldEdit();
        statusLabel.setText(status == null ? "" : status);
        Utils.addTooltipIfClipped(statusLabel);
    }

    private void onFieldEdit() {
        statusLabel.setText("");
        loginButton.setDisable(loginField.getText().isEmpty() || passwdField.getText().isEmpty());
    }

    private void loginClicked(ActionEvent event) {
        statusLabel.setText("Connecting auth server...");
        loginButton.setDisable(true);
        cancelButton.setDisable(true);
        loginField.setDisable(true);
        passwdField.setDisable(true);
        Utils.runTask(
                () -> Keycloak.getDefault().login(loginField.getText(), passwdField.getText()),
                session -> {
                    statusLabel.setText("Login Success");
                    listener.accept(session);
                },
                e -> statusLabel.setText("Login failed: " + e.getMessage()),
                () -> {
                    cancelButton.setDisable(false);
                    loginField.setDisable(false);
                    passwdField.setDisable(false);
                });
    }
}
