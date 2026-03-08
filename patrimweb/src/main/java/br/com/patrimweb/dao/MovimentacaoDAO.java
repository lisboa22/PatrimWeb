package br.com.patrimweb.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import br.com.patrimweb.model.Equipamento;
import br.com.patrimweb.model.Fabricante;
import br.com.patrimweb.model.Movimentacao;
import br.com.patrimweb.model.Unidade;
import br.com.patrimweb.model.Usuario;

/**
 * DAO responsável pela persistência e consulta das movimentações de equipamentos.
 *
 * Responsabilidades principais:
 * - Registrar movimentações entre unidades.
 * - Consultar histórico de movimentações.
 * - Atualizar e excluir registros.
 * - Fornecer dados agregados para dashboards e relatórios.
 *
 * Regras de negócio implícitas:
 * - Cada movimentação está associada a um equipamento, unidade de origem,
 *   unidade de destino, usuário responsável pela liberação e recepção.
 * - As consultas utilizam JOINs para reconstruir o objeto completo
 *   a partir das relações do banco de dados.
 *
 * Pontos críticos:
 * - Dependência direta de uma conexão JDBC válida.
 * - Construção manual dos objetos relacionados após consultas SQL.
 * - Uso intensivo de JOINs, podendo impactar performance em grandes volumes.
 */
public class MovimentacaoDAO {

    private Connection conexao;

    /**
     * Construtor que recebe a conexão ativa com o banco de dados.
     *
     * @param conexao conexão JDBC utilizada nas operações SQL.
     */
    public MovimentacaoDAO(Connection conexao) {
        this.conexao = conexao;
    }

    // -------------------------------------------------------------------------
    // CRUD existente (sem alterações)
    // -------------------------------------------------------------------------

    /**
     * Insere uma nova movimentação no banco de dados.
     *
     * Regra de negócio:
     * - A movimentação registra transferência ou alteração de estado
     *   de um equipamento entre unidades e usuários.
     *
     * Interação com banco:
     * - Executa INSERT na tabela movimentacao.
     *
     * @param movimentacao objeto contendo todos os dados da movimentação.
     * @throws Exception erro durante operação SQL.
     */
    public void adicionarMovimentacao(Movimentacao movimentacao) throws Exception {
        String sql = "INSERT INTO movimentacao (equipamento, tipo_movimentacao, "
                   + "unidade_origem, usuario_origem, unidade_destino, usuario_destino, "
                   + "observacao, data_insercao) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement stmt = conexao.prepareStatement(sql);

        // Associação dos atributos do objeto aos parâmetros SQL.
        stmt.setInt(1,    movimentacao.getEquipamento().getIdEquip());
        stmt.setString(2, movimentacao.getTipoMovimentacaoMov());
        stmt.setInt(3,    movimentacao.getUnidadeOrigem().getIdUnid());
        stmt.setInt(4,    movimentacao.getUsuarioOrigem().getIdUsu());
        stmt.setInt(5,    movimentacao.getUnidadeDestino().getIdUnid());
        stmt.setInt(6,    movimentacao.getUsuarioDestino().getIdUsu());
        stmt.setString(7, movimentacao.getObservacaoMov());
        stmt.setTimestamp(8, movimentacao.getDataInsercao());

        // Executa gravação no banco.
        stmt.executeUpdate();
        stmt.close();
    }

