package br.com.patrimweb.model;

import java.sql.Timestamp;

/**
 * Classe de modelo (Model) responsável por representar uma Unidade
 * dentro do sistema PatrimWeb.
 *
 * Objetivo:
 * - Armazenar informações das unidades organizacionais onde equipamentos
 *   podem estar localizados ou vinculados.
 * - Servir como entidade base para movimentações patrimoniais,
 *   permitindo identificar origem e destino de equipamentos.
 *
 * Contexto de negócio:
 * - Uma unidade pode representar escolas, postos de saúde,
 *   setores administrativos ou qualquer local físico gerenciado.
 * - É utilizada em processos de movimentação, controle patrimonial
 *   e rastreabilidade logística.
 *
 * Observação:
 * Esta classe atua apenas como estrutura de dados (Entity/DTO),
 * não contendo regras de persistência ou acesso direto ao banco.
 */
public class Unidade {

	/**
     * Identificador único da unidade.
     * Geralmente gerado automaticamente pelo banco de dados.
     */
	private int idUnid;

    /**
     * Nome da unidade.
     * Representa a identificação principal utilizada nas operações do sistema.
     */
    private String nomeUnid;

    /**
     * Telefone de contato da unidade.
     * Utilizado para comunicação operacional e administrativa.
     */
    private String telefoneUnid;

    /**
     * E-mail institucional da unidade.
     * Pode ser utilizado para notificações e comunicações formais.
     */
    private String emailUnid;

    /**
     * Endereço físico da unidade.
     * Importante para localização logística e auditoria patrimonial.
     */
    private String enderecoUnid;

    /**
     * Data e hora de inserção do registro no sistema.
     * Utiliza Timestamp para compatibilidade direta com campos DATETIME/TIMESTAMP do banco.
     */
    private Timestamp dataInsercao;
    
    /**
     * Construtor vazio.
     *
     * Finalidade:
     * - Necessário para frameworks, serialização,
     *   bibliotecas ORM e mecanismos baseados em reflexão.
     */
    public Unidade() {
    }
    
    /**
     * Construtor completo da entidade Unidade.
     *
     * @param idUnid Identificador da unidade.
     * @param nomeUnid Nome da unidade.
     * @param telefoneUnid Telefone de contato.
     * @param emailUnid E-mail da unidade.
     * @param enderecoUnid Endereço físico.
     * @param dataInsercao Data e hora do cadastro.
     *
     * Uso comum:
     * - Reconstrução de objetos provenientes de consultas ao banco
     *   de dados (operações SELECT).
     */
    public Unidade(int idUnid, String nomeUnid, String telefoneUnid, 
                   String emailUnid, String enderecoUnid, Timestamp dataInsercao) {
        this.idUnid = idUnid;
        this.nomeUnid = nomeUnid;
        this.telefoneUnid = telefoneUnid;
        this.emailUnid = emailUnid;
        this.enderecoUnid = enderecoUnid;
        this.dataInsercao = dataInsercao;
    }
    
    /**
     * Construtor utilizado para criação de novas unidades antes da persistência.
     *
     * @param nomeUnid Nome da unidade.
     * @param telefoneUnid Telefone de contato.
     * @param emailUnid E-mail da unidade.
     * @param enderecoUnid Endereço físico.
     * @param dataInsercao Data e hora do cadastro.
     *
     * Regra implícita:
     * - O ID não é informado pois normalmente é gerado automaticamente
     *   pelo banco de dados durante a inserção.
     */
    public Unidade(String nomeUnid, String telefoneUnid, String emailUnid, 
                   String enderecoUnid, Timestamp dataInsercao) {
        this.nomeUnid = nomeUnid;
        this.telefoneUnid = telefoneUnid;
        this.emailUnid = emailUnid;
        this.enderecoUnid = enderecoUnid;
        this.dataInsercao = dataInsercao;
    }
    
    /**
     * Retorna o identificador da unidade.
     *
     * @return idUnid ID único da unidade.
     */
    public int getIdUnid() {
        return idUnid;
    }
    
    /**
     * Define o identificador da unidade.
     *
     * @param idUnid Novo ID da unidade.
     *
     * Ponto crítico:
     * - Alterações manuais podem gerar inconsistências caso o
     *   objeto já esteja persistido no banco de dados.
     */
    public void setIdUnid(int idUnid) {
        this.idUnid = idUnid;
    }
    
    /**
     * Retorna o nome da unidade.
     *
     * @return nomeUnid Nome da unidade cadastrada.
     */
    public String getNomeUnid() {
        return nomeUnid;
    }
    
    /**
     * Define o nome da unidade.
     *
     * @param nomeUnid Nome identificador da unidade.
     *
     * Importância:
     * - Campo principal utilizado em exibições, buscas e relatórios.
     */
    public void setNomeUnid(String nomeUnid) {
        this.nomeUnid = nomeUnid;
    }
    
    /**
     * Retorna o telefone da unidade.
     *
     * @return telefoneUnid Número telefônico cadastrado.
     */
    public String getTelefoneUnid() {
        return telefoneUnid;
    }
    
    /**
     * Define o telefone da unidade.
     *
     * @param telefoneUnid Número telefônico.
     *
     * Observação:
     * - Pode ser utilizado para contato durante processos
     *   de movimentação ou suporte técnico.
     */
    public void setTelefoneUnid(String telefoneUnid) {
        this.telefoneUnid = telefoneUnid;
    }
    
    /**
     * Retorna o e-mail da unidade.
     *
     * @return emailUnid Endereço eletrônico da unidade.
     */
    public String getEmailUnid() {
        return emailUnid;
    }
    
    /**
     * Define o e-mail da unidade.
     *
     * @param emailUnid Endereço de e-mail institucional.
     */
    public void setEmailUnid(String emailUnid) {
        this.emailUnid = emailUnid;
    }
    
    /**
     * Retorna o endereço físico da unidade.
     *
     * @return enderecoUnid Endereço cadastrado.
     */
    public String getEnderecoUnid() {
        return enderecoUnid;
    }
    
    /**
     * Define o endereço físico da unidade.
     *
     * @param enderecoUnid Localização física da unidade.
     *
     * Relevância:
     * - Fundamental para logística patrimonial e auditorias.
     */
    public void setEnderecoUnid(String enderecoUnid) {
        this.enderecoUnid = enderecoUnid;
    }
    
    /**
     * Retorna a data de inserção do registro.
     *
     * @return dataInsercao Timestamp do cadastro.
     */
    public Timestamp getDataInsercao() {
        return dataInsercao;
    }
    
    /**
     * Define a data de inserção da unidade.
     *
     * @param dataInsercao Data e hora do cadastro.
     *
     * Observação:
     * - Geralmente definida automaticamente no momento
     *   da persistência no banco de dados.
     */
    public void setDataInsercao(Timestamp dataInsercao) {
        this.dataInsercao = dataInsercao;
    }
}
