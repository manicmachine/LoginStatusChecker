package net.manicmachine.model;

public class Computer {
    private String identifier; // the value passed by the user before resolving its associated values
    private String hostName;
    private String canonicalHostName;
    private String ipAddress;
    private String currentUser;
    private String computerType;
    private boolean reachable;
    private boolean available;

    public Computer(String identifier) {
        setIdentifier(identifier);
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getCanonicalHostName() {
        return canonicalHostName;
    }

    public void setCanonicalHostName(String canonicalHostName) {
        this.canonicalHostName = canonicalHostName;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(String currentUser) {
        this.currentUser = currentUser;
    }

    public String getComputerType() {
        return computerType;
    }

    public void setComputerType(String computerType) {
        this.computerType = computerType;
    }

    public boolean isReachable() {
        return reachable;
    }

    public void setReachable(boolean reachable) {
        this.reachable = reachable;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    @Override
    public String toString() {
        return "[Computer ID: " +
                this.getIdentifier() +
                ", Hostname: " +
                this.getHostName() +
                ", Canonical Hostname: " +
                this.getCanonicalHostName() +
                ", IP Address: " +
                this.getIpAddress() +
                ", Reachable: " +
                this.isReachable() + "]";
    }
}
