package br.com.patrimweb.utils;

import java.io.InputStream;
import java.util.Properties;

public class DatabaseConfig {

    private static final Properties properties = new Properties();

    static {
        try {

            InputStream input =
                DatabaseConfig.class
                .getClassLoader()
                .getResourceAsStream("database.properties");

            if (input == null) {
                throw new RuntimeException(
                    "Arquivo database.properties não encontrado no classpath."
                );
            }

            properties.load(input);

        } catch (Exception e) {
            throw new RuntimeException("Erro ao carregar configurações do banco.", e);
        }
    }

    public static String getUrl() {
        return properties.getProperty("db.url");
    }

    public static String getUsuario() {
        return properties.getProperty("db.user");
    }

    public static String getSenha() {
        return properties.getProperty("db.senha");
    }

    public static String getDriver() {
        return properties.getProperty("db.driver");
    }
}
