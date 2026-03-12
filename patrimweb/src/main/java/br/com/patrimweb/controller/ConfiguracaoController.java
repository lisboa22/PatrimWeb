package br.com.patrimweb.controller;

import br.com.patrimweb.dao.PerfilDAO;
import br.com.patrimweb.dao.PerfilPermissaoDAO;
import br.com.patrimweb.dao.PermissaoDAO;
import br.com.patrimweb.dao.UsuarioDAO;
import br.com.patrimweb.model.Usuario;
import br.com.patrimweb.utils.Conexao;
import br.com.patrimweb.utils.SenhaUtils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import javax.imageio.ImageIO;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
 *   POST action=atualizarFoto     → faz upload e salva a foto de perfil
 *   POST action=alterarSenha      → altera senha do usuário logado
 *   POST action=salvarPermissoes  → salva permissões de um perfil via AJAX
 * =====================================================================================
 */
@WebServlet("/ConfiguracaoController")
@javax.servlet.annotation.MultipartConfig(
    maxFileSize    = 5 * 1024 * 1024,   // 5 MB por arquivo
    maxRequestSize = 6 * 1024 * 1024    // 6 MB por requisição completa
)
public class ConfiguracaoController extends HttpServlet {

    // ─────────────────────────────────────────────────────────────────────────
    // Constantes de navegação
    // ─────────────────────────────────────────────────────────────────────────

    private static final String VIEW_CONFIGURACOES = "/pages/configuracoes.jsp";
    private static final String REDIRECT_DASHBOARD = "/DashboardController";
    private static final String REDIRECT_LOGIN     = "index.jsp";
    private static final String PERFIL_ADMIN       = "ADMINISTRADOR";

    // ─────────────────────────────────────────────────────────────────────────
    // Constantes de upload de foto
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Subpasta dentro do webapp onde as fotos de perfil são salvas.
     *
     * O caminho físico real é resolvido no método processarAtualizarFoto()
     * usando getServletContext().getRealPath(), que retorna o local exato
     * onde o Tomcat fez o deploy da aplicação — incluindo durante o
     * desenvolvimento no Eclipse, apontando para a pasta do projeto.
     */
    private static final String PASTA_FOTOS = "imagens/perfil/";

