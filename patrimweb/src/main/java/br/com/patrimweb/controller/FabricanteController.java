package br.com.patrimweb.controller;

import br.com.patrimweb.dao.FabricanteDAO;
import br.com.patrimweb.model.Fabricante;
import br.com.patrimweb.utils.Conexao;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller responsável pelo gerenciamento de Fabricantes.
 *
 * Responsabilidades:
 * - Controlar operações de CRUD (Create, Read, Update, Delete).
 * - Interagir com a camada DAO para persistência no banco de dados.
 * - Validar sessão do usuário autenticado.
 * - Encaminhar dados para a view (fabricantes.jsp).
 *
 * Atua como camada intermediária entre a interface JSP e o banco de dados.
 */
@WebServlet("/FabricanteController")
public class FabricanteController extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // DAO responsável pelas operações de persistência da entidade Fabricante
    private FabricanteDAO fabricanteDAO;

    /**
     * Método executado na inicialização do Servlet.
     *
     * Responsável por instanciar o FabricanteDAO com conexão ativa.
     * Caso ocorra falha na conexão, a aplicação interrompe a inicialização.
     */
    @Override
    public void init() throws ServletException {
        try {
            fabricanteDAO = new FabricanteDAO(Conexao.getConnection());
        } catch (Exception e) {
            throw new ServletException("Erro ao inicializar FabricanteDAO", e);
        }
    }

    /**
     * Processa requisições HTTP GET.
     *
     * Fluxo:
     * - Verifica se o usuário está autenticado.
     * - Impede cache da página.
     * - Lista fabricantes cadastrados.
     *
     * @param request  objeto da requisição HTTP
     * @param response objeto da resposta HTTP
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    	
    	// 🔐 Verificação de sessão
        // Regra de segurança: somente usuários autenticados podem acessar o módulo.
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuarioLogado") == null) {
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }

        // 🧼 Evita cache da página
        // Garante que o navegador não armazene informações sensíveis.
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        
        
        try {
            listarFabricantes(request, response);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    /**
     * Processa requisições HTTP POST.
     *
     * Controla as ações com base no parâmetro "action".
     *
     * Ações suportadas:
     * - adicionar
     * - editar
     * - deletar
     *
     * @param request  objeto da requisição
     * @param response objeto da resposta
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Define codificação para evitar problemas com caracteres especiais
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");

        try {
            String action = request.getParameter("action");
           
            // Caso nenhuma ação seja enviada, executa listagem padrão
            if (action == null) {
                listarFabricantes(request, response);
                return;
            }

            // Estrutura de decisão responsável por direcionar a regra de negócio
            switch (action) {
                case "adicionar":
                    adicionarFabricante(request, response);
                    break;
                case "editar":
                    editarFabricante(request, response);
                    break;
                case "deletar":
                    deletarFabricante(request, response);
                    break;
                default:
                    listarFabricantes(request, response);
                    break;
            }

        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    // ==========================
    // ADICIONAR
    // ==========================

    /**
     * Realiza a inserção de um novo fabricante.
     *
     * Regras de negócio:
     * - O nome do fabricante é obrigatório (validação esperada na camada de interface).
     * - A data de inserção é gerada automaticamente com o timestamp atual.
     *
     * Interação com banco:
     * - Insere novo registro através do FabricanteDAO.
     *
     * @param request  contém nome do fabricante
     * @param response redirecionamento após inserção
     */
    private void adicionarFabricante(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

    	try {
	        String nomeFab = request.getParameter("nome_fab");
	
	        // Gera data/hora atual para registrar momento da criação
	        Timestamp dataInsercao = Timestamp.valueOf(LocalDateTime.now());
	
	        // Cria objeto Fabricante com dados recebidos
	        Fabricante fabricante = new Fabricante(
	                nomeFab,
	                dataInsercao
	        );
	
	        // Persiste no banco de dados
	        fabricanteDAO.adicionarFabricante(fabricante);
	        request.getSession().setAttribute("mensagemSucesso", "Fabricante cadastrado com sucesso!");
    	}  catch (Exception e) {
    		request.getSession().setAttribute("mensagemErro", "Erro ao cadastrar fabricante.");
            throw e;
    	}

        // Redireciona para evitar reenvio do formulário (padrão PRG)
        response.sendRedirect(request.getContextPath() + "/FabricanteController");
    }

    // ==========================
    // EDITAR
    // ==========================

    /**
     * Atualiza os dados de um fabricante existente.
     *
     * Regras:
     * - O fabricante deve existir.
     * - A data de inserção é atualizada com o timestamp atual.
     *
     * Interação com banco:
     * - Atualiza registro através do FabricanteDAO.
     *
     * @param request  contém id e novos dados do fabricante
     * @param response redirecionamento após atualização
     */
    private void editarFabricante(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        try {
	    	int idFab = Integer.parseInt(request.getParameter("id_fab"));
	        String nomeFab = request.getParameter("nome_fab");
	
	        // Atualiza timestamp para momento da edição
	        Timestamp dataInsercao = Timestamp.valueOf(LocalDateTime.now());
	
	        // Cria objeto Fabricante com dados atualizados
	        Fabricante fabricante = new Fabricante(
	                idFab,
	                nomeFab,
	                dataInsercao
	        );
	
	        // Executa atualização no banco
	        fabricanteDAO.alterarFabricante(fabricante);
	        request.getSession().setAttribute("mensagemSucesso", "Fabricante alterado com sucesso!");
    	} catch (Exception e) {
            request.getSession().setAttribute("mensagemErro", "Erro ao alterar fabricante.");
            throw e;
    	}

        response.sendRedirect(request.getContextPath() + "/FabricanteController");
    }

    // ==========================
    // DELETAR
    // ==========================

    /**
     * Exclui um fabricante pelo ID.
     *
     * Regra crítica:
     * - Caso existam equipamentos vinculados (Foreign Key),
     *   a exclusão será bloqueada pelo banco de dados.
     *
     * Tratamento de exceções:
     * - Captura violação de integridade referencial.
     * - Armazena mensagens na sessão para exibição após redirect.
     *
     * @param request  contém id do fabricante
     * @param response redirecionamento para listagem
     */
    private void deletarFabricante(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        
        // Captura ID enviado pela requisição
        String idStr = request.getParameter("id_fab");
        
        if (idStr != null && !idStr.isEmpty()) {
            try {
                int idFab = Integer.parseInt(idStr);
                
                // Interação com banco: tentativa de exclusão
                fabricanteDAO.excluirFabricante(idFab);
                
                // Mensagem de sucesso armazenada na sessão
                request.getSession().setAttribute("mensagemSucesso", "Fabricante excluído com sucesso!");
                
            } catch (java.sql.SQLIntegrityConstraintViolationException e) {
                // Tratamento específico para erro de chave estrangeira
                request.getSession().setAttribute("mensagemErro", 
                    "Não é possível excluir: este fabricante possui equipamentos vinculados a ele.");
                    
            } catch (Exception e) {
                // Tratamento genérico para falhas inesperadas
                request.getSession().setAttribute("mensagemErro", "Erro inesperado ao tentar excluir o fabricante.");
                e.printStackTrace();
            }
        }

        // Redireciona independentemente do resultado,
        // levando as mensagens armazenadas na sessão
        response.sendRedirect(request.getContextPath() + "/FabricanteController");
    }

    // ==========================
    // LISTAR
    // ==========================

    /**
     * Lista todos os fabricantes cadastrados.
     *
     * Interação com banco:
     * - Consulta completa da tabela de fabricantes.
     *
     * @param request  requisição HTTP
     * @param response resposta HTTP
     */
    private void listarFabricantes(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        // Recupera lista de fabricantes do banco
        List<Fabricante> listaFabricantes = fabricanteDAO.listarFabricantes();

        // Envia lista para a camada de visualização
        request.setAttribute("fabricantes", listaFabricantes);

        // Encaminha para a página JSP responsável pela exibição
        RequestDispatcher dispatcher =
                request.getRequestDispatcher("/pages/fabricantes.jsp");
        dispatcher.forward(request, response);
    }
}
