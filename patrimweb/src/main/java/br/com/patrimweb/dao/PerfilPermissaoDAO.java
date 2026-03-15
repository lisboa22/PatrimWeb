package br.com.patrimweb.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.patrimweb.model.Fabricante;
import br.com.patrimweb.model.Perfil;
import br.com.patrimweb.model.PerfilPermissao;
import br.com.patrimweb.model.Permissao;

/**
 * DAO: PerfilPermissaoDAO
 *
 * Gerencia a tabela de junção "perfil_permissao":
 *
 *   CREATE TABLE perfil_permissao (
 *       id_perfil    INTEGER NOT NULL REFERENCES perfil(id)    ON DELETE CASCADE,
 *       id_permissao INTEGER NOT NULL REFERENCES permissao(id) ON DELETE CASCADE,
 *       visualizar   BOOLEAN NOT NULL DEFAULT FALSE,
 *       inserir      BOOLEAN NOT NULL DEFAULT FALSE,
 *       editar       BOOLEAN NOT NULL DEFAULT FALSE,
 *       excluir      BOOLEAN NOT NULL DEFAULT FALSE,
 *       PRIMARY KEY (id_perfil, id_permissao)
 *   );
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * ALTERAÇÕES em relação à versão anterior:
 *
 *   1. getMapaSimples()  [NOVO]
 *      Retorna Map<Integer idPerfil, List<Integer idPermissao>>
 *      compatível com o permData do JSP configuracoes.jsp.
 *      Uma permissão aparece na lista se ao menos UMA coluna booleana = TRUE.
 *
 *   2. salvarDaLista()  [NOVO]
 *      Recebe a lista plana de idPermissao enviada pelo AJAX do JSP
 *      e persiste com TODOS os booleanos = TRUE (acesso total ao módulo).
 *      Substitui a lógica granular quando o JSP usa checkbox único por permissão.
 *
 *   3. getMapaCompleto(), getPermissoesDoPerfil(), temAcesso(), salvar()
 *      Mantidos sem alteração para uso futuro ou telas granulares.
 * ─────────────────────────────────────────────────────────────────────────────
 */
public class PerfilPermissaoDAO {

    private final Connection conn;

