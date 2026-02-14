package br.com.patrimweb.dao;

//import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/*import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;*/

import br.com.patrimweb.model.Usuario;
import br.com.patrimweb.utils.Conexao;

/**
 * DAO responsável pelas operações de persistência da entidade Usuario.
 *
 * Esta classe centraliza toda a comunicação com o banco de dados referente
 * à tabela "usuario", implementando operações CRUD, autenticação,
 * filtros dinâmicos e consultas estatísticas utilizadas em relatórios.
 *
 * Responsabilidades:
 * - Inserir, atualizar, remover e consultar usuários
 * - Realizar autenticação baseada em login/senha
 * - Executar filtros dinâmicos para relatórios
 * - Fornecer dados agregados para gráficos e análises
 *
 * Observações importantes:
 * - Utiliza PreparedStatement para segurança contra SQL Injection.
 * - A conexão é fornecida externamente e não é gerenciada pela classe,
 *   exceto em métodos específicos que solicitam nova conexão.
 */
public class UsuarioDAO {

	private Connection conexao; //Declaração de um atributo conexao do tipo Connection, que será usado para interagir com o banco de dados.

	 /**
	  * Construtor responsável por receber uma conexão ativa com o banco.
	  *
	  * @param conexao conexão JDBC previamente criada
	  */
	 public UsuarioDAO(Connection conexao) {
	     this.conexao = conexao;
	 }

	 /**
	  * Insere um novo usuário na base de dados.
	  *
	  * Regras de negócio:
	  * - Todos os campos necessários devem estar preenchidos no objeto Usuario.
	  * - A data de inserção é definida pela camada superior da aplicação.
	  *
	  * Interação com banco:
	  * - Executa comando INSERT na tabela usuario.
	  *
	  * @param usuario objeto contendo os dados do usuário
	  * @throws Exception em caso de erro durante execução SQL
	  */
	 public void adicionarUsuario(Usuario usuario) throws Exception {
		 String sql = "INSERT INTO usuario (nome_usu, cpf_usu, telefone_usu, email_usu, endereco_usu, data_insercao, login_google, senha_usu) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		 PreparedStatement stmt = conexao.prepareStatement(sql);

		 // Associação dos parâmetros SQL aos atributos da entidade
		 stmt.setString(1, usuario.getNomeUsu());
	     stmt.setString(2, usuario.getCpfUsu());
	     stmt.setString(3, usuario.getTelefoneUsu());
	     stmt.setString(4, usuario.getEmailUsu());
	     stmt.setString(5, usuario.getEnderecoUsu());
	     stmt.setTimestamp(6, usuario.getDataInsercao());
	     stmt.setBoolean(7, usuario.getLoginGoogle());
	     stmt.setString(8, usuario.getSenhaUsu());

	     // Executa inserção no banco
	     stmt.executeUpdate();
	     stmt.close();
	 }

	 /**
	  * Recupera todos os usuários cadastrados.
	  *
	  * Estrutura relevante:
	  * - Loop percorre o ResultSet convertendo cada registro em objeto Usuario.
	  *
	  * @return lista contendo todos os usuários
	  * @throws Exception em caso de erro de acesso ao banco
	  */
	 public List<Usuario> listarUsuarios() throws Exception {
	     List<Usuario> usuarios = new ArrayList<>();
	     String sql = "SELECT * FROM usuario";
	     PreparedStatement stmt = conexao.prepareStatement(sql);
	     ResultSet rs = stmt.executeQuery();

	     // Percorre todos os registros retornados
	     while (rs.next()) {

	    	 // Mapeamento linha do banco → objeto de domínio
	    	 Usuario usuario = new Usuario(
	    		 rs.getInt("id_usu"),
	    		 rs.getString("nome_usu"),
	    		 rs.getString("cpf_usu"),
	    		 rs.getString("telefone_usu"),
	    		 rs.getString("email_usu"),
	    		 rs.getString("endereco_usu"),
	    		 rs.getTimestamp("data_insercao"),
	    		 rs.getBoolean("login_google"),
	    		 rs.getString("senha_usu")
	    	 );

	    	 usuarios.add(usuario);
	     }

	     rs.close();
	     stmt.close();
	     return usuarios;
	 }

