package br.com.patrimweb.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import br.com.patrimweb.model.Unidade;

/**
 * DAO responsável pela persistência e recuperação de dados da entidade Unidade.
 *
 * Esta classe implementa operações de acesso ao banco de dados seguindo o padrão DAO
 * (Data Access Object), encapsulando toda a lógica SQL relacionada à tabela "unidade".
 *
 * Responsabilidades principais:
 * - Realizar operações CRUD (Create, Read, Update, Delete)
 * - Executar consultas filtradas para relatórios
 * - Fornecer dados agregados utilizados em gráficos e análises estatísticas
 *
 * Regras importantes:
 * - Utiliza PreparedStatement para evitar SQL Injection.
 * - Depende de uma conexão previamente criada e injetada via construtor.
 * - Não gerencia abertura ou fechamento da conexão, apenas statements/resultsets.
 */
public class UnidadeDAO {

    /**
     * Conexão ativa com o banco de dados utilizada por todos os métodos da DAO.
     * A responsabilidade de criação e gerenciamento do ciclo de vida da conexão
     * pertence à camada superior da aplicação.
     */
    private Connection conexao;

    /**
     * Construtor responsável por receber a conexão com o banco.
     *
     * @param conexao conexão JDBC já estabelecida
     */
    public UnidadeDAO(Connection conexao) {
        this.conexao = conexao;
    }

    // -------------------------------------------------------------------------
    // CRUD existente (sem alterações)
    // -------------------------------------------------------------------------

    /**
     * Insere uma nova Unidade no banco de dados.
     *
     * Regra de negócio:
     * - Todos os dados necessários devem estar previamente preenchidos no objeto Unidade.
     * - A data de inserção é persistida conforme fornecida pela camada superior.
     *
     * Interação com banco:
     * - Executa comando INSERT na tabela unidade.
     *
     * @param unidade objeto contendo os dados a serem persistidos
     * @throws Exception caso ocorra erro durante a execução SQL
     */
    public void adicionarUnidade(Unidade unidade) throws Exception {
        String sql = "INSERT INTO unidade (nome_unid, telefone_unid, email_unid, endereco_unid, data_insercao) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement stmt = conexao.prepareStatement(sql);

        // Associação dos parâmetros da query aos atributos da entidade
        stmt.setString(1, unidade.getNomeUnid());
        stmt.setString(2, unidade.getTelefoneUnid());
        stmt.setString(3, unidade.getEmailUnid());
        stmt.setString(4, unidade.getEnderecoUnid());
        stmt.setTimestamp(5, unidade.getDataInsercao());

        // Executa inserção no banco
        stmt.executeUpdate();
        stmt.close();
    }

    /**
     * Recupera todas as unidades cadastradas no banco de dados.
     *
     * Regra de negócio:
     * - Retorna todos os registros existentes sem filtros.
     *
     * Estrutura relevante:
     * - Loop while percorre o ResultSet convertendo cada linha em um objeto Unidade.
     *
     * @return lista contendo todas as unidades cadastradas
     * @throws Exception em caso de erro de acesso ao banco
     */
    public List<Unidade> listarUnidades() throws Exception {
        List<Unidade> unidades = new ArrayList<>();
        String sql = "SELECT * FROM unidade";
        PreparedStatement stmt = conexao.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();

        // Percorre todos os registros retornados pelo banco
        while (rs.next()) {

            // Mapeamento linha → objeto de domínio
            Unidade unidade = new Unidade(
                rs.getInt("id_unid"),
                rs.getString("nome_unid"),
                rs.getString("telefone_unid"),
                rs.getString("email_unid"),
                rs.getString("endereco_unid"),
                rs.getTimestamp("data_insercao")
            );

            unidades.add(unidade);
        }

        rs.close();
        stmt.close();
        return unidades;
    }

