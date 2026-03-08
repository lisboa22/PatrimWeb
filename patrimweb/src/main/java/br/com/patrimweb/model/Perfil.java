package br.com.patrimweb.model;

	/**
	 * Classe de modelo responsável por representar o Perfil de acesso
	 * dentro do sistema PatrimWeb.
	 *
	 * Objetivo:
	 * - Definir o papel (role) de um usuário no sistema.
	 * - Agrupar permissões que serão herdadas pelos usuários.
	 *
	 * Exemplos de perfis:
	 * - ADMINISTRADOR
	 * - TECNICO
	 * - OPERADOR
	 */
	public class Perfil {

	    /**
	     * Identificador único do perfil.
	     */
	    private int id_perfil;

	    /**
	     * Nome do perfil.
	     * Representa o tipo de acesso do usuário.
	     */
	    private String nome_perfil;

	    /**
	     * Construtor vazio.
	     * Necessário para frameworks e mecanismos de reflexão.
	     */
	    public Perfil() {
	    }

	    /**
	     * Construtor completo.
	     * Utilizado ao reconstruir o objeto vindo do banco (SELECT).
	     */
	    public Perfil(int id_perfil, String nome_perfil) {
	        this.id_perfil = id_perfil;
	        this.nome_perfil = nome_perfil;
	    }

	    /**
	     * Construtor para inserção.
	     */
	    public Perfil(String nome_perfil) {
	        this.nome_perfil = nome_perfil;
	    }

	    public int getIdPerfil() {
	        return id_perfil;
	    }

	    public void setIdPerfil(int id_perfil) {
	        this.id_perfil = id_perfil;
	    }

	    public String getNomePerfil() {
	        return nome_perfil;
	    }

	    public void setNomePerfil(String nome_perfil) {
	        this.nome_perfil = nome_perfil;
	    }
	}
	

