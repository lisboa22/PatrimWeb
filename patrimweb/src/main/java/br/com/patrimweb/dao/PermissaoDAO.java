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
 * Tabela: permissao (id, modulo, descricao)
 * As ações (pode_visualizar, etc.) ficam em perfil_permissao.
 *
 * MÉTODOS:
 *   listarPermissoes() — original, mantido sem alteração
 *   listarOrdenado()   — NOVO: usado pelo ConfiguracaoController para a aba Permissões
 *                        ordena por modulo+descricao para agrupamento visual no JSP
 *   buscarPorId()      — original, mantido
 *   adicionarPermissao() — original, mantido
 *   alterarPermissao()   — original, mantido
 *   excluirPermissao()   — original, mantido
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
            INSERT INTO permissao (modulo, descricao)
            VALUES (?, ?)
        """;

        PreparedStatement stmt = conexao.prepareStatement(sql);
        stmt.setString(1, permissao.getModulo());
        stmt.setString(2, permissao.getDescricao());
        stmt.executeUpdate();
        stmt.close();
    }

    // ==========================================
    // SELECT LIST — original (mantido)
    // ==========================================
    public List<Permissao> listarPermissoes() throws Exception {

        List<Permissao> permissoes = new ArrayList<>();

        String sql = "SELECT * FROM permissao ORDER BY id, modulo";

        PreparedStatement stmt = conexao.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            Permissao permissao = new Permissao(
                rs.getInt("id"),
                rs.getString("modulo"),
                rs.getString("descricao")
            );
            permissoes.add(permissao);
        }

        rs.close();
        stmt.close();

        return permissoes;
    }

    // ==========================================
    // SELECT LIST ORDENADO — usado pelo ConfiguracaoController
    // ==========================================
    /**
     * Lista todas as permissões ordenadas por módulo e depois por descrição.
     *
     * A ordenação por módulo é OBRIGATÓRIA para que o JSP configuracoes.jsp
     * consiga agrupar os checkboxes por módulo corretamente usando a variável
     * de controle "modAtual":
     *
     *   <c:forEach var="perm" items="${permissoes}">
     *       <c:if test="${perm.modulo != modAtual}">
     *           <!-- abre novo grupo visual do módulo -->
     *       </c:if>
     *   </c:forEach>
     *
     * O campo ${perm.acao} no JSP é satisfeito pelo método getAcao() do model,
     * que retorna descricao — sem necessidade de coluna extra no banco.
     *
     * @return Lista de Permissao ordenada por modulo ASC, descricao ASC
     */
    public List<Permissao> listarOrdenado() throws Exception {

        List<Permissao> permissoes = new ArrayList<>();

        String sql = "SELECT id, modulo, descricao " +
                     "FROM permissao " +
                     "ORDER BY modulo ASC, descricao ASC";

        PreparedStatement stmt = conexao.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            Permissao p = new Permissao(
                rs.getInt("id"),
                rs.getString("modulo"),
                rs.getString("descricao")
            );
            permissoes.add(p);
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
            SET modulo = ?, descricao = ?
            WHERE id = ?
        """;

        PreparedStatement stmt = conexao.prepareStatement(sql);
        stmt.setString(1, permissao.getModulo());
        stmt.setString(2, permissao.getDescricao());
        stmt.setInt(3, permissao.getIdPermissao());
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
