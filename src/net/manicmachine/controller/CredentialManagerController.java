package net.manicmachine.controller;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import net.manicmachine.model.CredType;
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
    @FXML PasswordField passText;
    @FXML TextField patternText;
    @FXML ChoiceBox<String> typeSelection;
    @FXML Button addBtn;
    @FXML Button deleteBtn;
    @FXML Button clearBtn;

    private CredentialManager credentialManager;
    private ObservableList<Credential> credentialList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Set table cell values and listeners
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

            credentialList = credentialManager.getCredentialList();
            credTable.setItems(credentialList);
        });

        // Populate choicebox
        ObservableList<String> credTypes = FXCollections.observableArrayList();
        for (CredType credType: CredType.values()) {
            credTypes.add(credType.toString());
        }

        typeSelection.setItems(credTypes);

        // Populate textfields based on row selected
        credTable.getSelectionModel().selectedIndexProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection.intValue() > -1) {
                Credential selectedCredential = (Credential) credTable.getSelectionModel().getSelectedItem();

                nameText.textProperty().setValue(selectedCredential.getCredName());
                userText.textProperty().setValue(selectedCredential.getUsername());
                passText.textProperty().setValue(selectedCredential.getPassword());
                patternText.textProperty().setValue(selectedCredential.getCredPattern());
                typeSelection.getSelectionModel().select(selectedCredential.getCredType().toString());

                Platform.runLater(() -> {
                    addBtn.setText("Update");
                    deleteBtn.setDisable(false);
                });
            }
        });
    }

    void initData(CredentialManager credentialManager) {
        this.credentialManager = credentialManager;
    }

    @FXML void addCredential() {
        boolean isValid = true;
        String alertText = "";

        if (nameText.getText().isEmpty()) {
            isValid = false;
            alertText += "Credential name cannot be empty.\n";
        }
        if (userText.getText().isEmpty()) {
            isValid = false;
            alertText += "Username cannot be empty.\n";
        }
        if (passText.getText().isEmpty()) {
            isValid = false;
            alertText += "Password cannot be empty.\n";
        }
        if (typeSelection.getSelectionModel().getSelectedIndex() == -1) {
            isValid = false;
            alertText += "A credential type must be selected.\n";
        }
        if (patternText.getText().isEmpty()) {
            isValid = false;
            alertText += "REGEX Pattern cannot be empty.";
        }

        if (isValid) {
            Credential newCred = new Credential(nameText.getText(),
                    userText.getText(),
                    passText.getText(),
                    patternText.getText(),
                    CredType.valueOf(typeSelection.getValue())
            );

            if (credTable.getSelectionModel().getSelectedIndex() == -1) {
                credentialManager.addCredential(newCred);
                credentialList.add(newCred);
            } else {
                Credential selectedCred = (Credential) credTable.getSelectionModel().getSelectedItem();
                credentialManager.updateCredential(selectedCred, newCred);
                credentialList.remove(selectedCred);
                credentialList.add(newCred);
            }

            clearSelection();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Invalid Credential");
            alert.setHeaderText("The credential provided is invalid.");
            alert.setContentText(alertText);
            alert.showAndWait();
        }
    }

    @FXML void deleteCredential() {
        Credential selectedCred = (Credential) credTable.getSelectionModel().getSelectedItem();
        credentialManager.deleteCredential(selectedCred.getCredName());
        credentialList.remove(selectedCred);
        clearSelection();
    }

    @FXML void clearSelection() {
        Platform.runLater(() -> {
            credTable.getSelectionModel().clearSelection();
            nameText.clear();
            userText.clear();
            passText.clear();
            patternText.clear();
            typeSelection.getSelectionModel().clearSelection();
            addBtn.setText("Add");
            deleteBtn.setDisable(true);
        });
    }
}
