package br.com.patrimweb.controller;

import br.com.patrimweb.dao.PerfilDAO;
import br.com.patrimweb.dao.UsuarioDAO;
import br.com.patrimweb.model.Fabricante;
import br.com.patrimweb.model.Perfil;
import br.com.patrimweb.model.Usuario;
import br.com.patrimweb.utils.Conexao;
import br.com.patrimweb.utils.SenhaUtils; // ✅ Import da classe utilitária de criptografia
import br.com.patrimweb.utils.CpfUtils;   // ✅ Import da classe utilitária de validação de CPF

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
 * - Adição de novos usuários (com criptografia BCrypt da senha)
 * - Edição de usuários existentes (com criptografia BCrypt da senha)
 * - Exclusão de usuários
 *
 * Regra de segurança de senhas:
 * - A senha nunca é armazenada em texto puro no banco de dados.
 * - Antes de persistir, a senha é convertida para hash BCrypt via SenhaUtils.criptografar().
 * - A verificação no login é feita pelo UsuarioDAO usando SenhaUtils.verificar().
 */
@WebServlet("/UsuarioController")
public class UsuarioController extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private UsuarioDAO usuarioDAO;
    
    private PerfilDAO perfilDAO;

    /**
     * Inicializa o DAO ao carregar a Servlet.
     *
     * @throws ServletException caso ocorra erro ao inicializar o DAO.
     */
    @Override
    public void init() throws ServletException {
        try {
            usuarioDAO = new UsuarioDAO(Conexao.getConnection());
            perfilDAO = new PerfilDAO(Conexao.getConnection()); 
        } catch (Exception e) {
            throw new ServletException("Erro ao inicializar UsuarioDAO", e);
        }
    }

    /**
     * Processa requisições HTTP GET.
     *
     * Verifica sessão ativa e perfil ADMINISTRADOR antes de prosseguir.
     * Desabilita cache para páginas autenticadas.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("usuarioLogado") == null) {
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }

        // ✅ Controle de acesso: somente ADMINISTRADOR pode acessar o módulo de usuários.
        //    Qualquer outro perfil é redirecionado para o Dashboard com mensagem de erro.
        if (!isAdmin(session)) {
            session.setAttribute("mensagemErro", "Acesso negado. Apenas administradores podem gerenciar usuários.");
            response.sendRedirect(request.getContextPath() + "/DashboardController");
            return;
        }

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
     * Processa requisições HTTP POST, roteando para a ação correspondente
     * com base no parâmetro "action".
     *
     * Protegido contra acesso direto via POST por não-administradores.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");

        // ✅ Controle de acesso no POST — impede que não-admins executem
        //    operações CRUD mesmo que tentem acionar a URL diretamente.
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuarioLogado") == null) {
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }
        if (!isAdmin(session)) {
            session.setAttribute("mensagemErro", "Acesso negado. Apenas administradores podem gerenciar usuários.");
            response.sendRedirect(request.getContextPath() + "/DashboardController");
            return;
        }

        try {
            String action = request.getParameter("action");

            if (action == null) {
                listarUsuarios(request, response);
                return;
            }

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
    // CONTROLE DE ACESSO
    // ==========================

    /**
     * Verifica se o usuário da sessão possui perfil ADMINISTRADOR.
     *
     * Regra de negócio:
     * - A comparação é feita ignorando maiúsculas/minúsculas para evitar
     *   problemas com variações de cadastro (ex: "Administrador", "ADMINISTRADOR").
     * - Retorna false de forma segura caso o perfil não esteja preenchido.
     *
     * @param session sessão HTTP ativa
     * @return true se o perfil for ADMINISTRADOR, false caso contrário
     */
    private boolean isAdmin(HttpSession session) {
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null || usuarioLogado.getPerfilUsu() == null) {
            return false;
        }
        String nomePerfil = usuarioLogado.getPerfilUsu().getNomePerfil();
        return nomePerfil != null && nomePerfil.trim().equalsIgnoreCase("ADMINISTRADOR");
    }

    // ==========================
    // ADICIONAR
    // ==========================

    /**
     * Responsável por adicionar um novo usuário ao sistema.
     *
     * Fluxo:
     * 1. Captura os parâmetros enviados pelo formulário.
     * 2. Criptografa a senha com BCrypt antes de persistir.
     * 3. Instancia objeto Usuario com a senha já criptografada.
     * 4. Persiste no banco via UsuarioDAO.
     * 5. Redireciona para a listagem.
     *
     * Regra de segurança:
     * - A senha nunca é salva em texto puro.
     * - SenhaUtils.criptografar() gera um hash BCrypt com salt automático.
     */
    private void adicionarUsuario(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
    
        try {
	    	String nomeUsu = request.getParameter("nome_usu");
	        String cpfUsu = request.getParameter("cpf_usu");

	        // ✅ Validação do CPF antes de prosseguir com o cadastro
	        if (!CpfUtils.isValido(cpfUsu)) {
	            request.getSession().setAttribute("mensagemErro",
	                "CPF inválido. Verifique o número informado e tente novamente.");
	            response.sendRedirect(request.getContextPath() + "/UsuarioController");
	            return;
	        }

	        String telefoneUsu = request.getParameter("telefone_usu");
	        String emailUsu = request.getParameter("email_usu");
	        String enderecoUsu = request.getParameter("endereco_usu");
	
	        // Conversão de String para Boolean
	        Boolean login_google = Boolean.parseBoolean(request.getParameter("login_google"));
	
	        String senhaUsu = request.getParameter("senha_usu");

	        // ✅ Criptografa a senha com BCrypt antes de persistir
	        String senhaCriptografada = SenhaUtils.criptografar(senhaUsu);

	        // Data gerada no servidor
	        Timestamp dataInsercao = Timestamp.valueOf(LocalDateTime.now());

	        // Construção do objeto Perfil a partir do id selecionado no formulário
	        Perfil perfil = new Perfil();
	        perfil.setIdPerfil(Integer.parseInt(request.getParameter("id_perfil")));

	        Usuario usuario = new Usuario(
	                nomeUsu,
	                cpfUsu,
	                telefoneUsu,
	                emailUsu,
	                enderecoUsu,
	                dataInsercao,
	                login_google,
	                senhaCriptografada, // ✅ Hash BCrypt, nunca texto puro
	                perfil
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
     * 1. Recupera ID e novos dados do formulário.
     * 2. Se uma nova senha for informada, ela é criptografada com BCrypt.
     * 3. Se o campo senha estiver vazio, mantém a senha atual do banco (não sobrescreve).
     * 4. Executa atualização via DAO.
     *
     * Regra de segurança:
     * - A senha só é atualizada se o usuário informar uma nova senha no formulário.
     * - Senhas nunca são salvas em texto puro.
     */
    private void editarUsuario(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        try {
            int idUsu           = Integer.parseInt(request.getParameter("id_usu"));
            String nomeUsu      = request.getParameter("nome_usu");
            String cpfUsu       = request.getParameter("cpf_usu");

            // ✅ Validação do CPF antes de prosseguir com a edição
            if (!CpfUtils.isValido(cpfUsu)) {
                request.getSession().setAttribute("mensagemErro",
                    "CPF inválido. Verifique o número informado e tente novamente.");
                response.sendRedirect(request.getContextPath() + "/UsuarioController");
                return;
            }

            String telefoneUsu  = request.getParameter("telefone_usu_edt"); // ✅ nome correto conforme o formulário JSP
            String emailUsu     = request.getParameter("email_usu");
            String enderecoUsu  = request.getParameter("endereco_usu_edt"); // ✅ nome correto conforme o formulário JSP
            Boolean loginGoogle = Boolean.parseBoolean(request.getParameter("login_google"));
            String senhaUsu     = request.getParameter("senha_usu");
            //String perfilUsu     = request.getParameter("perfil_usu");

            // ✅ Só criptografa se uma nova senha foi informada
            // Caso o campo esteja vazio, recupera a senha atual do banco para não sobrescrever
            String senhaCriptografada;
            if (senhaUsu != null && !senhaUsu.isBlank()) {
                senhaCriptografada = SenhaUtils.criptografar(senhaUsu);
            } else {
                // Mantém a senha atual — busca o hash já armazenado no banco
                Usuario usuarioAtual = usuarioDAO.buscarPorId(idUsu);
                senhaCriptografada = (usuarioAtual != null) ? usuarioAtual.getSenhaUsu() : "";
            }

            Timestamp dataInsercao = Timestamp.valueOf(LocalDateTime.now());
            
         // Construção do objeto Perfil baseado nos dados do JOIN
	        Perfil perfil = new Perfil();

	        // IMPORTANTE: coluna "fabricante" representa a FK na tabela equipamento
	        perfil.setIdPerfil(Integer.parseInt(request.getParameter("id_perfil"))); 
	        perfil.setNomePerfil(request.getParameter("nome_perfil"));

            Usuario usuario = new Usuario(
                    idUsu,
                    nomeUsu,
                    cpfUsu,
                    telefoneUsu,
                    emailUsu,
                    enderecoUsu,
                    dataInsercao,
                    loginGoogle,
                    senhaCriptografada, // ✅ Hash BCrypt, nunca texto puro
                    perfil
            );

            usuarioDAO.alterarUsuarios(usuario);
            request.getSession().setAttribute("mensagemSucesso", "Usuário alterado com sucesso!");

        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                String mensagem = e.getMessage();

                if (mensagem.contains("uk_usuario_email")) {
                    request.getSession().setAttribute("mensagemErro", "Já existe um usuário cadastrado com este e-mail.");
                } else if (mensagem.contains("uk_usuario_cpf")) {
                    request.getSession().setAttribute("mensagemErro", "Já existe um usuário cadastrado com este CPF.");
                } else {
                    request.getSession().setAttribute("mensagemErro", "Registro duplicado no sistema.");
                }

                response.sendRedirect(request.getContextPath() + "/UsuarioController");
                return;
            }

            request.getSession().setAttribute("mensagemErro", "Erro ao alterar usuário.");
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
     * Regra de negócio crítica:
     * Caso o usuário possua movimentações vinculadas (FK), a exclusão não é permitida.
     */
    private void deletarUsuario(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        String idStr = request.getParameter("id_usu");

        if (idStr != null && !idStr.isEmpty()) {
            try {
                int idUsu = Integer.parseInt(idStr);
                usuarioDAO.excluirUsuario(idUsu);
                request.getSession().setAttribute("mensagemSucesso", "Usuário excluído com sucesso!");

            } catch (java.sql.SQLIntegrityConstraintViolationException e) {
                request.getSession().setAttribute("mensagemErro",
                    "Não é possível excluir este usuário: existem movimentações de patrimônio vinculadas a ele.");

            } catch (Exception e) {
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
     * Recupera todos os usuários e encaminha para a camada de visualização.
     */
    private void listarUsuarios(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

    	 Conexao conexao = new Conexao();
    	 PerfilDAO perfilDAO = new PerfilDAO(conexao.getConnection());
    	    
    	    
        List<Usuario> listaUsuarios = usuarioDAO.listarUsuarios();
        // Busca lista de fabricantes para associação
        List<Perfil> listaPerfis = perfilDAO.listarPerfis();
        System.out.println("Perfil: " + listaPerfis);
        // Envia dados para a camada de visualização
               
        request.setAttribute("usuarios", listaUsuarios);
        request.setAttribute("perfis", listaPerfis);

        RequestDispatcher dispatcher = request.getRequestDispatcher("/pages/usuarios.jsp");
        dispatcher.forward(request, response);
    }
}
