package br.com.patrimweb.model;

import java.sql.Timestamp;

/**
 * Classe de modelo (Model) responsável por representar a entidade Usuario
 * dentro do sistema PatrimWeb.
 *
 * Objetivo:
 * - Armazenar e transportar os dados de usuários do sistema entre as camadas
 *   da aplicação (Controller, DAO e View).
 *
 * Contexto de negócio:
 * - O usuário representa uma pessoa que interage com o sistema,
 *   podendo realizar operações administrativas, movimentações
 *   patrimoniais ou autenticação.
 * - A entidade também contempla autenticação tradicional (senha)
 *   e autenticação via Google.
 *
 * Observação:
 * - Esta classe atua apenas como estrutura de dados (POJO/Entity),
 *   não possuindo lógica de acesso ao banco de dados.
 */
public class Usuario {
	
	/**
     * Identificador único do usuário.
     * Normalmente gerado automaticamente pelo banco de dados.
     */
	private int idUsu;

    /**
     * Nome completo do usuário.
     * Utilizado para identificação visual e também pode ser usado como login.
     */
    private String nomeUsu;

    /**
     * CPF do usuário.
     * Pode ser utilizado como identificador único em regras de negócio
     * administrativas ou validações externas.
     */
    private String cpfUsu;

    /**
     * Telefone de contato do usuário.
     * Usado para comunicação operacional.
     */
    private String telefoneUsu;

    /**
     * E-mail do usuário.
     * Utilizado para autenticação, comunicação e recuperação de acesso.
     */
    private String emailUsu;

    /**
     * Endereço residencial ou institucional do usuário.
     */
    private String enderecoUsu;

    /**
     * Data e hora em que o usuário foi inserido no sistema.
     * Mantida como Timestamp para compatibilidade direta com o banco.
     */
    private Timestamp dataInsercao;

    /**
     * Indica se o usuário utiliza autenticação via Google.
     *
     * Regra de negócio implícita:
     * - true  -> login federado Google habilitado
     * - false -> autenticação tradicional por senha
     */
    private Boolean LoginGoogle;

    /**
     * Senha do usuário utilizada na autenticação tradicional.
     *
     * Ponto crítico:
     * - Representa informação sensível e deve ser tratada com
     *   mecanismos seguros (ex: hash) em camadas superiores.
     */
    private String senhaUsu;
    
    /**
     * Construtor vazio.
     *
     * Finalidade:
     * - Necessário para frameworks, serialização,
     *   bibliotecas ORM e mecanismos baseados em reflexão.
     */
    public Usuario() {
    }
    
    /**
     * Construtor completo da entidade Usuario.
     *
     * @param idUsu Identificador do usuário.
     * @param nomeUsu Nome do usuário.
     * @param cpfUsu CPF do usuário.
     * @param telefoneUsu Telefone de contato.
     * @param emailUsu E-mail do usuário.
     * @param enderecoUsu Endereço do usuário.
     * @param dataInsercao Data de cadastro no sistema.
     * @param LoginGoogle Indica autenticação via Google.
     * @param senhaUsu Senha do usuário.
     *
     * Uso comum:
     * - Reconstrução de objetos vindos de consultas ao banco de dados.
     */
    public Usuario(int idUsu, String nomeUsu, String cpfUsu, String telefoneUsu, String emailUsu, String enderecoUsu, Timestamp dataInsercao, Boolean LoginGoogle, String senhaUsu) {
        this.idUsu = idUsu;
        this.nomeUsu = nomeUsu;
        this.cpfUsu = cpfUsu;
        this.telefoneUsu = telefoneUsu;
        this.emailUsu = emailUsu;
        this.enderecoUsu = enderecoUsu;
        this.dataInsercao = dataInsercao;
        this.LoginGoogle = LoginGoogle;
        this.senhaUsu = senhaUsu;
    }
    
    /**
     * Construtor utilizado para criação de novos usuários antes da persistência.
     *
     * @param nomeUsu Nome do usuário.
     * @param cpfUsu CPF do usuário.
     * @param telefoneUsu Telefone do usuário.
     * @param emailUsu E-mail do usuário.
     * @param enderecoUsu Endereço do usuário.
     * @param dataInsercao Data de cadastro.
     * @param LoginGoogle Indica login via Google.
     * @param senhaUsu Senha do usuário.
     *
     * Regra implícita:
     * - O ID não é informado pois normalmente será gerado pelo banco.
     */
    public Usuario(String nomeUsu, String cpfUsu, String telefoneUsu, String emailUsu, String enderecoUsu, Timestamp dataInsercao, Boolean LoginGoogle, String senhaUsu) {
        this.nomeUsu = nomeUsu;
        this.cpfUsu = cpfUsu;
        this.telefoneUsu = telefoneUsu;
        this.emailUsu = emailUsu;
        this.enderecoUsu = enderecoUsu;
        this.dataInsercao = dataInsercao;
        this.LoginGoogle = LoginGoogle;
        this.senhaUsu = senhaUsu;
    }
    
