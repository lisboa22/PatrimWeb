package br.com.patrimweb.utils;

import br.com.patrimweb.utils.Conexao;
import br.com.patrimweb.utils.SenhaUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * ============================================================================
 * SCRIPT DE MIGRAÇÃO: SENHAS TEXTO PURO → HASH BCRYPT
 * ============================================================================
 *
 * Propósito:
 * Migrar todos os usuários que ainda possuem senha em texto puro no banco
 * para o formato hash BCrypt, sem quebrar o acesso de nenhum usuário.
 *
 * Quando executar:
 * - UMA ÚNICA VEZ, após implantar a nova versão do sistema com BCrypt.
 * - Antes de liberar o sistema para os usuários.
 *
 * Como executar:
 * - Pode ser executado como main standalone (classe com método main).
 * - Ou adaptado para um Servlet de inicialização com @WebServlet.
 *
 * Segurança:
 * - O script detecta automaticamente se a senha JÁ é um hash BCrypt
 *   (hashes BCrypt sempre começam com "$2a$" ou "$2b$").
 * - Senhas já migradas são ignoradas, tornando a execução idempotente —
 *   ou seja, pode ser executado múltiplas vezes sem efeito colateral.
 *
 * Observação importante:
 * - Após a migração, aumente o campo senha_usu para VARCHAR(255) se necessário,
 *   pois hashes BCrypt têm 60 caracteres fixos.
 *
 * SQL recomendado antes de rodar:
 *   ALTER TABLE usuario MODIFY COLUMN senha_usu VARCHAR(255) NOT NULL;
 * ============================================================================
 */
public class MigrarSenhasParaBCrypt {

    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println("  INICIANDO MIGRAÇÃO DE SENHAS PARA BCRYPT   ");
        System.out.println("==============================================\n");

        int totalProcessados = 0;
        int totalMigrados    = 0;
        int totalIgnorados   = 0;
        int totalErros       = 0;

        // ─────────────────────────────────────────────────
        // 1. Obtém todos os usuários com suas senhas atuais
        // ─────────────────────────────────────────────────
        String sqlSelect = "SELECT id_usu, nome_usu, senha_usu FROM usuario";
        String sqlUpdate = "UPDATE usuario SET senha_usu = ? WHERE id_usu = ?";

        try (Connection conn = Conexao.getConnection();
             PreparedStatement psSelect = conn.prepareStatement(sqlSelect);
             PreparedStatement psUpdate = conn.prepareStatement(sqlUpdate)) {

            ResultSet rs = psSelect.executeQuery();

            while (rs.next()) {
                int    id    = rs.getInt("id_usu");
                String nome  = rs.getString("nome_usu");
                String senha = rs.getString("senha_usu");

                totalProcessados++;

                // ─────────────────────────────────────────────────────────
                // 2. Verifica se a senha já é um hash BCrypt
                //    Hashes BCrypt começam com "$2a$" ou "$2b$" — 60 chars.
                //    Se já for hash, pula sem alterar (idempotência).
                // ─────────────────────────────────────────────────────────
                if (senha != null && (senha.startsWith("$2a$") || senha.startsWith("$2b$"))) {
                    System.out.printf("  [IGNORADO]  id=%-4d | %-30s | Senha já é BCrypt.%n", id, nome);
                    totalIgnorados++;
                    continue;
                }

                // ─────────────────────────────────────────────────────────
                // 3. Senha está em texto puro — gera o hash BCrypt e atualiza
                // ─────────────────────────────────────────────────────────
                try {
                    if (senha == null || senha.isBlank()) {
                        System.out.printf("  [PULADO]    id=%-4d | %-30s | Senha vazia, não migrada.%n", id, nome);
                        totalIgnorados++;
                        continue;
                    }

                    String hashBCrypt = SenhaUtils.criptografar(senha);

                    psUpdate.setString(1, hashBCrypt);
                    psUpdate.setInt(2, id);
                    psUpdate.executeUpdate();

                    System.out.printf("  [MIGRADO]   id=%-4d | %-30s | Hash gerado com sucesso.%n", id, nome);
                    totalMigrados++;

                } catch (Exception e) {
                    System.out.printf("  [ERRO]      id=%-4d | %-30s | %s%n", id, nome, e.getMessage());
                    totalErros++;
                }
            }

        } catch (Exception e) {
            System.err.println("\n[ERRO CRÍTICO] Falha ao conectar ou executar a migração:");
            e.printStackTrace();
        }

        // ─────────────────────────────────────────────────
        // 4. Exibe resumo final da migração
        // ─────────────────────────────────────────────────
        System.out.println("\n==============================================");
        System.out.println("  RESUMO DA MIGRAÇÃO");
        System.out.println("==============================================");
        System.out.printf("  Total processados : %d%n", totalProcessados);
        System.out.printf("  Migrados          : %d%n", totalMigrados);
        System.out.printf("  Já eram BCrypt    : %d%n", totalIgnorados);
        System.out.printf("  Erros             : %d%n", totalErros);
        System.out.println("==============================================");

        if (totalErros == 0) {
            System.out.println("  ✅ Migração concluída sem erros!");
        } else {
            System.out.println("  ⚠️  Migração concluída com erros. Verifique os logs acima.");
        }
    }
}
