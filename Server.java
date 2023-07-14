/**********************************************
 Workshop 11
 Course: JAC444 - Semester 4
 Last Name: Tse
 First Name: Chungon
 ID: 154928188
 Section: NAA
 This assignment represents my own work in accordance with Seneca Academic Policy.
 CHUNGON
 Date: 19 Apr 2023
 **********************************************/
package application;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Scanner;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * Server
 */
public class Server extends Application {
    /**
     * constructor
     */
    public Server(){}
    private final ArrayList<Client> clients = new ArrayList<>();
    private final TextArea textArea = new TextArea();
    private ServerSocket serverSocket;
    private Socket socket;

    @Override
    public void start(Stage primaryStage) {
        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets(15, 15, 15, 15));
        pane.setPrefWidth(Double.MAX_VALUE);
        pane.setPrefHeight(Double.MAX_VALUE);

        ScrollPane scrollPane = new ScrollPane(textArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        pane.setCenter(scrollPane);

        Scene scene = new Scene(pane, 650, 250);
        scene.setFill(Color.TRANSPARENT); // make the background of the scene transparent
        scene.getRoot().setStyle("-fx-background-color: steelblue;"); // set the color of the window title bar

        primaryStage.setScene(scene);
        primaryStage.setTitle("Multi-thread Server");
        primaryStage.show();

        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(8080);
                Platform.runLater(() ->
                        textArea.appendText("MultiThreadServer started at " +
                                DateTimeFormatter.ofPattern("EEEE MMMM d kk:mm:ss z u")
                                        .format(ZonedDateTime.now(ZoneId.systemDefault())) + "\n\n"));

                while (true) {
                    socket = serverSocket.accept();
                    clients.add(new Client(socket));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        primaryStage.setOnCloseRequest(e -> {
            e.consume();
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Exit");
            alert.setHeaderText(null);
            alert.setContentText("Do you want to exit?");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    // close all clients
                    for (Client client : clients) {
                        client.close();
                        System.out.println("Chat has disconnected");
                    }
                    // close the server socket
                    if (socket != null) {
                        socket.close();
                        System.out.println("Server closed");
                    }
                    System.exit(0);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });
    }

    private void broadcast(String message) {
        Platform.runLater(() -> textArea.appendText(message + "\n"));//blank line on Server window
        LocalDateTime now = LocalDateTime.now();
        String fileName = "chat_log_" + DateTimeFormatter.ofPattern("yyyy-MM-dd").format(now) + ".txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
            writer.write("[" + DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(now) + "] " + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (Client client : clients) {
            client.sendMessage(message);
        }
    }

    private class Client implements Runnable {
        private final Socket socket;
        private PrintWriter writer;

        public Client(Socket socket) {
            this.socket = socket;
            new Thread(this).start();
        }

        @Override
        public void run() {
            try (Scanner scanner = new Scanner(socket.getInputStream())) {
                writer = new PrintWriter(socket.getOutputStream());
                Platform.runLater(() ->
                        textArea.appendText("Connection from " + socket  + " at " +
                                DateTimeFormatter.ofPattern("EEEE MMMM d - kk:mm:ss z u")
                                        .format(ZonedDateTime.now(ZoneId.systemDefault())) + "\n\n"));

                while (true) {
                    String message = scanner.nextLine();
                    broadcast(message + "\n");
                }
            } catch (IOException | NoSuchElementException e) {
                Platform.runLater(() -> textArea.appendText("Chat has disconnected.\n"));
                clients.remove(this);

            }
        }
        public void sendMessage(String message) {
            writer.println(message);
            writer.flush();
        }

        public void close() throws IOException{
            if (socket != null) {
                socket.close();
            }
        }
    }

    /**
     * main
     * @param args  launch
     */
    public static void main(String[] args) {
        launch(args);
    }
}
