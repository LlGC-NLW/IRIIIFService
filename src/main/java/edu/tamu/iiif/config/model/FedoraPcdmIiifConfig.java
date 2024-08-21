package edu.tamu.iiif.config.model;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "iiif.fedora")
public class FedoraPcdmIiifConfig extends AbstractIiifConfig {

    private String user;

    private String password;

    private int version;

    public void setUser(String user) {
        this.user = user;
    }

    public String getUser() {
        return this.user;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return this.password;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getVersion() {
        return this.version;
    }

    public FedoraPcdmIiifConfig() {
        super();
        setUrl("http://localhost:9000/fcrepo/rest");
        setIdentifier("fedora");
    }

}
