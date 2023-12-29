package srpr.grpc.twitter.gui;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import srpr.grpc.twitter.TwitterServiceOuterClass.TwitItem;

import java.util.Date;

import static java.lang.String.format;
import static srpr.grpc.twitter.gui.Utils.initComponent;

public class TwitItemView extends VBox {
    private static final String LAYOUT = "/twit_item_view.fxml";

    @FXML
    private TextField twitContent;
    @FXML
    private TextField twitFooter;

    public TwitItemView() {
        initComponent(this, LAYOUT);
    }

    public TwitItemView(TwitItem item) {
        this();
        setItem(item);
    }

    public void setItem(TwitItem twit) {
        twitContent.setText(twit.getMessage());
        twitFooter.setText(format("Added by %s (%s) at: %s", twit.getAuthor().getName(),
                twit.getAuthor().getEmail(), new Date(twit.getTimestamp())));
    }
}