    /**
     * Lista todas as movimentações cadastradas.
     *
     * Regras importantes:
     * - Utiliza múltiplos JOINs para recuperar dados completos
     *   das entidades relacionadas.
     * - Reconstrói manualmente os objetos Equipamento, Unidade e Usuario.
     *
     * Estrutura relevante:
     * - Loop while percorre ResultSet realizando o mapeamento objeto-relacional.
     *
     * @return lista de movimentações existentes.
     * @throws Exception erro durante consulta SQL.
     */
    public List<Movimentacao> listarMovimentacoes() throws Exception {
        List<Movimentacao> movimentacoes = new ArrayList<>();

        String sql = "SELECT "
                + "    m.id_mov              AS id, "
                + "    m.tipo_movimentacao, "
                + "    m.observacao          AS observacoes, "
                + "    m.data_insercao       AS data_hora, "
                + "    e.id_equip            AS id_equipamento, "
                + "    e.nome_equip          AS nome_equipamento, "
                + "    e.num_serie_equip     AS numero_equipamento, "
                + "    f.id_fab              AS id_fabricante, "
                + "    f.nome_fab            AS nome_fabricante, "
                + "    uo.id_unid            AS id_origem, "
                + "    uo.nome_unid          AS unidade_origem, "
                + "    ud.id_unid            AS id_destino, "
                + "    ud.nome_unid          AS unidade_destino, "
                + "    ul.id_usu             AS id_usuario_liberacao, "
                + "    ul.nome_usu           AS nome_usuario_liberacao, "
                + "    ur.id_usu             AS id_usuario_recepcao, "
                + "    ur.nome_usu           AS nome_usuario_recepcao "
                + "FROM movimentacao m "
                + "JOIN equipamento e  ON m.equipamento     = e.id_equip "
                + "JOIN fabricante f   ON e.fabricante = f.id_fab "
                + "JOIN unidade uo     ON m.unidade_origem  = uo.id_unid "
                + "JOIN unidade ud     ON m.unidade_destino = ud.id_unid "
                + "JOIN usuario ul     ON m.usuario_origem  = ul.id_usu "
                + "JOIN usuario ur     ON m.usuario_destino = ur.id_usu "
                + "ORDER BY m.id_mov";

        PreparedStatement stmt = conexao.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();

        // Reconstrução completa do objeto Movimentacao a partir do ResultSet.
        while (rs.next()) {
            Equipamento equipamento = new Equipamento();
            equipamento.setIdEquip(rs.getInt("id_equipamento"));
            equipamento.setNomeEquip(rs.getString("nome_equipamento"));
            equipamento.setNumSerieEquip(rs.getString("numero_equipamento"));
            
            Fabricante fabricante = new Fabricante();
            fabricante.setIdFab(rs.getInt("id_fabricante"));
            fabricante.setNomeFab(rs.getString("nome_fabricante"));

            Unidade unidade_origem = new Unidade();
            unidade_origem.setIdUnid(rs.getInt("id_origem"));
            unidade_origem.setNomeUnid(rs.getString("unidade_origem"));

            Unidade unidade_destino = new Unidade();
            unidade_destino.setIdUnid(rs.getInt("id_destino"));
            unidade_destino.setNomeUnid(rs.getString("unidade_destino"));

            Usuario usuario_liberacao = new Usuario();
            usuario_liberacao.setIdUsu(rs.getInt("id_usuario_liberacao"));
            usuario_liberacao.setNomeUsu(rs.getString("nome_usuario_liberacao"));

            Usuario usuario_recepcao = new Usuario();
            usuario_recepcao.setIdUsu(rs.getInt("id_usuario_recepcao"));
            usuario_recepcao.setNomeUsu(rs.getString("nome_usuario_recepcao"));

            Movimentacao movimentacao = new Movimentacao(
                rs.getInt("id"),
                equipamento,
                fabricante,
                rs.getString("tipo_movimentacao"),
                unidade_origem,
                usuario_liberacao,
                unidade_destino,
                usuario_recepcao,
                rs.getString("observacoes"),
                rs.getTimestamp("data_hora")
            );

            movimentacao.setIdMov(rs.getInt("id"));
            movimentacoes.add(movimentacao);
        }

        rs.close();
        stmt.close();
        
        return movimentacoes;
    }

    /**
     * Atualiza uma movimentação existente.
     *
     * Regra de negócio:
     * - A atualização ocorre baseada no id_mov.
     *
     * @param movimentacao objeto contendo os novos dados.
     * @throws Exception erro durante execução SQL.
     */
    public void alterarMovimentacao(Movimentacao movimentacao) throws Exception {
        String sql = "UPDATE movimentacao SET equipamento = ?, "
                   + "tipo_movimentacao = ?, unidade_origem = ?, usuario_origem = ?, "
                   + "unidade_destino = ?, usuario_destino = ?, observacao = ? "
                   + "WHERE id_mov = ?";

        PreparedStatement stmt = conexao.prepareStatement(sql);

        stmt.setInt(1,    movimentacao.getEquipamento().getIdEquip());
        stmt.setString(2, movimentacao.getTipoMovimentacaoMov());
        stmt.setInt(3,    movimentacao.getUnidadeOrigem().getIdUnid());
        stmt.setInt(4,    movimentacao.getUsuarioOrigem().getIdUsu());
        stmt.setInt(5,    movimentacao.getUnidadeDestino().getIdUnid());
        stmt.setInt(6,    movimentacao.getUsuarioDestino().getIdUsu());
        stmt.setString(7, movimentacao.getObservacaoMov());
        stmt.setInt(8,    movimentacao.getIdMov());

        stmt.executeUpdate();
        stmt.close();
    }