	 /**
	  * Atualiza dados cadastrais de um usuário existente.
	  *
	  * Regra de negócio:
	  * - A atualização ocorre com base no id_usu.
	  * - Campos sensíveis como senha não são alterados neste método.
	  *
	  * @param usuario objeto contendo novos dados
	  * @throws Exception em caso de erro SQL
	  */
	 public void alterarUsuarios(Usuario usuario) throws Exception {
		 String sql = "UPDATE usuario SET nome_usu = ?, cpf_usu = ?, telefone_usu = ?, email_usu = ?, endereco_usu = ? WHERE id_usu = ?";
		 PreparedStatement stmt = conexao.prepareStatement(sql);

		 stmt.setString(1, usuario.getNomeUsu());
	     stmt.setString(2, usuario.getCpfUsu());
	     stmt.setString(3, usuario.getTelefoneUsu());
	     stmt.setString(4, usuario.getEmailUsu());
	     stmt.setString(5, usuario.getEnderecoUsu());
	     stmt.setInt(6, usuario.getIdUsu());

	     stmt.executeUpdate();
	     stmt.close();
	 }

	 /**
	  * Remove permanentemente um usuário do banco de dados.
	  *
	  * @param id identificador do usuário
	  * @throws Exception em caso de erro SQL
	  */
	 public void excluirUsuario(int id) throws Exception {
		 String sql = "DELETE FROM usuario WHERE id_usu = ?";
	     PreparedStatement stmt = conexao.prepareStatement(sql);

	     stmt.setInt(1, id);

	     stmt.executeUpdate();
	     stmt.close();
	 }
	 
	/**
	 * Busca um usuário específico pelo ID.
	 *
	 * Estrutura de decisão:
	 * - O objeto só é criado se houver resultado no banco.
	 *
	 * @param id identificador do usuário
	 * @return Usuario encontrado ou null caso não exista
	 * @throws Exception em caso de erro SQL
	 */
	 public Usuario buscarPorId(int id) throws Exception {

	     Usuario usuario = null;

	     String sql = "SELECT * FROM usuario WHERE id_usu = ?";
	     PreparedStatement stmt = conexao.prepareStatement(sql);

	     stmt.setInt(1, id);
	     ResultSet rs = stmt.executeQuery();

	     // Verifica existência de registro retornado
	     if (rs.next()) {

	         usuario = new Usuario(
	             rs.getInt("id_usu"),
	             rs.getString("nome_usu"),
	             rs.getString("cpf_usu"),
	             rs.getString("telefone_usu"),
	             rs.getString("email_usu"),
	             rs.getString("endereco_usu"),
	             rs.getTimestamp("data_insercao"),
	             rs.getBoolean("login_google"),
	             rs.getString("senha_usu")
	         );
	     }

	     return usuario;
	 }
	 
	 /**
	  * Busca usuário pelo e-mail.
	  *
	  * Regra de negócio:
	  * - Utilizado principalmente para validação de login social ou verificação
	  *   de existência de conta.
	  *
	  * @param email e-mail do usuário
	  * @return Usuario encontrado ou null
	  */
	 public Usuario buscarPorEmail(String email) {

		    String sql = "SELECT * FROM usuario WHERE email_usu = ?";
		    Usuario usuario = null;

		    try (PreparedStatement ps = conexao.prepareStatement(sql)) {

		        ps.setString(1, email);
		        ResultSet rs = ps.executeQuery();

		        if (rs.next()) {
		            usuario = new Usuario();

		            // Mapeamento parcial dos dados necessários
		            usuario.setIdUsu(rs.getInt("id_usu"));
		            usuario.setNomeUsu(rs.getString("nome_usu"));
		            usuario.setEmailUsu(rs.getString("email_usu"));
		            usuario.setLoginGoogle(rs.getBoolean("login_google"));
		        }

		    } catch (SQLException e) {
		        throw new RuntimeException("Erro ao buscar usuário por e-mail", e);
		    }

		    return usuario;
		}
	 
