package srpr.grpc.twitter.gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import srpr.grpc.twitter.Config;
import srpr.grpc.twitter.GrpcClient;
import srpr.grpc.twitter.Keycloak;
import srpr.grpc.twitter.Tls;

import static java.lang.System.exit;

public class GuiApp extends Application {
    private Stage stage;
    private GrpcClient grpcClient;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        stage.setTitle("Twitter gRCP Client");
        showLogin(null);
        stage.show();
    }

    private void showLogin(String status) {
        if (stage.isMaximized()) stage.setMaximized(false);
        transitionTo(new LoginPanel(status, this::onLogin));
        stage.setResizable(false);
    }

    private void showTwitter(Keycloak.Session session) {
        grpcClient = new GrpcClient(
                Config.get().serverHost(), Config.get().serverPort(),
                Tls.channelCredentials(), session.credentials());
        transitionTo(new MainPanel(session.user(), grpcClient, this::onLogout));
        stage.setResizable(true);
    }

    private void transitionTo(Region newParent) {
        stage.setScene(new Scene(newParent));
        stage.setMinWidth(newParent.getPrefWidth());
        stage.setMinHeight(newParent.getPrefHeight());
        stage.centerOnScreen();
    }

    private void onLogout(String status) {
        if (grpcClient != null) {
            grpcClient.close();
            grpcClient = null;
        }
        showLogin(status);
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
        if (grpcClient != null) {
            grpcClient.closeAndWait(2000);
            grpcClient = null;
        }
        exit(0);
    }
}