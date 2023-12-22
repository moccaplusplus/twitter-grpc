package srpr.grpc.twitter;

import io.grpc.CallCredentials;
import srpr.grpc.twitter.TwitterServiceOuterClass.TwitItem;

import javax.naming.AuthenticationException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

import static java.lang.System.out;
import static srpr.grpc.twitter.Config.CONFIG;

public record Console(GrpcClient client) {
    private static final BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

    public static void main(String[] args) throws IOException, AuthenticationException {
        out.println("Welcome to Twitter Console");
        var host = CONFIG.serverHost();
        var port = CONFIG.serverPort();
        out.println("Server: " + host + ":" + port);
        var credentials = login();
        try (var client = new GrpcClient(host, port, Tls.channelCredentials(), credentials)) {
            new Console(client).loop();
        }
    }

    private static CallCredentials login() throws IOException, AuthenticationException {
        out.print("Login: ");
        var login = in.readLine();
        out.print("Passwd: ");
        var passwd = in.readLine();
        return Keycloak.DEFAULT.login(login, passwd);
    }

    private void loop() throws IOException {
        while (true) {
            out.println("Type:");
            out.println("\t/r - to receive last twits.");
            out.println("\t/w - to write a new twit.");
            out.println("\t/q - to exit.");
            var cmd = in.readLine();
            if (cmd.toLowerCase().startsWith("/r")) receive();
            else if (cmd.startsWith("/w")) write();
            else if (cmd.startsWith("/q")) break;
            out.println("---");
        }
    }

    private void receive() throws IOException {
        out.print("How many twits to fetch? [1, 10] ");
        var answer = in.readLine();
        final int count;
        try {
            count = Integer.parseInt(answer);
        } catch (NumberFormatException e) {
            out.println(e.getMessage());
            return;
        }

        var twits = client.get(count);
        if (twits.isEmpty()) {
            out.println("---");
            out.println("No twits available");
        } else {
            twits.forEach(this::printTwit);
            out.println("---");
            out.println("Available twits: " + twits.size() + "/" + count);
        }
    }

    private void write() throws IOException {
        out.println("Type your twit, please: ");
        var msg = in.readLine();
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
        out.printf("Added by %s (%s) at: %s%n", twit.getAuthor().getName(),
                twit.getAuthor().getEmail(), new Date(twit.getTimestamp()));
    }
}
