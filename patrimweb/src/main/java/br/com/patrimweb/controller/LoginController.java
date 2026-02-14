package br.com.patrimweb.controller;

import br.com.patrimweb.dao.UsuarioDAO;
import br.com.patrimweb.model.Usuario;
import br.com.patrimweb.utils.Conexao;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.Connection;

/**
 * Controller responsável pelo processo de autenticação de usuários.
 *
 * Responsabilidades:
 * - Receber credenciais enviadas pelo formulário de login.
 * - Validar usuário e senha através da camada DAO.
 * - Criar sessão para usuário autenticado.
 * - Redirecionar para o Dashboard em caso de sucesso.
 * - Redirecionar para a página inicial em caso de falha.
 *
 * Este controller atua como ponto de entrada para o sistema.
 */
@WebServlet("/LoginController")
public class LoginController extends HttpServlet {
	
    /**
     * Processa requisições HTTP POST oriundas do formulário de login.
     *
     * Fluxo da regra de negócio:
     * 1. Captura usuário e senha informados.
     * 2. Consulta o banco de dados para autenticação.
     * 3. Se válido:
     *      - Cria sessão.
     *      - Armazena objeto Usuario na sessão.
     *      - Redireciona para o Dashboard.
     *    Se inválido:
     *      - Redireciona para página inicial.
     *
     * Interação com banco:
     * - Utiliza UsuarioDAO.autenticar(usuario, senha).
     *
     * @param request  contém credenciais enviadas pelo formulário
     * @param response responsável pelo redirecionamento
     * @throws ServletException em caso de erro interno
     * @throws IOException      em caso de falha de I/O
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    	

        // Captura parâmetros enviados pelo formulário
        String usuarioInput = request.getParameter("usuario");
        String senha = request.getParameter("senha");

        // Bloco try-with-resources garante fechamento automático da conexão
        try (Connection conn = Conexao.getConnection()) {

            // Instancia DAO responsável pela autenticação
            UsuarioDAO usuarioDAO = new UsuarioDAO(conn);

            // Realiza validação das credenciais no banco
            Usuario usuario = usuarioDAO.autenticar(usuarioInput, senha);

            if (usuario != null) {
                // 🔐 Cria sessão HTTP
                // Regra de segurança: usuário autenticado passa a ter sessão ativa
                HttpSession sessao = request.getSession(true);

                // Armazena objeto Usuario na sessão para controle de acesso
                sessao.setAttribute("usuarioLogado", usuario);
            	
            	// Pega o nome do objeto usuario (ex: usuario.getNomeUsu()) e salva como 'nomeUsuario'
                sessao.setAttribute("nomeUsuario", usuario.getNomeUsu());

                // 🚀 Redireciona para o Dashboard após login bem-sucedido
                response.sendRedirect(request.getContextPath() + "/DashboardController");

            } else {
                // ❌ Login inválido
                // Regra de negócio: credenciais incorretas não criam sessão

                // Redireciona para página inicial (login)
                response.sendRedirect("/patrimweb/index.jsp");
            }

        } catch (Exception e) {
            // Encapsula exceções técnicas como ServletException
            throw new ServletException("Erro no LoginController", e);
        }
    }
}