    /**
     * Remove uma movimentação pelo identificador.
     *
     * @param id identificador da movimentação.
     * @throws Exception erro durante exclusão.
     */
    public void excluirMovimentacao(int id) throws Exception {
        String sql = "DELETE FROM movimentacao WHERE id_mov = ?";
        PreparedStatement stmt = conexao.prepareStatement(sql);
        stmt.setInt(1, id);
        stmt.executeUpdate();
        stmt.close();
    }

    /**
     * Conta quantos equipamentos estão atualmente em manutenção.
     *
     * Regra de negócio:
     * - Considera registros cujo tipo_movimentacao seja "Manutenção".
     *
     * @return quantidade total encontrada.
     * @throws Exception erro durante consulta SQL.
     */
    public int contarEquipamentosEmManutencao() throws Exception {
        String sql = "SELECT COUNT(*) AS total FROM movimentacao m "
                   + "WHERE m.tipo_movimentacao = 'Manutenção'";

        try (PreparedStatement ps = conexao.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
        }
        return 0;
    }

    /**
     * Retorna ranking de equipamentos mais movimentados.
     *
     * Regra de negócio:
     * - Mantém apenas os 5 equipamentos mais movimentados.
     * - Os demais são agrupados como "Outros".
     *
     * Estruturas relevantes:
     * - LinkedHashMap preserva ordem do resultado.
     * - Loop realiza agregação manual dos excedentes.
     *
     * @return mapa contendo equipamento e quantidade de movimentações.
     */
    public Map<String, Integer> contarEquipamentosEmMovimentacao() {
        Map<String, Integer> mapaBruto = new LinkedHashMap<>();

        String sql = "SELECT e.nome_equip AS nomeEquipamento, "
                   + "COUNT(*) AS total FROM movimentacao m "
                   + "JOIN equipamento e ON e.id_equip = m.equipamento "
                   + "GROUP BY e.nome_equip "
                   + "ORDER BY total DESC";

        try (PreparedStatement ps = conexao.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                mapaBruto.put(rs.getString("nomeEquipamento"), rs.getInt("total"));
            }

        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }

        Map<String, Integer> resultado = new LinkedHashMap<>();
        int count = 0;
        int outros = 0;

        // Mantém apenas os 5 primeiros resultados e agrega o restante.
        for (Map.Entry<String, Integer> entry : mapaBruto.entrySet()) {
            if (count < 5) {
                resultado.put(entry.getKey(), entry.getValue());
            } else {
                outros += entry.getValue();
            }
            count++;
        }

        if (outros > 0) {
            resultado.put("Outros", outros);
        }

