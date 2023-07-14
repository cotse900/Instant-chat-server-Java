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

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Scanner;

/**
 * Chat
 */
public class Chat extends Application {
    /**
     * constructor
     */
    public Chat(){}

    private final TextArea appMsg = new TextArea();
    private PrintWriter printServer;
    private TextArea newMessage = new TextArea();
    private Stage stage;
    private Socket socket;
    class NewMessage implements Runnable {

        public NewMessage(Socket s) {
            socket = s;
            Thread thread = new Thread(this);
            thread.start();
        }

        public void run() {
            try {
                try (Scanner getText = new Scanner(socket.getInputStream())) {
                    while (true) {
                        String message = getText.nextLine();
                        Platform.runLater(() -> appMsg.appendText(message+"\n"));//blank line on Chat window
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (NoSuchElementException e) {
                Platform.runLater(() -> appMsg.appendText("Server is closed.\n" +
                        "Your chat log has been saved. Please feel free to close this window."));
            }
        }
    }

    @Override
    public void start(Stage primaryStage) {
        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets(15, 15, 15, 15));
        pane.setPrefWidth(Double.MAX_VALUE);
        pane.setPrefHeight(Double.MAX_VALUE);
        appMsg.setEditable(false);

        Scene scene = new Scene(pane, 650, 400);
        scene.setFill(Color.TRANSPARENT); // make the background of the scene transparent
        scene.getRoot().setStyle("-fx-background-color: steelblue;");
        primaryStage.setScene(scene);
        primaryStage.show();
        this.stage = primaryStage;

        try {
            Socket socket = new Socket("localhost", 8080);

            if (socket.isConnected()){
                appMsg.appendText("Connected successfully, running on: " + socket + "\n\n");

                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("Welcome");
                dialog.setHeaderText("Enter your name:");

                Optional<String> result = dialog.showAndWait();
                if (result.isPresent()) {
                    String title = result.get();
                    primaryStage.setTitle(title);
                }
            }
            printServer = new PrintWriter(socket.getOutputStream());
            new NewMessage(socket);

        }
        catch (IOException e) {
            appMsg.appendText("Problem to connect to server / server not found!\n");
        }

        ScrollPane scrollPane = new ScrollPane(appMsg);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        pane.setCenter(scrollPane);

        Button m_btn = new Button("Send");
        m_btn.setPrefWidth(80);

        GridPane m_panel = new GridPane();
        m_panel.setHgap(5);
        m_panel.setVgap(5);
        m_panel.setAlignment(Pos.CENTER);

        newMessage = new TextArea();
        newMessage.setPrefColumnCount(50);
        newMessage.setPrefHeight(80);
        m_panel.add(newMessage, 0, 1);
        m_panel.add(m_btn, 1, 1);
        pane.setBottom(m_panel);

        m_btn.setOnAction(e -> sendMessage());
        newMessage.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER && e.isShiftDown()) {
                // Shift + Enter was pressed, insert a new line without sending the message
                int caretPos = newMessage.getCaretPosition();
                newMessage.setText(newMessage.getText().substring(0, caretPos) + "\n" + newMessage.getText().substring(caretPos));
                newMessage.positionCaret(caretPos + 1);
                e.consume();
            } else if (e.getCode() == KeyCode.ENTER) {
                // Enter was pressed, send the message
                sendMessage();
                e.consume();
            }
        });
        stage.setOnCloseRequest(e -> {
            e.consume();
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Exit");
            alert.setHeaderText(null);
            alert.setContentText("Do you want to exit?");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                if (socket != null) {
                    try {
                        socket.close();
                        System.out.println("Chat closed");
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
                Platform.exit();
            }
        });
    }

    private void sendMessage() {
        try {
            if (!newMessage.getText().equals("")) {
                printServer.print(stage.getTitle() + ": " + newMessage.getText());
                newMessage.setText("");
                printServer.flush();
                newMessage.requestFocus();
            } else {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
                alert.setTitle("Warning");
                alert.setHeaderText(null);
                alert.setContentText("Please input a message");
                alert.showAndWait();
            }
        } catch (NullPointerException n) {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Server is not running, client closing...");
            alert.showAndWait();
            System.exit(0);
        }
    }
}