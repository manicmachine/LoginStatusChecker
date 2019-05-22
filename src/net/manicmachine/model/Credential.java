package net.manicmachine.model;

public class Credential {
    private String credName;
    private String username;
    private String password;
    private String credText;
    private CredType credType;

    public Credential (String credName, String username, String password, String credText, CredType credType) {
        this.credName = credName;
        this.username = username;
        this.password = password;
        this.credText = credText;
        this.credType = credType;
    }

    public String getCredName() {
        return credName;
    }

    public void setCredName(String credName) {
        this.credName = credName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCredText() {
        return credText;
    }

    public void setCredText(String credText) {
        this.credText = credText;
    }

    public CredType getCredType() {
        return credType;
    }

    public void setCredType(CredType credType) {
        this.credType = credType;
    }
}
