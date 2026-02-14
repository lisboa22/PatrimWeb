package br.com.patrimweb.controller;

import br.com.patrimweb.dao.UsuarioDAO;
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
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller responsável pelo gerenciamento das requisições relacionadas à entidade Usuario.
 *
 * Atua como camada intermediária entre a camada de visualização (JSP) e a camada de persistência (DAO),
 * centralizando as operações de:
 * - Listagem de usuários
 * - Adição de novos usuários
 * - Edição de usuários existentes
 * - Exclusão de usuários
 *
 * Esta classe segue o padrão MVC, onde:
 * - A View é representada pela página usuarios.jsp
 * - O Model é representado pela classe Usuario
 * - O acesso a dados é feito através da classe UsuarioDAO
 *
 * Todas as requisições são mapeadas para a URL "/UsuarioController".
 */
@WebServlet("/UsuarioController")
public class UsuarioController extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /**
     * Objeto responsável por realizar operações de persistência
     * no banco de dados relacionadas à entidade Usuario.
     */
    private UsuarioDAO usuarioDAO;

    /**
     * Método executado automaticamente pelo container no momento
     * da inicialização da Servlet.
     *
     * Responsável por instanciar o UsuarioDAO utilizando
     * uma conexão obtida através da classe utilitária Conexao.
     *
     * Regra importante:
     * Caso ocorra falha na criação do DAO ou na obtenção da conexão,
     * a aplicação interrompe a inicialização da Servlet lançando
     * uma ServletException.
     *
     * @throws ServletException caso ocorra erro ao inicializar o DAO.
     */
    @Override
    public void init() throws ServletException {
        try {
            usuarioDAO = new UsuarioDAO(Conexao.getConnection());
        } catch (Exception e) {
            throw new ServletException("Erro ao inicializar UsuarioDAO", e);
        }
    }

    /**
     * Método responsável por tratar requisições HTTP do tipo GET.
     *
     * Fluxo principal:
     * 1. Verifica se existe sessão válida.
     * 2. Impede cache da resposta (controle de segurança).
     * 3. Chama o método de listagem de usuários.
     *
     * Regra de segurança:
     * O acesso é permitido apenas se existir atributo "usuarioLogado"
     * na sessão. Caso contrário, o usuário é redirecionado para a raiz
     * da aplicação (possível tela de login).
     *
     * @param request  objeto contendo dados da requisição HTTP.
     * @param response objeto responsável por gerar a resposta HTTP.
     * @throws ServletException em caso de erro interno.
     * @throws IOException em caso de erro de I/O.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    	
    	// 🔐 Verificação de sessão
        HttpSession session = request.getSession(false);

        // Validação de sessão ativa e usuário autenticado
        if (session == null || session.getAttribute("usuarioLogado") == null) {
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }

        // 🧼 Evita cache do dashboard
        // Garante que páginas sensíveis não sejam armazenadas no navegador
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        
        try {
            listarUsuarios(request, response);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    /**
     * Método responsável por tratar requisições HTTP do tipo POST.
     *
     * A ação executada depende do parâmetro "action" enviado pelo formulário.
     *
     * Possíveis ações:
     * - adicionar
     * - editar
     * - deletar
     *
     * Caso nenhuma ação seja informada, o sistema realiza a listagem padrão.
     *
     * @param request  objeto contendo dados da requisição HTTP.
     * @param response objeto responsável por gerar a resposta HTTP.
     * @throws ServletException em caso de erro interno.
     * @throws IOException em caso de erro de I/O.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    	
    	// Define codificação para suportar caracteres especiais (acentuação)
    	request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");
       
        try {
        	 String action = request.getParameter("action");

            // Caso nenhuma ação seja enviada, executa listagem
            if (action == null) {
                listarUsuarios(request, response);
                return;
            }

            // Estrutura de decisão baseada na ação solicitada
            switch (action) {
                case "adicionar":
                    adicionarUsuario(request, response);
                    break;

                case "editar":
                    editarUsuario(request, response);
                    break;

                case "deletar":
                    deletarUsuario(request, response);
                    break;

                default:
                    listarUsuarios(request, response);
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
     * Responsável por adicionar um novo usuário ao sistema.
     *
     * Fluxo:
     * 1. Captura os parâmetros enviados pelo formulário.
     * 2. Gera a data de inserção no servidor.
     * 3. Instancia objeto Usuario.
     * 4. Persiste no banco via UsuarioDAO.
     * 5. Redireciona para a listagem.
     *
     * Regra de negócio:
     * A data de inserção é sempre gerada no servidor,
     * evitando manipulação pelo cliente.
     *
     * Interação com banco:
     * Chamada ao método usuarioDAO.adicionarUsuario(usuario).
     *
     * @param request  requisição HTTP com dados do formulário.
     * @param response resposta HTTP.
     * @throws Exception caso ocorra erro na persistência.
     */
    private void adicionarUsuario(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
    
        try {
	    	String nomeUsu = request.getParameter("nome_usu");
	        String cpfUsu = request.getParameter("cpf_usu");
	        String telefoneUsu = request.getParameter("telefone_usu");
	        String emailUsu = request.getParameter("email_usu");
	        String enderecoUsu = request.getParameter("endereco_usu");
	
	        // Conversão de String para Boolean
	        Boolean login_google = Boolean.parseBoolean(request.getParameter("login_google"));
	
	        String senhaUsu = request.getParameter("senha_usu");
	
	        // Data gerada no servidor
	        Timestamp dataInsercao = Timestamp.valueOf(LocalDateTime.now());
	        
	        Usuario usuario = new Usuario(
	                nomeUsu,
	                cpfUsu,
	                telefoneUsu,
	                emailUsu,
	                enderecoUsu,
	                dataInsercao,
	                login_google,
	                senhaUsu
	        );
	
	
	        // Persistência no banco de dados
	        usuarioDAO.adicionarUsuario(usuario);
	        request.getSession().setAttribute("mensagemSucesso", "Usuário cadastrado com sucesso!");
    	} catch (SQLException e) {

    	    // ERRO UNIQUE (email duplicado)
    	    if (e.getErrorCode() == 1062) {

    	    	String mensagem = e.getMessage();

    	        if (mensagem.contains("uk_usuario_email")) {

    	            request.getSession().setAttribute(
    	                "mensagemErro",
    	                "Já existe um usuário cadastrado com este e-mail."
    	            );

    	        } else if (mensagem.contains("uk_usuario_cpf")) {

    	            request.getSession().setAttribute(
    	                "mensagemErro",
    	                "Já existe um usuário cadastrado com este CPF."
    	            );

    	        } else {

    	            request.getSession().setAttribute(
    	                "mensagemErro",
    	                "Registro duplicado no sistema."
    	            );
    	        }

    	        response.sendRedirect(request.getContextPath() + "/UsuarioController");
    	        return; // interrompe o fluxo normal
    	    }

    	    // outros erros SQL continuam
    	    request.getSession().setAttribute("mensagemErro", "Erro ao cadastrar usuário.");
    	    throw e;
    	} catch (Exception e) {
    		request.getSession().setAttribute("mensagemErro", "Erro ao cadastrar usuário.");
            throw e;
    	} 

        // Redirecionamento para evitar reenvio de formulário (Post/Redirect/Get)
        response.sendRedirect(request.getContextPath() + "/UsuarioController");
    }

    // ==========================
    // EDITAR
    // ==========================

    /**
     * Responsável por atualizar os dados de um usuário existente.
     *
     * Fluxo:
     * 1. Recupera ID do usuário.
     * 2. Captura novos dados enviados pelo formulário.
     * 3. Cria objeto Usuario com ID.
     * 4. Executa atualização via DAO.
     *
     * Regra importante:
     * A dataInsercao é novamente atribuída com data atual.
     *
     * Interação com banco:
     * Chamada ao método usuarioDAO.alterarUsuarios(usuario).
     *
     * @param request  requisição HTTP com dados atualizados.
     * @param response resposta HTTP.
     * @throws Exception caso ocorra erro na atualização.
     */
    private void editarUsuario(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

    	try {
	
	        int idUsu = Integer.parseInt(request.getParameter("id_usu"));
	
	        String nomeUsu = request.getParameter("nome_usu");
	        String cpfUsu = request.getParameter("cpf_usu");
	        String telefoneUsu = request.getParameter("telefone_usu");
	        String emailUsu = request.getParameter("email_usu");
	        String enderecoUsu = request.getParameter("endereco_usu");
	        Boolean login_google = Boolean.parseBoolean(request.getParameter("login_google"));
	        String senhaUsu = request.getParameter("senha_usu");
	
	        Timestamp dataInsercao = Timestamp.valueOf(LocalDateTime.now());
	
	        Usuario usuario = new Usuario(
	                idUsu,
	                nomeUsu,
	                cpfUsu,
	                telefoneUsu,
	                emailUsu,
	                enderecoUsu,
	                dataInsercao,
	                login_google,
	                senhaUsu
	        );
	
	        // Atualização no banco de dados
	        usuarioDAO.alterarUsuarios(usuario);
	        request.getSession().setAttribute("mensagemSucesso", "Usuário alterado com sucesso!");
    	} catch (SQLException e) {

    	    // ERRO UNIQUE (email duplicado)
    	    if (e.getErrorCode() == 1062) {

    	    	String mensagem = e.getMessage();

    	        if (mensagem.contains("uk_usuario_email")) {

    	            request.getSession().setAttribute(
    	                "mensagemErro",
    	                "Já existe um usuário cadastrado com este e-mail."
    	            );

    	        } else if (mensagem.contains("uk_usuario_cpf")) {

    	            request.getSession().setAttribute(
    	                "mensagemErro",
    	                "Já existe um usuário cadastrado com este CPF."
    	            );

    	        } else {

    	            request.getSession().setAttribute(
    	                "mensagemErro",
    	                "Registro duplicado no sistema."
    	            );
    	        }

    	        response.sendRedirect(request.getContextPath() + "/UsuarioController");
    	        return; // interrompe o fluxo normal
    	    }

    	    // outros erros SQL continuam
    	    request.getSession().setAttribute("mensagemErro", "Erro ao cadastrar usuário.");
    	    throw e;
    	} catch (Exception e) {
            request.getSession().setAttribute("mensagemErro", "Erro ao alterar usuário.");
            throw e;
    	}

        response.sendRedirect(request.getContextPath() + "/UsuarioController");
    }

    // ==========================
    // DELETAR
    // ==========================

    /**
     * Responsável por excluir um usuário do sistema.
     *
     * Fluxo:
     * 1. Recupera o ID enviado via parâmetro.
     * 2. Valida se o ID é válido.
     * 3. Executa exclusão via DAO.
     * 4. Trata possíveis exceções de integridade referencial.
     *
     * Regra de negócio crítica:
     * Caso o usuário possua movimentações vinculadas (FK),
     * a exclusão não é permitida.
     *
     * Interação com banco:
     * Chamada ao método usuarioDAO.excluirUsuario(idUsu).
     *
     * @param request  requisição HTTP contendo ID.
     * @param response resposta HTTP.
     * @throws Exception em caso de erro inesperado.
     */
    private void deletarUsuario(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        
        String idStr = request.getParameter("id_usu");
        
        // Validação básica de existência do ID
        if (idStr != null && !idStr.isEmpty()) {
            try {

                int idUsu = Integer.parseInt(idStr);

                // Exclusão no banco
                usuarioDAO.excluirUsuario(idUsu);

                // Mensagem de sucesso armazenada na sessão
                request.getSession().setAttribute("mensagemSucesso", "Usuário excluído com sucesso!");
                
            } catch (java.sql.SQLIntegrityConstraintViolationException e) {

                // Tratamento específico para violação de chave estrangeira (FK)
                request.getSession().setAttribute("mensagemErro", 
                    "Não é possível excluir este usuário: existem movimentações de patrimônio vinculadas a ele.");
                
            } catch (Exception e) {

                // Tratamento genérico para outras falhas
                request.getSession().setAttribute("mensagemErro", "Erro inesperado ao tentar excluir o usuário.");
                e.printStackTrace();
            }
        }

        response.sendRedirect(request.getContextPath() + "/UsuarioController");
    }

    // ==========================
    // LISTAR
    // ==========================

    /**
     * Responsável por recuperar todos os usuários do banco
     * e encaminhar para a camada de visualização.
     *
     * Fluxo:
     * 1. Consulta lista via DAO.
     * 2. Armazena lista como atributo da requisição.
     * 3. Encaminha para usuarios.jsp.
     *
     * Interação com banco:
     * Chamada ao método usuarioDAO.listarUsuarios().
     *
     * @param request  requisição HTTP.
     * @param response resposta HTTP.
     * @throws Exception em caso de erro na consulta.
     */
    private void listarUsuarios(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        List<Usuario> listaUsuarios = usuarioDAO.listarUsuarios();

        // Disponibiliza lista para a JSP
        request.setAttribute("usuarios", listaUsuarios);

        RequestDispatcher dispatcher =
                request.getRequestDispatcher("/pages/usuarios.jsp");

        dispatcher.forward(request, response);
    }
}