    /** Formatos aceitos para foto de perfil */
    private static final java.util.Set<String> TIPOS_PERMITIDOS =
            new java.util.HashSet<>(java.util.Arrays.asList(
                    "image/jpeg", "image/png", "image/webp"
            ));


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
            case "atualizarFoto":
                processarAtualizarFoto(request, response, session);
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
     * AÇÃO POST: atualizarFoto
     *
     * Salva a foto de perfil na pasta webapp/imagens/perfil/ — SEM alterar o banco.
     *
     * ESTRATÉGIA DE NOME DE ARQUIVO:
     *   O arquivo é sempre salvo como "{id_usu}.{extensão}" — ex: "42.jpg".
     *   Como o nome é derivado do ID (que já está na sessão), não é preciso
     *   guardar nada no banco. O JSP monta a URL diretamente com o ID.
     *
     * COMO O CAMINHO É RESOLVIDO:
     *   getServletContext().getRealPath() converte o caminho relativo dentro
     *   do webapp para o caminho físico real no disco do servidor.
     *   Isso funciona automaticamente no Tomcat em qualquer sistema operacional.
     *
     * CACHE DO NAVEGADOR:
     *   Como o arquivo tem nome fixo (ex: "42.jpg"), sem controle o browser
     *   exibiria a foto antiga após uma troca. Para evitar isso, gravamos um
     *   timestamp na sessão e o JSP o usa como ?v= na URL da imagem.
     *   URL diferente = browser busca imagem nova.
     *
     * FLUXO:
     *   1. Valida tipo (JPEG, PNG, WEBP) e tamanho (máx 5 MB)
     *   2. Resolve o caminho físico da pasta via getRealPath()
     *   3. Cria as pastas imagens/perfil/ automaticamente se não existirem
     *   4. Apaga arquivos antigos do mesmo usuário (outras extensões)
     *   5. Salva o novo arquivo como "{id_usu}.{extensão}"
     *   6. Grava o timestamp na sessão para quebrar o cache do navegador
     */
    private void processarAtualizarFoto(HttpServletRequest request,
                                         HttpServletResponse response,
                                         HttpSession session)
            throws IOException, ServletException {

        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");

        // ── 1. Recupera o arquivo enviado pelo formulário ─────────────────────
        javax.servlet.http.Part fotoPart = request.getPart("foto_perfil");

        if (fotoPart == null || fotoPart.getSize() == 0) {
            session.setAttribute("mensagemErro", "Nenhum arquivo selecionado.");
            response.sendRedirect(request.getContextPath() + "/ConfiguracaoController");
            return;
        }

        // ── 2. Valida o tipo MIME ─────────────────────────────────────────────
        String contentType = fotoPart.getContentType();
        if (contentType == null || !TIPOS_PERMITIDOS.contains(contentType.toLowerCase())) {
            session.setAttribute("mensagemErro",
                    "Formato inválido. Use apenas JPG, PNG ou WEBP.");
            response.sendRedirect(request.getContextPath() + "/ConfiguracaoController");
            return;
        }

        // ── 3. Valida o tamanho ───────────────────────────────────────────────
        if (fotoPart.getSize() > 5 * 1024 * 1024) {
            session.setAttribute("mensagemErro",
                    "Arquivo muito grande. O tamanho máximo permitido é 5 MB.");
            response.sendRedirect(request.getContextPath() + "/ConfiguracaoController");
            return;
        }

        // ── 4. Define o caminho físico da pasta imagens/perfil/ ──────────────
        //
        //      O caminho é construído a partir da variável de sistema
        //      "user.home" — que no Windows retorna automaticamente:
        //        C:\Users\{usuario_logado_no_windows}
        //
        //      A partir daí completamos com o restante do caminho do projeto.
        //      Estrutura do projeto confirmada:
        //        {user.home}\patrimweb\patrimweb\src\main\webapp\imagens\perfil
        //
        //      Vantagem: funciona em qualquer máquina Windows sem precisar
        //      digitar o nome do usuário fixo no código, pois "user.home"
        //      é resolvido automaticamente pelo sistema operacional.
        String userHome = System.getProperty("user.home");

        // ── Pasta 1: projeto real — arquivo permanente, não se perde no redeploy
        Path diretorioProjeto = Paths.get(userHome,
                                          "patrimweb",
                                          "patrimweb",
                                          "src", "main", "webapp",
                                          "imagens", "perfil");

        // ── Pasta 2: deploy do Tomcat — arquivo servido estaticamente pelo browser
        //    getRealPath() aponta para a pasta temporária do Tomcat, que é exatamente
        //    de onde o browser busca arquivos estáticos como /imagens/perfil/1.jpg
        String caminhoTomcat  = getServletContext().getRealPath("imagens/perfil/");
        Path   diretorioTomcat = Paths.get(caminhoTomcat);

        // ── 5. Cria as pastas nas dois locais se não existirem ────────────────
        Files.createDirectories(diretorioProjeto);
        Files.createDirectories(diretorioTomcat);

        // ── 6. Define o nome do arquivo: sempre salvo como "{id_usu}.jpg" ──────
        //      Independentemente do formato enviado (PNG, WEBP, JPEG), a imagem
        //      é convertida para JPEG antes de salvar, garantindo compatibilidade
        //      total com o JSP e com todos os navegadores.
        String nomeArquivo = usuarioLogado.getIdUsu() + ".jpg";

        // ── 7. Apaga arquivos antigos nas duas pastas (todas as extensões) ─────
        for (String ext : new String[]{"jpg", "png", "webp"}) {
            try {
                Files.deleteIfExists(diretorioProjeto.resolve(usuarioLogado.getIdUsu() + "." + ext));
                Files.deleteIfExists(diretorioTomcat.resolve(usuarioLogado.getIdUsu() + "." + ext));
            } catch (IOException ex) {
                System.err.println("[PatrimWeb] Aviso: não foi possível apagar foto anterior: "
                        + ex.getMessage());
            }
        }

        // ── 8. Converte a imagem recebida para JPEG e obtém os bytes ─────────
        //      Qualquer formato suportado (PNG, WEBP, JPEG) é lido via ImageIO,
        //      desenhado num BufferedImage com fundo branco (para preservar
        //      transparências de PNG/WEBP) e re-codificado como JPEG.
        byte[] bytesImagem;
        try (InputStream input = fotoPart.getInputStream();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            BufferedImage imagemOriginal = ImageIO.read(input);

            if (imagemOriginal == null) {
                session.setAttribute("mensagemErro",
                        "Não foi possível processar a imagem. Verifique se o arquivo não está corrompido.");
                response.sendRedirect(request.getContextPath() + "/ConfiguracaoController");
                return;
            }

            // Cria canvas RGB com fundo branco para acomodar transparência de PNG/WEBP
            BufferedImage imagemJpg = new BufferedImage(
                    imagemOriginal.getWidth(),
                    imagemOriginal.getHeight(),
                    BufferedImage.TYPE_INT_RGB);
            imagemJpg.createGraphics().drawImage(imagemOriginal, 0, 0,
                    java.awt.Color.WHITE, null);

            // Codifica como JPEG
            boolean escrito = ImageIO.write(imagemJpg, "jpg", baos);
            if (!escrito) {
                session.setAttribute("mensagemErro",
                        "Erro ao converter a imagem para JPG. Tente outro arquivo.");
                response.sendRedirect(request.getContextPath() + "/ConfiguracaoController");
                return;
            }

            bytesImagem = baos.toByteArray();
        }

        // ── 9. Salva o JPEG convertido nas duas pastas ────────────────────────
        // Salva no projeto (permanente)
        Files.write(diretorioProjeto.resolve(nomeArquivo), bytesImagem);

        // Salva no Tomcat (para servir estaticamente via /imagens/perfil/)
        Files.write(diretorioTomcat.resolve(nomeArquivo), bytesImagem);

        // ── 10. Grava timestamp na sessão para forçar reload da imagem ────────
        //      O JSP usa como ?v={timestamp} na URL, fazendo o browser ignorar
        //      o cache e buscar a foto nova.
        session.setAttribute("fotoPerfil_v", System.currentTimeMillis());
        session.setAttribute("mensagemSucesso", "Foto de perfil atualizada com sucesso!");

        response.sendRedirect(request.getContextPath() + "/ConfiguracaoController");
    }


