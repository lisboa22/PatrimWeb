package br.com.patrimweb.controller;

	import br.com.patrimweb.dao.PermissaoDAO;
	import br.com.patrimweb.model.Permissao;
	import br.com.patrimweb.utils.Conexao;

	import javax.servlet.RequestDispatcher;
	import javax.servlet.ServletException;
	import javax.servlet.annotation.WebServlet;
	import javax.servlet.http.*;

	import java.io.IOException;
	import java.util.List;

	/**
	 * Controller responsável pelo gerenciamento das permissões do sistema.
	 */
	@WebServlet("/PermissaoController")
	public class PermissaoController extends HttpServlet {

	    private static final long serialVersionUID = 1L;
	    private PermissaoDAO permissaoDAO;

	    @Override
	    public void init() throws ServletException {
	        try {
	            permissaoDAO = new PermissaoDAO(Conexao.getConnection());
	        } catch (Exception e) {
	            throw new ServletException("Erro ao inicializar PermissaoDAO", e);
	        }
	    }

	    // ==========================
	    // GET
	    // ==========================
	    @Override
	    protected void doGet(HttpServletRequest request, HttpServletResponse response)
	            throws ServletException, IOException {

	        HttpSession session = request.getSession(false);

	        if (session == null || session.getAttribute("usuarioLogado") == null) {
	            response.sendRedirect(request.getContextPath() + "/");
	            return;
	        }

	        try {
	            listarPermissoes(request, response);
	        } catch (Exception e) {
	            throw new ServletException(e);
	        }
	    }

	    // ==========================
	    // POST
	    // ==========================
	    @Override
	    protected void doPost(HttpServletRequest request, HttpServletResponse response)
	            throws ServletException, IOException {

	        request.setCharacterEncoding("UTF-8");

	        try {

	            String action = request.getParameter("action");

	            if (action == null) {
	                listarPermissoes(request, response);
	                return;
	            }

	            switch (action) {
	                case "adicionar":
	                    adicionarPermissao(request, response);
	                    break;
	                case "editar":
	                    editarPermissao(request, response);
	                    break;
	                case "excluir":
	                    deletarPermissao(request, response);
	                    break;
	                default:
	                    listarPermissoes(request, response);
	            }

	        } catch (Exception e) {
	            throw new ServletException(e);
	        }
	    }

	    // ==========================
	    // ADICIONAR
	    // ==========================
	    private void adicionarPermissao(HttpServletRequest request, HttpServletResponse response)
	            throws Exception {

	        try {
	            Permissao permissao = new Permissao(
	                    request.getParameter("modulo"),
	                    request.getParameter("acao_permissao"),
	                    request.getParameter("descricao")
	            );

	            permissaoDAO.adicionarPermissao(permissao);

	            request.getSession().setAttribute("mensagemSucesso",
	                    "Permissão cadastrada com sucesso!");
	        } catch (Exception e) {
	            request.getSession().setAttribute("mensagemErro",
	                    "Erro ao cadastrar permissão.");
	            throw e;
	        }

	        response.sendRedirect(request.getContextPath() + "/PermissaoController");
	    }

	    // ==========================
	    // EDITAR
	    // ==========================
	    private void editarPermissao(HttpServletRequest request, HttpServletResponse response)
	            throws Exception {

	        try {
	            Permissao permissao = new Permissao(
	                    Integer.parseInt(request.getParameter("id_permissao")),
	                    request.getParameter("modulo"),
	                    request.getParameter("acao"),
	                    request.getParameter("descricao")
	            );

	            permissaoDAO.alterarPermissao(permissao);

	            request.getSession().setAttribute("mensagemSucesso",
	                    "Permissão alterada com sucesso!");
	        } catch (Exception e) {
	            request.getSession().setAttribute("mensagemErro",
	                    "Erro ao alterar permissão.");
	            throw e;
	        }

	        response.sendRedirect(request.getContextPath() + "/PermissaoController");
	    }

	    // ==========================
	    // DELETAR
	    // ==========================
	    private void deletarPermissao(HttpServletRequest request, HttpServletResponse response)
	            throws Exception {

	        try {
	            int id = Integer.parseInt(request.getParameter("id_permissao"));

	            permissaoDAO.excluirPermissao(id);

	            request.getSession().setAttribute("mensagemSucesso",
	                    "Permissão excluída com sucesso!");
	        } catch (Exception e) {
	            request.getSession().setAttribute("mensagemErro",
	                    "Erro ao excluir permissão.");
	        }

	        response.sendRedirect(request.getContextPath() + "/PermissaoController");
	    }

	    // ==========================
	    // LISTAR
	    // ==========================
	    private void listarPermissoes(HttpServletRequest request, HttpServletResponse response)
	            throws Exception {

	        List<Permissao> lista = permissaoDAO.listarPermissoes();

	        request.setAttribute("permissoes", lista);

	        RequestDispatcher dispatcher =
	                request.getRequestDispatcher("/pages/permissao.jsp");

	        dispatcher.forward(request, response);
	    }
	}