	 /**
	  * Realiza autenticação do usuário.
	  *
	  * Regra de negócio:
	  * - Permite login utilizando email OU nome de usuário.
	  * - A validação ocorre comparando diretamente a senha armazenada.
	  *
	  * Ponto crítico:
	  * - A autenticação depende da correspondência exata da senha.
	  *
	  * @param login email ou nome do usuário
	  * @param senha senha informada
	  * @return Usuario autenticado ou null caso inválido
	  */
	 public Usuario autenticar(String login, String senha) {

		    String sql = "SELECT * FROM usuario " +
		                 "WHERE (email_usu = ? OR nome_usu = ?) " +
		                 "AND senha_usu = ?";

		    try (PreparedStatement ps = conexao.prepareStatement(sql)) {

		        ps.setString(1, login);
		        ps.setString(2, login);
		        ps.setString(3, senha);

		        ResultSet rs = ps.executeQuery();

		        if (rs.next()) {
		            Usuario usuario = new Usuario();

		            usuario.setIdUsu(rs.getInt("id_usu"));
		            usuario.setNomeUsu(rs.getString("nome_usu"));
		            usuario.setEmailUsu(rs.getString("email_usu"));
		            usuario.setLoginGoogle(rs.getBoolean("login_google"));
		            return usuario;
		        }

		    } catch (SQLException e) {
		        throw new RuntimeException("Erro ao autenticar usuário", e);
		    }

		    return null;
		}

	 /**
	  * Filtra usuários dinamicamente conforme critérios opcionais.
	  *
	  * Regras de negócio:
	  * - Apenas filtros informados são adicionados à query.
	  * - Uso de WHERE 1=1 simplifica concatenação dinâmica.
	  *
	  * @param dataInicio data mínima de cadastro
	  * @param dataFim data máxima de cadastro
	  * @param nome filtro parcial por nome
	  * @param cpf filtro parcial por CPF
	  * @return lista filtrada de usuários
	  * @throws Exception em caso de erro SQL
	  */
	 public List<Usuario> filtrarUsuarios(
		        Timestamp dataInicio,
		        Timestamp dataFim,
		        String nome,
		        String cpf
		) throws Exception {

		    List<Usuario> lista = new ArrayList<>();

		    StringBuilder sql = new StringBuilder();
		    sql.append("SELECT * FROM usuario WHERE 1=1 ");

		    if (dataInicio != null) {
		        sql.append(" AND data_insercao >= ? ");
		    }

		    if (dataFim != null) {
		        sql.append(" AND data_insercao <= ? ");
		    }

		    if (nome != null && !nome.trim().isEmpty()) {
		        sql.append(" AND nome_usu LIKE ? ");
		    }

		    if (cpf != null && !cpf.trim().isEmpty()) {
		        sql.append(" AND cpf_usu LIKE ? ");
		    }

		    PreparedStatement stmt = conexao.prepareStatement(sql.toString());
		    int index = 1;

		    // Associação dinâmica dos parâmetros conforme filtros aplicados
		    if (dataInicio != null) {
		        stmt.setTimestamp(index++, dataInicio);
		    }

		    if (dataFim != null) {
		        stmt.setTimestamp(index++, dataFim);
		    }

		    if (nome != null && !nome.trim().isEmpty()) {
		        stmt.setString(index++, "%" + nome.trim() + "%");
		    }

		    if (cpf != null && !cpf.trim().isEmpty()) {
		        stmt.setString(index++, "%" + cpf.trim() + "%");
		    }

		    ResultSet rs = stmt.executeQuery();

		    while (rs.next()) {
		        Usuario usuario = new Usuario(
		            rs.getInt("id_usu"),
		            rs.getString("nome_usu"),
		            rs.getString("cpf_usu"),
		            rs.getString("telefone_usu"),
		            rs.getString("email_usu"),
		            rs.getString("endereco_usu"),
		            rs.getTimestamp("data_insercao"),
		            rs.getBoolean("login_google"),
		            rs.getString("senha_usu")
		        );
		        lista.add(usuario);
		    }

		    rs.close();
		    stmt.close();

		    return lista;
		}
	 