        return resultado;
    }

    // -------------------------------------------------------------------------
    // NOVOS MÉTODOS — suporte ao relatório dinâmico
    // -------------------------------------------------------------------------

    /**
     * Filtra movimentações com critérios opcionais.
     *
     * Regras de negócio:
     * - Apenas filtros informados são aplicados.
     * - Permite busca textual por nome do equipamento ou número de série.
     *
     * Ponto crítico:
     * - Controle manual do índice dos parâmetros devido à query dinâmica.
     *
     * @param dataInicio data inicial do filtro.
     * @param dataFim data final do filtro.
     * @param tipoMovimentacao tipo da movimentação.
     * @param equipamento termo de busca textual.
     * @return lista de movimentações filtradas.
     * @throws Exception erro durante execução SQL.
     */
    public List<Movimentacao> filtrarMovimentacoes(
            Timestamp dataInicio,
            Timestamp dataFim,
            String tipoMovimentacao,
            String equipamento
    ) throws Exception {

        List<Movimentacao> lista = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
            "SELECT "
          + "    m.id_mov              AS id, "
          + "    m.tipo_movimentacao, "
          + "    m.observacao          AS observacoes, "
          + "    m.data_insercao       AS data_hora, "
          + "    e.id_equip            AS id_equipamento, "
          + "    e.nome_equip          AS nome_equipamento, "
          + "    e.fabricante          AS id_fabricante, "
          + "    f.nome_fab            AS nome_fabricante, "
          + "    uo.id_unid            AS id_origem, "
          + "    uo.nome_unid          AS unidade_origem, "
          + "    ud.id_unid            AS id_destino, "
          + "    ud.nome_unid          AS unidade_destino, "
          + "    ul.id_usu             AS id_usuario_liberacao, "
          + "    ul.nome_usu           AS nome_usuario_liberacao, "
          + "    ur.id_usu             AS id_usuario_recepcao, "
          + "    ur.nome_usu           AS nome_usuario_recepcao "
          + "FROM movimentacao m "
          + "JOIN equipamento e  ON m.equipamento    = e.id_equip "
          + "JOIN fabricante f   ON e.fabricante     = f.id_fab "
          + "JOIN unidade uo     ON m.unidade_origem  = uo.id_unid "
          + "JOIN unidade ud     ON m.unidade_destino = ud.id_unid "
          + "JOIN usuario ul     ON m.usuario_origem  = ul.id_usu "
          + "JOIN usuario ur     ON m.usuario_destino = ur.id_usu "
          + "WHERE 1=1 "
        );

        if (dataInicio != null) {
            sql.append(" AND m.data_insercao >= ? ");
        }
        if (dataFim != null) {
            sql.append(" AND m.data_insercao <= ? ");
        }
        if (tipoMovimentacao != null && !tipoMovimentacao.trim().isEmpty()) {
            sql.append(" AND m.tipo_movimentacao = ? ");
        }
        if (equipamento != null && !equipamento.trim().isEmpty()) {
            sql.append(" AND (e.nome_equip LIKE ? OR e.num_serie_equip LIKE ?) ");
        }

        sql.append(" ORDER BY id_mov DESC ");

        PreparedStatement stmt = conexao.prepareStatement(sql.toString());
        int index = 1;

        if (dataInicio != null) {
            stmt.setTimestamp(index++, dataInicio);
        }
        if (dataFim != null) {
            stmt.setTimestamp(index++, dataFim);
        }
        if (tipoMovimentacao != null && !tipoMovimentacao.trim().isEmpty()) {
            stmt.setString(index++, tipoMovimentacao.trim());
        }
        if (equipamento != null && !equipamento.trim().isEmpty()) {
            String termo = "%" + equipamento.trim() + "%";
            stmt.setString(index++, termo);
            stmt.setString(index++, termo);
        }

        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            Equipamento equip = new Equipamento();
            equip.setIdEquip(rs.getInt("id_equipamento"));
            equip.setNomeEquip(rs.getString("nome_equipamento"));
            
            Fabricante fabricante = new Fabricante();
            fabricante.setIdFab(rs.getInt("id_fabricante"));
            fabricante.setNomeFab(rs.getString("nome_fabricante"));

            Unidade unidade_origem = new Unidade();
            unidade_origem.setIdUnid(rs.getInt("id_origem"));
            unidade_origem.setNomeUnid(rs.getString("unidade_origem"));

            Unidade unidade_destino = new Unidade();
            unidade_destino.setIdUnid(rs.getInt("id_destino"));
            unidade_destino.setNomeUnid(rs.getString("unidade_destino"));

            Usuario usuario_liberacao = new Usuario();
            usuario_liberacao.setIdUsu(rs.getInt("id_usuario_liberacao"));
            usuario_liberacao.setNomeUsu(rs.getString("nome_usuario_liberacao"));

            Usuario usuario_recepcao = new Usuario();
            usuario_recepcao.setIdUsu(rs.getInt("id_usuario_recepcao"));
            usuario_recepcao.setNomeUsu(rs.getString("nome_usuario_recepcao"));

            Movimentacao mov = new Movimentacao(
                rs.getInt("id"),
                equip,
                fabricante,
                rs.getString("tipo_movimentacao"),
                unidade_origem,
                usuario_liberacao,
                unidade_destino,
                usuario_recepcao,
                rs.getString("observacoes"),
                rs.getTimestamp("data_hora")
            );
            mov.setIdMov(rs.getInt("id"));
            lista.add(mov);
        }

        rs.close();
        stmt.close();
        return lista;
    }

    /**
     * Retorna anos distintos com movimentações registradas.
     *
     * @return lista de anos em ordem decrescente.
     * @throws Exception erro durante consulta.
     */
    public List<Integer> listarAnosCadastro() throws Exception {
        List<Integer> anos = new ArrayList<>();

        String sql = """
            SELECT DISTINCT YEAR(data_insercao) AS ano
            FROM movimentacao
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
     * Retorna a quantidade de movimentações agrupadas por mês.
     *
     * @param ano ano utilizado como filtro.
     * @return mapa onde chave = mês e valor = total de registros.
     * @throws Exception erro durante consulta SQL.
     */
    public Map<Integer, Integer> quantidadeMovimentacoesPorMes(int ano) throws Exception {
        Map<Integer, Integer> dados = new LinkedHashMap<>();

        String sql = """
            SELECT
                MONTH(data_insercao) AS mes,
                COUNT(*)             AS total
            FROM movimentacao
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
     * Lista movimentações a partir de uma data específica.
     *
     * Regra de negócio:
     * - Utilizado para dashboards que exibem movimentações recentes.
     *
     * @param dataInicio data mínima considerada.
     * @return lista de movimentações encontradas.
     * @throws Exception erro durante consulta SQL.
     */
    public List<Movimentacao> listarMovimentacoesPorPeriodo(Timestamp dataInicio) throws Exception {
        List<Movimentacao> movimentacoes = new ArrayList<>();
        
        String sql = "SELECT "
                + "    m.id_mov              AS id, "
                + "    m.tipo_movimentacao, "
                + "    m.observacao          AS observacoes, "
                + "    m.data_insercao       AS data_hora, "
                + "    e.id_equip            AS id_equipamento, "
                + "    e.nome_equip          AS nome_equipamento, "
                + "    f.id_fab              AS id_fabricante, "
                + "    f.nome_fab            AS nome_fabricante, "
                + "    uo.id_unid            AS id_origem, "
                + "    uo.nome_unid          AS unidade_origem, "
                + "    ud.id_unid            AS id_destino, "
                + "    ud.nome_unid          AS unidade_destino, "
                + "    ul.id_usu             AS id_usuario_liberacao, "
                + "    ul.nome_usu           AS nome_usuario_liberacao, "
                + "    ur.id_usu             AS id_usuario_recepcao, "
                + "    ur.nome_usu           AS nome_usuario_recepcao "
                + "FROM movimentacao m "
                + "JOIN equipamento e  ON m.equipamento    = e.id_equip "
                + "JOIN fabricante f   ON e.fabricante = f.id_fab "
                + "JOIN unidade uo     ON m.unidade_origem  = uo.id_unid "
                + "JOIN unidade ud     ON m.unidade_destino = ud.id_unid "
                + "JOIN usuario ul     ON m.usuario_origem  = ul.id_usu "
                + "JOIN usuario ur     ON m.usuario_destino = ur.id_usu "
                + "WHERE m.data_insercao >= ? "
                + "ORDER BY m.data_insercao DESC";
        
        PreparedStatement stmt = conexao.prepareStatement(sql);
        stmt.setTimestamp(1, dataInicio);
        ResultSet rs = stmt.executeQuery();
        
        while (rs.next()) {
            Equipamento equipamento = new Equipamento();
            equipamento.setIdEquip(rs.getInt("id_equipamento"));
            equipamento.setNomeEquip(rs.getString("nome_equipamento"));
            
            Fabricante fabricante = new Fabricante();
            fabricante.setIdFab(rs.getInt("id_fabricante"));
            fabricante.setNomeFab(rs.getString("nome_fabricante"));
            
            Unidade unidade_origem = new Unidade();
            unidade_origem.setIdUnid(rs.getInt("id_origem"));
            unidade_origem.setNomeUnid(rs.getString("unidade_origem"));
            
            Unidade unidade_destino = new Unidade();
            unidade_destino.setIdUnid(rs.getInt("id_destino"));
            unidade_destino.setNomeUnid(rs.getString("unidade_destino"));
            
            Usuario usuario_liberacao = new Usuario();
            usuario_liberacao.setIdUsu(rs.getInt("id_usuario_liberacao"));
            usuario_liberacao.setNomeUsu(rs.getString("nome_usuario_liberacao"));
            
            Usuario usuario_recepcao = new Usuario();
            usuario_recepcao.setIdUsu(rs.getInt("id_usuario_recepcao"));
            usuario_recepcao.setNomeUsu(rs.getString("nome_usuario_recepcao"));
            
            Movimentacao movimentacao = new Movimentacao(
                rs.getInt("id"),
                equipamento,
                fabricante,
                rs.getString("tipo_movimentacao"),
                unidade_origem,
                usuario_liberacao,
                unidade_destino,
                usuario_recepcao,
                rs.getString("observacoes"),
                rs.getTimestamp("data_hora")
            );
            movimentacao.setIdMov(rs.getInt("id"));
            movimentacoes.add(movimentacao);
        }
        
        rs.close();
        stmt.close();
        
        return movimentacoes;
    }
}
