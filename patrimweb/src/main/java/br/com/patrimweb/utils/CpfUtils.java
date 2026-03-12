package br.com.patrimweb.utils;

/**
 * Utilitário para validação de CPF.
 *
 * <p>
 * Implementa o algoritmo oficial de validação do CPF brasileiro:
 * - Remove caracteres não numéricos (pontos, traços, espaços).
 * - Rejeita CPFs com todos os dígitos iguais (ex: 000.000.000-00, 111.111.111-11).
 * - Calcula e confere os dois dígitos verificadores.
 * </p>
 *
 * Uso:
 * <pre>
 *     if (!CpfUtils.isValido(cpf)) {
 *         // CPF inválido
 *     }
 * </pre>
 */
public class CpfUtils {

    // Construtor privado — classe utilitária não deve ser instanciada
    private CpfUtils() {}

    /**
     * Valida um CPF informado pelo usuário.
     *
     * O CPF pode ser enviado formatado (ex: "123.456.789-09") ou apenas com dígitos.
     * A formatação é removida internamente antes da validação.
     *
     * @param cpf CPF a ser validado (formatado ou apenas dígitos).
     * @return true se o CPF for válido, false caso contrário.
     */
    public static boolean isValido(String cpf) {

        if (cpf == null) {
            return false;
        }

        // Remove tudo que não for dígito: pontos, traços, espaços etc.
        String cpfLimpo = cpf.replaceAll("\\D", "");

        // CPF deve ter exatamente 11 dígitos
        if (cpfLimpo.length() != 11) {
            return false;
        }

        // Rejeita sequências conhecidas inválidas (111.111.111-11, 000.000.000-00, etc.)
        if (cpfLimpo.chars().distinct().count() == 1) {
            return false;
        }

        // ── Cálculo do 1º dígito verificador ──
        int soma = 0;
        for (int i = 0; i < 9; i++) {
            soma += Character.getNumericValue(cpfLimpo.charAt(i)) * (10 - i);
        }
        int primeiroDigito = calcularDigito(soma);

        if (primeiroDigito != Character.getNumericValue(cpfLimpo.charAt(9))) {
            return false;
        }

        // ── Cálculo do 2º dígito verificador ──
        soma = 0;
        for (int i = 0; i < 10; i++) {
            soma += Character.getNumericValue(cpfLimpo.charAt(i)) * (11 - i);
        }
        int segundoDigito = calcularDigito(soma);

        return segundoDigito == Character.getNumericValue(cpfLimpo.charAt(10));
    }

    /**
     * Calcula um dígito verificador a partir da soma ponderada.
     *
     * Regra oficial: resto = soma % 11.
     * - Se resto < 2  → dígito = 0
     * - Se resto >= 2 → dígito = 11 - resto
     *
     * @param soma soma ponderada acumulada.
     * @return dígito verificador calculado.
     */
    private static int calcularDigito(int soma) {
        int resto = soma % 11;
        return (resto < 2) ? 0 : (11 - resto);
    }
}
