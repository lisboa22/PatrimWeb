package br.com.patrimweb.controller;

import br.com.patrimweb.dao.UnidadeDAO;
import br.com.patrimweb.model.Unidade;
import br.com.patrimweb.utils.Conexao;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller responsável pelo gerenciamento das operações CRUD
 * relacionadas à entidade Unidade.
 *
 * <p>
 * Esta classe atua como camada de controle (Controller) dentro do padrão MVC,
 * sendo responsável por intermediar a comunicação entre a View (JSP) e a
 * camada de persistência (DAO).
 * </p>
 *
 * <p>
 * Responsabilidades principais:
 * - Validar acesso do usuário através de sessão autenticada.
 * - Receber requisições HTTP e direcionar para a operação adequada.
 * - Manipular dados recebidos dos formulários.
 * - Invocar operações de persistência através do UnidadeDAO.
 * - Encaminhar dados para as páginas JSP.
 * </p>
 *
 * <p>
 * Regras de negócio aplicadas:
 * - Apenas usuários autenticados podem acessar as operações.
 * - A data de inserção/alteração é sempre gerada no servidor.
 * - Exclusões respeitam integridade referencial do banco.
 * - Mensagens de sucesso e erro são persistidas na sessão para exibição após redirect.
 * </p>
 */
