package fr.usmb.depocheck.Objects;

public class DependencieObject {
    private String name;
    private String version;
    private String scope;
    private String type;

    public DependencieObject(String name, String version, String scope, String type) {
        this.name = name;
        this.version = version;
        this.scope = scope;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getScope() {
        return scope;
    }

    public String getType() {
        return type;
    }
}
