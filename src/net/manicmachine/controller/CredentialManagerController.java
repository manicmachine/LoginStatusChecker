package net.manicmachine.controller;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import net.manicmachine.model.Credential;

import java.net.URL;
import java.util.ResourceBundle;

public class CredentialManagerController implements Initializable {

    @FXML TableView credTable;
    @FXML TableColumn<Credential, String> nameCol;
    @FXML TableColumn<Credential, String> userCol;
    @FXML TableColumn<Credential, String> typeCol;
    @FXML TableColumn<Credential, String> patternCol;
    @FXML TextField nameText;
    @FXML TextField userText;
    @FXML TextField patternText;
    @FXML ChoiceBox<String> typeSelection;

    private CredentialManager credentialManager;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> {
            nameCol.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getCredName()));
            nameCol.setCellFactory(column -> new TableCell<Credential, String>() {
                @Override
                protected void updateItem(String text, boolean empty) {
                    super.updateItem(text, empty);
                    setText(text);
                }
            });

            userCol.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getUsername()));
            userCol.setCellFactory(column -> new TableCell<Credential, String>() {
                @Override
                protected void updateItem(String text, boolean empty) {
                    super.updateItem(text, empty);
                    setText(text);
                }
            });

            typeCol.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getCredType().toString()));
            typeCol.setCellFactory(column -> new TableCell<Credential, String>() {
                @Override
                protected void updateItem(String text, boolean empty) {
                    super.updateItem(text, empty);
                    setText(text);
                }
            });

            patternCol.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getCredPattern()));
            patternCol.setCellFactory(column -> new TableCell<Credential, String>() {
                @Override
                protected void updateItem(String text, boolean empty) {
                    super.updateItem(text, empty);
                    setText(text);
                }
            });

            credTable.setItems(credentialManager.getCredentialList());
        });

        // TODO: Set selection listener to populate associated textfields and choicebox. Additionally, add add/delete/update buttons
    }

    void initData(CredentialManager credentialManager) {
        this.credentialManager = credentialManager;
    }
}
