package br.com.patrimweb.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import br.com.patrimweb.model.Fabricante;

/**
 * Classe DAO (Data Access Object) responsável por realizar
 * todas as operações de persistência da entidade Fabricante.
 *
 * Responsabilidades:
 * - Inserção, atualização, remoção e consulta de fabricantes.
 * - Execução de consultas específicas para relatórios.
 * - Interação direta com o banco de dados utilizando JDBC.
 *
 * Regras de negócio implícitas:
 * - O controle de datas de inserção é persistido no banco.
 * - Consultas de relatório utilizam filtros opcionais.
 * - Métodos retornam objetos de domínio já mapeados.
 *
 * Pontos críticos:
 * - Dependência direta de uma conexão válida com o banco.
 * - Uso de PreparedStatement para evitar SQL Injection.
 */
public class FabricanteDAO {

	private Connection conexao; // Conexão ativa utilizada para executar operações SQL no banco de dados.

	/**
	 * Construtor responsável por receber e armazenar a conexão
	 * que será utilizada por todos os métodos DAO.
	 *
	 * @param conexao conexão JDBC previamente criada.
	 */
	 public FabricanteDAO(Connection conexao) {
	     this.conexao = conexao;
	 }

	/**
	 * Insere um novo fabricante na base de dados.
	 *
	 * Regra de negócio:
	 * - O nome e a data de inserção devem ser fornecidos pelo objeto Fabricante.
	 * - A data representa o momento do cadastro do registro.
	 *
	 * Interação com banco:
	 * - Executa comando INSERT na tabela fabricante.
	 *
	 * @param fabricante objeto contendo os dados a serem persistidos.
	 * @throws Exception caso ocorra erro de acesso ao banco.
	 */
	 public void adicionarFabricante(Fabricante fabricante) throws Exception {
		 // SQL parametrizado para inserção segura de dados.
		 String sql = "INSERT INTO fabricante (nome_fab, data_insercao) VALUES (?, ?)";
		 
		 // PreparedStatement evita SQL Injection e melhora performance.
		 PreparedStatement stmt = conexao.prepareStatement(sql);

		 // Associação dos parâmetros da query com os atributos do objeto.
		 stmt.setString(1, fabricante.getNomeFab());
	     stmt.setTimestamp(2, fabricante.getDataInsercao());

	     // Executa operação de escrita no banco.
	     stmt.executeUpdate();

	     // Liberação explícita de recurso JDBC.
	     stmt.close();
	 }

	/**
	 * Recupera todos os fabricantes cadastrados no banco.
	 *
	 * Regra de negócio:
	 * - Não há filtros aplicados.
	 * - Todos os registros são carregados e convertidos em objetos Fabricante.
	 *
	 * Estrutura relevante:
	 * - Loop while percorre o ResultSet realizando o mapeamento objeto-relacional.
	 *
	 * @return Lista contendo todos os fabricantes cadastrados.
	 * @throws Exception caso ocorra erro na consulta.
	 */
	 public List<Fabricante> listarFabricantes() throws Exception {
	     List<Fabricante> fabricantes = new ArrayList<>();

	     // Consulta simples retornando todos os registros da tabela.
	     String sql = "SELECT * FROM fabricante";

	     PreparedStatement stmt = conexao.prepareStatement(sql);
	     ResultSet rs = stmt.executeQuery();

	     // Percorre cada linha retornada e transforma em objeto de domínio.
	     while (rs.next()) {

	    	 // Mapeamento direto das colunas do banco para o objeto Fabricante.
	    	 Fabricante fabricante = new Fabricante(
	    			 rs.getInt("id_fab"),
	    			 rs.getString("nome_fab"),
	    			 rs.getTimestamp("data_insercao")
	    	 );
	         fabricantes.add(fabricante);
	     }

	     // Fechamento dos recursos após uso.
	     rs.close();
	     stmt.close();

	     return fabricantes;
	 }

