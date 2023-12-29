package srpr.grpc.twitter.gui;

import javafx.application.Platform;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import srpr.grpc.twitter.TwitterServiceOuterClass.TwitItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TwitListView extends ScrollPane {
    private final VBox vBox;

    public TwitListView() {
        vBox = new VBox();
        setContent(vBox);
        setFitToWidth(true);
        setVbarPolicy(ScrollBarPolicy.ALWAYS);
        focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) setFocused(false);
        });
        setFocused(false);
    }

    public void setItems(List<TwitItem> items) {
        int pos = 0;
        var reversed = new ArrayList<>(items);
        Collections.reverse(reversed);
        var children = vBox.getChildren();
        while (pos < reversed.size() && pos < children.size()) {
            ((TwitItemView) children.get(pos)).setItem(reversed.get(pos));
            pos++;
        }
        if (pos < reversed.size()) {
            children.addAll(reversed.stream().skip(pos).limit(reversed.size() - pos).map(TwitItemView::new).toList());
        } else if (pos < children.size()) {
            children.remove(pos, children.size());
        }
        Platform.runLater(() -> setVvalue(getVmax()));
    }
}
