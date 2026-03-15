package br.com.patrimweb.controller;

import br.com.patrimweb.dao.PerfilDAO;
import br.com.patrimweb.dao.PerfilPermissaoDAO;
import br.com.patrimweb.dao.PermissaoDAO;
import br.com.patrimweb.model.Perfil;
import br.com.patrimweb.model.Permissao;
import br.com.patrimweb.model.PerfilPermissao;
import br.com.patrimweb.utils.Conexao;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller responsável pela página de Configurações do PatrimWeb.
 *
 * Abas tratadas:
 *   - Perfil       → atualizarPerfil, atualizarFoto
 *   - Segurança    → alterarSenha
 *   - Permissões   → carregarDadosPermissoes (GET) + salvarPermissoes (POST/AJAX)
 *
 * Fluxo da aba Permissões:
 *
 *   GET /ConfiguracaoController
 *     └── carregarDadosPermissoes()
 *           ├── PerfilDAO.listarPerfis()
 *           │     → request.setAttribute("perfis", List<Perfil>)
 *           ├── PermissaoDAO.listarOrdenado()
 *           │     → request.setAttribute("permissoes", List<Permissao>)
 *           │       ordenado por modulo+acao para o agrupamento visual do JSP
 *           └── PerfilPermissaoDAO.getMapaSimples()
 *                 → request.setAttribute("perfilPermissoes", Map<Integer, List<Integer>>)
 *                   serializado pelo JSP como permData = { "1":[10,11], "2":[10] }
 *
 *   POST /ConfiguracaoController  (AJAX — application/x-www-form-urlencoded)
 *     action       = salvarPermissoes
 *     id_perfil    = <int>
 *     permissao_ids= <int>  (repetido para cada checkbox marcado)
 *     └── salvarPermissoes()
 *           └── PerfilPermissaoDAO.salvarDaLista(idPerfil, List<Integer>)
 *                 → HTTP 200 (ok) ou 500 (erro)
 *                 → sem redirect, sem forward — resposta lida pelo fetch() do JS
 */