    /**
     * Atualiza os dados de uma unidade existente.
     *
     * Regra de negócio:
     * - A atualização ocorre com base no identificador único (id_unid).
     * - Apenas campos editáveis são modificados.
     *
     * @param unidade objeto contendo os novos dados
     * @throws Exception em caso de erro SQL
     */
    public void alterarUnidade(Unidade unidade) throws Exception {
        String sql = "UPDATE unidade SET nome_unid = ?, telefone_unid = ?, email_unid = ?, endereco_unid = ? WHERE id_unid = ?";
        PreparedStatement stmt = conexao.prepareStatement(sql);

        // Define novos valores para atualização
        stmt.setString(1, unidade.getNomeUnid());
        stmt.setString(2, unidade.getTelefoneUnid());
        stmt.setString(3, unidade.getEmailUnid());
        stmt.setString(4, unidade.getEnderecoUnid());
        stmt.setInt(5, unidade.getIdUnid());

        stmt.executeUpdate();
        stmt.close();
    }

    /**
     * Remove uma unidade do banco de dados.
     *
     * Regra de negócio:
     * - A exclusão é permanente (DELETE físico).
     * - A busca é realizada pelo identificador primário.
     *
     * @param id identificador da unidade
     * @throws Exception em caso de erro SQL
     */
    public void excluirUnidade(int id) throws Exception {
        String sql = "DELETE FROM unidade WHERE id_unid = ?";
        PreparedStatement stmt = conexao.prepareStatement(sql);

        stmt.setInt(1, id);

        stmt.executeUpdate();
        stmt.close();
    }

    /**
     * Busca uma unidade específica pelo seu ID.
     *
     * Validação implícita:
     * - Caso nenhum registro seja encontrado, retorna null.
     *
     * Estrutura de decisão:
     * - O if(rs.next()) garante que apenas um registro será convertido.
     *
     * @param id identificador da unidade
     * @return objeto Unidade encontrado ou null se inexistente
     * @throws Exception em caso de erro SQL
     */
    public Unidade buscarPorId(int id) throws Exception {
        Unidade unidade = null;
        String sql = "SELECT * FROM unidade WHERE id_unid = ?";
        PreparedStatement stmt = conexao.prepareStatement(sql);

        stmt.setInt(1, id);
        ResultSet rs = stmt.executeQuery();

        // Verifica existência de resultado
        if (rs.next()) {
            unidade = new Unidade(
                rs.getInt("id_unid"),
                rs.getString("nome_unid"),
                rs.getString("telefone_unid"),
                rs.getString("email_unid"),
                rs.getString("endereco_unid"),
                rs.getTimestamp("data_insercao")
            );
        }

        rs.close();
        stmt.close();
        return unidade;
    }

    // -------------------------------------------------------------------------
    // NOVOS MÉTODOS — suporte ao relatório dinâmico
    // -------------------------------------------------------------------------