	/**
	 * Atualiza os dados de um fabricante existente.
	 *
	 * Regra de negócio:
	 * - Apenas o nome do fabricante é atualizado.
	 * - A identificação ocorre através do id_fab.
	 *
	 * @param fabricante objeto contendo id e novo nome.
	 * @throws Exception erro durante execução SQL.
	 */
	 public void alterarFabricante(Fabricante fabricante) throws Exception {
		 String sql = "UPDATE fabricante SET nome_fab = ? WHERE id_fab = ?";

		 PreparedStatement stmt = conexao.prepareStatement(sql);

		 // Define os valores que substituirão os parâmetros da query.
	     stmt.setString(1, fabricante.getNomeFab());
	     stmt.setInt(2, fabricante.getIdFab());

	     // Executa atualização no banco.
	     stmt.executeUpdate();
	     stmt.close();
	 }

	/**
	 * Remove um fabricante do banco de dados.
	 *
	 * Regra de negócio:
	 * - Exclusão baseada exclusivamente no identificador.
	 *
	 * Ponto crítico:
	 * - Pode falhar caso existam registros dependentes via chave estrangeira.
	 *
	 * @param id identificador do fabricante.
	 * @throws Exception erro durante execução SQL.
	 */
	 public void excluirFabricante(int id) throws Exception {
		 String sql = "DELETE FROM fabricante WHERE id_fab = ?";

	     PreparedStatement stmt = conexao.prepareStatement(sql);

	     // Define o id utilizado como critério de exclusão.
	     stmt.setInt(1, id);

	     stmt.executeUpdate();
	     stmt.close();
	 }
	 
	 
	/**
	 * Busca um fabricante específico pelo seu identificador.
	 *
	 * Regra de negócio:
	 * - Retorna apenas um registro, pois o id é único.
	 *
	 * Estrutura relevante:
	 * - Uso de if(rs.next()) pois espera-se no máximo um resultado.
	 *
	 * @param id identificador do fabricante.
	 * @return objeto Fabricante encontrado ou null caso não exista.
	 * @throws Exception erro durante consulta ao banco.
	 */
  	 public Fabricante buscarPorId(int id) throws Exception {
  		 Fabricante fabricante = null;

  		 String sql = "SELECT * FROM fabricante WHERE id_fab = ?";

  		 PreparedStatement stmt = conexao.prepareStatement(sql);

  		 // Define o parâmetro de busca.
  		 stmt.setInt(1, id);

  		 ResultSet rs = stmt.executeQuery();

  		 // Verifica existência de registro retornado.
  		 if (rs.next()) {
  			 fabricante = new Fabricante(
  				 rs.getInt("id_fab"), 
  				 rs.getString("nome_fab"), 
  				 rs.getTimestamp("data_insercao")
  			 );
  		 }

  		 // Retorna null caso não encontrado.
  		 return fabricante;
  	 }
  	 
  	 
  	 // ============================================
  	 // MÉTODOS PARA RELATÓRIOS
  	 // ============================================
  	 
