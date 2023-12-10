package srpr.grpc.twitter;

import srpr.grpc.twitter.TwitterServiceOuterClass.TwitItem;

import java.util.Date;

import static java.lang.System.console;
import static java.lang.System.out;

public record Console(GrpcClient client) {

    public static void main(String[] args) {
        out.println("Welcome to Twitter Console");
        out.print("Provide server url: ");
        var url = console().readLine();
        try (var client = new GrpcClient(url)) {
            new Console(client).loop();
        }
    }

    private void loop() {
        while (true) {
            out.println("Type:");
            out.println("\t/r - to receive last twits.");
            out.println("\t/w - to write a new twit.");
            out.println("\t/q - to exit.");
            var cmd = console().readLine();
            if (cmd.toLowerCase().startsWith("/r")) receive();
            else if (cmd.startsWith("/w")) write();
            else if (cmd.startsWith("/q")) break;
            out.println("---");
        }
    }

    private void receive() {
        out.print("How many twits to fetch? [1, 10] ");
        var answer = console().readLine();
        final int count;
        try {
            count = Integer.parseInt(answer);
        } catch (NumberFormatException e) {
            out.println(e.getMessage());
            return;
        }
        var twits = client.get(count);
        var size = twits.peek(this::printTwit).count();
        out.println("---");
        out.println(size == 0 ? "No twits available" : "Available twits: " + size + "/" + count);
    }

    private void write() {
        out.println("Type your twit, please: ");
        var msg = console().readLine();
        if (msg.isBlank()) {
            out.println("Empty message - not send");
            return;
        }
        if (msg.length() > 80) {
            out.println("Message truncated to 80 characters.");
            msg = msg.substring(0, 80);
        }
        out.println("Sending message");
        var twitItem = client.send(msg);
        printTwit(twitItem);
    }

    private void printTwit(TwitItem twit) {
        out.println("---");
        out.println(twit.getMessage());
        out.println("Added at: " + new Date(twit.getTimestamp()));
    }
}