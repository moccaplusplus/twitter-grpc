package srpr.grpc.twitter.gui;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Consumer;

public class Utils {
    public static <T> T initComponent(Region component, String layout) {
        final var fxmlLoader = new FXMLLoader(component.getClass().getResource(layout));
        fxmlLoader.setRoot(component);
        fxmlLoader.setController(component);
        try {
            return fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public static void addTooltipIfClipped(Label label) {
        label.needsLayoutProperty().addListener((observable, oldValue, newValue) -> {
            if (isClipped(label)) {
                var tooltip = new Tooltip(label.getText());
                tooltip.setShowDelay(Duration.ZERO);
                tooltip.setShowDuration(Duration.seconds(15));
                tooltip.setAutoHide(true);
                label.setTooltip(tooltip);
            } else {
                label.setTooltip(null);
            }
        });
    }

    public static boolean isClipped(Label label) {
        var text = new Text(label.getText());
        text.setFont(label.getFont());
        var tb = text.getBoundsInLocal();
        var stencil = new Rectangle(tb.getMinX(), tb.getMinY(), tb.getWidth(), tb.getHeight());
        var intersection = Shape.intersect(text, stencil);
        var ib = intersection.getBoundsInLocal();
        return ib.getWidth() > label.getWidth();
    }

    public static <T> void runTask(Callable<T> task, Consumer<T> onSuccess, Consumer<Exception> onError, Runnable onFinish) {
        ForkJoinPool.commonPool().execute(() -> {
            try {
                var result = task.call();
                if (onSuccess != null) Platform.runLater(() -> onSuccess.accept(result));
            } catch (Exception e) {
                if (onError != null) Platform.runLater(() -> onError.accept(e));
            } finally {
                if (onFinish != null) Platform.runLater(onFinish);
            }
        });
    }
}
