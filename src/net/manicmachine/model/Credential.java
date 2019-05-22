package net.manicmachine.model;

public class Credential {
    private String credName;
    private String username;
    private String password;
    private String credPattern;
    private CredType credType;

    public Credential (String credName, String username, String password, String credPattern, CredType credType) {
        this.credName = credName;
        this.username = username;
        this.password = password;
        this.credPattern = credPattern;
        this.credType = credType;
    }

    // TODO: Remove this override, only here for testing.
    @Override
    public String toString() {
        return "CredName: " + this.credName + ", Username: " + this.username + ", CredText: " + this.credPattern + ", CredType: " + this.credType.toString();
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

    public String getCredPattern() {
        return credPattern;
    }

    public void setCredPattern(String credPattern) {
        this.credPattern = credPattern;
    }

    public CredType getCredType() {
        return credType;
    }

    public void setCredType(CredType credType) {
        this.credType = credType;
    }
}
