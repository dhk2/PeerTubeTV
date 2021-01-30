package net.anticlimacticteleservices.peertube.model;

public class ServerConfigSignup {
    private boolean allowed;
    private boolean allowedForCurrentIP;
    private boolean requiresEmailVerification;

    public boolean isAllowed() {
        return allowed;
    }

    public void setAllowed(boolean allowed) {
        this.allowed = allowed;
    }

    public boolean isAllowedForCurrentIP() {
        return allowedForCurrentIP;
    }

    public void setAllowedForCurrentIP(boolean allowedForCurrentIP) {
        this.allowedForCurrentIP = allowedForCurrentIP;
    }

    public boolean isRequiresEmailVerification() {
        return requiresEmailVerification;
    }

    public void setRequiresEmailVerification(boolean requiresEmailVerification) {
        this.requiresEmailVerification = requiresEmailVerification;
    }
}
