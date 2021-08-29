package com.kantenkugel.totpapp.control;

import com.kantenkugel.totpapp.util.Crypto;
import javafx.application.Platform;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.VBox;

import javax.crypto.SecretKey;
import javax.security.auth.DestroyFailedException;
import java.util.Optional;
import java.util.concurrent.*;

public class KeyHandler {
    private static final KeyHandler INSTANCE = new KeyHandler();
    private static final ScheduledExecutorService EXECUTOR = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        t.setUncaughtExceptionHandler((thread, throwable) -> {
            throwable.printStackTrace();
        });
        return t;
    });

    public static KeyHandler getInstance() {
        return INSTANCE;
    }

    private SecretKey key;
    private CompletableFuture<SecretKey> futureHolder;
    private String keyData;

    public void initialize(String keyData) {
        this.keyData = keyData;
    }

    public CompletionStage<SecretKey> getKey() {
        synchronized(this) {
            if(key != null) {
                return CompletableFuture.completedFuture(key);
            }
            if(futureHolder != null) {
                return futureHolder;
            }
            futureHolder = new CompletableFuture<>();
            Platform.runLater(this::generateKey);
            return futureHolder;
        }
    }

    private void generateKey() {
        Optional<String> password = getPasswordDialog().showAndWait();
        if(password.isEmpty()) {
            futureHolder.complete(null);
            return;
        }
        SecretKey key = Crypto.getKey(password.get(), keyData);
        synchronized(this) {
            this.key = key;
            futureHolder.complete(key);
            futureHolder = null;
            EXECUTOR.schedule(this::clearKey, 15, TimeUnit.MINUTES);
        }
    }

    private void clearKey() {
        synchronized(this) {
            if(key != null) {
                try {
                    key.destroy();
                } catch(DestroyFailedException e) {
                    e.printStackTrace();
                }
                key = null;
            }
        }
    }

    private Dialog<String> getPasswordDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Passworteingabe");
        dialog.setHeaderText("Bitte Passwort (erneut) eingeben.");
        dialog.setGraphic(null);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        VBox content = new VBox();
        PasswordField pwd = new PasswordField();
        content.getChildren().add(pwd);

        dialog.setOnShown(event -> Platform.runLater(pwd::requestFocus));

        dialog.getDialogPane().setContent(content);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return pwd.getText();
            }
            return null;
        });
        return dialog;
    }

    private KeyHandler() {}
}
