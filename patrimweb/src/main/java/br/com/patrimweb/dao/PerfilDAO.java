package br.com.patrimweb.dao;

	import java.sql.Connection;
	import java.sql.PreparedStatement;
	import java.sql.ResultSet;
	import java.util.ArrayList;
	import java.util.List;

	import br.com.patrimweb.model.Perfil;

	/**
	 * DAO responsável pela persistência da entidade Perfil.
	 *
	 * Responsabilidades:
	 * - CRUD de perfis de acesso.
	 * - Consulta e recuperação de dados do banco.
	 */
	public class PerfilDAO {

	    private Connection conexao;

	    public PerfilDAO(Connection conexao) {
	        this.conexao = conexao;
	    }

	    // ==========================================
	    // INSERT
	    // ==========================================
	    public void adicionarPerfil(Perfil perfil) throws Exception {

	        String sql = "INSERT INTO perfil (nome) VALUES (?)";

	        PreparedStatement stmt = conexao.prepareStatement(sql);
	        stmt.setString(1, perfil.getNomePerfil());

	        stmt.executeUpdate();
	        stmt.close();
	    }

	    // ==========================================
	    // SELECT LIST
	    // ==========================================
	    public List<Perfil> listarPerfis() throws Exception {

	        List<Perfil> perfis = new ArrayList<>();

	        String sql = "SELECT * FROM perfil ORDER BY id, nome";

	        PreparedStatement stmt = conexao.prepareStatement(sql);
	        ResultSet rs = stmt.executeQuery();

	        while (rs.next()) {
	            Perfil perfil = new Perfil(
	                rs.getInt("id"),
	                rs.getString("nome")
	            );
	            
	            perfis.add(perfil);
	        }

	        rs.close();
	        stmt.close();
	       
	        return perfis;
	    }

	    // ==========================================
	    // SELECT BY ID
	    // ==========================================
	    public Perfil buscarPorId(int id) throws Exception {

	        Perfil perfil = null;

	        String sql = "SELECT * FROM perfil WHERE id = ?";

	        PreparedStatement stmt = conexao.prepareStatement(sql);
	        stmt.setInt(1, id);

	        ResultSet rs = stmt.executeQuery();

	        if (rs.next()) {
	            perfil = new Perfil(
	                rs.getInt("id"),
	                rs.getString("nome")
	            );
	        }

	        rs.close();
	        stmt.close();

	        return perfil;
	    }

	    // ==========================================
	    // UPDATE
	    // ==========================================
	    public void alterarPerfil(Perfil perfil) throws Exception {

	        String sql = "UPDATE perfil SET nome = ? WHERE id = ?";

	        PreparedStatement stmt = conexao.prepareStatement(sql);

	        stmt.setString(1, perfil.getNomePerfil());
	        stmt.setInt(2, perfil.getIdPerfil());

	        stmt.executeUpdate();
	        stmt.close();
	    }

	    // ==========================================
	    // DELETE
	    // ==========================================
	    public void excluirPerfil(int id) throws Exception {

	        String sql = "DELETE FROM perfil WHERE id = ?";

	        PreparedStatement stmt = conexao.prepareStatement(sql);
	        stmt.setInt(1, id);

	        stmt.executeUpdate();
	        stmt.close();
	    }
	}

