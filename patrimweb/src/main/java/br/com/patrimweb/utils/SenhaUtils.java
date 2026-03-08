package br.com.patrimweb.utils;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utilitário responsável pela criptografia e verificação de senhas.
 *
 * Utiliza o algoritmo BCrypt, que aplica hashing unidirecional com salt
 * automático, tornando impossível reverter o hash para a senha original.
 *
 * Por que BCrypt?
 * - Gera um salt aleatório a cada hash, evitando ataques de rainbow table.
 * - Possui fator de custo configurável (workload), dificultando ataques de força bruta.
 * - É o padrão recomendado pela indústria para armazenamento seguro de senhas.
 *
 * Dependência necessária no pom.xml:
 * <dependency>
 *     <groupId>org.mindrot</groupId>
 *     <artifactId>jbcrypt</artifactId>
 *     <version>0.4</version>
 * </dependency>
 */
public class SenhaUtils {

    /**
     * Fator de custo do BCrypt.
     *
     * Quanto maior o valor, mais lento o hashing (mais seguro).
     * O valor 12 é considerado seguro e equilibrado para uso em produção.
     * Valores recomendados: entre 10 e 14.
     */
    private static final int CUSTO = 12;

    /**
     * Gera o hash BCrypt de uma senha em texto puro.
     *
     * Este método deve ser chamado antes de salvar a senha no banco de dados.
     * O hash gerado já inclui o salt embutido, não sendo necessário armazená-lo separadamente.
     *
     * @param senhaTextoPlano senha informada pelo usuário no formulário
     * @return hash BCrypt da senha, pronto para ser armazenado no banco
     * @throws IllegalArgumentException se a senha informada for nula ou vazia
     */
    public static String criptografar(String senhaTextoPlano) {
        if (senhaTextoPlano == null || senhaTextoPlano.isBlank()) {
            throw new IllegalArgumentException("A senha não pode ser nula ou vazia.");
        }
        return BCrypt.hashpw(senhaTextoPlano, BCrypt.gensalt(CUSTO));
    }

    /**
     * Verifica se uma senha em texto puro corresponde a um hash BCrypt armazenado.
     *
     * Internamente, o BCrypt extrai o salt do próprio hash e reaplica o algoritmo
     * para comparação segura, sem expor a senha original.
     *
     * @param senhaTextoPlano senha digitada pelo usuário no formulário de login
     * @param hashArmazenado  hash BCrypt recuperado do banco de dados
     * @return {@code true} se a senha corresponder ao hash; {@code false} caso contrário
     */
    public static boolean verificar(String senhaTextoPlano, String hashArmazenado) {
        if (senhaTextoPlano == null || hashArmazenado == null) {
            return false;
        }
        return BCrypt.checkpw(senhaTextoPlano, hashArmazenado);
    }
}
