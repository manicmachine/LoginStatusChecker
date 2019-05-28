package net.manicmachine.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import net.manicmachine.model.Credential;

import java.util.HashMap;
import java.util.Map;

public class CredentialManager {
    private HashMap<String, Credential> credentials;

    public CredentialManager(HashMap<String, Credential> credentials) {
        this.credentials = credentials;
    }

    protected ObservableList<Credential> getCredentialList() {
        ObservableList<Credential> credentialList = FXCollections.observableArrayList();

        for (Map.Entry<String, Credential> credential: credentials.entrySet()) {
            credentialList.add(credential.getValue());
        }

        return credentialList;
    }
}