@WebServlet("/UnidadeController")
public class UnidadeController extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // DAO responsável pelas operações de persistência da entidade Unidade.
    // Centraliza todas as interações com o banco de dados.
    private UnidadeDAO unidadeDAO;

    /**
     * Método executado automaticamente pelo container na inicialização do Servlet.
     *
     * Objetivo:
     * - Inicializar recursos necessários antes do processamento das requisições.
     * - Criar a instância do DAO utilizando conexão ativa com o banco.
     *
     * Ponto crítico:
     * Caso ocorra falha aqui, o servlet não será carregado corretamente,
     * impedindo o funcionamento do módulo.
     *
     * @throws ServletException caso ocorra erro ao inicializar o DAO.
     */
    @Override
    public void init() throws ServletException {
        try {
            // Inicializa o DAO com a conexão do banco obtida pela classe utilitária.
            // Esta conexão será reutilizada durante o ciclo de vida do servlet.
            unidadeDAO = new UnidadeDAO(Conexao.getConnection());
        } catch (Exception e) {
            // Falha crítica de inicialização: impede subida do servlet.
            throw new ServletException("Erro ao inicializar UnidadeDAO", e);
        }
    }

    /**
     * Processa requisições HTTP GET.
     *
     * Regra geral:
     * - GET é utilizado para operações de leitura (listagem).
     *
     * Fluxo:
     * 1) Valida existência de sessão autenticada.
     * 2) Impede cache da página protegida.
     * 3) Carrega a listagem de unidades.
     *
     * @param request  contém dados da requisição HTTP.
     * @param response responsável por enviar resposta ao cliente.
     *
     * @throws ServletException erro interno de processamento.
     * @throws IOException      erro de comunicação.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    	
    	// 🔐 Verificação de sessão
        // Recupera sessão existente sem criar nova sessão automaticamente.
        // Isso evita que usuários não autenticados ganhem uma sessão válida.
        HttpSession session = request.getSession(false);

        // Regra de segurança:
        // Caso não exista sessão ou usuário autenticado, redireciona para página inicial.
        if (session == null || session.getAttribute("usuarioLogado") == null) {
            response.sendRedirect(request.getContextPath() + "/");
            return; // Interrompe execução para impedir acesso indevido
        }

        // 🧼 Evita cache do dashboard
        // Garante que páginas protegidas não fiquem armazenadas no navegador,
        // evitando acesso após logout utilizando botão "voltar".
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        
        
        try {
            // Delegação da responsabilidade de listagem para método específico.
            listarUnidades(request, response);
        } catch (Exception e) {
            // Encapsula exceções internas no padrão de erro do servlet.
            throw new ServletException(e);
        }
    }

    /**
     * Processa requisições HTTP POST.
     *
     * Responsabilidade:
     * - Controlar operações de escrita (Create, Update e Delete).
     *
     * Fluxo:
     * 1) Define codificação UTF-8 para evitar problemas com caracteres especiais.
     * 2) Lê parâmetro "action" enviado pelo formulário.
     * 3) Direciona a execução conforme a ação solicitada.
     *
     * Estrutura de decisão:
     * Utiliza switch para separar claramente cada operação CRUD.
     *
     * @param request  contém parâmetros enviados pelo formulário.
     * @param response envia resposta ao cliente.
     *
     * @throws ServletException erro interno de processamento.
     * @throws IOException      erro de comunicação.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Define codificação para evitar problemas com acentuação e caracteres especiais.
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");

        try {
            // Parâmetro responsável por indicar qual operação será executada.
            String action = request.getParameter("action");

            // Regra de fallback:
            // Caso nenhuma ação seja enviada, realiza apenas a listagem.
            if (action == null) {
                listarUnidades(request, response);
                return;
            }

            // Estrutura decisória principal do controller.
            // Direciona a execução para o método correspondente à ação solicitada.
            switch (action) {
                case "adicionar":
                    adicionarUnidade(request, response);
                    break;
                case "editar":
                    editarUnidade(request, response);
                    break;
                case "deletar":
                    deletarUnidade(request, response);
                    break;
                default:
                    // Segurança lógica: qualquer ação desconhecida retorna para listagem.
                    listarUnidades(request, response);
                    break;
            }
        } catch (Exception e) {
            // Propaga exceções encapsuladas como erro de servlet.
            throw new ServletException(e);
        }
    }

    // ==========================
    // ADICIONAR
    // ==========================

    /**
     * Insere uma nova unidade no banco de dados.
     *
     * Regras de negócio:
     * - Dados são recebidos do formulário HTTP.
     * - A data de inserção NÃO vem do cliente.
     * - O timestamp é gerado no servidor para garantir integridade.
     * - Utiliza padrão PRG (Post/Redirect/Get) para evitar duplicidade.
     *
     * Interação com banco:
     * - Realiza INSERT através do UnidadeDAO.
     *
     * @param request  contém dados enviados pelo formulário.
     * @param response utilizado para redirecionamento após operação.
     *
     * @throws Exception erros provenientes da camada DAO.
     */
    private void adicionarUnidade(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        
    	try {
	        // Captura parâmetros enviados via formulário HTML.
	        String nomeUnid = request.getParameter("nome_unid");
	        String telefoneUnid = request.getParameter("telefone_unid");
	        String emailUnid = request.getParameter("email_unid");
	        String enderecoUnid = request.getParameter("endereco_unid");
	
	        // Regra de negócio crítica:
	        // A data é controlada exclusivamente pelo servidor.
	        Timestamp dataInsercao = Timestamp.valueOf(LocalDateTime.now());
	
	        // Criação do objeto de domínio que representa a entidade Unidade.
	        Unidade unidade = new Unidade(
	                nomeUnid,
	                telefoneUnid,
	                emailUnid,
	                enderecoUnid,
	                dataInsercao
	        );
	
	        // Persistência no banco através da camada DAO.
	        unidadeDAO.adicionarUnidade(unidade);

	        // Mensagem armazenada na sessão para ser exibida após redirect.
	        request.getSession().setAttribute("mensagemSucesso", "Unidade cadastrado com sucesso!");
    	}  catch (SQLException e) {

    	    // ERRO UNIQUE (email duplicado)
    	    if (e.getErrorCode() == 1062) {

    	    	String mensagem = e.getMessage();

    	        if (mensagem.contains("uk_unidade_email")) {

    	            request.getSession().setAttribute(
    	                "mensagemErro",
    	                "Já existe uma unidade cadastrada com este e-mail."
    	            );

    	        }/* else if (mensagem.contains("uk_usuario_cnpj")) {

    	            request.getSession().setAttribute(
    	                "mensagemErro",
    	                "Já existe uma unidade cadastrada com este CNPJ."
    	            );

    	        }*/ else {

    	            request.getSession().setAttribute(
    	                "mensagemErro",
    	                "Registro duplicado no sistema."
    	            );
    	        }

    	        response.sendRedirect(request.getContextPath() + "/UnidadeController");
    	        return; // interrompe o fluxo normal
    	    }

    	    // outros erros SQL continuam
    	    request.getSession().setAttribute("mensagemErro", "Erro ao cadastrar unidade.");
    	    throw e;
    	} catch (Exception e) {
    		// Registro de erro funcional para feedback ao usuário.
    		request.getSession().setAttribute("mensagemErro", "Erro ao cadastrar unidade.");
            throw e;
    	}

        // PRG Pattern:
        // Evita reenvio automático do formulário ao atualizar a página.
        response.sendRedirect(request.getContextPath() + "/UnidadeController");
    }

    // ==========================
    // EDITAR
    // ==========================

    /**
     * Atualiza uma unidade existente no banco de dados.
     *
     * Regras de negócio:
     * - O ID da unidade deve ser informado.
     * - A data é atualizada para o momento da edição.
     *
     * Interação com banco:
     * - Realiza UPDATE através do UnidadeDAO.
     *
     * @param request  contém dados atualizados da unidade.
     * @param response utilizado para redirecionamento após operação.
     *
     * @throws Exception erros provenientes da camada DAO.
     */
    private void editarUnidade(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        
        try {
	    	// Conversão do ID recebido como String para inteiro.
	        int idUnid = Integer.parseInt(request.getParameter("id_unid"));
	
	        // Captura dos novos valores enviados pelo formulário.
	        String nomeUnid = request.getParameter("nome_unid");
	        String telefoneUnid = request.getParameter("telefone_unid");
	        String emailUnid = request.getParameter("email_unid");
	        String enderecoUnid = request.getParameter("endereco_unid");
	
	        // Regra atual:
	        // A data passa a representar o momento da última alteração.
	        Timestamp dataInsercao = Timestamp.valueOf(LocalDateTime.now());
	
	        // Instancia objeto com ID para atualização no banco.
	        Unidade unidade = new Unidade(
	                idUnid,
	                nomeUnid,
	                telefoneUnid,
	                emailUnid,
	                enderecoUnid,
	                dataInsercao
	        );
	
	        // Atualização persistida via DAO.
	        unidadeDAO.alterarUnidade(unidade);

	        // Feedback positivo armazenado na sessão.
	        request.getSession().setAttribute("mensagemSucesso", "Unidade alterado com sucesso!");
    	} catch (SQLException e) {

    	    // ERRO UNIQUE (email duplicado)
    	    if (e.getErrorCode() == 1062) {

    	    	String mensagem = e.getMessage();

    	        if (mensagem.contains("uk_unidade_email")) {

    	            request.getSession().setAttribute(
    	                "mensagemErro",
    	                "Já existe uma unidade cadastrada com este e-mail."
    	            );

    	        }/* else if (mensagem.contains("uk_usuario_cnpj")) {

    	            request.getSession().setAttribute(
    	                "mensagemErro",
    	                "Já existe uma unidade cadastrada com este CNPJ."
    	            );

    	        }*/ else {

    	            request.getSession().setAttribute(
    	                "mensagemErro",
    	                "Registro duplicado no sistema."
    	            );
    	        }

    	        response.sendRedirect(request.getContextPath() + "/UnidadeController");
    	        return; // interrompe o fluxo normal
    	    }

    	    // outros erros SQL continuam
    	    request.getSession().setAttribute("mensagemErro", "Erro ao cadastrar usuário.");
    	    throw e;
    	} catch (Exception e) {
            // Feedback de erro funcional.
            request.getSession().setAttribute("mensagemErro", "Erro ao alterar unidade.");
            throw e;
    	}

        // Redirecionamento para atualização da listagem.
        response.sendRedirect(request.getContextPath() + "/UnidadeController");
    }

    // ==========================
    // DELETAR
    // ==========================

    /**
     * Exclui uma unidade do sistema.
     *
     * Regras de negócio:
     * - Exclusão só ocorre se não houver vínculos com outras entidades.
     * - Respeita integridade referencial do banco.
     *
     * Interação com banco:
     * - Executa DELETE através do UnidadeDAO.
     *
     * Tratamento especial:
     * - Captura exceção de violação de chave estrangeira.
     *
     * @param request  contém o ID da unidade a ser excluída.
     * @param response utilizado para redirecionamento.
     *
     * @throws Exception erros provenientes da camada DAO.
     */
    private void deletarUnidade(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        // Captura o ID enviado pela requisição.
        String idStr = request.getParameter("id_unid");
        
        // Validação básica:
        // Evita tentativa de conversão quando valor é nulo ou vazio.
        if (idStr != null && !idStr.isEmpty()) {
            try {
                int idUnid = Integer.parseInt(idStr);
                
                // Operação de exclusão no banco de dados.
                unidadeDAO.excluirUnidade(idUnid);
                
                // Mensagem de sucesso persistida na sessão.
                request.getSession().setAttribute("mensagemSucesso", "Unidade excluída com sucesso!");
                
            } catch (java.sql.SQLIntegrityConstraintViolationException e) {
                // Regra de negócio crítica:
                // Não permite exclusão quando há dependências relacionadas.
                request.getSession().setAttribute("mensagemErro", 
                    "Não é possível excluir: esta unidade possui equipamentos ou movimentações vinculadas a ela.");
                    
            } catch (Exception e) {
                // Captura erros inesperados.
                request.getSession().setAttribute("mensagemErro", "Erro inesperado ao tentar excluir a unidade.");
                e.printStackTrace();
            }
        }

        // Redirecionamento para recarregar dados e exibir mensagens.
        response.sendRedirect(request.getContextPath() + "/UnidadeController");
    }

    // ==========================
    // LISTAR
    // ==========================

    /**
     * Carrega todas as unidades cadastradas e encaminha para a JSP.
     *
     * Interação com banco:
     * - Executa consulta SELECT através do UnidadeDAO.
     *
     * Fluxo:
     * 1) Consulta lista no banco.
     * 2) Anexa resultado como atributo da requisição.
     * 3) Encaminha para página de visualização.
     *
     * @param request  objeto da requisição HTTP.
     * @param response objeto da resposta HTTP.
     *
     * @throws Exception erros provenientes da camada DAO.
     */
    private void listarUnidades(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        // Consulta todas as unidades cadastradas no banco.
        List<Unidade> listaUnidades = unidadeDAO.listarUnidades();

        // Disponibiliza dados para a camada View (JSP).
        request.setAttribute("unidades", listaUnidades);
        
        // Encaminhamento interno (forward) preservando atributos da requisição.
        RequestDispatcher dispatcher = request.getRequestDispatcher("/pages/unidades.jsp");
        dispatcher.forward(request, response);
    }
}
