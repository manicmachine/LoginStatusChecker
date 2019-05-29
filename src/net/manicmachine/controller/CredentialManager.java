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

    protected void addCredential(Credential credential) {
        credentials.put(credential.getCredName(), credential);
    }

    protected void deleteCredential(String credentialName) {
        credentials.remove(credentialName);
    }

    protected void updateCredential(Credential originalCred, Credential updatedCred) {
        if (originalCred.getCredName().equals(updatedCred.getCredName())) {
            credentials.replace(originalCred.getCredName(), updatedCred);
        } else {
            credentials.remove(originalCred.getCredName());
            credentials.put(updatedCred.getCredName(), updatedCred);
        }
    }

    protected HashMap<String, Credential> getCredentials() {
        return credentials;
    }

    protected Credential getCredential(String credName) {
        return credentials.get(credName);
    }

    // Check stored credential patterns to determine which should be used
    protected String getCorrectCred(String hostname, String distinguishedName) {
        
    }
}
