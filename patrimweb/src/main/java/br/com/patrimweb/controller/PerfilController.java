package br.com.patrimweb.controller;

	import br.com.patrimweb.dao.PerfilDAO;
	import br.com.patrimweb.model.Perfil;
	import br.com.patrimweb.utils.Conexao;

	import javax.servlet.RequestDispatcher;
	import javax.servlet.ServletException;
	import javax.servlet.annotation.WebServlet;
	import javax.servlet.http.*;

	import java.io.IOException;
	import java.util.List;

	/**
	 * Controller responsável pelo gerenciamento de Perfis de acesso.
	 */
	@WebServlet("/PerfilController")
	public class PerfilController extends HttpServlet {

	    private static final long serialVersionUID = 1L;
	    private PerfilDAO perfilDAO;

	    @Override
	    public void init() throws ServletException {
	        try {
	            perfilDAO = new PerfilDAO(Conexao.getConnection());
	        } catch (Exception e) {
	            throw new ServletException("Erro ao inicializar PerfilDAO", e);
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

	        response.setHeader("Cache-Control","no-cache, no-store, must-revalidate");
	        response.setHeader("Pragma","no-cache");
	        response.setDateHeader("Expires",0);

	        try {
	            listarPerfis(request, response);
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
	                listarPerfis(request, response);
	                return;
	            }

	            switch (action) {
	                case "adicionar":
	                    adicionarPerfil(request, response);
	                    break;
	                case "editar":
	                    editarPerfil(request, response);
	                    break;
	                case "excluir":
	                    deletarPerfil(request, response);
	                    break;
	                default:
	                    listarPerfis(request, response);
	            }

	        } catch (Exception e) {
	            throw new ServletException(e);
	        }
	    }

	    // ==========================
	    // ADICIONAR
	    // ==========================
	    private void adicionarPerfil(HttpServletRequest request, HttpServletResponse response)
	            throws Exception {

	        try {
	            String nome = request.getParameter("nome_perfil");

	            Perfil perfil = new Perfil(nome);
	            System.out.println("Nome: " + perfil);
	            perfilDAO.adicionarPerfil(perfil);

	            request.getSession().setAttribute("mensagemSucesso",
	                    "Perfil cadastrado com sucesso!");
	        } catch (Exception e) {
	            request.getSession().setAttribute("mensagemErro",
	                    "Erro ao cadastrar perfil.");
	            throw e;
	        }

	        response.sendRedirect(request.getContextPath() + "/PerfilController");
	    }

	    // ==========================
	    // EDITAR
	    // ==========================
	    private void editarPerfil(HttpServletRequest request, HttpServletResponse response)
	            throws Exception {

	        try {
	            int id = Integer.parseInt(request.getParameter("id_perfil"));
	            String nome = request.getParameter("nome_perfil");

	            Perfil perfil = new Perfil(id, nome);

	            perfilDAO.alterarPerfil(perfil);

	            request.getSession().setAttribute("mensagemSucesso",
	                    "Perfil alterado com sucesso!");
	        } catch (Exception e) {
	            request.getSession().setAttribute("mensagemErro",
	                    "Erro ao alterar perfil.");
	            throw e;
	        }

	        response.sendRedirect(request.getContextPath() + "/PerfilController");
	    }

	    // ==========================
	    // DELETAR
	    // ==========================
	    private void deletarPerfil(HttpServletRequest request, HttpServletResponse response)
	            throws Exception {

	        try {
	            int id = Integer.parseInt(request.getParameter("id_perfil"));

	            perfilDAO.excluirPerfil(id);

	            request.getSession().setAttribute("mensagemSucesso",
	                    "Perfil excluído com sucesso!");
	        } catch (Exception e) {
	            request.getSession().setAttribute("mensagemErro",
	                    "Erro ao excluir perfil.");
	        }

	        response.sendRedirect(request.getContextPath() + "/PerfilController");
	    }

	    // ==========================
	    // LISTAR
	    // ==========================
	    private void listarPerfis(HttpServletRequest request, HttpServletResponse response)
	            throws Exception {

	        List<Perfil> perfis = perfilDAO.listarPerfis();

	        request.setAttribute("perfis", perfis);

	        RequestDispatcher dispatcher =
	                request.getRequestDispatcher("/pages/perfil.jsp");

	        dispatcher.forward(request, response);
	    }
	}

