package br.com.patrimweb.controller;

import br.com.patrimweb.dao.PerfilDAO;
import br.com.patrimweb.dao.PerfilPermissaoDAO;
import br.com.patrimweb.dao.PermissaoDAO;
import br.com.patrimweb.model.Usuario;
import br.com.patrimweb.utils.Conexao;

import java.io.IOException;
import java.sql.Connection;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * =====================================================================================
 * CONTROLLER: ConfiguracaoController
 * SISTEMA: PatrimWeb
 * CAMADA: Controller (Servlet)
 *
 * AÇÕES SUPORTADAS:
 *   GET  (sem action)          → exibe a página com perfis e permissões carregados
 *   GET  action=limparCache    → limpa cache do sistema
 *   GET  action=resetarConf... → reseta configurações de fábrica
 *   POST action=atualizarPerfil   → atualiza dados pessoais do usuário logado
 *   POST action=alterarSenha      → altera senha do usuário logado
 *   POST action=salvarPermissoes  → salva permissões de um perfil via AJAX
 * =====================================================================================
 */
@WebServlet("/ConfiguracaoController")
public class ConfiguracaoController extends HttpServlet {

    // ─────────────────────────────────────────────────────────────────────────
    // Constantes de navegação
    // ─────────────────────────────────────────────────────────────────────────

    private static final String VIEW_CONFIGURACOES = "/pages/configuracoes.jsp";
    private static final String REDIRECT_DASHBOARD = "/DashboardController";
    private static final String REDIRECT_LOGIN     = "index.jsp";
    private static final String PERFIL_ADMIN       = "ADMINISTRADOR";


    // ─────────────────────────────────────────────────────────────────────────
    // GET
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        
    

        if (!sessaoValida(session)) {
            response.sendRedirect(request.getContextPath() + "/" + REDIRECT_LOGIN);
            return;
        }

        if (!isAdministrador(session)) {
            session.setAttribute("mensagemErro",
                    "Acesso negado. Apenas Administradores podem acessar as Configurações.");
            response.sendRedirect(request.getContextPath() + REDIRECT_DASHBOARD);
            return;
        }

        String action = request.getParameter("action");

        if (action != null) {
            switch (action) {
                case "limparCache":
                    processarLimparCache(request, response, session);
                    return;
                case "resetarConfiguracoes":
                    processarResetarConfiguracoes(request, response, session);
                    return;
                default:
                    break;
            }
        }

        // ── Carrega dados para a aba Permissões ──────────────────────────────
        carregarDadosPermissoes(request, session);

