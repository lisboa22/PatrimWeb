package br.com.patrimweb.model;

	/**
	 * Classe de modelo responsável por representar uma Permissão
	 * dentro do sistema PatrimWeb.
	 *
	 * Objetivo:
	 * - Definir ações permitidas em cada módulo do sistema.
	 * - Controlar acesso granular às funcionalidades.
	 *
	 * Exemplo:
	 * MODULO: USUARIO
	 * ACAO: EXCLUIR
	 */
	public class Permissao {

	    /**
	     * Identificador único da permissão.
	     */
	    private int id_permissao;

	    /**
	     * Módulo do sistema ao qual a permissão pertence.
	     * Ex: USUARIO, EQUIPAMENTO, MOVIMENTACAO
	     */
	    private String modulo;

	    /**
	     * Descrição textual da permissão.
	     */
	    private String descricao;

	    /**
	     * Construtor vazio.
	     */
	    public Permissao() {
	    }

	    /**
	     * Construtor completo (SELECT).
	     */
	    public Permissao(int id_permissao, String modulo,
	                     String descricao) {
	        this.id_permissao = id_permissao;
	        this.modulo = modulo;
	        this.descricao = descricao;
	    }

	    /**
	     * Construtor para inserção.
	     */
	    public Permissao(String modulo, String descricao) {
	        this.modulo = modulo;
	        this.descricao = descricao;
	    }

	    public int getIdPermissao() {
	        return id_permissao;
	    }

	    public void setIdPermissao(int id_permissao) {
	        this.id_permissao = id_permissao;
	    }

	    public String getModulo() {
	        return modulo;
	    }

	    public void setModulo(String modulo) {
	        this.modulo = modulo;
	    }

	    public String getDescricao() {
	        return descricao;
	    }

	    public void setDescricao(String descricao) {
	        this.descricao = descricao;
	    }
	}