    /**
     * Filtra unidades dinamicamente conforme critérios informados.
     *
     * Regras de negócio:
     * - Todos os parâmetros são opcionais.
     * - Apenas filtros informados são adicionados à query.
     * - Utiliza "WHERE 1=1" para facilitar concatenação dinâmica.
     *
     * Ponto crítico:
     * - A ordem dos parâmetros deve coincidir exatamente com a ordem
     *   de inclusão das cláusulas SQL.
     *
     * @param dataInicio data mínima de cadastro
     * @param dataFim data máxima de cadastro
     * @param nome filtro parcial pelo nome da unidade
     * @param endereco filtro parcial pelo endereço
     * @return lista de unidades que atendem aos critérios
     * @throws Exception em caso de erro SQL
     */
    public List<Unidade> filtrarUnidades(
            Timestamp dataInicio,
            Timestamp dataFim,
            String nome,
            String endereco
    ) throws Exception {

        List<Unidade> lista = new ArrayList<>();

        // Query construída dinamicamente conforme filtros informados
        StringBuilder sql = new StringBuilder("SELECT * FROM unidade WHERE 1=1 ");

        if (dataInicio != null) {
            sql.append(" AND data_insercao >= ? ");
        }
        if (dataFim != null) {
            sql.append(" AND data_insercao <= ? ");
        }
        if (nome != null && !nome.trim().isEmpty()) {
            sql.append(" AND nome_unid LIKE ? ");
        }
        if (endereco != null && !endereco.trim().isEmpty()) {
            sql.append(" AND endereco_unid LIKE ? ");
        }

        PreparedStatement stmt = conexao.prepareStatement(sql.toString());
        int index = 1;

        // Associação dinâmica dos parâmetros respeitando a ordem SQL
        if (dataInicio != null) {
            stmt.setTimestamp(index++, dataInicio);
        }
        if (dataFim != null) {
            stmt.setTimestamp(index++, dataFim);
        }
        if (nome != null && !nome.trim().isEmpty()) {
            stmt.setString(index++, "%" + nome.trim() + "%");
        }
        if (endereco != null && !endereco.trim().isEmpty()) {
            stmt.setString(index++, "%" + endereco.trim() + "%");
        }

        ResultSet rs = stmt.executeQuery();

        // Conversão dos registros retornados em objetos de domínio
        while (rs.next()) {
            Unidade unidade = new Unidade(
                rs.getInt("id_unid"),
                rs.getString("nome_unid"),
                rs.getString("telefone_unid"),
                rs.getString("email_unid"),
                rs.getString("endereco_unid"),
                rs.getTimestamp("data_insercao")
            );
            lista.add(unidade);
        }

        rs.close();
        stmt.close();
        return lista;
    }

    /**
     * Retorna todos os anos distintos em que houve cadastro de unidades.
     *
     * Regra de negócio:
     * - Ignora registros sem data válida.
     * - Retorna anos em ordem decrescente para facilitar seleção em relatórios.
     *
     * @return lista de anos disponíveis para filtro
     * @throws Exception em caso de erro SQL
     */
    public List<Integer> listarAnosCadastro() throws Exception {

        List<Integer> anos = new ArrayList<>();

        String sql = """
            SELECT DISTINCT YEAR(data_insercao) AS ano
            FROM unidade
            WHERE data_insercao IS NOT NULL
              AND YEAR(data_insercao) > 0
            ORDER BY ano DESC
        """;

        PreparedStatement stmt = conexao.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();

        // Itera sobre resultados agregados
        while (rs.next()) {
            anos.add(rs.getInt("ano"));
        }

        rs.close();
        stmt.close();
        return anos;
    }

    /**
     * Retorna a quantidade de unidades cadastradas por mês em determinado ano.
     *
     * Regra de negócio:
     * - Apenas registros com data válida são considerados.
     * - Resultado é ordenado cronologicamente pelo mês.
     *
     * Estrutura retornada:
     * - Chave: número do mês (1 a 12)
     * - Valor: quantidade de registros naquele mês
     *
     * @param ano ano utilizado como filtro estatístico
     * @return mapa contendo distribuição mensal de cadastros
     * @throws Exception em caso de erro SQL
     */
    public Map<Integer, Integer> quantidadeUnidadesPorMes(int ano) throws Exception {

        Map<Integer, Integer> dados = new LinkedHashMap<>();

        String sql = """
            SELECT
                MONTH(data_insercao) AS mes,
                COUNT(*)             AS total
            FROM unidade
            WHERE data_insercao IS NOT NULL
              AND YEAR(data_insercao) = ?
            GROUP BY MONTH(data_insercao)
            ORDER BY mes
        """;

        PreparedStatement stmt = conexao.prepareStatement(sql);

        // Define o ano utilizado como critério de agregação
        stmt.setInt(1, ano);

        ResultSet rs = stmt.executeQuery();

        // Percorre resultados agregados preenchendo estrutura de relatório
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
}