    /**
     * AÇÃO POST: atualizarPerfil
     *
     * Atualiza os dados pessoais do usuário logado (nome, e-mail, telefone,
     * CPF e endereço). O cargo/perfil é mantido intacto — não é recebido
     * pelo formulário e não é passado ao DAO.
     */
    private void processarAtualizarPerfil(HttpServletRequest request,
                                           HttpServletResponse response,
                                           HttpSession session)
            throws IOException {

        String nomeUsu     = request.getParameter("nome_usu");
        String emailUsu    = request.getParameter("email_usu");
        String telefoneUsu = request.getParameter("telefone_usu");
        String cpfUsu      = request.getParameter("cpf_usu");
        String enderecoUsu = request.getParameter("endereco_usu");

        if (nomeUsu == null || nomeUsu.trim().isEmpty()
                || emailUsu == null || emailUsu.trim().isEmpty()) {

            session.setAttribute("mensagemErro",
                    "Nome e E-mail são obrigatórios para atualizar o perfil.");
            response.sendRedirect(request.getContextPath() + "/ConfiguracaoController");
            return;
        }

        try (Connection conn = Conexao.getConnection()) {

            Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");

            // Atualiza os dados editáveis no objeto de sessão
            usuarioLogado.setNomeUsu(nomeUsu.trim());
            usuarioLogado.setEmailUsu(emailUsu.trim());
            usuarioLogado.setTelefoneUsu(telefoneUsu != null ? telefoneUsu.trim() : "");
            usuarioLogado.setCpfUsu(cpfUsu != null ? cpfUsu.trim() : "");
            usuarioLogado.setEnderecoUsu(enderecoUsu != null ? enderecoUsu.trim() : "");

            // Persiste apenas os campos pessoais — senha e perfil/cargo não são alterados
            UsuarioDAO usuarioDAO = new UsuarioDAO(conn);
            usuarioDAO.atualizarDadosPerfil(usuarioLogado);

            // Atualiza o objeto na sessão com os novos dados
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

        // Obtém o usuário logado da sessão (necessário em todas as validações abaixo)
        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");

        // Bloqueia alteração de senha para usuários que autenticam via Google
        if (usuarioLogado != null && usuarioLogado.getLoginGoogle()) {
            session.setAttribute("mensagemErro",
                    "Usuários que fazem login com o Google não podem alterar a senha por aqui.");
            response.sendRedirect(request.getContextPath() + "/ConfiguracaoController?aba=seguranca");
            return;
        }

        String senhaAtual     = request.getParameter("senha_atual");
        String novaSenha      = request.getParameter("nova_senha");
        String confirmarSenha = request.getParameter("confirmar_senha");

        if (senhaAtual == null || senhaAtual.isEmpty()
                || novaSenha == null || novaSenha.isEmpty()
                || confirmarSenha == null || confirmarSenha.isEmpty()) {

            session.setAttribute("mensagemErro", "Todos os campos de senha são obrigatórios.");
            response.sendRedirect(request.getContextPath() + "/ConfiguracaoController?aba=seguranca");
            return;
        }

        if (!novaSenha.equals(confirmarSenha)) {
            session.setAttribute("mensagemErro",
                    "A nova senha e a confirmação não correspondem.");
            response.sendRedirect(request.getContextPath() + "/ConfiguracaoController?aba=seguranca");
            return;
        }

        if (novaSenha.length() < 6) {
            session.setAttribute("mensagemErro",
                    "A nova senha deve ter no mínimo 6 caracteres.");
            response.sendRedirect(request.getContextPath() + "/ConfiguracaoController?aba=seguranca");
            return;
        }

        try (Connection conn = Conexao.getConnection()) {

            UsuarioDAO usuarioDAO = new UsuarioDAO(conn);

            // 1. Busca o hash atual gravado no banco para este usuário
            String hashAtual = usuarioDAO.buscarHashSenha(usuarioLogado.getIdUsu());

            if (hashAtual == null) {
                session.setAttribute("mensagemErro", "Usuário não encontrado. Faça login novamente.");
                response.sendRedirect(request.getContextPath() + "/ConfiguracaoController?aba=seguranca");
                return;
            }

            // 2. Verifica se a senha atual digitada corresponde ao hash do banco (BCrypt)
            if (!SenhaUtils.verificar(senhaAtual, hashAtual)) {
                session.setAttribute("mensagemErro", "Senha atual incorreta. Verifique e tente novamente.");
                response.sendRedirect(request.getContextPath() + "/ConfiguracaoController?aba=seguranca");
                return;
            }

            // 3. Criptografa a nova senha em BCrypt e persiste no banco
            String novoHash = SenhaUtils.criptografar(novaSenha);
            usuarioDAO.alterarSenha(usuarioLogado.getIdUsu(), novoHash);

            session.setAttribute("mensagemSucesso", "Senha alterada com sucesso!");

        } catch (Exception e) {
            session.setAttribute("mensagemErro",
                    "Erro ao alterar a senha: " + e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/ConfiguracaoController?aba=seguranca");
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
