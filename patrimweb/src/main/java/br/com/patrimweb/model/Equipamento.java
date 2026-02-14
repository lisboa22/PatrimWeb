package br.com.patrimweb.model;

import java.sql.Timestamp;

/**
 * Classe de modelo (Model) responsável por representar a entidade Equipamento
 * dentro do sistema PatrimWeb.
 *
 * Objetivo:
 * - Encapsular os dados relacionados a um equipamento cadastrado.
 * - Servir como objeto de transferência de dados (DTO/Entity) entre as
 *   camadas Controller, DAO e View.
 *
 * Regras de negócio implícitas:
 * - Cada equipamento possui um identificador único (id_equip).
 * - Um equipamento está associado a um Fabricante.
 * - A data de inserção registra o momento em que o equipamento foi cadastrado
 *   no sistema ou persistido no banco de dados.
 *
 * Observação:
 * Esta classe não contém lógica de negócio ou acesso ao banco,
 * sendo utilizada exclusivamente para modelagem de dados.
 */
public class Equipamento {

	/**
     * Identificador único do equipamento.
     * Normalmente gerado pelo banco de dados.
     */
	private int id_equip;

    /**
     * Nome descritivo do equipamento.
     */
    private String nome_equip;

    /**
     * Objeto que representa o fabricante associado ao equipamento.
     * Define um relacionamento entre entidades do domínio.
     */
    private Fabricante fabricante;

    /**
     * Data e hora em que o equipamento foi inserido no sistema.
     * Utiliza Timestamp para compatibilidade direta com tipos DATETIME/TIMESTAMP
     * do banco de dados.
     */
    private Timestamp data_insercao;
    
    /**
     * Construtor vazio.
     *
     * Finalidade:
     * - Necessário para frameworks, bibliotecas de serialização,
     *   JSP/EL e mecanismos de reflexão que exigem um construtor padrão.
     */
    public Equipamento() {
    }
    
    /**
     * Construtor completo da entidade Equipamento.
     *
     * @param id_equip Identificador único do equipamento.
     * @param nome_equip Nome do equipamento.
     * @param fabricante Objeto Fabricante associado.
     * @param data_insercao Data e hora de inserção do registro.
     *
     * Uso comum:
     * - Reconstrução de objetos vindos do banco de dados (SELECT).
     */
    public Equipamento(int id_equip, String nome_equip, Fabricante fabricante, 
                       Timestamp data_insercao) {
        this.id_equip = id_equip;
        this.nome_equip = nome_equip;
        this.fabricante = fabricante;
        this.data_insercao = data_insercao;
    }
    
    /**
     * Construtor utilizado quando a data de inserção não é necessária
     * no momento da criação do objeto.
     *
     * @param id_equip Identificador do equipamento.
     * @param nome_equip Nome do equipamento.
     * @param fabricante Fabricante associado.
     *
     * Cenário comum:
     * - Operações intermediárias onde a data é definida posteriormente
     *   pelo sistema ou banco de dados.
     */
    public Equipamento(int id_equip, String nome_equip, Fabricante fabricante) {
        this.id_equip = id_equip;
        this.nome_equip = nome_equip;
        this.fabricante = fabricante;
    }
    
    /**
     * Construtor utilizado para inserção de novos registros no banco.
     *
     * @param nome_equip Nome do equipamento.
     * @param fabricante Fabricante associado.
     * @param data_insercao Data de inserção do registro.
     *
     * Regra implícita:
     * - O ID não é informado pois normalmente é gerado automaticamente
     *   pelo banco de dados (auto increment).
     */
    public Equipamento(String nome_equip, Fabricante fabricante, Timestamp data_insercao) {
        this.nome_equip = nome_equip;
        this.fabricante = fabricante;
        this.data_insercao = data_insercao;
    }
    
    /**
     * Retorna o identificador do equipamento.
     *
     * @return id_equip ID único do equipamento.
     */
    public int getIdEquip() {
        return id_equip;
    }
    
    /**
     * Define o identificador do equipamento.
     *
     * @param id_equip Novo ID do equipamento.
     *
     * Ponto crítico:
     * - Alterar o ID manualmente pode causar inconsistências caso
     *   o valor seja controlado pelo banco de dados.
     */
    public void setIdEquip(int id_equip) {
        this.id_equip = id_equip;
    }
    
    /**
     * Retorna o nome do equipamento.
     *
     * @return nome_equip Nome descritivo do equipamento.
     */
    public String getNomeEquip() {
        return nome_equip;
    }
    
    /**
     * Define o nome do equipamento.
     *
     * @param nome_equip Nome a ser atribuído ao equipamento.
     *
     * Regra implícita:
     * - Espera-se que o nome represente de forma clara o equipamento
     *   para identificação no sistema.
     */
    public void setNomeEquip(String nome_equip) {
        this.nome_equip = nome_equip;
    }
    
    /**
     * Retorna o fabricante associado ao equipamento.
     *
     * @return fabricante Objeto Fabricante vinculado.
     */
    public Fabricante getFabricante() {
        return fabricante;
    }
    
    /**
     * Define o fabricante do equipamento.
     *
     * @param fabricante Objeto Fabricante a ser associado.
     *
     * Regra de negócio:
     * - Um equipamento deve possuir um fabricante válido para manter
     *   integridade relacional no sistema.
     */
    public void setFabricante(Fabricante fabricante) {
        this.fabricante = fabricante;
    }
    
    /**
     * Retorna a data de inserção do equipamento.
     *
     * @return data_insercao Timestamp contendo data e hora do cadastro.
     */
    public Timestamp getDataInsercao() {
        return data_insercao;
    }
    
    /**
     * Define a data de inserção do equipamento.
     *
     * @param data_insercao Data e hora a serem atribuídas ao registro.
     *
     * Observação:
     * - Geralmente preenchido automaticamente no momento da persistência
     *   no banco de dados.
     */
    public void setDataInsercao(Timestamp data_insercao) {
        this.data_insercao = data_insercao;
    }
}
