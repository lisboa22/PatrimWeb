package br.com.patrimweb.dao;


//import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/*import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;*/

import br.com.patrimweb.model.Equipamento;
import br.com.patrimweb.model.Fabricante;

/**
 * Classe DAO responsável pela camada de persistência da entidade Equipamento.
 *
 * <p>
 * Responsabilidades principais:
 * - Realizar operações CRUD na tabela equipamento.
 * - Executar consultas com JOIN para obtenção de dados relacionados ao Fabricante.
 * - Aplicar filtros dinâmicos para relatórios.
 * - Fornecer dados estatísticos para relatórios (anos e quantidades por mês).
 * </p>
 *
 * <p>
 * Regras de negócio implícitas:
 * - Todo equipamento está associado a um fabricante.
 * - Datas de inserção são utilizadas para relatórios e filtros.
 * - Consultas utilizam PreparedStatement para segurança contra SQL Injection.
 * </p>
 *
 * <p>
 * Pontos críticos:
 * - A conexão é recebida externamente e não é gerenciada nesta classe.
 * - O fechamento de ResultSet e PreparedStatement é feito manualmente.
 * - Dependência direta do FabricanteDAO para recuperação de dados relacionados.
 * </p>
 */
//Classe DAO para manipulação dos produtos no banco de dados
public class EquipamentoDAO {//Declaração da classe EquipamentoDAO, responsável pelo acesso ao banco de dados para a entidade Equipamento.
	
	 /**
	  * Conexão ativa com o banco de dados utilizada por todas as operações DAO.
	  */
	 private Connection conexao; //Declaração de um atributo conexao do tipo Connection, que será usado para interagir com o banco de dados.

	 /**
	  * DAO auxiliar utilizado para recuperar informações do fabricante.
	  * Representa uma dependência entre entidades relacionadas.
	  */
	 private FabricanteDAO fabricanteDAO;

	 /**
	  * Construtor responsável por receber a conexão ativa e inicializar
	  * dependências necessárias para consultas relacionadas.
	  *
	  * @param conexao conexão JDBC já estabelecida.
	  */
	 //Construtor da classe ProdutoDAO, que recebe uma conexão com o banco de dados e a armazena no atributo conexao.
	 public EquipamentoDAO(Connection conexao) {
	     this.conexao = conexao;
	     this.fabricanteDAO = new FabricanteDAO(conexao);
	 }

	 /**
	  * Insere um novo equipamento no banco de dados.
	  *
	  * Regras aplicadas:
	  * - O equipamento deve possuir fabricante válido.
	  * - A data de inserção já deve vir definida no objeto.
	  *
	  * Interação com banco:
	  * - Executa comando INSERT utilizando PreparedStatement.
	  *
	  * @param equipamento objeto contendo os dados a serem persistidos.
	  * @throws Exception caso ocorra falha na operação SQL.
	  */
	 //Método para inserir um novo equipamento no banco de dados. Ele recebe um objeto Equipamento e pode lançar uma exceção em caso de erro.
	 public void adicionarEquipamento(Equipamento equipamento) throws Exception {
	     //Declaração da string SQL para inserir um novo produto. Os valores são representados por ?, que serão preenchidos posteriormente.
		 String sql = "INSERT INTO equipamento (nome_equip, num_serie_equip, fabricante, data_insercao) VALUES (?, ?, ?, ?)";
	     //Criação de um PreparedStatement, que é um objeto usado para executar a consulta SQL de forma segura e eficiente.
		 PreparedStatement stmt = conexao.prepareStatement(sql);

	     // Associação dos parâmetros da query com os dados do objeto de domínio
		 stmt.setString(1, equipamento.getNomeEquip());
		 stmt.setString(2, equipamento.getNumSerieEquip());
	     stmt.setInt (3, equipamento.getFabricante().getIdFab());
	     stmt.setTimestamp(4, equipamento.getDataInsercao());

	     // Executa inserção no banco
	     stmt.executeUpdate(); //Executa o update 

	     // Liberação explícita do recurso JDBC
	     stmt.close(); //fecha o objeto e libera recurso de maquina
	 }