	 /**
	  * Retorna anos distintos de cadastro de usuários.
	  *
	  * @return lista de anos disponíveis para relatórios
	  * @throws Exception em caso de erro SQL
	  */
	 public List<Integer> listarAnosCadastro() throws Exception {

		    List<Integer> anos = new ArrayList<>();

		    String sql = """
		        SELECT DISTINCT YEAR(data_insercao) AS ano
		        FROM usuario
		        WHERE data_insercao IS NOT NULL
		        AND YEAR(data_insercao) > 0
		        ORDER BY ano DESC
		    """;

		    PreparedStatement stmt = conexao.prepareStatement(sql);
		    ResultSet rs = stmt.executeQuery();

		    while (rs.next()) {
		        anos.add(rs.getInt("ano"));
		    }

		    rs.close();
		    stmt.close();

		    return anos;
		}

	 /**
	  * Retorna quantidade de usuários cadastrados por mês em um ano específico.
	  *
	  * @param ano ano utilizado como filtro estatístico
	  * @return mapa contendo mês → total de usuários
	  * @throws Exception em caso de erro SQL
	  */
	 public Map<Integer, Integer> quantidadeUsuariosPorMes(int ano) throws Exception {

		    Map<Integer, Integer> dados = new LinkedHashMap<>();

		    String sql = """
		        SELECT 
		            MONTH(data_insercao) AS mes,
		            COUNT(*) AS total
		        FROM usuario
		        WHERE data_insercao IS NOT NULL
		          AND YEAR(data_insercao) = ?
		        GROUP BY MONTH(data_insercao)
		        ORDER BY mes
		    """;

		    PreparedStatement stmt = conexao.prepareStatement(sql);
		    stmt.setInt(1, ano);

		    ResultSet rs = stmt.executeQuery();

		    while (rs.next()) {
		        dados.put(
		            rs.getInt("mes"),
		            rs.getInt("total")
		        );
		    }

		    rs.close();
		    stmt.close();

		    return dados;
		}

	 /**
	  * Conta o total de usuários cadastrados em determinado ano.
	  *
	  * Interação com banco:
	  * - Abre nova conexão utilizando classe utilitária Conexao.
	  * - Executa consulta agregada COUNT(*).
	  *
	  * @param ano ano desejado
	  * @return total de usuários cadastrados
	  * @throws SQLException em caso de erro SQL
	  */
	 public int contarUsuariosPorAno(int ano) throws SQLException {
	        int total = 0;
	        
	        Connection conn = Conexao.getConnection();
	        
	        UsuarioDAO usuarioDAO = new UsuarioDAO(conn);

	        String sql = """
	            SELECT COUNT(*) 
	            FROM usuarios 
	            WHERE YEAR(data_cadastro) = ?
	        """;
	        
	        try (PreparedStatement ps = conn.prepareStatement(sql)) {
	            ps.setInt(1, ano);

	            ResultSet rs = ps.executeQuery();

	            // Estrutura de decisão que valida retorno da consulta
	            if (rs.next()) {
	                total = rs.getInt(1);
	            }

	        } catch (SQLException e) {
	            e.printStackTrace();
	        }

	        System.out.println("Total: " + total);
	        return total;
	    }
}
