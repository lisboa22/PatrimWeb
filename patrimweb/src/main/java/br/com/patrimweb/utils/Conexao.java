package br.com.patrimweb.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexao {

	    
	    public static Connection getConnection() throws SQLException {
	        
	        try {
	            Class.forName(DatabaseConfig.getDriver());
	        } catch (ClassNotFoundException e) {
	        	throw new RuntimeException("Registrar Drive manualmente", e);
	        }
	    	
	    	
	    	try {
	            return DriverManager.getConnection(
	            		DatabaseConfig.getUrl(),
	                    DatabaseConfig.getUsuario(),
	                    DatabaseConfig.getSenha());
	        } catch (SQLException e) {
	            throw new RuntimeException("Erro ao conectar ao banco de dados.", e);
	        }
	    }
	    
	    public static void main(String[] args) {
	        try {
	            Connection conexao = getConnection();
	            if (conexao != null) {
	                System.out.println("Conexão bem-sucedida!");
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }
}
