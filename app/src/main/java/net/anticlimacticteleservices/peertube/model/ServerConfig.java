package net.anticlimacticteleservices.peertube.model;

public class ServerConfig {
    private ServerConfigInstance instance;
    private ServerConfigSignup signup;
    public ServerConfigInstance getInstance() {
        return instance;
    }

    public void setInstance(ServerConfigInstance instance) {
        this.instance = instance;
    }

    public ServerConfigSignup getSignup() {
        return signup;
    }

    public void setSignup(ServerConfigSignup signup) {
        this.signup = signup;
    }
}