	 /**
	  * Retorna todos os equipamentos cadastrados juntamente com seus fabricantes.
	  *
	  * Estratégia utilizada:
	  * - INNER JOIN entre equipamento e fabricante para evitar múltiplas consultas.
	  *
	  * Estrutura relevante:
	  * - Loop while responsável por mapear cada registro do ResultSet
	  *   para objetos de domínio.
	  *
	  * @return lista de equipamentos encontrados no banco.
	  * @throws Exception erro durante execução da consulta.
	  */
	 //Método que retorna uma lista de todos os produtos do banco de dados. Pode lançar uma exceção em caso de erro.
	 public List<Equipamento> listarEquipamentos() throws Exception {
		    List<Equipamento> equipamentos = new ArrayList<>();
		    
		    // SQL com INNER JOIN para buscar dados do Equipamento e do Fabricante juntos
		    String sql = "SELECT e.*, f.nome_fab FROM equipamento e " +
		                 "INNER JOIN fabricante f ON e.fabricante = f.id_fab ORDER BY e.id_equip ASC";
		    
		    PreparedStatement stmt = conexao.prepareStatement(sql);
		    ResultSet rs = stmt.executeQuery();

		    // Percorre todos os registros retornados pela consulta
		    while (rs.next()) {

		        // Construção do objeto Fabricante baseado nos dados do JOIN
		        Fabricante fabricante = new Fabricante();

		        // IMPORTANTE: coluna "fabricante" representa a FK na tabela equipamento
		        fabricante.setIdFab(rs.getInt("fabricante")); 
		        fabricante.setNomeFab(rs.getString("nome_fab"));

		        // Mapeamento do registro para objeto Equipamento
		        Equipamento equipamento = new Equipamento(
		            rs.getInt("id_equip"), 
		            rs.getString("nome_equip"),
		            rs.getString("num_serie_equip"),
		            fabricante, 
		            rs.getTimestamp("data_insercao")
		        );

		        // Adiciona objeto à lista de retorno
		        equipamentos.add(equipamento);
		    }

		    rs.close();
		    stmt.close();

		    return equipamentos;
		}

	 /**
	  * Atualiza os dados de um equipamento existente.
	  *
	  * Regra:
	  * - A atualização ocorre com base no id_equip.
	  *
	  * @param equipamento objeto contendo novos dados.
	  * @throws Exception erro durante execução SQL.
	  */
	 //Método para atualizar os dados de um produto no banco de dados.
	 public void alterarEquipamento(Equipamento equipamento) throws Exception {
	     //Declaração da string SQL para atualizar um produto específico com base no seu idEquip.
		 String sql = "UPDATE equipamento SET nome_equip = ?, num_serie_equip = ?, fabricante = ? WHERE id_equip = ?";
	     //Preenchimento dos parâmetros ? com os valores do objeto Equipamento.
		 PreparedStatement stmt = conexao.prepareStatement(sql);
	     stmt.setString(1, equipamento.getNomeEquip());
	     stmt.setString(2, equipamento.getNumSerieEquip());
	     stmt.setInt(3, equipamento.getFabricante().getIdFab());
	     stmt.setInt(4, equipamento.getIdEquip());

	     // Execução da atualização no banco
	     stmt.executeUpdate();

	     // Liberação do recurso JDBC
	     stmt.close();
	 }

	 /**
	  * Remove um equipamento do banco de dados pelo ID.
	  *
	  * @param id identificador do equipamento.
	  * @throws Exception erro durante execução SQL.
	  */
	 //Método para excluir um produto do banco de dados com base no seu id.
	 public void excluirEquipamento(int id) throws Exception {
	     //Declaração da string SQL para deletar um produto específico recebendo o id do equipamento como chave de busca
		 String sql = "DELETE FROM equipamento WHERE id_equip = ?";
	     PreparedStatement stmt = conexao.prepareStatement(sql);

	     // Definição do parâmetro da query
	     stmt.setInt(1, id);

	     // Execução da exclusão
	     stmt.executeUpdate();

	     stmt.close();
	 }
	 
	 /**
	  * Busca um equipamento específico pelo ID.
	  *
	  * Regra importante:
	  * - Após recuperar o equipamento, o fabricante é buscado separadamente
	  *   utilizando FabricanteDAO, garantindo objeto completo.
	  *
	  * @param id identificador do equipamento.
	  * @return Equipamento encontrado ou null caso não exista.
	  * @throws Exception erro durante consulta SQL.
	  */
	// Método para buscar um equipamento no banco de dados com base no seu ID
	 public Equipamento buscarPorId(int id) throws Exception {

	     Equipamento equipamento = null;

	     // SQL para buscar um equipamento específico pelo id_equip
	     String sql = "SELECT * FROM equipamento WHERE id_equip = ?";

	     // PreparedStatement para executar a consulta de forma segura
	     PreparedStatement stmt = conexao.prepareStatement(sql);

	     // Define o valor do ID no parâmetro ?
	     stmt.setInt(1, id);

	     // Executa a consulta
	     ResultSet rs = stmt.executeQuery();

	     // Estrutura condicional para verificar existência de registro
	     if (rs.next()) {

	         // Busca o fabricante associado ao equipamento
	         int idFabricante = rs.getInt("fabricante");
	         Fabricante fabricante = fabricanteDAO.buscarPorId(idFabricante);

	         // Criação do objeto Equipamento com dados recuperados
	         equipamento = new Equipamento(
	             rs.getInt("id_equip"),
	             rs.getString("nome_equip"),
	             rs.getString("num_serie_equip"),
	             fabricante,
	             rs.getTimestamp("data_insercao")
	         );
	     }

	     return equipamento; // <- aqui
	 }