    public PerfilPermissaoDAO(Connection conn) {
        this.conn = conn;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LEITURA — SIMPLES (compatível com configuracoes.jsp)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Retorna Map<idPerfil, List<idPermissao>> para TODOS os perfis.
     *
     * Uma permissão só entra na lista se ao menos uma ação (visualizar,
     * inserir, editar ou excluir) estiver marcada como TRUE no banco.
     * Isso impede que permissões completamente desabilitadas apareçam
     * como "marcadas" nos checkboxes do JSP.
     *
     * Estrutura esperada pelo permData do JSP:
     *   const permData = {
     *       "1": [10, 11, 15],
     *       "2": [10],
     *       "3": []
     *   };
     *
     * O JSP verifica:
     *   cb.checked = ativos.indexOf(parseInt(cb.dataset.permId)) !== -1;
     *
     * @return mapa preenchido; perfis sem permissões NÃO são incluídos —
     *         o ConfiguracaoController garante que todos os perfis existam no mapa.
     */
    public Map<Integer, List<Integer>> getMapaSimples() throws SQLException {

        String sql = "SELECT id_perfil, id_permissao " +
                     "FROM perfil_permissao " +
                     "WHERE (pode_visualizar = TRUE OR pode_inserir = TRUE " +
                     "       OR pode_editar = TRUE  OR pode_excluir = TRUE) " +
                     "ORDER BY id_perfil, id_permissao";

        Map<Integer, List<Integer>> mapa = new HashMap<>();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int idPerfil    = rs.getInt("id_perfil");
                int idPermissao = rs.getInt("id_permissao");
                mapa.computeIfAbsent(idPerfil, k -> new ArrayList<>()).add(idPermissao);
            }
        }
        System.out.println("Permissões: "+mapa);
        return mapa;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ESCRITA — SIMPLES (compatível com configuracoes.jsp)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Substitui TODAS as permissões de um perfil de forma atômica,
     * usando a lista plana de IDs enviada pelo AJAX do JSP.
     *
     * Estratégia: DELETE total + INSERT em batch dentro de uma transação.
     * Se qualquer passo falhar, o ROLLBACK preserva o estado anterior.
     *
     * Todos os booleanos são gravados como TRUE, pois o JSP usa checkbox
     * único por permissão (sem granularidade de ação). Isso equivale a
     * "conceder acesso completo ao módulo/permissão selecionada".
     *
     * Se no futuro a tela evoluir para checkboxes individuais por ação,
     * use o método salvar(int, Map<Integer, List<String>>) já existente.
     *
     * @param idPerfil       ID do perfil cujas permissões serão sobrescritas
     * @param idsPermissao   Lista final de IDs de permissão a serem ativos
     *                       (lista vazia = revogar tudo)
     *
     * Chamado por: ConfiguracaoController.salvarPermissoes()
     */
    public void salvarDaLista(int idPerfil, List<Integer> idsPermissao) throws SQLException {

        boolean autoCommitOriginal = conn.getAutoCommit();

        try {
            conn.setAutoCommit(false);

            // 1. Remove todas as permissões atuais do perfil
            try (PreparedStatement psDel = conn.prepareStatement(
                    "DELETE FROM perfil_permissao WHERE id_perfil = ?")) {
                psDel.setInt(1, idPerfil);
                psDel.executeUpdate();
            }

            // 2. Insere as novas permissões em batch (todos os booleanos = TRUE)
            if (idsPermissao != null && !idsPermissao.isEmpty()) {

                String sqlInsert =
                    "INSERT INTO perfil_permissao " +
                    "(id_perfil, id_permissao, pode_visualizar, pode_inserir, pode_editar, pode_excluir) " +
                    "VALUES (?, ?, TRUE, TRUE, TRUE, TRUE)";

                try (PreparedStatement psIns = conn.prepareStatement(sqlInsert)) {
                    for (int idPerm : idsPermissao) {
                        psIns.setInt(1, idPerfil);
                        psIns.setInt(2, idPerm);
                        psIns.addBatch();
                    }
                    psIns.executeBatch();
                }
            }

            conn.commit();

        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException rb) {
                System.err.println("[PatrimWeb] Erro no rollback: " + rb.getMessage());
            }
            throw e; // propaga para o Controller responder HTTP 500
        } finally {
            try { conn.setAutoCommit(autoCommitOriginal); } catch (SQLException ignored) {}
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LEITURA — COMPLETA (granular — mantida para uso futuro)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Retorna o mapa completo de permissões granulares de TODOS os perfis.
     *
     * Estrutura:
     *   { idPerfil → { idPermissao → ["visualizar", "editar"] } }
     *
     * Ações com FALSE no banco não são incluídas na lista interna.
     */
    public Map<Integer, Map<Integer, List<String>>> getMapaCompleto() throws SQLException {

        String sql = "SELECT id_perfil, id_permissao, " +
                     "       pode_visualizar, pode_inserir, pode_editar, pode_excluir " +
                     "FROM perfil_permissao " +
                     "ORDER BY id_perfil, id_permissao";

        Map<Integer, Map<Integer, List<String>>> mapa = new HashMap<>();

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int idPerfil    = rs.getInt("id_perfil");
                int idPermissao = rs.getInt("id_permissao");

                mapa.computeIfAbsent(idPerfil, k -> new HashMap<>());

                List<String> acoes = new ArrayList<>();
                if (rs.getBoolean("pode_visualizar")) acoes.add("pode_visualizar");
                if (rs.getBoolean("pode_inserir"))    acoes.add("pode_inserir");
                if (rs.getBoolean("pode_editar"))     acoes.add("pode_editar");
                if (rs.getBoolean("pode_excluir"))    acoes.add("pode_excluir");

                if (!acoes.isEmpty()) {
                    mapa.get(idPerfil).put(idPermissao, acoes);
                }
            }
        }
        System.out.println("Permissões1: "+mapa);
        return mapa;
    }

    /**
     * Retorna as permissões granulares de um perfil específico.
     * Útil para verificação pontual (ex: middleware de autorização).
     */
    public Map<Integer, List<String>> getPermissoesDoPerfil(int idPerfil) throws SQLException {

        String sql = "SELECT id_permissao, pode_visualizar, pode_inserir, pode_editar, pode_excluir " +
                     "FROM perfil_permissao " +
                     "WHERE id_perfil = ? " +
                     "ORDER BY id_permissao";

        Map<Integer, List<String>> mapa = new HashMap<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPerfil);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int idPermissao = rs.getInt("id_permissao");

                    List<String> acoes = new ArrayList<>();
                    if (rs.getBoolean("pode_visualizar")) acoes.add("pode_visualizar");
                    if (rs.getBoolean("pode_inserir"))    acoes.add("pode_inserir");
                    if (rs.getBoolean("pode_editar"))     acoes.add("pode_editar");
                    if (rs.getBoolean("pode_excluir"))    acoes.add("pode_excluir");

                    if (!acoes.isEmpty()) {
                        mapa.put(idPermissao, acoes);
                    }
                }
            }
        }
        System.out.println("Permissões2: "+mapa);
        return mapa;
    }

    /**
     * Verifica se um perfil possui uma ação específica sobre uma permissão.
     * Exemplo: temAcesso(idPerfil, idPermissao, "inserir")
     */
    public boolean temAcesso(int idPerfil, int idPermissao, String acao) throws SQLException {

        if (!acao.equals("pode_visualizar") && !acao.equals("pode_inserir")
                && !acao.equals("pode_editar") && !acao.equals("pode_excluir")) {
            return false;
        }

        String sql = "SELECT " + acao +
                     " FROM perfil_permissao " +
                     " WHERE id_perfil = ? AND id_permissao = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPerfil);
            ps.setInt(2, idPermissao);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean(acao);
                }
            }
        }

        return false;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ESCRITA — GRANULAR (mantida para uso futuro)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Persiste permissões granulares (por ação) de um perfil de forma atômica.
     *
     * @param idPerfil   ID do perfil
     * @param mapaAcoes  Map<idPermissao, List<acao>> — ex: {10: ["visualizar","inserir"]}
     */
    public void salvar(int idPerfil, Map<Integer, List<String>> mapaAcoes) throws SQLException {

        boolean autoCommitOriginal = conn.getAutoCommit();

        try {
            conn.setAutoCommit(false);

            try (PreparedStatement psDel = conn.prepareStatement(
                    "DELETE FROM perfil_permissao WHERE id_perfil = ?")) {
                psDel.setInt(1, idPerfil);
                psDel.executeUpdate();
            }

            if (mapaAcoes != null && !mapaAcoes.isEmpty()) {

                String sqlInsert =
                    "INSERT INTO perfil_permissao " +
                    "(id_perfil, id_permissao, pode_visualizar, pode_inserir, pode_editar, pode_excluir) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

                try (PreparedStatement psIns = conn.prepareStatement(sqlInsert)) {

                    for (Map.Entry<Integer, List<String>> entry : mapaAcoes.entrySet()) {
                        int          idPermissao = entry.getKey();
                        List<String> acoes       = entry.getValue();

                        if (acoes == null || acoes.isEmpty()) continue;

                        psIns.setInt    (1, idPerfil);
                        psIns.setInt    (2, idPermissao);
                        psIns.setBoolean(3, acoes.contains("pode_visualizar"));
                        psIns.setBoolean(4, acoes.contains("pode_inserir"));
                        psIns.setBoolean(5, acoes.contains("pode_editar"));
                        psIns.setBoolean(6, acoes.contains("pode_excluir"));
                        psIns.addBatch();
                    }

                    psIns.executeBatch();
                }
            }

            conn.commit();

        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException rb) {
                System.err.println("[PatrimWeb] Erro no rollback: " + rb.getMessage());
            }
            throw e;
        } finally {
            try { conn.setAutoCommit(autoCommitOriginal); } catch (SQLException ignored) {}
        }
    }
    
}
