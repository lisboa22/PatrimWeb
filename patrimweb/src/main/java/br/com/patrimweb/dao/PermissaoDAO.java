package br.com.patrimweb.dao;

	import java.sql.Connection;
	import java.sql.PreparedStatement;
	import java.sql.ResultSet;
	import java.util.ArrayList;
	import java.util.List;

	import br.com.patrimweb.model.Permissao;

	/**
	 * DAO responsável pela persistência da entidade Permissao.
	 *
	 * Responsabilidades:
	 * - Gerenciar permissões do sistema.
	 * - Permitir consultas por módulo e ação.
	 */
	public class PermissaoDAO {

	    private Connection conexao;

	    public PermissaoDAO(Connection conexao) {
	        this.conexao = conexao;
	    }

	    // ==========================================
	    // INSERT
	    // ==========================================
	    public void adicionarPermissao(Permissao permissao) throws Exception {

	        String sql = """
	            INSERT INTO permissao (modulo, acao, descricao)
	            VALUES (?, ?, ?)
	        """;

	        PreparedStatement stmt = conexao.prepareStatement(sql);

	        stmt.setString(1, permissao.getModulo());
	        stmt.setString(2, permissao.getAcao());
	        stmt.setString(3, permissao.getDescricao());

	        stmt.executeUpdate();
	        stmt.close();
	    }

	    // ==========================================
	    // SELECT LIST
	    // ==========================================
	    public List<Permissao> listarPermissoes() throws Exception {

	        List<Permissao> permissoes = new ArrayList<>();

	        String sql = "SELECT * FROM permissao ORDER BY id, modulo, acao";

	        PreparedStatement stmt = conexao.prepareStatement(sql);
	        ResultSet rs = stmt.executeQuery();

	        while (rs.next()) {

	            Permissao permissao = new Permissao(
	                rs.getInt("id"),
	                rs.getString("modulo"),
	                rs.getString("acao"),
	                rs.getString("descricao")
	            );

	            permissoes.add(permissao);
	        }

	        rs.close();
	        stmt.close();

	        return permissoes;
	    }

	    // ==========================================
	    // SELECT BY ID
	    // ==========================================
	    public Permissao buscarPorId(int id) throws Exception {

	        Permissao permissao = null;

	        String sql = "SELECT * FROM permissao WHERE id = ?";

	        PreparedStatement stmt = conexao.prepareStatement(sql);
	        stmt.setInt(1, id);

	        ResultSet rs = stmt.executeQuery();

	        if (rs.next()) {
	            permissao = new Permissao(
	                rs.getInt("id"),
	                rs.getString("modulo"),
	                rs.getString("acao"),
	                rs.getString("descricao")
	            );
	        }

	        rs.close();
	        stmt.close();

	        return permissao;
	    }

	    // ==========================================
	    // UPDATE
	    // ==========================================
	    public void alterarPermissao(Permissao permissao) throws Exception {

	        String sql = """
	            UPDATE permissao
	            SET modulo = ?, acao = ?, descricao = ?
	            WHERE id = ?
	        """;

	        PreparedStatement stmt = conexao.prepareStatement(sql);

	        stmt.setString(1, permissao.getModulo());
	        stmt.setString(2, permissao.getAcao());
	        stmt.setString(3, permissao.getDescricao());
	        stmt.setInt(4, permissao.getIdPermissao());

	        stmt.executeUpdate();
	        stmt.close();
	    }

	    // ==========================================
	    // DELETE
	    // ==========================================
	    public void excluirPermissao(int id) throws Exception {

	        String sql = "DELETE FROM permissao WHERE id = ?";

	        PreparedStatement stmt = conexao.prepareStatement(sql);
	        stmt.setInt(1, id);

	        stmt.executeUpdate();
	        stmt.close();
	    }
	}

