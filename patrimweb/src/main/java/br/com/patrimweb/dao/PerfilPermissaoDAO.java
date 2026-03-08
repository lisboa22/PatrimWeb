package br.com.patrimweb.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.patrimweb.utils.Conexao;

/**
 * =====================================================================================
 * DAO: PerfilPermissaoDAO
 * SISTEMA: PatrimWeb
 * CAMADA: DAO (Data Access Object)
 *
 * PROPÓSITO:
 * - Gerenciar o relacionamento N:N entre Perfil e Permissao.
 * - Fornecer o mapa idPerfil -> [idPermissao, ...] consumido pela view.
 * - Salvar (substituir) as permissões de um perfil via AJAX.
 *
 * TABELA ESPERADA NO BANCO:
 *   CREATE TABLE perfil_permissao (
 *       id_perfil    INT NOT NULL,
 *       id_permissao INT NOT NULL,
 *       PRIMARY KEY (id_perfil, id_permissao),
 *       FOREIGN KEY (id_perfil)    REFERENCES perfil(id),
 *       FOREIGN KEY (id_permissao) REFERENCES permissao(id)
 *   );
 * =====================================================================================
 */
public class PerfilPermissaoDAO {

    // ─────────────────────────────────────────────────────────────────────────
    // Consultar: mapa completo idPerfil -> lista de idPermissao
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Retorna um mapa com todas as associações perfil → permissões.
     *
     * Utilizado pelo ConfiguracaoController para popular o atributo
     * "perfilPermissoes" no request, que é serializado em JSON pela JSP.
     *
     * @return Map<Integer, List<Integer>>  chave = idPerfil, valor = lista de idPermissao
     * @throws Exception em caso de erro de acesso ao banco
     */
    public Map<Integer, List<Integer>> getMapaCompleto() throws Exception {

        Map<Integer, List<Integer>> mapa = new HashMap<>();

        String sql = "SELECT id_perfil, id_permissao FROM perfil_permissao ORDER BY id_perfil";

        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int idPerfil    = rs.getInt("id_perfil");
                int idPermissao = rs.getInt("id_permissao");

                mapa.computeIfAbsent(idPerfil, k -> new ArrayList<>()).add(idPermissao);
            }
        }

        return mapa;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Consultar: permissões de um perfil específico
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Retorna a lista de IDs de permissão associados a um perfil.
     *
     * @param idPerfil ID do perfil consultado
     * @return List<Integer> com os IDs das permissões concedidas
     * @throws Exception em caso de erro de acesso ao banco
     */
    public List<Integer> listarPermissoesDoPerfil(int idPerfil) throws Exception {

        List<Integer> ids = new ArrayList<>();

        String sql = "SELECT id_permissao FROM perfil_permissao WHERE id_perfil = ?";

        try (Connection conn = Conexao.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idPerfil);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt("id_permissao"));
                }
            }
        }

        return ids;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Salvar: substitui todas as permissões de um perfil (DELETE + INSERT)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Substitui as permissões de um perfil de forma atômica.
     *
     * Estratégia: DELETE das associações existentes + INSERT das novas.
     * Operação executada dentro de uma única transação para garantir
     * consistência — ou tudo é salvo, ou nada é alterado.
     *
     * Chamado pelo ConfiguracaoController na ação "salvarPermissoes".
     *
     * @param idPerfil      ID do perfil cujas permissões serão substituídas
     * @param idsPermissoes Lista de IDs das permissões a conceder (pode ser vazia)
     * @throws Exception em caso de erro de acesso ao banco
     */
    public void salvar(int idPerfil, List<Integer> idsPermissoes) throws Exception {

        String sqlDelete = "DELETE FROM perfil_permissao WHERE id_perfil = ?";
        String sqlInsert = "INSERT INTO perfil_permissao (id_perfil, id_permissao) VALUES (?, ?)";

        try (Connection conn = Conexao.getConnection()) {

            // Desativa auto-commit para garantir atomicidade
            conn.setAutoCommit(false);

            try {
                // ── 1. Remove todas as permissões atuais do perfil ──────────
                try (PreparedStatement stmtDel = conn.prepareStatement(sqlDelete)) {
                    stmtDel.setInt(1, idPerfil);
                    stmtDel.executeUpdate();
                }

                // ── 2. Insere as novas permissões (se houver) ────────────────
                if (idsPermissoes != null && !idsPermissoes.isEmpty()) {
                    try (PreparedStatement stmtIns = conn.prepareStatement(sqlInsert)) {
                        for (int idPermissao : idsPermissoes) {
                            stmtIns.setInt(1, idPerfil);
                            stmtIns.setInt(2, idPermissao);
                            stmtIns.addBatch();
                        }
                        stmtIns.executeBatch();
                    }
                }

                conn.commit();

            } catch (Exception e) {
                // Desfaz tudo em caso de erro
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }
}