  	 /**
  	  * Filtra fabricantes utilizando critérios opcionais.
  	  *
  	  * Regras de negócio:
  	  * - Os filtros são aplicados apenas quando possuem valor.
  	  * - A cláusula "WHERE 1=1" permite adicionar condições dinamicamente.
  	  *
  	  * Validações:
  	  * - Nome só é aplicado se não for nulo nem vazio.
  	  *
  	  * Pontos críticos:
  	  * - Controle manual do índice dos parâmetros da query dinâmica.
  	  *
  	  * @param dataInicio data mínima de cadastro.
  	  * @param dataFim data máxima de cadastro.
  	  * @param nome nome parcial do fabricante.
  	  * @return lista de fabricantes filtrados.
  	  * @throws Exception erro durante execução SQL.
  	  */
  	 public List<Fabricante> filtrarFabricantes(
  		        Timestamp dataInicio,
  		        Timestamp dataFim,
  		        String nome
  		) throws Exception {

  		    List<Fabricante> lista = new ArrayList<>();

  		    StringBuilder sql = new StringBuilder();
  		    sql.append("SELECT * FROM fabricante WHERE 1=1 ");

  		    // Inclusão condicional dos filtros conforme parâmetros informados.
  		    if (dataInicio != null) {
  		        sql.append(" AND data_insercao >= ? ");
  		    }

  		    if (dataFim != null) {
  		        sql.append(" AND data_insercao <= ? ");
  		    }

  		    if (nome != null && !nome.trim().isEmpty()) {
  		        sql.append(" AND nome_fab LIKE ? ");
  		    }

  		    PreparedStatement stmt = conexao.prepareStatement(sql.toString());
  		    int index = 1;

  		    // Associação dinâmica dos parâmetros conforme ordem adicionada.
  		    if (dataInicio != null) {
  		        stmt.setTimestamp(index++, dataInicio);
  		    }

  		    if (dataFim != null) {
  		        stmt.setTimestamp(index++, dataFim);
  		    }

  		    if (nome != null && !nome.trim().isEmpty()) {
  		        stmt.setString(index++, "%" + nome.trim() + "%");
  		    }

  		    ResultSet rs = stmt.executeQuery();

  		    // Mapeamento dos registros retornados para objetos Fabricante.
  		    while (rs.next()) {
  		        Fabricante fabricante = new Fabricante(
  		            rs.getInt("id_fab"),
  		            rs.getString("nome_fab"),
  		            rs.getTimestamp("data_insercao")
  		        );
  		        lista.add(fabricante);
  		    }

  		    rs.close();
  		    stmt.close();

  		    return lista;
  		}
  	 
  	 
  	 /**
  	  * Retorna todos os anos distintos em que houve cadastro de fabricantes.
  	  *
  	  * Regra de negócio:
  	  * - Apenas registros com data válida são considerados.
  	  * - Anos são retornados em ordem decrescente.
  	  *
  	  * @return lista de anos de cadastro existentes.
  	  * @throws Exception erro durante consulta.
  	  */
  	 public List<Integer> listarAnosCadastro() throws Exception {

  		    List<Integer> anos = new ArrayList<>();

  		    String sql = """
  		        SELECT DISTINCT YEAR(data_insercao) AS ano
  		        FROM fabricante
  		        WHERE data_insercao IS NOT NULL
  		        AND YEAR(data_insercao) > 0
  		        ORDER BY ano DESC
  		    """;

  		    PreparedStatement stmt = conexao.prepareStatement(sql);
  		    ResultSet rs = stmt.executeQuery();

  		    // Cada linha representa um ano distinto encontrado.
  		    while (rs.next()) {
  		        anos.add(rs.getInt("ano"));
  		    }

  		    rs.close();
  		    stmt.close();

  		    return anos;
  		}

  	 
  	 /**
  	  * Calcula a quantidade de fabricantes cadastrados por mês
  	  * em um determinado ano.
  	  *
  	  * Regra de negócio:
  	  * - Apenas registros com data válida são considerados.
  	  * - O agrupamento é realizado por mês.
  	  *
  	  * Estrutura relevante:
  	  * - Uso de LinkedHashMap mantém a ordem cronológica dos meses.
  	  *
  	  * @param ano ano utilizado como filtro.
  	  * @return mapa onde:
  	  *         chave = mês (1–12)
  	  *         valor = quantidade de cadastros.
  	  * @throws Exception erro durante consulta SQL.
  	  */
  	 public Map<Integer, Integer> quantidadeFabricantesPorMes(int ano) throws Exception {

  		    Map<Integer, Integer> dados = new LinkedHashMap<>();

  		    String sql = """
  		        SELECT 
  		            MONTH(data_insercao) AS mes,
  		            COUNT(*) AS total
  		        FROM fabricante
  		        WHERE data_insercao IS NOT NULL
  		          AND YEAR(data_insercao) = ?
  		        GROUP BY MONTH(data_insercao)
  		        ORDER BY mes
  		    """;

  		    PreparedStatement stmt = conexao.prepareStatement(sql);

  		    // Define o ano utilizado no filtro da consulta.
  		    stmt.setInt(1, ano);

  		    ResultSet rs = stmt.executeQuery();

  		    // Cada linha representa um mês e sua respectiva quantidade.
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
