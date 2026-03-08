package br.com.patrimweb.model;

import java.sql.Timestamp;

/**
 * Classe de modelo (Model) responsável por representar uma Movimentação
 * de equipamentos dentro do sistema PatrimWeb.
 *
 * Objetivo:
 * - Registrar o histórico de movimentações de equipamentos entre unidades
 *   e usuários responsáveis.
 * - Permitir rastreabilidade completa da origem, destino e responsáveis
 *   envolvidos em cada operação.
 *
 * Contexto de negócio:
 * - Cada movimentação representa uma alteração de localização ou posse
 *   de um equipamento.
 * - A movimentação pode envolver transferência entre unidades,
 *   troca de responsável ou registro operacional.
 * - O histórico é essencial para auditoria patrimonial e controle logístico.
 *
 * Observação:
 * Esta classe atua apenas como estrutura de dados (DTO/Entity),
 * não possuindo regras de persistência ou acesso direto ao banco.
 */
public class Movimentacao {

	/**
     * Identificador único da movimentação.
     * Normalmente gerado automaticamente pelo banco de dados.
     */
	private int idMov;

    /**
     * Equipamento associado à movimentação.
     * Define qual item patrimonial está sendo movimentado.
     */
    private Equipamento equipamento;
    
    /**
     * Fabricante associado à movimentação.
     * Define qual item patrimonial está sendo movimentado.
     */
    private Fabricante fabricante;

    /**
     * Tipo da movimentação realizada.
     * Exemplos comuns:
     * - Transferência
     * - Instalação
     * - Manutenção
     * - Baixa
     */
    private String tipoMovimentacaoMov;

    /**
     * Unidade de origem da movimentação.
     * Representa o local anterior do equipamento.
     */
    private Unidade unidadeOrigem;

    /**
     * Usuário responsável pela liberação/origem do equipamento.
     */
    private Usuario usuarioOrigem;

    /**
     * Unidade de destino da movimentação.
     * Representa o novo local do equipamento.
     */
    private Unidade unidadeDestino;

    /**
     * Usuário responsável pelo recebimento/destino do equipamento.
     */
    private Usuario usuarioDestino;

    /**
     * Observações adicionais relacionadas à movimentação.
     * Campo utilizado para registrar detalhes operacionais,
     * justificativas ou ocorrências.
     */
    private String observacaoMov;

    /**
     * Data e hora em que a movimentação foi registrada no sistema.
     * Utiliza Timestamp para compatibilidade direta com o banco de dados.
     */
    private Timestamp dataInsercao;
    
    /**
     * Construtor vazio.
     *
     * Finalidade:
     * - Necessário para frameworks, serialização,
     *   bibliotecas ORM e mecanismos baseados em reflexão.
     */
    public Movimentacao() {
    }
    
    /**
     * Construtor completo da entidade Movimentacao.
     *
     * @param idMov Identificador da movimentação.
     * @param equipamento Equipamento movimentado.
     * @param numeroSerieMov Número de série do equipamento.
     * @param tipoMovimentacaoMov Tipo da movimentação realizada.
     * @param unidadeOrigem Unidade de origem do equipamento.
     * @param usuarioOrigem Usuário responsável na origem.
     * @param unidadeDestino Unidade de destino do equipamento.
     * @param usuarioDestino Usuário responsável no destino.
     * @param observacaoMov Observações relacionadas à movimentação.
     * @param dataInsercao Data e hora do registro.
     *
     * Uso comum:
     * - Reconstrução de objetos provenientes de consultas
     *   ao banco de dados (operações SELECT).
     */
    public Movimentacao(int idMov, Equipamento equipamento, Fabricante fabricante, String tipoMovimentacaoMov, Unidade unidadeOrigem, Usuario usuarioOrigem, 
                        Unidade unidadeDestino, Usuario usuarioDestino, String observacaoMov, 
                        Timestamp dataInsercao) {
        this.idMov = idMov;
        this.equipamento = equipamento;
        this.fabricante = fabricante;
        this.tipoMovimentacaoMov = tipoMovimentacaoMov;
        this.unidadeOrigem = unidadeOrigem;
        this.usuarioOrigem = usuarioOrigem;
        this.unidadeDestino = unidadeDestino;
        this.usuarioDestino = usuarioDestino;
        this.observacaoMov = observacaoMov;
        this.dataInsercao = dataInsercao;
    }
    
    /**
     * Construtor utilizado para inserção de novas movimentações no banco.
     *
     * @param equipamento Equipamento movimentado.
     * @param numeroSerieMov Número de série do equipamento.
     * @param tipoMovimentacaoMov Tipo da movimentação.
     * @param unidadeOrigem Unidade de origem.
     * @param usuarioOrigem Usuário responsável na origem.
     * @param unidadeDestino Unidade de destino.
     * @param usuarioDestino Usuário responsável no destino.
     * @param observacaoMov Observações da movimentação.
     * @param dataInsercao Data e hora do registro.
     *
     * Regra implícita:
     * - O ID não é informado pois geralmente é gerado automaticamente
     *   pelo banco de dados.
     */
    public Movimentacao(Equipamento equipamento, Fabricante fabricante, String tipoMovimentacaoMov, 
    					Unidade unidadeOrigem, Usuario usuarioOrigem, Unidade unidadeDestino, 
    					Usuario usuarioDestino, String observacaoMov, Timestamp dataInsercao) {
        this.equipamento = equipamento;
        this.fabricante = fabricante;
        this.tipoMovimentacaoMov = tipoMovimentacaoMov;
        this.unidadeOrigem = unidadeOrigem;
        this.usuarioOrigem = usuarioOrigem;
        this.unidadeDestino = unidadeDestino;
        this.usuarioDestino = usuarioDestino;
        this.observacaoMov = observacaoMov;
        this.dataInsercao = dataInsercao;
    }
    
