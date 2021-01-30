package net.anticlimacticteleservices.peertube.model;

public class ServerConfigInstance {

    private String name;
    private String shortDescription;
    private String defaultClientRoute;
    private Boolean isNSFW;
    private String defaultNSFWPolicy;
    private ServerConfigInstanceCustomization customizations;
    private String Description;
    private String terms;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getDefaultClientRoute() {
        return defaultClientRoute;
    }

    public void setDefaultClientRoute(String defaultClientRoute) {
        this.defaultClientRoute = defaultClientRoute;
    }

    public Boolean getNSFW() {
        return isNSFW;
    }

    public void setNSFW(Boolean NSFW) {
        isNSFW = NSFW;
    }

    public String getDefaultNSFWPolicy() {
        return defaultNSFWPolicy;
    }

    public void setDefaultNSFWPolicy(String defaultNSFWPolicy) {
        this.defaultNSFWPolicy = defaultNSFWPolicy;
    }

    public ServerConfigInstanceCustomization getCustomizations() {
        return customizations;
    }

    public void setCustomizations(ServerConfigInstanceCustomization customizations) {
        this.customizations = customizations;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getTerms() {
        return terms;
    }

    public void setTerms(String terms) {
        this.terms = terms;
    }
}