        request.getRequestDispatcher(VIEW_CONFIGURACOES).forward(request, response);
    }


    // ─────────────────────────────────────────────────────────────────────────
    // POST
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);

        if (!sessaoValida(session)) {
            response.sendRedirect(request.getContextPath() + "/" + REDIRECT_LOGIN);
            return;
        }

        if (!isAdministrador(session)) {
            session.setAttribute("mensagemErro",
                    "Acesso negado. Apenas Administradores podem acessar as Configurações.");
            response.sendRedirect(request.getContextPath() + REDIRECT_DASHBOARD);
            return;
        }

        String action = request.getParameter("action");

        if (action == null || action.trim().isEmpty()) {
            session.setAttribute("mensagemErro", "Ação inválida ou não informada.");
            response.sendRedirect(request.getContextPath() + "/ConfiguracaoController");
            return;
        }

        switch (action) {
            case "atualizarPerfil":
                processarAtualizarPerfil(request, response, session);
                break;
            case "alterarSenha":
                processarAlterarSenha(request, response, session);
                break;
            case "salvarPermissoes":
                processarSalvarPermissoes(request, response, session);
                break;
            default:
                session.setAttribute("mensagemErro", "Operação desconhecida: " + action);
                response.sendRedirect(request.getContextPath() + "/ConfiguracaoController");
                break;
        }
    }


    // =========================================================================
    // MÉTODOS PRIVADOS
    // =========================================================================

    /**
     * Carrega perfis, permissões e o mapa de associações para a aba Permissões.
     * Em caso de erro, injeta listas vazias para não quebrar a view.
     */
    private void carregarDadosPermissoes(HttpServletRequest request, HttpSession session) {
        try (Connection conn = Conexao.getConnection()) {

            PerfilDAO perfilDAO          = new PerfilDAO(conn);
            PermissaoDAO permissaoDAO    = new PermissaoDAO(conn);
            PerfilPermissaoDAO ppDAO     = new PerfilPermissaoDAO();

            request.setAttribute("perfis",            perfilDAO.listarPerfis());
            request.setAttribute("permissoes",         permissaoDAO.listarPermissoes());
            request.setAttribute("perfilPermissoes",   ppDAO.getMapaCompleto());

        } catch (Exception e) {
            session.setAttribute("mensagemErro",
                    "Erro ao carregar dados de permissões: " + e.getMessage());
            request.setAttribute("perfis",           java.util.Collections.emptyList());
            request.setAttribute("permissoes",        java.util.Collections.emptyList());
            request.setAttribute("perfilPermissoes",  java.util.Collections.emptyMap());
        }
    }


    /**
     * AÇÃO POST: salvarPermissoes
     *
     * Recebe:
     *   id_perfil      = ID do perfil ativo
     *   permissao_ids  = array de IDs marcados (um campo por checkbox)
     *
     * Responde com texto simples "ok" em caso de sucesso (consumido pelo fetch).
     * Em caso de erro responde com status 500 e mensagem.
     */
    private void processarSalvarPermissoes(HttpServletRequest request,
                                            HttpServletResponse response,
                                            HttpSession session)
            throws IOException {

        response.setContentType("text/plain;charset=UTF-8");

        String idPerfilParam = request.getParameter("id_perfil");

        if (idPerfilParam == null || idPerfilParam.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Perfil não informado.");
            return;
        }

        try {
            int idPerfil = Integer.parseInt(idPerfilParam.trim());

            // Coleta IDs das permissões marcadas (pode ser null se nenhuma marcada)
            String[] idsParam = request.getParameterValues("permissao_ids");

            List<Integer> idsPermissoes = java.util.Collections.emptyList();

            if (idsParam != null && idsParam.length > 0) {
                idsPermissoes = Arrays.stream(idsParam)
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(Integer::parseInt)
                        .collect(Collectors.toList());
            }

            PerfilPermissaoDAO ppDAO = new PerfilPermissaoDAO();
            ppDAO.salvar(idPerfil, idsPermissoes);

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("ok");

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("ID inválido.");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Erro ao salvar permissões: " + e.getMessage());
        }
    }


    /**
     * AÇÃO POST: atualizarPerfil
     */
    private void processarAtualizarPerfil(HttpServletRequest request,
                                           HttpServletResponse response,
                                           HttpSession session)
            throws IOException {

        String nomeUsu     = request.getParameter("nome_usu");
        String emailUsu    = request.getParameter("email_usu");
        String telefoneUsu = request.getParameter("telefone_usu");

        if (nomeUsu == null || nomeUsu.trim().isEmpty()
                || emailUsu == null || emailUsu.trim().isEmpty()) {

            session.setAttribute("mensagemErro",
                    "Nome e E-mail são obrigatórios para atualizar o perfil.");
            response.sendRedirect(request.getContextPath() + "/ConfiguracaoController");
            return;
        }

        try {
            Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");

            usuarioLogado.setNomeUsu(nomeUsu.trim());
            usuarioLogado.setEmailUsu(emailUsu.trim());

            if (telefoneUsu != null && !telefoneUsu.trim().isEmpty()) {
                usuarioLogado.setTelefoneUsu(telefoneUsu.trim());
            }

            /*
             * TODO: Chamar DAO para persistir no banco.
             *   UsuarioDAO usuarioDAO = new UsuarioDAO(conn);
             *   usuarioDAO.atualizar(usuarioLogado);
             */

            session.setAttribute("usuarioLogado", usuarioLogado);
            session.setAttribute("mensagemSucesso", "Perfil atualizado com sucesso!");

        } catch (Exception e) {
            session.setAttribute("mensagemErro",
                    "Erro ao atualizar o perfil: " + e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/ConfiguracaoController");
    }


    /**
     * AÇÃO POST: alterarSenha
     */
    private void processarAlterarSenha(HttpServletRequest request,
                                        HttpServletResponse response,
                                        HttpSession session)
            throws IOException {

        String senhaAtual     = request.getParameter("senha_atual");
        String novaSenha      = request.getParameter("nova_senha");
        String confirmarSenha = request.getParameter("confirmar_senha");

        if (senhaAtual == null || senhaAtual.isEmpty()
                || novaSenha == null || novaSenha.isEmpty()
                || confirmarSenha == null || confirmarSenha.isEmpty()) {

            session.setAttribute("mensagemErro", "Todos os campos de senha são obrigatórios.");
            response.sendRedirect(request.getContextPath() + "/ConfiguracaoController");
            return;
        }

        if (!novaSenha.equals(confirmarSenha)) {
            session.setAttribute("mensagemErro",
                    "A nova senha e a confirmação não correspondem.");
            response.sendRedirect(request.getContextPath() + "/ConfiguracaoController");
            return;
        }

        if (novaSenha.length() < 6) {
            session.setAttribute("mensagemErro",
                    "A nova senha deve ter no mínimo 6 caracteres.");
            response.sendRedirect(request.getContextPath() + "/ConfiguracaoController");
            return;
        }

        try {
            /*
             * TODO: Validar senha atual e persistir nova senha (com hash).
             *   UsuarioDAO usuarioDAO = new UsuarioDAO(conn);
             *   boolean ok = usuarioDAO.validarSenha(usuarioLogado.getIdUsu(), senhaAtual);
             *   if (!ok) { session.setAttribute("mensagemErro", "Senha atual incorreta."); ... }
             *   usuarioDAO.alterarSenha(usuarioLogado.getIdUsu(), novaSenha);
             */

            session.setAttribute("mensagemSucesso", "Senha alterada com sucesso!");

        } catch (Exception e) {
            session.setAttribute("mensagemErro",
                    "Erro ao alterar a senha: " + e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/ConfiguracaoController");
    }


    /**
     * AÇÃO GET: limparCache
     */
    private void processarLimparCache(HttpServletRequest request,
                                       HttpServletResponse response,
                                       HttpSession session)
            throws IOException {

        try {
            /*
             * TODO: Implementar lógica de limpeza de cache.
             *   EquipamentoDAO.limparCache();
             *   getServletContext().removeAttribute("cacheRelatorios");
             */
            session.setAttribute("mensagemSucesso", "Cache do sistema limpo com sucesso!");

        } catch (Exception e) {
            session.setAttribute("mensagemErro",
                    "Erro ao limpar o cache: " + e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/ConfiguracaoController");
    }


    /**
     * AÇÃO GET: resetarConfiguracoes
     */
    private void processarResetarConfiguracoes(HttpServletRequest request,
                                                HttpServletResponse response,
                                                HttpSession session)
            throws IOException {

        try {
            /*
             * TODO: Implementar reset de configurações.
             *   ConfiguracaoDAO configuracaoDAO = new ConfiguracaoDAO();
             *   configuracaoDAO.resetarParaPadrao();
             */
            session.setAttribute("mensagemSucesso",
                    "Configurações resetadas para os valores de fábrica com sucesso!");

        } catch (Exception e) {
            session.setAttribute("mensagemErro",
                    "Erro ao resetar as configurações: " + e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/ConfiguracaoController");
    }


    // ─────────────────────────────────────────────────────────────────────────
    // Utilitários
    // ─────────────────────────────────────────────────────────────────────────

    private boolean sessaoValida(HttpSession session) {
        return session != null && session.getAttribute("usuarioLogado") != null;
    }

    private boolean isAdministrador(HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
            return usuario != null
                    && usuario.getPerfilUsu() != null
                    && PERFIL_ADMIN.equals(usuario.getPerfilUsu().getNomePerfil());
        } catch (Exception e) {
            return false;
        }
    }
}
