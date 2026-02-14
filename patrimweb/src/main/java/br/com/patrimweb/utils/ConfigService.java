package br.com.patrimweb.utils;

import java.io.InputStream;
import java.util.Properties;

public class ConfigService {

    private static Properties properties = new Properties();

    // bloco executado uma única vez (Singleton)
    static {
        try {
            InputStream input =
                ConfigService.class
                .getClassLoader()
                .getResourceAsStream("config.properties");
            properties.load(input);

        } catch (Exception e) {
            throw new RuntimeException("Erro ao carregar config.properties", e);
        }
    }

    // método encapsulado
    public static String getClientId() {
        return properties.getProperty("oauth.client_id");
    }

    public static String getClientSecret() {
        return properties.getProperty("oauth.client_secret");
    }
}
