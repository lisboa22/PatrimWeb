package br.com.patrimweb.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

/**
 * Controller responsável por realizar o processo de logout do usuário
 * no sistema PatrimWeb.
 *
 * RESPONSABILIDADES:
 * - Encerrar a sessão HTTP ativa do usuário autenticado.
 * - Garantir que dados armazenados em sessão sejam removidos.
 * - Redirecionar o usuário para a página inicial após o logout.
 *
 * REGRAS DE NEGÓCIO:
 * - O logout apenas invalida sessões existentes, evitando a criação
 *   desnecessária de novas sessões.
 * - Após o encerramento da sessão, o usuário perde qualquer estado
 *   de autenticação armazenado no servidor.
 *
 * PONTOS CRÍTICOS:
 * - A invalidação da sessão remove todos os atributos armazenados,
 *   incluindo o objeto "usuarioLogado".
 * - O redirecionamento utiliza o contextPath para manter compatibilidade
 *   caso a aplicação seja implantada em diferentes contextos do servidor.
 */
@WebServlet("/LogoutController")
public class LogoutController extends HttpServlet {

    /**
     * Identificador de serialização da Servlet.
     * Utilizado pelo container para controle interno da classe.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Método responsável por processar requisições HTTP GET para logout.
     *
     * FLUXO DE EXECUÇÃO:
     * 1. Recupera a sessão atual do usuário sem criar uma nova.
     * 2. Verifica se existe uma sessão ativa.
     * 3. Invalida a sessão, encerrando autenticação e removendo dados.
     * 4. Redireciona o usuário para a página inicial do sistema.
     *
     * @param request  Objeto HttpServletRequest contendo dados da requisição HTTP.
     * @param response Objeto HttpServletResponse utilizado para envio da resposta HTTP.
     *
     * @throws ServletException caso ocorra erro interno relacionado à Servlet.
     * @throws IOException      caso ocorra erro durante o redirecionamento HTTP.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 🔐 Recupera sessão existente sem criar uma nova sessão caso não exista.
        // Isso evita consumo desnecessário de recursos do servidor.
        HttpSession session = request.getSession(false);

        // Estrutura condicional que garante que a invalidação
        // só ocorra quando houver uma sessão ativa.
        if (session != null) {
            // Encerra completamente a sessão HTTP:
            // - Remove todos os atributos armazenados
            // - Finaliza autenticação do usuário
            // - Invalida o identificador de sessão no servidor
            session.invalidate();
        }

        // 🔄 Redireciona o usuário para a página inicial.
        // O uso de getContextPath() garante que o caminho funcione
        // corretamente independentemente do nome do contexto da aplicação.
        response.sendRedirect(request.getContextPath() + "/index.jsp");
    }
}
