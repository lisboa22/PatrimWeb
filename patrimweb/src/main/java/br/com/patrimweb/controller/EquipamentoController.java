package br.com.patrimweb.controller;

import br.com.patrimweb.dao.EquipamentoDAO;
import br.com.patrimweb.dao.FabricanteDAO;
import br.com.patrimweb.model.Equipamento;
import br.com.patrimweb.model.Fabricante;
import br.com.patrimweb.model.Usuario;
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
 * Controller responsável pelo gerenciamento de Equipamentos.
 *
 * Responsabilidades:
 * - Controlar requisições relacionadas a CRUD de equipamentos.
 * - Interagir com a camada DAO para persistência no banco de dados.
 * - Validar sessão do usuário.
 * - Encaminhar dados para a view (equipamentos.jsp).
 *
 * Atua como camada intermediária entre a interface web (JSP)
 * e a camada de acesso a dados (DAO).
 */
@WebServlet("/EquipamentoController")
public class EquipamentoController extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // DAOs responsáveis pela persistência no banco de dados
    private EquipamentoDAO equipamentoDAO;
    private FabricanteDAO fabricanteDAO;

    /**
     * Método executado na inicialização do Servlet.
     *
     * Responsável por instanciar os DAOs com conexão ativa.
     * Caso ocorra falha na criação, a aplicação interrompe a inicialização.
     */
    @Override
    public void init() throws ServletException {
        try {
            equipamentoDAO = new EquipamentoDAO(Conexao.getConnection());
            fabricanteDAO = new FabricanteDAO(Conexao.getConnection());
        } catch (Exception e) {
            throw new ServletException("Erro ao inicializar DAOs de Equipamento", e);
        }
    }

    /**
     * Processa requisições HTTP GET.
     *
     * Fluxo:
     * - Valida sessão ativa.
     * - Impede cache da página.
     * - Lista equipamentos cadastrados.
     *
     * @param request  dados da requisição
     * @param response dados da resposta
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    	
    	// 🔐 Verificação de sessão
        // Regra de segurança: somente usuários autenticados acessam o módulo.
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuarioLogado") == null) {
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }

        // 🧼 Evita cache da página
        // Garante que dados sensíveis não fiquem armazenados no navegador.
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        
        try {
            listarEquipamentos(request, response);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    /**
     * Processa requisições HTTP POST.
     *
     * Controla as ações de CRUD com base no parâmetro "action".
     *
     * Ações possíveis:
     * - adicionar
     * - editar
     * - deletar
     *
     * @param request  dados da requisição
     * @param response dados da resposta
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Define codificação para evitar problemas com acentuação
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");

        String action = request.getParameter("action");

        try {
            if (action == null) {
                listarEquipamentos(request, response);
                return;
            }

            // Estrutura de decisão que direciona para a regra de negócio correta
            switch (action) {
                case "adicionar":
                    adicionarEquipamento(request, response);
                    break;
                case "editar":
                    editarEquipamento(request, response);
                    break;
                case "deletar":
                    deletarEquipamento(request, response);
                    break;
                default:
                    listarEquipamentos(request, response);
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
     * Realiza a inserção de um novo equipamento.
     *
     * Regras de negócio:
     * - O fabricante deve existir previamente.
     * - A data de inserção é gerada automaticamente com timestamp atual.
     *
     * Interações com banco:
     * - Busca fabricante por ID.
     * - Insere novo equipamento.
     *
     * @param request  requisição contendo nome e fabricante
     * @param response resposta para redirecionamento
     */
    private void adicionarEquipamento(HttpServletRequest request, HttpServletResponse response)
            throws Exception {    
  
    	try {
	    	String nomeEquip = request.getParameter("nome_equip");
	    	String numserieEquip = request.getParameter("numero_equip");
	        int idFabricante = Integer.parseInt(request.getParameter("id_fabricante"));
	
	        // Recupera objeto Fabricante completo a partir do ID informado
	        Fabricante fabricante = fabricanteDAO.buscarPorId(idFabricante);
	
	        // Gera data/hora atual para registro de criação
	        Timestamp dataInsercao = Timestamp.valueOf(LocalDateTime.now());
	
	        // Cria entidade Equipamento associando fabricante e timestamp
	        Equipamento equipamento = new Equipamento(nomeEquip, numserieEquip, fabricante, dataInsercao);
	
	        // Persiste no banco de dados
	        equipamentoDAO.adicionarEquipamento(equipamento);
	        
	        request.getSession().setAttribute("mensagemSucesso", "Equipamento cadastrado com sucesso!");
    	}  catch (Exception e) {
    		request.getSession().setAttribute("mensagemErro", "Erro ao cadastrar movimentação.");
            throw e;
    	}

        // Redireciona para evitar reenvio de formulário (Post-Redirect-Get)
        response.sendRedirect(request.getContextPath() + "/EquipamentoController");
    }

    // ==========================
    // EDITAR
    // ==========================

    /**
     * Atualiza dados de um equipamento existente.
     *
     * Regras:
     * - O equipamento deve existir.
     * - O fabricante informado deve existir.
     *
     * Interações com banco:
     * - Busca fabricante por ID.
     * - Atualiza registro do equipamento.
     *
     * @param request  contém id do equipamento e novos dados
     * @param response redirecionamento após atualização
     */
    private void editarEquipamento(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
    	try {
	        int idEquip = Integer.parseInt(request.getParameter("id_equip"));
	        String nomeEquip = request.getParameter("nome_equip");
	        String numserieEquip = request.getParameter("numero_equip");
	        int idFabricante = Integer.parseInt(request.getParameter("id_fabricante"));
	
	        // Busca fabricante correspondente
	        Fabricante fabricante = fabricanteDAO.buscarPorId(idFabricante);
	    
	        // Cria objeto com dados atualizados
	        Equipamento equipamento = new Equipamento(idEquip, nomeEquip, numserieEquip, fabricante);
	
	        // Atualiza no banco
	        equipamentoDAO.alterarEquipamento(equipamento);
	        request.getSession().setAttribute("mensagemSucesso", "Equipamento alterado com sucesso!");
    	} catch (Exception e) {
            request.getSession().setAttribute("mensagemErro", "Erro ao alterar equipamento.");
            throw e;
    	}

        response.sendRedirect(request.getContextPath() + "/EquipamentoController");
    }

    // ==========================
    // DELETAR
    // ==========================

    /**
     * Exclui um equipamento pelo ID.
     *
     * Regras críticas:
     * - Caso existam movimentações vinculadas (Foreign Key),
     *   a exclusão é bloqueada pelo banco.
     *
     * Tratamento de exceções:
     * - Captura violação de integridade referencial.
     * - Armazena mensagens na sessão para exibição após redirect.
     *
     * @param request  contém id do equipamento
     * @param response redirecionamento para listagem
     */
    private void deletarEquipamento(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        
        // Captura ID enviado pela requisição
        String idStr = request.getParameter("id_equip");
        
        if (idStr != null && !idStr.isEmpty()) {
            try {
                int idEquip = Integer.parseInt(idStr);

                // Interação com banco: exclusão por ID
                equipamentoDAO.excluirEquipamento(idEquip);
                
                // Mensagem de sucesso persistida na sessão
                request.getSession().setAttribute("mensagemSucesso", "Equipamento excluído com sucesso!");
                
            } catch (java.sql.SQLIntegrityConstraintViolationException e) {
                // Tratamento específico para violação de chave estrangeira
                request.getSession().setAttribute("mensagemErro", 
                    "Não é possível excluir: este equipamento possui registros de movimentação vinculados a ele.");
                    
            } catch (Exception e) {
                // Tratamento genérico para falhas inesperadas
                request.getSession().setAttribute("mensagemErro", "Erro inesperado ao tentar excluir o equipamento.");
                e.printStackTrace();
            }
        }

        // Redireciona após tentativa de exclusão
        response.sendRedirect(request.getContextPath() + "/EquipamentoController");
    }

    // ==========================
    // LISTAR
    // ==========================

    /**
     * Lista todos os equipamentos cadastrados.
     *
     * Também carrega lista de fabricantes para:
     * - Preencher <select> no formulário/modal de cadastro e edição.
     *
     * Interações com banco:
     * - Consulta todos os equipamentos.
     * - Consulta todos os fabricantes.
     *
     * @param request  requisição HTTP
     * @param response resposta HTTP
     */
    private void listarEquipamentos(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        
        // Busca lista completa de equipamentos
        List<Equipamento> listaEquipamentos = equipamentoDAO.listarEquipamentos();

        // Busca lista de fabricantes para associação
        List<Fabricante> listaFabricantes = fabricanteDAO.listarFabricantes();
        
        // Envia dados para a camada de visualização
        request.setAttribute("equipamentos", listaEquipamentos);
        request.setAttribute("fabricantes", listaFabricantes);

        // Encaminha para página JSP
        RequestDispatcher dispatcher = request.getRequestDispatcher("/pages/equipamentos.jsp");
        dispatcher.forward(request, response);
    }
}