@WebServlet("/ConfiguracaoController")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,       // 1 MB — flush para disco acima desse limite
    maxFileSize       = 5  * 1024 * 1024,  // 5 MB — tamanho máximo por arquivo
    maxRequestSize    = 10 * 1024 * 1024   // 10 MB — tamanho máximo da requisição inteira
)
public class ConfiguracaoController extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // ─────────────────────────────────────────────────────────────────────────
    // GET — carrega a página com todos os dados necessários para as abas
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("usuarioLogado") == null) {
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }

        // Sem cache — a página contém dados sensíveis de permissões
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        // Carrega dados da aba Permissões antes de fazer o forward
        carregarDadosPermissoes(request);

        RequestDispatcher rd = request.getRequestDispatcher("/pages/configuracoes.jsp");
        rd.forward(request, response);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST — despacha para o método correto conforme o parâmetro "action"
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("usuarioLogado") == null) {
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }

        String action = request.getParameter("action");
        if (action == null) action = "";

        try {
            switch (action) {

                case "atualizarPerfil":
                    atualizarPerfil(request, response);
                    break;

                case "atualizarFoto":
                    atualizarFoto(request, response);
                    break;

                case "alterarSenha":
                    alterarSenha(request, response);
                    break;

                // ── AJAX: salva permissões da aba Permissões ──────────────
                case "salvarPermissoes":
                    salvarPermissoes(request, response);
                    break;

                default:
                    response.sendRedirect(request.getContextPath() + "/ConfiguracaoController");
            }

        } catch (Exception e) {
            throw new ServletException("Erro no ConfiguracaoController [action=" + action + "]", e);
        }
    }

    // =========================================================================
    // ABA PERMISSÕES — LEITURA
    // =========================================================================

    /**
     * Carrega e disponibiliza para o JSP os três objetos necessários
     * para renderizar a aba de Permissões corretamente:
     *
     *   perfis           → painel esquerdo (lista clicável de perfis)
     *   permissoes       → painel direito  (checkboxes agrupados por módulo)
     *   perfilPermissoes → estado inicial  (quais checkboxes ficam marcados)
     *
     * Em caso de falha no banco, os três atributos recebem listas/mapas
     * vazios para que o JSP exiba o estado "nenhum item cadastrado"
     * em vez de lançar NullPointerException na EL.
     */
    private void carregarDadosPermissoes(HttpServletRequest request) {

        Connection conn = null;

        try {
            conn = Conexao.getConnection();

            PerfilDAO          perfilDAO = new PerfilDAO(conn);
            PermissaoDAO       permDAO   = new PermissaoDAO(conn);
            PerfilPermissaoDAO ppDAO     = new PerfilPermissaoDAO(conn);
           

            // 1. Lista de perfis — painel esquerdo do JSP
            List<Perfil> perfis = perfilDAO.listarPerfis();

            // 2. Lista de permissões ordenada por módulo+ação — checkboxes do JSP
            List<Permissao> permissoes = permDAO.listarOrdenado();

            // 3. Mapa idPerfil → [idPermissao, ...] — estado dos checkboxes
            Map<Integer, List<Integer>> perfilPermissoes = ppDAO.getMapaSimples();
            
            System.out.println("PerfPerm: "+perfilPermissoes);
            // Garante que todo perfil exista no mapa, mesmo sem permissões.
            // Sem isso, perfis novos não teriam entrada em permData e o JS
            // os trataria como "undefined", quebrando o indexOf().
            for (Perfil p : perfis) {
                perfilPermissoes.putIfAbsent(p.getIdPerfil(), new ArrayList<>());
            }

            request.setAttribute("perfis",           perfis);
            request.setAttribute("permissoes",        permissoes);
            request.setAttribute("perfilPermissoes",  perfilPermissoes);

        } catch (Exception e) {
            System.err.println("[PatrimWeb] Erro ao carregar dados de permissões: " + e.getMessage());
            e.printStackTrace();

            // Fallback seguro — JSP exibirá "Nenhum perfil/permissão cadastrado"
            request.setAttribute("perfis",           new ArrayList<>());
            request.setAttribute("permissoes",        new ArrayList<>());
            request.setAttribute("perfilPermissoes",  new HashMap<>());

        } finally {
            fecharConexao(conn);
        }
    }

    // =========================================================================
    // ABA PERMISSÕES — ESCRITA (AJAX)
    // =========================================================================

    /**
     * Persiste as permissões de um perfil recebidas via AJAX.
     *
     * Payload esperado (application/x-www-form-urlencoded):
     *   action        = salvarPermissoes
     *   id_perfil     = 3
     *   permissao_ids = 10
     *   permissao_ids = 11
     *   permissao_ids = 15
     *
     * Resposta:
     *   HTTP 200 — sucesso (o JS atualiza permData e mostra toast verde)
     *   HTTP 400 — parâmetros inválidos
     *   HTTP 500 — erro no banco (o JS mostra toast vermelho)
     *
     * NÃO faz redirect nem forward — o fetch() do JS lê apenas o status HTTP.
     */
    private void salvarPermissoes(HttpServletRequest request,
                                   HttpServletResponse response) throws IOException {

        // 1. Valida e lê id_perfil
        String rawIdPerfil = request.getParameter("id_perfil");
        if (rawIdPerfil == null || rawIdPerfil.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                               "Parâmetro id_perfil ausente.");
            return;
        }

        int idPerfil;
        try {
            idPerfil = Integer.parseInt(rawIdPerfil.trim());
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                               "id_perfil inválido: " + rawIdPerfil);
            return;
        }

        // 2. Lê a lista de IDs de permissão marcados
        //    getParameterValues retorna null se nenhum checkbox estiver marcado
        //    (ex: "Revogar Tudo" foi clicado) — isso é válido e resulta em lista vazia.
        String[] rawIds = request.getParameterValues("permissao_ids");
        List<Integer> idsPermissao = new ArrayList<>();

        if (rawIds != null) {
            for (String s : rawIds) {
                try {
                    idsPermissao.add(Integer.parseInt(s.trim()));
                } catch (NumberFormatException e) {
                    // ID inválido na lista — loga e ignora; não interrompe o fluxo
                    System.err.println("[PatrimWeb] permissao_id inválido ignorado: " + s);
                }
            }
        }

        // 3. Persiste no banco
        Connection conn = null;
        try {
            conn = Conexao.getConnection();
            PerfilPermissaoDAO ppDAO = new PerfilPermissaoDAO(conn);
            ppDAO.salvarDaLista(idPerfil, idsPermissao);

            response.setStatus(HttpServletResponse.SC_OK);

        } catch (SQLException e) {
            System.err.println("[PatrimWeb] Erro ao salvar permissões do perfil "
                               + idPerfil + ": " + e.getMessage());
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                               "Erro ao salvar permissões.");

        } finally {
            fecharConexao(conn);
        }
    }

    // =========================================================================
    // ABA PERFIL — atualizar dados pessoais
    // =========================================================================

    private void atualizarPerfil(HttpServletRequest request,
                                  HttpServletResponse response) throws Exception {

        // TODO: implementar atualização de dados do usuário logado
        // Exemplo de campos: nome_usu, email_usu, telefone_usu, cpf_usu, endereco_usu
        request.getSession().setAttribute("mensagemSucesso",
                "Perfil atualizado com sucesso!");
        response.sendRedirect(request.getContextPath() + "/ConfiguracaoController");
    }

    // =========================================================================
    // ABA PERFIL — upload de foto
    // =========================================================================

    private void atualizarFoto(HttpServletRequest request,
                                HttpServletResponse response) throws Exception {

        try {
            Part fotoPart = request.getPart("foto_perfil");
            if (fotoPart == null || fotoPart.getSize() == 0) {
                request.getSession().setAttribute("mensagemErro",
                        "Nenhuma foto enviada.");
                response.sendRedirect(request.getContextPath() + "/ConfiguracaoController");
                return;
            }

            HttpSession session  = request.getSession(false);
            Object      usuario  = session.getAttribute("usuarioLogado");

            // Obtém o id do usuário via reflexão simples
            int idUsu = (int) usuario.getClass().getMethod("getIdUsu").invoke(usuario);

            // Diretório absoluto de imagens no servidor
            String uploadDir = getServletContext().getRealPath("/imagens/perfil");
            Files.createDirectories(Paths.get(uploadDir));

            String destino = uploadDir + "/" + idUsu + ".jpg";

            try (InputStream is = fotoPart.getInputStream()) {
                Files.copy(is, Paths.get(destino), StandardCopyOption.REPLACE_EXISTING);
            }

            // Atualiza versão de cache da foto na sessão
            session.setAttribute("fotoPerfil_v", System.currentTimeMillis());

            request.getSession().setAttribute("mensagemSucesso",
                    "Foto atualizada com sucesso!");

        } catch (Exception e) {
            request.getSession().setAttribute("mensagemErro",
                    "Erro ao atualizar foto. Tente novamente.");
            e.printStackTrace();
        }

        response.sendRedirect(request.getContextPath() + "/ConfiguracaoController");
    }

    // =========================================================================
    // ABA SEGURANÇA — alterar senha
    // =========================================================================

    private void alterarSenha(HttpServletRequest request,
                               HttpServletResponse response) throws Exception {

        // TODO: implementar alteração de senha com validação de senha_atual,
        //       nova_senha e confirmar_senha
        request.getSession().setAttribute("mensagemSucesso",
                "Senha alterada com sucesso!");
        response.sendRedirect(request.getContextPath()
                              + "/ConfiguracaoController?aba=seguranca");
    }

    // =========================================================================
    // UTILITÁRIOS
    // =========================================================================

    /**
     * Fecha a conexão com o banco de forma segura.
     * Centralizado aqui para evitar try-catch repetitivo nos métodos acima.
     */
    private void fecharConexao(Connection conn) {
        if (conn != null) {
            try { conn.close(); } catch (SQLException e) {
                System.err.println("[PatrimWeb] Erro ao fechar conexão: " + e.getMessage());
            }
        }
    }
}