	 /**
	  * Filtra equipamentos dinamicamente conforme parâmetros informados.
	  *
	  * Regras:
	  * - Apenas filtros informados são adicionados à query.
	  * - Utiliza WHERE 1=1 para facilitar concatenação condicional.
	  *
	  * Estruturas relevantes:
	  * - Construção dinâmica de SQL com StringBuilder.
	  * - Controle de índices de parâmetros para PreparedStatement.
	  *
	  * @param dataInicio data mínima de inserção.
	  * @param dataFim data máxima de inserção.
	  * @param nomeEquip filtro parcial por nome.
	  * @param fabricanteId filtro por fabricante.
	  *
	  * @return lista de equipamentos filtrados.
	  * @throws Exception erro durante consulta.
	  */
	 public List<Equipamento> filtrarEquipamentos(
	         Timestamp dataInicio,
	         Timestamp dataFim,
	         String nomeEquip,
	         Integer fabricanteId
	 ) throws Exception {

	     List<Equipamento> lista = new ArrayList<>();

	     StringBuilder sql = new StringBuilder();
	     sql.append("SELECT e.*, f.nome_fab FROM equipamento e ");
	     sql.append("INNER JOIN fabricante f ON e.fabricante = f.id_fab ");
	     sql.append("WHERE 1=1 ");

	     // Inclusão condicional dos filtros conforme parâmetros recebidos
	     if (dataInicio != null) {
	         sql.append(" AND e.data_insercao >= ? ");
	     }

	     if (dataFim != null) {
	         sql.append(" AND e.data_insercao <= ? ");
	     }

	     if (nomeEquip != null && !nomeEquip.trim().isEmpty()) {
	         sql.append(" AND e.nome_equip LIKE ? ");
	     }

	     if (fabricanteId != null) {
	         sql.append(" AND e.fabricante = ? ");
	     }

	     PreparedStatement stmt = conexao.prepareStatement(sql.toString());
	     int index = 1;

	     // Associação dinâmica dos parâmetros respeitando ordem de inclusão
	     if (dataInicio != null) {
	         stmt.setTimestamp(index++, dataInicio);
	     }

	     if (dataFim != null) {
	         stmt.setTimestamp(index++, dataFim);
	     }

	     if (nomeEquip != null && !nomeEquip.trim().isEmpty()) {
	         stmt.setString(index++, "%" + nomeEquip.trim() + "%");
	     }

	     if (fabricanteId != null) {
	         stmt.setInt(index++, fabricanteId);
	     }

	     ResultSet rs = stmt.executeQuery();

	     // Mapeamento dos resultados para objetos de domínio
	     while (rs.next()) {
	         Fabricante fabricante = new Fabricante();
	         fabricante.setIdFab(rs.getInt("fabricante"));
	         fabricante.setNomeFab(rs.getString("nome_fab"));

	         Equipamento equipamento = new Equipamento(
	             rs.getInt("id_equip"),
	             rs.getString("nome_equip"),
	             rs.getString("num_serie_equip"),
	             fabricante,
	             rs.getTimestamp("data_insercao")
	         );
	         lista.add(equipamento);
	     }

	     rs.close();
	     stmt.close();

	     return lista;
	 }

	 /**
	  * Retorna todos os anos distintos em que houve cadastro de equipamentos.
	  *
	  * Utilizado principalmente para relatórios estatísticos.
	  *
	  * @return lista de anos ordenados de forma decrescente.
	  * @throws Exception erro durante consulta SQL.
	  */
	 public List<Integer> listarAnosCadastro() throws Exception {

	     List<Integer> anos = new ArrayList<>();

	     String sql = """
	         SELECT DISTINCT YEAR(data_insercao) AS ano
	         FROM equipamento
	         WHERE data_insercao IS NOT NULL
	         AND YEAR(data_insercao) > 0
	         ORDER BY ano DESC
	     """;

	     PreparedStatement stmt = conexao.prepareStatement(sql);
	     ResultSet rs = stmt.executeQuery();

	     // Loop responsável por coletar todos os anos encontrados
	     while (rs.next()) {
	         anos.add(rs.getInt("ano"));
	     }

	     rs.close();
	     stmt.close();

	     return anos;
	 }

	 /**
	  * Retorna a quantidade de equipamentos cadastrados por mês em determinado ano.
	  *
	  * Regra:
	  * - Apenas registros com data válida são considerados.
	  *
	  * Estrutura retornada:
	  * - Map onde chave = mês e valor = quantidade de cadastros.
	  *
	  * @param ano ano de referência para o relatório.
	  * @return mapa contendo quantidade por mês.
	  * @throws Exception erro durante consulta SQL.
	  */
	 public Map<Integer, Integer> quantidadeEquipamentosPorMes(int ano) throws Exception {

	     Map<Integer, Integer> dados = new LinkedHashMap<>();

	     String sql = """
	         SELECT 
	             MONTH(data_insercao) AS mes,
	             COUNT(*) AS total
	         FROM equipamento
	         WHERE data_insercao IS NOT NULL
	           AND YEAR(data_insercao) = ?
	         GROUP BY MONTH(data_insercao)
	         ORDER BY mes
	     """;

	     PreparedStatement stmt = conexao.prepareStatement(sql);
	     stmt.setInt(1, ano);

	     ResultSet rs = stmt.executeQuery();

	     // Percorre resultados agregados do GROUP BY
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
