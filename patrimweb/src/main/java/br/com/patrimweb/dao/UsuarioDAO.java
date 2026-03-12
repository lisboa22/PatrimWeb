package br.com.patrimweb.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import br.com.patrimweb.model.Fabricante;
import br.com.patrimweb.model.Perfil;
import br.com.patrimweb.model.Usuario;
import br.com.patrimweb.utils.Conexao;
import br.com.patrimweb.utils.SenhaUtils; // ✅ Import da classe utilitária de criptografia

/**
 * DAO responsável pelas operações de persistência da entidade Usuario.
 *
 * Esta classe centraliza toda a comunicação com o banco de dados referente
 * à tabela "usuario", implementando operações CRUD, autenticação,
 * filtros dinâmicos e consultas estatísticas utilizadas em relatórios.
 *
 * Responsabilidades:
 * - Inserir, atualizar, remover e consultar usuários
 * - Realizar autenticação baseada em login/senha com verificação BCrypt
 * - Executar filtros dinâmicos para relatórios
 * - Fornecer dados agregados para gráficos e análises
 *
 * Observações importantes:
 * - Utiliza PreparedStatement para segurança contra SQL Injection.
 * - Senhas são armazenadas como hash BCrypt — nunca em texto puro.
 * - A conexão é fornecida externamente e não é gerenciada pela classe,
 *   exceto em métodos específicos que solicitam nova conexão.
 */
public class UsuarioDAO {

	private Connection conexao;

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
	  * - A senha deve ser criptografada ANTES de chamar este método.
	  *   A criptografia é responsabilidade da camada Controller (UsuarioController).
	  * - A data de inserção é definida pela camada superior da aplicação.
	  *
	  * Interação com banco:
	  * - Executa comando INSERT na tabela usuario.
	  *
	  * @param usuario objeto contendo os dados do usuário (senha já criptografada)
	  * @throws Exception em caso de erro durante execução SQL
	  */
	 public void adicionarUsuario(Usuario usuario) throws Exception {
		 String sql = "INSERT INTO usuario (nome_usu, cpf_usu, telefone_usu, email_usu, endereco_usu, data_insercao, login_google, senha_usu, id_perfil) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
		 PreparedStatement stmt = conexao.prepareStatement(sql);

		 stmt.setString(1, usuario.getNomeUsu());
	     stmt.setString(2, usuario.getCpfUsu());
	     stmt.setString(3, usuario.getTelefoneUsu());
	     stmt.setString(4, usuario.getEmailUsu());
	     stmt.setString(5, usuario.getEnderecoUsu());
	     stmt.setTimestamp(6, usuario.getDataInsercao());
	     stmt.setBoolean(7, usuario.getLoginGoogle());
	     stmt.setString(8, usuario.getSenhaUsu()); // Senha já chega criptografada do Controller
	     stmt.setInt(9, usuario.getPerfilUsu().getIdPerfil()); // ✅ FK do perfil selecionado

	     stmt.executeUpdate();
	     stmt.close();
	 }

