package app.util;

import java.io.IOException;
import java.util.Properties;

public class Config {
    private static Config instance;
    public static Config getInstance() {
        return (instance == null) ? new Config() : instance;
    }

    private Properties props;

    private Config() {
        props = new Properties();
        try {
            props.load(getClass().getResourceAsStream("/app.properties"));
        } catch (IOException e) {
            System.out.println("Couldn't read from properties from file.");
        }
    }

    public int getInt(String property) {
        return Integer.parseInt(getString(property));
    }

    public String getString(String property) {
        return (String) props.get(property);
    }
}
