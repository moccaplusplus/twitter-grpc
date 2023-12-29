package srpr.grpc.twitter.gui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.BorderPane;
import srpr.grpc.twitter.Config;
import srpr.grpc.twitter.GrpcClient;
import srpr.grpc.twitter.Keycloak;
import srpr.grpc.twitter.Tls;

import java.util.Date;

import static java.lang.String.format;
import static srpr.grpc.twitter.gui.Utils.initComponent;

public class MainPanel extends BorderPane {
    private static final String LAYOUT = "/main_panel.fxml";
    private static final String COUNT_REGEXP = "^[1-9][0-9]?$";

    private final GrpcClient grpcClient;
    private final Keycloak.User user;

    @FXML
    private TextField twitCountField;
    @FXML
    private Button refreshButton;
    @FXML
    private ProgressIndicator twitLoader;
    @FXML
    private TwitListView twitList;
    @FXML
    private TextArea twitEditBox;
    @FXML
    private Button addTwitButton;
    @FXML
    private Label loggedUserLabel;
    @FXML
    private Button logoutButton;
    @FXML
    private Label statusLabel;

    public MainPanel(Keycloak.Session session, Runnable logoutListener) {
        initComponent(this, LAYOUT);
        user = session.user();
        loggedUserLabel.setText(format("Logged in as: %s (%s)", user.name(), user.email()));
        grpcClient = new GrpcClient(
                Config.CONFIG.serverHost(), Config.CONFIG.serverPort(),
                Tls.channelCredentials(), session.credentials());
        twitCountField.setTextFormatter(new TextFormatter<>(change ->
                change.getControlNewText().matches(COUNT_REGEXP) ? change : null));
        refreshButton.setOnAction(event -> loadTwits());
        twitEditBox.textProperty().addListener((observable, oldValue, newValue) -> onTwitEdit());
        addTwitButton.setOnAction(event -> addTwit());
        logoutButton.setOnAction(event -> logoutListener.run());
        Utils.addTooltipIfClipped(statusLabel);
        loadTwits();
        onTwitEdit();
        Platform.runLater(this::requestFocus);
    }

    private void onTwitEdit() {
        addTwitButton.setDisable(twitEditBox.getText().isBlank());
    }

    private void addTwit() {
        var twitText = twitEditBox.getText();
        twitEditBox.setDisable(true);
        addTwitButton.setDisable(true);
        Utils.runTask(
                () -> grpcClient.send(twitText),
                twit -> {
                    statusLabel.setText("Twit Added: " + new Date(twit.getTimestamp()));
                    twitEditBox.setText("");
                    loadTwits();
                },
                e -> {
                    statusLabel.setText("Adding Twit Failed: " + e.getMessage());
                    addTwitButton.setDisable(false);
                },
                () -> twitEditBox.setDisable(false)
        );
    }

    private void loadTwits() {
        twitList.setVisible(false);
        twitLoader.setVisible(true);
        var count = Integer.parseInt(twitCountField.getText());
        statusLabel.setText(format("Loading Twits: Requested Count: %d", count));
        Utils.runTask(
                () -> grpcClient.get(count),
                twits -> {
                    statusLabel.setText(format("Twit Load Success: %d/%d (Loaded/Requested)", twits.size(), count));
                    twitList.setItems(twits);
                },
                e -> statusLabel.setText("Loading Twits Failed: " + e.getMessage()),
                () -> {
                    twitLoader.setVisible(false);
                    twitList.setVisible(true);
                });
    }
}