	 /**
	  * Recupera todos os usuários cadastrados.
	  *
	  * @return lista contendo todos os usuários
	  * @throws Exception em caso de erro de acesso ao banco
	  */
	 public List<Usuario> listarUsuarios() throws Exception {
	     List<Usuario> usuarios = new ArrayList<>();
	     String sql = "SELECT u.*, "
	                + " p.id              AS id_perfil, "
	                + " p.nome            AS nome_perfil "
	                + "FROM usuario u "
	                + "JOIN perfil p      ON p.id = u.id_perfil "
	                + "ORDER BY u.id_usu";
	     
	     //String sql = "SELECT * FROM usuario";
	     PreparedStatement stmt = conexao.prepareStatement(sql);
	     ResultSet rs = stmt.executeQuery();

	     while (rs.next()) {
	    	 
	    	 Perfil perfil = new Perfil();
		     
	    	 perfil.setIdPerfil(rs.getInt("id_perfil"));
	    	 
	    	 perfil.setNomePerfil(rs.getString("nome_perfil"));
	    	 
	    	 Usuario usuario = new Usuario(
	    		 rs.getInt("id_usu"),
	    		 rs.getString("nome_usu"),
	    		 rs.getString("cpf_usu"),
	    		 rs.getString("telefone_usu"),
	    		 rs.getString("email_usu"),
	    		 rs.getString("endereco_usu"),
	    		 rs.getTimestamp("data_insercao"),
	    		 rs.getBoolean("login_google"),
	    		 rs.getString("senha_usu"),
	    		 perfil
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
		 // ✅ senha_usu e id_perfil incluídos no UPDATE
		 String sql = "UPDATE usuario SET nome_usu = ?, cpf_usu = ?, telefone_usu = ?, email_usu = ?, endereco_usu = ?, senha_usu = ?, id_perfil = ? WHERE id_usu = ?";
		 PreparedStatement stmt = conexao.prepareStatement(sql);

		 stmt.setString(1, usuario.getNomeUsu());
	     stmt.setString(2, usuario.getCpfUsu());
	     stmt.setString(3, usuario.getTelefoneUsu());
	     stmt.setString(4, usuario.getEmailUsu());
	     stmt.setString(5, usuario.getEnderecoUsu());
	     stmt.setString(6, usuario.getSenhaUsu()); // ✅ hash BCrypt vindo do Controller
	     stmt.setInt(7, usuario.getPerfilUsu().getIdPerfil()); // ✅ FK do perfil selecionado
	     stmt.setInt(8, usuario.getIdUsu());

	     stmt.executeUpdate();
	     stmt.close();
	 }

	 /**
	  * Atualiza apenas os dados pessoais do usuário logado (perfil próprio).
	  *
	  * Campos atualizados: nome, e-mail, telefone, CPF e endereço.
	  * Campos PRESERVADOS (não alterados por este método): senha_usu, id_perfil.
	  * Isso garante que o usuário não consiga alterar seu próprio cargo/perfil
	  * nem sobrescrever a senha por esta rota.
	  *
	  * @param usuario objeto com os novos dados (id_usu obrigatório)
	  * @throws Exception em caso de erro SQL
	  */
	 public void atualizarDadosPerfil(Usuario usuario) throws Exception {
		 String sql = "UPDATE usuario SET nome_usu = ?, email_usu = ?, telefone_usu = ?, cpf_usu = ?, endereco_usu = ? WHERE id_usu = ?";
		 PreparedStatement stmt = conexao.prepareStatement(sql);

		 stmt.setString(1, usuario.getNomeUsu());
		 stmt.setString(2, usuario.getEmailUsu());
		 stmt.setString(3, usuario.getTelefoneUsu());
		 stmt.setString(4, usuario.getCpfUsu());
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
	 * @param id identificador do usuário
	 * @return Usuario encontrado ou null caso não exista
	 * @throws Exception em caso de erro SQL
	 */
	 public Usuario buscarPorId(int id) throws Exception {
	     Usuario usuario = null;
	     String sql = "SELECT u.*, p.id AS id_perfil, p.nome AS nome_perfil "
	                + "FROM usuario u "
	                + "JOIN perfil p ON p.id = u.id_perfil "
	                + "WHERE u.id_usu = ?";
	     PreparedStatement stmt = conexao.prepareStatement(sql);
	     stmt.setInt(1, id);
	     ResultSet rs = stmt.executeQuery();

	     if (rs.next()) {
	    	 Perfil perfil = new Perfil();
	    	 perfil.setIdPerfil(rs.getInt("id_perfil"));
	    	 perfil.setNomePerfil(rs.getString("nome_perfil"));

	         usuario = new Usuario(
	             rs.getInt("id_usu"),
	             rs.getString("nome_usu"),
	             rs.getString("cpf_usu"),
	             rs.getString("telefone_usu"),
	             rs.getString("email_usu"),
	             rs.getString("endereco_usu"),
	             rs.getTimestamp("data_insercao"),
	             rs.getBoolean("login_google"),
	             rs.getString("senha_usu"),
	             perfil
	         );
	     }

	     rs.close();
	     stmt.close();
	     return usuario;
	 }

	 /**
	  * Busca usuário pelo e-mail.
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
	  * Realiza autenticação do usuário com verificação BCrypt.
	  *
	  * Fluxo de autenticação:
	  * 1. Busca o usuário no banco pelo login (email OU nome).
	  *    IMPORTANTE: A senha NÃO é mais comparada diretamente no SQL,
	  *    pois o banco armazena um hash BCrypt, não a senha em texto puro.
	  * 2. Se o usuário for encontrado, utiliza SenhaUtils.verificar()
	  *    para comparar a senha digitada com o hash armazenado no banco.
	  * 3. Retorna o objeto Usuario apenas se a verificação for bem-sucedida.
	  *
	  * Por que a senha saiu do SQL?
	  * - BCrypt gera um hash diferente a cada vez, mesmo para a mesma senha.
	  * - Por isso, não é possível comparar hashes diretamente no banco.
	  * - A verificação deve ser feita em Java, via BCrypt.checkpw().
	  *
	  * @param login email ou nome do usuário
	  * @param senhaDigitada senha informada pelo usuário no formulário de login
	  * @return Usuario autenticado ou null caso as credenciais sejam inválidas
	  */
	 public Usuario autenticar(String login, String senhaDigitada) {

		    // ✅ Busca apenas pelo login — a senha será verificada em Java via BCrypt
		    String sql = "SELECT * FROM usuario " +
		                 "WHERE (email_usu = ? OR nome_usu = ?)";

		    try (PreparedStatement ps = conexao.prepareStatement(sql)) {

		        ps.setString(1, login);
		        ps.setString(2, login);

		        ResultSet rs = ps.executeQuery();

		        if (rs.next()) {
		            String hashArmazenado = rs.getString("senha_usu");

		            // ✅ Verificação BCrypt: compara senha digitada com o hash do banco
		            if (SenhaUtils.verificar(senhaDigitada, hashArmazenado)) {

		                Usuario usuario = new Usuario();
		                usuario.setIdUsu(rs.getInt("id_usu"));
		                usuario.setNomeUsu(rs.getString("nome_usu"));
		                usuario.setEmailUsu(rs.getString("email_usu"));
		                usuario.setLoginGoogle(rs.getBoolean("login_google"));
		                return usuario;
		            }
		        }

		    } catch (SQLException e) {
		        throw new RuntimeException("Erro ao autenticar usuário", e);
		    }

		    // ❌ Credenciais inválidas — usuário não encontrado ou senha incorreta
		    return null;
		}

	 /**
	  * Filtra usuários dinamicamente conforme critérios opcionais.
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

		    // ✅ JOIN com perfil obrigatório — id_perfil e nome_perfil precisam
		    //    constar no ResultSet para popular o objeto Perfil dentro de Usuario
		    StringBuilder sql = new StringBuilder();
		    sql.append("SELECT u.*, p.id AS id_perfil, p.nome AS nome_perfil ");
		    sql.append("FROM usuario u ");
		    sql.append("JOIN perfil p ON p.id = u.id_perfil ");
		    sql.append("WHERE 1=1 ");

		    if (dataInicio != null) sql.append(" AND u.data_insercao >= ? ");
		    if (dataFim != null)    sql.append(" AND u.data_insercao <= ? ");
		    if (nome != null && !nome.trim().isEmpty()) sql.append(" AND u.nome_usu LIKE ? ");
		    if (cpf  != null && !cpf.trim().isEmpty())  sql.append(" AND u.cpf_usu LIKE ? ");

		    PreparedStatement stmt = conexao.prepareStatement(sql.toString());
		    int index = 1;

		    if (dataInicio != null) stmt.setTimestamp(index++, dataInicio);
		    if (dataFim != null)    stmt.setTimestamp(index++, dataFim);
		    if (nome != null && !nome.trim().isEmpty()) stmt.setString(index++, "%" + nome.trim() + "%");
		    if (cpf  != null && !cpf.trim().isEmpty())  stmt.setString(index++, "%" + cpf.trim() + "%");

		    ResultSet rs = stmt.executeQuery();

		    while (rs.next()) {

		        Perfil perfil = new Perfil();
		        perfil.setIdPerfil(rs.getInt("id_perfil"));
		        perfil.setNomePerfil(rs.getString("nome_perfil"));

		        Usuario usuario = new Usuario(
		            rs.getInt("id_usu"),
		            rs.getString("nome_usu"),
		            rs.getString("cpf_usu"),
		            rs.getString("telefone_usu"),
		            rs.getString("email_usu"),
		            rs.getString("endereco_usu"),
		            rs.getTimestamp("data_insercao"),
		            rs.getBoolean("login_google"),
		            rs.getString("senha_usu"),
		            perfil
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
		    while (rs.next()) anos.add(rs.getInt("ano"));
		    rs.close();
		    stmt.close();
		    return anos;
		}

	 /**
	  * Retorna quantidade de usuários cadastrados por mês em um ano específico.
	  * Aceita Integer para evitar NullPointerException quando anoSelecionado for null
	  * (situação que ocorre quando não há nenhum cadastro ainda no banco).
	  *
	  * @param ano ano utilizado como filtro estatístico (pode ser null)
	  * @return mapa contendo mês → total de usuários, vazio se ano for null
	  * @throws Exception em caso de erro SQL
	  */
	 public Map<Integer, Integer> quantidadeUsuariosPorMes(Integer ano) throws Exception {
		    Map<Integer, Integer> dados = new LinkedHashMap<>();

		    // ✅ Proteção contra null: retorna mapa vazio sem consultar o banco
		    if (ano == null) return dados;

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
		    while (rs.next()) dados.put(rs.getInt("mes"), rs.getInt("total"));
		    rs.close();
		    stmt.close();
		    return dados;
		}

	 /**
	  * Conta o total de usuários cadastrados em determinado ano.
	  *
	  * @param ano ano desejado
	  * @return total de usuários cadastrados
	  * @throws SQLException em caso de erro SQL
	  */
	 public int contarUsuariosPorAno(int ano) throws SQLException {
	        int total = 0;
	        Connection conn = Conexao.getConnection();
	        String sql = """
	            SELECT COUNT(*) 
	            FROM usuarios 
	            WHERE YEAR(data_cadastro) = ?
	        """;
	        try (PreparedStatement ps = conn.prepareStatement(sql)) {
	            ps.setInt(1, ano);
	            ResultSet rs = ps.executeQuery();
	            if (rs.next()) total = rs.getInt(1);
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	        System.out.println("Total: " + total);
	        return total;
	    }

	 /**
	  * Retorna o hash BCrypt da senha armazenada para o usuário informado.
	  *
	  * Usado pelo processarAlterarSenha do controller para verificar se a
	  * "senha atual" digitada corresponde ao hash gravado no banco antes de
	  * permitir a troca.
	  *
	  * @param idUsu identificador do usuário
	  * @return hash BCrypt da senha ou null se o usuário não for encontrado
	  * @throws Exception em caso de erro SQL
	  */
	 public String buscarHashSenha(int idUsu) throws Exception {
	     String sql = "SELECT senha_usu FROM usuario WHERE id_usu = ?";
	     try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
	         stmt.setInt(1, idUsu);
	         ResultSet rs = stmt.executeQuery();
	         if (rs.next()) {
	             return rs.getString("senha_usu");
	         }
	     }
	     return null;
	 }

	 /**
	  * Persiste a nova senha do usuário, já criptografada em BCrypt.
	  *
	  * A criptografia (SenhaUtils.criptografar) deve ser aplicada pelo
	  * Controller ANTES de chamar este método — o DAO nunca recebe nem
	  * armazena senhas em texto puro.
	  *
	  * @param idUsu    identificador do usuário
	  * @param novoHash novo hash BCrypt da senha
	  * @throws Exception em caso de erro SQL
	  */
	 public void alterarSenha(int idUsu, String novoHash) throws Exception {
	     String sql = "UPDATE usuario SET senha_usu = ? WHERE id_usu = ?";
	     try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
	         stmt.setString(1, novoHash);
	         stmt.setInt(2, idUsu);
	         stmt.executeUpdate();
	     }
	 }
}
