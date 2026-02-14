package br.com.patrimweb.model;

import java.sql.Timestamp;

/**
 * Classe de modelo (Model) responsável por representar a entidade Fabricante
 * dentro do sistema PatrimWeb.
 *
 * Objetivo:
 * - Encapsular os dados relacionados aos fabricantes de equipamentos.
 * - Servir como objeto de transferência de dados entre as camadas
 *   Controller, DAO e View.
 *
 * Regras de negócio implícitas:
 * - Cada fabricante possui um identificador único.
 * - O nome do fabricante identifica a origem ou marca do equipamento.
 * - A data de inserção registra o momento em que o fabricante foi
 *   cadastrado no sistema.
 *
 * Observação:
 * Esta classe não contém regras de acesso ao banco de dados nem lógica
 * de processamento, sendo utilizada apenas como estrutura de dados.
 */
public class Fabricante {

	/**
     * Identificador único do fabricante.
     * Geralmente gerado automaticamente pelo banco de dados.
     */
	private int id_fab;

    /**
     * Nome do fabricante.
     * Representa a marca ou empresa responsável pelo equipamento.
     */
    private String nome_fab;

    /**
     * Data e hora em que o fabricante foi inserido no sistema.
     * Utiliza Timestamp para compatibilidade direta com campos
     * temporais do banco de dados.
     */
    private Timestamp data_insercao;
    
    /**
     * Construtor vazio.
     *
     * Finalidade:
     * - Necessário para frameworks, bibliotecas de serialização,
     *   JSP/EL e mecanismos baseados em reflexão que exigem
     *   um construtor padrão.
     */
    public Fabricante() {
    }
    
    /**
     * Construtor completo da entidade Fabricante.
     *
     * @param id_fab Identificador único do fabricante.
     * @param nome_fab Nome do fabricante.
     * @param data_insercao Data e hora do cadastro do fabricante.
     *
     * Uso comum:
     * - Reconstrução do objeto a partir de registros retornados
     *   pelo banco de dados (operações SELECT).
     */
    public Fabricante(int id_fab, String nome_fab, Timestamp data_insercao) {
        this.id_fab = id_fab;
        this.nome_fab = nome_fab;
        this.data_insercao = data_insercao;
    }
    
    /**
     * Construtor utilizado para inserção de novos fabricantes no banco.
     *
     * @param nome_fab Nome do fabricante.
     * @param data_insercao Data e hora do cadastro.
     *
     * Regra implícita:
     * - O ID não é informado pois normalmente é gerado automaticamente
     *   pelo banco de dados (auto increment).
     */
    public Fabricante(String nome_fab, Timestamp data_insercao) {
        this.nome_fab = nome_fab;
        this.data_insercao = data_insercao;
    }
    
    /**
     * Retorna o identificador do fabricante.
     *
     * @return id_fab ID único do fabricante.
     */
    public int getIdFab() {
        return id_fab;
    }
    
    /**
     * Define o identificador do fabricante.
     *
     * @param id_fab Novo ID do fabricante.
     *
     * Ponto crítico:
     * - Alterar manualmente o ID pode gerar inconsistências
     *   caso o valor seja controlado pelo banco de dados.
     */
    public void setIdFab(int id_fab) {
        this.id_fab = id_fab;
    }
    
    /**
     * Retorna o nome do fabricante.
     *
     * @return nome_fab Nome do fabricante.
     */
    public String getNomeFab() {
        return nome_fab;
    }
    
    /**
     * Define o nome do fabricante.
     *
     * @param nome_fab Nome a ser atribuído ao fabricante.
     *
     * Regra implícita:
     * - O nome deve identificar claramente o fabricante
     *   para fins de cadastro e relacionamento com equipamentos.
     */
    public void setNomeFab(String nome_fab) {
        this.nome_fab = nome_fab;
    }
    
    /**
     * Retorna a data de inserção do fabricante.
     *
     * @return data_insercao Timestamp contendo data e hora do cadastro.
     */
    public Timestamp getDataInsercao() {
        return data_insercao;
    }
    
    /**
     * Define a data de inserção do fabricante.
     *
     * @param data_insercao Data e hora a serem atribuídas ao registro.
     *
     * Observação:
     * - Normalmente preenchida automaticamente no momento
     *   da persistência no banco de dados.
     */
    public void setDataInsercao(Timestamp data_insercao) {
        this.data_insercao = data_insercao;
    }

}