    /**
     * Retorna o identificador do usuário.
     *
     * @return idUsu ID único do usuário.
     */
    public int getIdUsu() {
        return idUsu;
    }
    
    /**
     * Define o identificador do usuário.
     *
     * @param idUsu Novo ID do usuário.
     *
     * Ponto crítico:
     * - Alterar manualmente após persistência pode causar inconsistência
     *   entre objeto e registro do banco.
     */
    public void setIdUsu(int idUsu) {
        this.idUsu = idUsu;
    }
    
    /**
     * Retorna o nome do usuário.
     *
     * @return nomeUsu Nome cadastrado.
     */
    public String getNomeUsu() {
        return nomeUsu;
    }
    
    /**
     * Define o nome do usuário.
     *
     * @param nomeUsu Nome completo do usuário.
     *
     * Regra de negócio:
     * - Pode ser utilizado também como login dependendo da
     *   implementação de autenticação.
     */
    public void setNomeUsu(String nomeUsu) {
        this.nomeUsu = nomeUsu;
    }
    
    /**
     * Retorna o CPF do usuário.
     *
     * @return cpfUsu CPF cadastrado.
     */
    public String getCpfUsu() {
        return cpfUsu;
    }
    
    /**
     * Define o CPF do usuário.
     *
     * @param cpfUsu CPF do usuário.
     *
     * Observação:
     * - Validações formais de CPF devem ocorrer em camadas superiores.
     */
    public void setCpfUsu(String cpfUsu) {
        this.cpfUsu = cpfUsu;
    }
    
    /**
     * Retorna o telefone do usuário.
     *
     * @return telefoneUsu Número telefônico.
     */
    public String getTelefoneUsu() {
        return telefoneUsu;
    }
    
    /**
     * Define o telefone do usuário.
     *
     * @param telefoneUsu Telefone de contato.
     */
    public void setTelefoneUsu(String telefoneUsu) {
        this.telefoneUsu = telefoneUsu;
    }
    
    /**
     * Retorna o e-mail do usuário.
     *
     * @return emailUsu Endereço eletrônico.
     */
    public String getEmailUsu() {
        return emailUsu;
    }
    
    /**
     * Define o e-mail do usuário.
     *
     * @param emailUsu E-mail do usuário.
     *
     * Importância:
     * - Utilizado como identificador de autenticação e comunicação.
     */
    public void setEmailUsu(String emailUsu) {
        this.emailUsu = emailUsu;
    }
    
    /**
     * Retorna o endereço do usuário.
     *
     * @return enderecoUsu Endereço cadastrado.
     */
    public String getEnderecoUsu() {
        return enderecoUsu;
    }
    
    /**
     * Define o endereço do usuário.
     *
     * @param enderecoUsu Endereço físico do usuário.
     */
    public void setEnderecoUsu(String enderecoUsu) {
        this.enderecoUsu = enderecoUsu;
    }
    
    /**
     * Retorna a data de inserção do usuário no sistema.
     *
     * @return dataInsercao Timestamp do cadastro.
     */
    public Timestamp getDataInsercao() {
        return dataInsercao;
    }
    
    /**
     * Define a data de inserção do usuário.
     *
     * @param dataInsercao Data e hora do cadastro.
     *
     * Observação:
     * - Geralmente definida automaticamente durante o processo de inserção.
     */
    public void setDataInsercao(Timestamp dataInsercao) {
        this.dataInsercao = dataInsercao;
    }
    
    /**
     * Retorna se o usuário utiliza autenticação via Google.
     *
     * @return Boolean indicando uso de login Google.
     */
    public Boolean getLoginGoogle() {
        return LoginGoogle;
    }
    
    /**
     * Define se o usuário utiliza login via Google.
     *
     * @param LoginGoogle Indicador de autenticação federada.
     */
    public void setLoginGoogle(Boolean LoginGoogle) {
        this.LoginGoogle = LoginGoogle;
    }
    
    /**
     * Retorna a senha do usuário.
     *
     * @return senhaUsu Senha cadastrada.
     *
     * Ponto crítico:
     * - Manipulação de senha deve seguir boas práticas de segurança.
     */
    public String getSenhaUsu() {
        return senhaUsu;
    }
    
    /**
     * Método responsável por definir a senha do usuário.
     *
     * @param senhaUsu Nova senha a ser atribuída.
     *
     * Observação técnica:
     * - Este método possui comportamento definido conforme implementação atual,
     *   sendo mantido sem alterações conforme regras de documentação solicitadas.
     */
    public void getSenhaUsu(String senhaUsu) {
        this.senhaUsu = enderecoUsu;
    }

}
