package br.com.patrimweb.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexao {

	/*
	public class Conexao {
	    private static final String URL = "jdbc:mysql://localhost:3306/projetoLab1";
	    private static final String USUARIO = "eoteles";
	    private static final String SENHA = "130884";

	    public static Connection getConnection() {
	        try {
	            Class.forName("com.mysql.cj.jdbc.Driver");
	            return DriverManager.getConnection(URL, USUARIO, SENHA);
	        } catch (ClassNotFoundException | SQLException e) {
	            throw new RuntimeException("Erro na conexão com o banco de dados", e);
	        }
	    }
	}*/


	    //private static final String URL = "jdbc:mysql://localhost:3306/projetolab1";
	    private static final String URL = "jdbc:mysql://localhost:3306/patrimonio"; //"jdbc:mysql://localhost:3306/projetolab1?useSSL=false&serverTimezone=UTC";
	    private static final String USUARIO = "root";
	    private static final String SENHA = "501105";

	    /*
	    public static Connection getConnection() {
	        try {
	            Class.forName("com.mysql.cj.jdbc.Driver");
	            return DriverManager.getConnection(URL, USUARIO, SENHA);
	        } catch (ClassNotFoundException e) {
	            throw new RuntimeException("Driver JDBC do MySQL não encontrado.", e);
	        } catch (SQLException e) {
	            throw new RuntimeException("Erro ao conectar ao banco de dados.", e);
	        }
	    }
	*/
	    
	    public static Connection getConnection() throws SQLException {
	        
	        try {
	            Class.forName("com.mysql.cj.jdbc.Driver");
	        } catch (ClassNotFoundException e) {
	        	throw new RuntimeException("Registrar Drive manualmente", e);
	        }
	    	
	    	
	    	try {
	            return DriverManager.getConnection(URL, USUARIO, SENHA);
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
