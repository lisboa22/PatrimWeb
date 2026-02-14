package br.com.patrimweb.controller;

import org.mindrot.jbcrypt.BCrypt;

/**
 * =========================================================================
 * Classe: Seguranca
 * -------------------------------------------------------------------------
 * Propósito:
 * Classe utilitária responsável por operações relacionadas à segurança
 * de senhas no sistema PatrimWeb.
 *
 * Responsabilidades:
 * - Gerar hash criptografado de senhas utilizando o algoritmo BCrypt.
 * - Validar senhas digitadas comparando com hash armazenado.
 *
 * Observações Importantes:
 * - Utiliza a biblioteca org.mindrot.jbcrypt.BCrypt.
 * - BCrypt já aplica internamente salt automático, aumentando a segurança.
 * - Não realiza qualquer persistência em banco de dados.
 * - Esta classe não mantém estado (todos os métodos são estáticos).
 *
 * Regras de Segurança:
 * - Senhas nunca devem ser armazenadas em texto puro.
 * - Apenas o hash gerado deve ser salvo no banco.
 * - A verificação é feita comparando senha digitada com hash armazenado.
 * =========================================================================
 */
public class Seguranca {
	
	/**
	 * ---------------------------------------------------------------------
	 * Método: hashSenha
	 * ---------------------------------------------------------------------
	 * Responsável por gerar o hash criptografado de uma senha em texto puro.
	 *
	 * Regra de negócio:
	 * - Toda senha deve ser criptografada antes de ser persistida.
	 * - Utiliza BCrypt com salt automático gerado por gensalt().
	 *
	 * @param senha String contendo a senha em texto puro.
	 *
	 * @return String contendo o hash criptografado da senha.
	 *
	 * Pontos críticos:
	 * - Não deve receber senha nula (validação deve ser feita externamente).
	 * - O retorno deve ser armazenado no banco de dados.
	 * - Cada execução gera hash diferente devido ao salt automático.
	 */
    public static String hashSenha(String senha) {
        return BCrypt.hashpw(senha, BCrypt.gensalt());
    }

    /**
     * ---------------------------------------------------------------------
     * Método: verificarSenha
     * ---------------------------------------------------------------------
     * Responsável por validar se a senha digitada pelo usuário corresponde
     * ao hash armazenado no banco de dados.
     *
     * Regra de negócio:
     * - A comparação nunca deve ser feita manualmente.
     * - BCrypt.checkpw realiza internamente a validação segura.
     *
     * @param senhaDigitada     String contendo a senha informada pelo usuário.
     * @param senhaArmazenada   String contendo o hash salvo no banco.
     *
     * @return boolean
     *         true  -> senha válida (corresponde ao hash)
     *         false -> senha inválida
     *
     * Interação com banco de dados:
     * - O parâmetro senhaArmazenada deve ser previamente recuperado do banco.
     *
     * Pontos críticos:
     * - Caso senhaArmazenada esteja inválida ou corrompida,
     *   o comportamento dependerá da biblioteca BCrypt.
     * - A segurança da autenticação depende da integridade do hash armazenado.
     */
    public static boolean verificarSenha(String senhaDigitada, String senhaArmazenada) {
        return BCrypt.checkpw(senhaDigitada, senhaArmazenada);
    }
    

}
