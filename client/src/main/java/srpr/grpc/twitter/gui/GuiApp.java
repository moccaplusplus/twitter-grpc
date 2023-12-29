package srpr.grpc.twitter.gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import srpr.grpc.twitter.Keycloak;

import static java.lang.System.exit;

public class GuiApp extends Application {
    private Stage stage;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        stage.setTitle("Twitter gRCP Client");
        showLogin();
        stage.show();
    }

    private void showLogin() {
        if (stage.isMaximized()) stage.setMaximized(false);
        transitionTo(new LoginPanel(this::onLogin));
        stage.setResizable(false);
    }

    private void showTwitter(Keycloak.Session session) {
        transitionTo(new MainPanel(session, this::showLogin));
        stage.setResizable(true);
    }

    private void transitionTo(Region newParent) {
        stage.setScene(new Scene(newParent));
        stage.setMinWidth(newParent.getPrefWidth());
        stage.setMinHeight(newParent.getPrefHeight());
        stage.centerOnScreen();
    }

    private void onLogin(Keycloak.Session session) {
        if (session == null) {
            stage.close();
            return;
        }
        showTwitter(session);
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        exit(0);
    }
}