    /**
     * Retorna o identificador da movimentação.
     *
     * @return idMov ID único da movimentação.
     */
    public int getIdMov() {
        return idMov;
    }
    
    /**
     * Define o identificador da movimentação.
     *
     * @param idMov Novo ID da movimentação.
     *
     * Ponto crítico:
     * - Alterações manuais podem gerar inconsistência
     *   com registros persistidos no banco.
     */
    public void setIdMov(int idMov) {
        this.idMov = idMov;
    }
    
    /**
     * Retorna o equipamento associado à movimentação.
     *
     * @return Equipamento movimentado.
     */
    public Equipamento getEquipamento() {
        return equipamento;
    }
    
    /**
     * Define o equipamento da movimentação.
     *
     * @param equipamento Equipamento a ser associado.
     */
    public void setEquipamento(Equipamento equipamento) {
        this.equipamento = equipamento;
    }
    
    /**
     * Retorna o equipamento associado à movimentação.
     *
     * @return Equipamento movimentado.
     */
    public Fabricante getFabricante() {
        return fabricante;
    }
    
    /**
     * Define o equipamento da movimentação.
     *
     * @param equipamento Equipamento a ser associado.
     */
    public void setFabricante(Fabricante fabricante) {
        this.fabricante = fabricante;
    }
    
    /**
     * Retorna o tipo da movimentação.
     *
     * @return tipoMovimentacaoMov Tipo de operação realizada.
     */
    public String getTipoMovimentacaoMov() {
        return tipoMovimentacaoMov;
    }
    
    /**
     * Define o tipo da movimentação.
     *
     * @param tipoMovimentacaoMov Tipo da movimentação.
     *
     * Regra implícita:
     * - Deve representar corretamente o processo operacional
     *   realizado sobre o equipamento.
     */
    public void setTipoMovimentacaoMov(String tipoMovimentacaoMov) {
        this.tipoMovimentacaoMov = tipoMovimentacaoMov;
    }
    
    /**
     * Retorna a unidade de origem.
     *
     * @return Unidade de onde o equipamento saiu.
     */
    public Unidade getUnidadeOrigem() {
        return unidadeOrigem;
    }
    
    /**
     * Define a unidade de origem da movimentação.
     *
     * @param unidadeOrigem Unidade de origem.
     */
    public void setUnidadeOrigem(Unidade unidadeOrigem) {
        this.unidadeOrigem = unidadeOrigem;
    }
    
    /**
     * Retorna o usuário responsável pela origem.
     *
     * @return Usuario responsável pela liberação.
     */
    public Usuario getUsuarioOrigem() {
        return usuarioOrigem;
    }
    
    /**
     * Define o usuário responsável na origem.
     *
     * @param usuarioOrigem Usuário de origem.
     */
    public void setUsuarioOrigem(Usuario usuarioOrigem) {
        this.usuarioOrigem = usuarioOrigem;
    }
    
    /**
     * Retorna a unidade de destino.
     *
     * @return Unidade de destino do equipamento.
     */
    public Unidade getUnidadeDestino() {
        return unidadeDestino;
    }
    
    /**
     * Define a unidade de destino da movimentação.
     *
     * @param unidadeDestino Unidade de destino.
     */
    public void setUnidadeDestino(Unidade unidadeDestino) {
        this.unidadeDestino = unidadeDestino;
    }
    
    /**
     * Retorna o usuário responsável pelo destino.
     *
     * @return Usuario responsável pelo recebimento.
     */
    public Usuario getUsuarioDestino() {
        return usuarioDestino;
    }
    
    /**
     * Define o usuário responsável no destino.
     *
     * @param usuarioDestino Usuário de destino.
     */
    public void setUsuarioDestino(Usuario usuarioDestino) {
        this.usuarioDestino = usuarioDestino;
    }
    
    /**
     * Retorna as observações da movimentação.
     *
     * @return observacaoMov Texto descritivo da operação.
     */
    public String getObservacaoMov() {
        return observacaoMov;
    }
    
    /**
     * Define observações adicionais da movimentação.
     *
     * @param observacaoMov Observações operacionais.
     *
     * Uso:
     * - Registrar ocorrências, justificativas ou informações complementares.
     */
    public void setObservacaoMov(String observacaoMov) {
        this.observacaoMov = observacaoMov;
    }
    
    /**
     * Retorna a data de inserção da movimentação.
     *
     * @return dataInsercao Data e hora do registro.
     */
    public Timestamp getDataInsercao() {
        return dataInsercao;
    }
    
    /**
     * Define a data de inserção da movimentação.
     *
     * @param dataInsercao Timestamp do registro.
     *
     * Observação:
     * - Geralmente preenchido automaticamente no momento da persistência.
     */
    public void setDataInsercao(Timestamp dataInsercao) {
        this.dataInsercao = dataInsercao;
    }
}
