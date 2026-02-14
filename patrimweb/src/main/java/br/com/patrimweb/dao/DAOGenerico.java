package br.com.patrimweb.dao;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

/**
 * Classe utilitária responsável por centralizar operações genéricas
 * de acesso ao banco de dados.
 *
 * <p>
 * Esta classe implementa um padrão DAO genérico com as seguintes responsabilidades:
 * - Carregar configurações de conexão a partir de arquivo properties.
 * - Criar conexões JDBC dinamicamente.
 * - Executar comandos SQL parametrizados (INSERT, UPDATE, DELETE).
 * - Executar consultas SQL parametrizadas (SELECT).
 * </p>
 *
 * <p>
 * Regras e características importantes:
 * - As configurações do banco são externas ao código-fonte.
 * - Utiliza PreparedStatement para evitar SQL Injection.
 * - Parâmetros são recebidos de forma variádica (Object... params).
 * - Métodos são estáticos, permitindo uso sem instanciar a classe.
 * </p>
 *
 * <p>
 * Pontos críticos:
 * - Cada execução cria uma nova conexão com o banco.
 * - O fechamento da conexão não é tratado nesta classe.
 * - Caso o arquivo de configuração não seja encontrado, a conexão retorna null.
 * </p>
 *
 * @author robson
 */
public class DAOGenerico {

    /**
     * Caminho do arquivo de configuração contendo os dados de conexão
     * com o banco de dados.
     *
     * O arquivo deve estar disponível no classpath da aplicação.
     */
    private static final String CONFIG_FILE = "/lib/configuracao/database.properties";
    
    
    /**
     * Responsável por criar e retornar uma conexão com o banco de dados.
     *
     * Fluxo de execução:
     * 1) Carrega o arquivo database.properties.
     * 2) Lê parâmetros de conexão (URL, usuário, senha e driver).
     * 3) Carrega dinamicamente o driver JDBC.
     * 4) Estabelece conexão utilizando DriverManager.
     *
     * Interação com recursos externos:
     * - Sistema de arquivos (leitura do arquivo properties).
     * - Driver JDBC.
     * - Banco de dados.
     *
     * Validações:
     * - Verifica se o arquivo de configuração existe.
     *
     * Tratamento de erros:
     * - IOException → erro ao ler arquivo de configuração.
     * - ClassNotFoundException → driver JDBC não encontrado.
     * - SQLException → falha de autenticação ou conexão.
     *
     * @return Connection objeto de conexão ativa com o banco de dados,
     *         ou null caso o arquivo de configuração não seja encontrado.
     *
     * @throws SQLException caso ocorra erro relacionado ao banco.
     * @throws ClassNotFoundException caso o driver JDBC não seja localizado.
     */
    public static Connection getConexao() throws SQLException, ClassNotFoundException {
        
        Connection conexao = null;

        // Objeto utilizado para carregar propriedades do arquivo .properties
        Properties props = new Properties();
        
        // try-with-resources garante fechamento automático do InputStream
        try(InputStream input = DAOGenerico.class.getResourceAsStream(CONFIG_FILE)){

            // Validação crítica:
            // Caso o arquivo não seja encontrado no classpath, não é possível conectar.
            if (input == null){
                System.err.println("Erro: Arquivo de configuração" + CONFIG_FILE + "não encontrado!");
                return null;
            }
            
            // Carrega propriedades do arquivo
            props.load(input);

            // Recupera parâmetros de conexão definidos externamente
            String URL = props.getProperty("db.url");
            String USUARIO = props.getProperty("db.user");
            String SENHA = props.getProperty("db.senha");
            String DRIVER = props.getProperty("db.driver");
            
            // Carrega dinamicamente o driver JDBC especificado
            Class.forName(DRIVER);

            // Cria conexão com o banco utilizando DriverManager
            conexao = DriverManager.getConnection(URL, USUARIO, SENHA);

        } catch (IOException e){
            // Erro durante leitura do arquivo de configuração
           System.err.println("Erro ao ler o arquivos de configuração: " + e.getMessage());

        } catch (ClassNotFoundException e) {
            // Driver JDBC não encontrado no classpath
            System.err.println("Erro ao carregar o driver JDBC: " + e.getMessage());

        } catch (SQLException e){
            // Falha ao conectar ao banco de dados
            System.err.println("Erro ao conectar ao banco de dados: " + e.getMessage());

            // Regra específica baseada no SQLState:
            // Código 28000 normalmente indica erro de autenticação.
            if (e.getSQLState().equals("28000")){
              System.err.println("Usuario e senha incorretos ");  
            }
        }
        
        // Retorna conexão criada (ou null caso falha anterior)
        return conexao;
  
    }
    
    /**
     * Executa comandos SQL de modificação de dados.
     *
     * Exemplos de comandos suportados:
     * - INSERT
     * - UPDATE
     * - DELETE
     *
     * Funcionamento:
     * - Cria PreparedStatement a partir da query recebida.
     * - Associa parâmetros dinamicamente.
     * - Executa comando utilizando executeUpdate().
     *
     * Estrutura relevante:
     * - Loop responsável por mapear parâmetros variádicos para índices SQL.
     *
     * Interação com banco:
     * - Abre conexão.
     * - Executa comando SQL parametrizado.
     *
     * @param query  comando SQL parametrizado (com ?).
     * @param params lista variádica de parâmetros que serão associados à query.
     *
     * @return int quantidade de registros afetados pela operação.
     *
     * @throws SQLException erro durante execução SQL.
     * @throws ClassNotFoundException erro ao carregar driver JDBC.
     */
     public static int executarComando(String query, Object... params) throws SQLException, ClassNotFoundException {

        // Cria PreparedStatement utilizando conexão obtida dinamicamente
        PreparedStatement sql = (PreparedStatement)  getConexao().prepareStatement(query);

        // Estrutura de repetição responsável por associar cada parâmetro à query
        // Índices JDBC iniciam em 1, por isso i+1.
        for (int i = 0; i < params.length; i++) {
            sql.setObject(i+1,params[i]);
        }

        // Executa comando de atualização no banco
        int result = sql.executeUpdate();

        // Libera recurso PreparedStatement
        sql.close();

        // Retorna quantidade de linhas afetadas
        return result;
     }
     
    /**
     * Executa consultas SQL que retornam dados.
     *
     * Exemplos:
     * - SELECT simples
     * - SELECT com filtros parametrizados
     *
     * Funcionamento:
     * - Cria PreparedStatement.
     * - Associa parâmetros dinamicamente.
     * - Executa consulta retornando ResultSet.
     *
     * Ponto crítico:
     * - O fechamento do ResultSet e da conexão deve ser realizado
     *   pela camada que consome este método.
     *
     * @param query  comando SQL de consulta parametrizado.
     * @param params parâmetros utilizados na query.
     *
     * @return ResultSet contendo os registros retornados pela consulta.
     *
     * @throws SQLException erro durante execução SQL.
     * @throws ClassNotFoundException erro ao carregar driver JDBC.
     */
     public static ResultSet executarConsulta(String query, Object... params) throws SQLException, ClassNotFoundException {

        // Cria PreparedStatement com a query recebida
        PreparedStatement sql = (PreparedStatement)  getConexao().prepareStatement(query);

        // Associação dinâmica dos parâmetros à consulta SQL
        for (int i = 0; i < params.length; i++) {
            sql.setObject(i+1,params[i]);
        }

        // Executa consulta e retorna ResultSet ao chamador
        return sql.executeQuery();
    }
}
