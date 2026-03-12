package br.com.patrimweb.controller;

import br.com.patrimweb.dao.*;
import br.com.patrimweb.model.*;
import br.com.patrimweb.utils.Conexao;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller responsável pelo gerenciamento das movimentações de equipamentos
 * dentro do sistema PatrimWeb.
 *
 * RESPONSABILIDADES:
 * - Controlar o fluxo HTTP relacionado às movimentações (listar, adicionar,
 *   editar e excluir).
 * - Validar sessão do usuário autenticado.
 * - Intermediar a comunicação entre camada de apresentação (JSP)
 *   e camada de persistência (DAOs).
 *
 * REGRAS DE NEGÓCIO:
 * - Toda operação exige usuário autenticado.
 * - Movimentações registram origem e destino de equipamentos.
 * - Inserções registram automaticamente data/hora da movimentação.
 * - Exclusões respeitam restrições de integridade do banco.
 *
 * INTERAÇÃO COM BANCO:
 * - Utiliza múltiplos DAOs para buscar entidades relacionadas:
 *   Movimentacao, Equipamento, Unidade e Usuario.
 *
 * PONTOS CRÍTICOS:
 * - Conversões diretas de parâmetros HTTP para inteiros.
 * - Dependência de parâmetros distintos entre edição e cadastro.
 * - Uso de sessão para mensagens de feedback ao usuário.
 */
@WebServlet("/MovimentacaoController")
public class MovimentacaoController extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // DAOs responsáveis pelo acesso às tabelas relacionadas
    private MovimentacaoDAO movimentacaoDAO;
    private EquipamentoDAO equipamentoDAO;
    private UnidadeDAO unidadeDAO;
    private UsuarioDAO usuarioDAO;

    /**
     * Método executado na inicialização da Servlet.
     *
     * RESPONSABILIDADE:
     * - Instanciar os DAOs utilizando conexões com o banco de dados.
     *
     * INTERAÇÃO COM BANCO:
     * - Abre conexões através da classe utilitária Conexao.
     *
     * PONTO CRÍTICO:
     * - Caso ocorra falha na criação dos DAOs, a Servlet não será inicializada.
     */
    @Override
    public void init() throws ServletException {
        try {
            movimentacaoDAO = new MovimentacaoDAO(Conexao.getConnection());
            equipamentoDAO  = new EquipamentoDAO(Conexao.getConnection());
            unidadeDAO      = new UnidadeDAO(Conexao.getConnection());
            usuarioDAO      = new UsuarioDAO(Conexao.getConnection());
        } catch (Exception e) {
            throw new ServletException("Erro ao inicializar DAOs de Movimentação", e);
        }
    }

    // ==========================
    // GET → LISTAR
    // ==========================

    /**
     * Processa requisições GET.
     *
     * FLUXO:
     * 1. Valida sessão do usuário.
     * 2. Configura cabeçalhos para impedir cache.
     * 3. Carrega lista de movimentações.
     *
     * @param request  Requisição HTTP recebida.
     * @param response Resposta HTTP enviada ao cliente.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Validação de autenticação antes de permitir acesso
        if (!validarSessao(request, response)) return;

        // Evita que páginas protegidas sejam armazenadas em cache
        configurarNoCache(response);

        // Requisição AJAX para buscar localização atual do equipamento
        String action = request.getParameter("action");
        if ("buscarLocalizacao".equals(action)) {
            try {
				buscarLocalizacaoEquipamento(request, response);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            return;
        }

        try {
            listarMovimentacoes(request, response);
        } catch (Exception e) {
            throw new ServletException("Erro ao listar movimentações", e);
        }
    }

    // ==========================
    // POST → AÇÕES
    // ==========================

    /**
     * Processa requisições POST responsáveis pelas ações de CRUD.
     *
     * REGRAS:
     * - A ação executada depende do parâmetro "action".
     * - Caso não exista ação, o sistema retorna para listagem.
     *
     * @param request  Dados enviados pelo formulário.
     * @param response Resposta HTTP.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!validarSessao(request, response)) return;

        // Define codificação para suportar caracteres especiais
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");

        String action = request.getParameter("action");

        try {
            // Caso nenhuma ação seja informada, apenas lista registros
            if (action == null) {
                listarMovimentacoes(request, response);
                return;
            }

            // Estrutura de decisão responsável por direcionar o fluxo
            switch (action) {
                case "adicionar":
                    adicionarMovimentacao(request, response);
                    break;
                case "editar":
                    editarMovimentacao(request, response);
                    break;
                case "deletar":
                    deletarMovimentacao(request, response);
                    break;
                default:
                    listarMovimentacoes(request, response);
            }

        } catch (Exception e) {
            throw new ServletException("Erro ao processar ação da movimentação", e);
        }
    }

    // ==========================
    // VALIDA SESSÃO
    // ==========================

    /**
     * Verifica se existe sessão ativa e usuário autenticado.
     *
     * REGRA DE SEGURANÇA:
     * - Impede acesso às funcionalidades sem login.
     *
     * @return true se a sessão for válida, false caso contrário.
     */
    private boolean validarSessao(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);

        // Estrutura condicional que verifica autenticação
        if (session == null || session.getAttribute("usuarioLogado") == null) {
            response.sendRedirect(request.getContextPath() + "/");
            return false;
        }
        return true;
    }

    // ==========================
    // NO CACHE
    // ==========================

    /**
     * Configura cabeçalhos HTTP para impedir cache da página.
     *
     * OBJETIVO:
     * - Evitar que páginas autenticadas sejam acessadas via botão "voltar"
     *   após logout.
     */
    private void configurarNoCache(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
    }

    // ==========================
    // ADICIONAR
    // ==========================

    /**
     * Realiza o cadastro de uma nova movimentação.
     *
     * FLUXO:
     * - Monta objeto Movimentacao a partir da requisição.
     * - Persiste no banco via DAO.
     * - Define mensagem de sucesso ou erro em sessão.
     */
    private void adicionarMovimentacao(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        try {
            Movimentacao movimentacao = montarMovimentacao(request, false);

            // Interação direta com banco de dados
            movimentacaoDAO.adicionarMovimentacao(movimentacao);

            request.getSession().setAttribute("mensagemSucesso", "Movimentação cadastrada com sucesso!");

        } catch (Exception e) {

            // Registro de erro funcional exibido ao usuário
            request.getSession().setAttribute("mensagemErro", "Erro ao cadastrar movimentação.");
            throw e;
        }

        response.sendRedirect(request.getContextPath() + "/MovimentacaoController");
    }

    // ==========================
    // EDITAR
    // ==========================

    /**
     * Atualiza uma movimentação existente.
     *
     * REGRA:
     * - Utiliza parâmetros específicos de edição.
     */
    private void editarMovimentacao(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        try {
            Movimentacao movimentacao = montarMovimentacao(request, true);

            // Atualização de registro existente no banco
            movimentacaoDAO.alterarMovimentacao(movimentacao);

            request.getSession().setAttribute("mensagemSucesso", "Movimentação alterada com sucesso!");

        } catch (Exception e) {

            request.getSession().setAttribute("mensagemErro", "Erro ao alterar movimentação.");
            throw e;
        }

        response.sendRedirect(request.getContextPath() + "/MovimentacaoController");
    }

    // ==========================
    // DELETAR
    // ==========================

    /**
     * Remove uma movimentação do sistema.
     *
     * VALIDAÇÕES:
     * - Verifica se o ID foi informado.
     * - Trata exceções de integridade referencial.
     */
    private void deletarMovimentacao(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        String idStr = request.getParameter("id_mov");

        // Validação básica de parâmetro recebido
        if (idStr != null && !idStr.trim().isEmpty()) {
            try {
                int idMov = Integer.parseInt(idStr);

                // Exclusão no banco de dados
                movimentacaoDAO.excluirMovimentacao(idMov);

                request.getSession().setAttribute("mensagemSucesso",
                        "Movimentação excluída com sucesso!");

            } catch (java.sql.SQLIntegrityConstraintViolationException e) {

                // Regra de negócio: impedir exclusão quando há vínculos
                request.getSession().setAttribute("mensagemErro",
                        "Não é possível excluir: existem vínculos relacionados.");

            } catch (Exception e) {

                request.getSession().setAttribute("mensagemErro",
                        "Erro inesperado ao excluir movimentação.");
                throw e;
            }
        }

        response.sendRedirect(request.getContextPath() + "/MovimentacaoController");
    }

    // ==========================
    // LISTAR
    // ==========================

    /**
     * Carrega dados necessários para a tela de movimentações.
     *
     * INTERAÇÃO COM BANCO:
     * - Busca listas completas de movimentações, equipamentos,
     *   unidades e usuários.
     *
     * RESPONSABILIDADE:
     * - Encaminhar dados para a JSP responsável pela renderização.
     */
    private void listarMovimentacoes(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        /*
         * Regra de negócio — filtro do checkbox "Histórico de Movimentações":
         * - Desmarcado / padrão (exibirTodos ausente ou "false"):
         *     Exibe apenas a última movimentação de cada equipamento.
         * - Marcado (exibirTodos = "true"):
         *     Exibe o histórico completo de todas as movimentações.
         *
         * Em ambos os casos, "ultimosIds" é enviado à JSP para que os botões
         * de editar/excluir sejam habilitados somente na última movimentação
         * de cada equipamento, independentemente do modo de exibição.
         */
        String exibirTodosParam = request.getParameter("exibirTodos");
        boolean exibirTodos = "true".equals(exibirTodosParam);

        if (exibirTodos) {
            request.setAttribute("movimentacoes", movimentacaoDAO.listarMovimentacoes());
        } else {
            request.setAttribute("movimentacoes", movimentacaoDAO.listarUltimaMovimentacaoPorEquipamento());
        }

        request.setAttribute("ultimosIds",   movimentacaoDAO.buscarIdsUltimaMovimentacaoPorEquipamento());
        request.setAttribute("exibirTodos",  exibirTodos);
        request.setAttribute("equipamentos", equipamentoDAO.listarEquipamentos());
        request.setAttribute("unidades",     unidadeDAO.listarUnidades());
        request.setAttribute("usuarios",     usuarioDAO.listarUsuarios());

        RequestDispatcher dispatcher =
                request.getRequestDispatcher("/pages/movimentacoes.jsp");

        dispatcher.forward(request, response);
    }

    // ==========================
    // MÉTODO AUXILIAR
    // ==========================

    /**
     * Monta o objeto Movimentacao com base nos parâmetros recebidos
     * na requisição HTTP.
     *
     * REGRA DE NEGÓCIO:
     * - Quando for cadastro, define automaticamente data de inserção.
     * - Quando for edição, mantém data original (null).
     * - O Fabricante é obtido diretamente do Equipamento buscado no banco,
     *   não como parâmetro independente do formulário.
     *
     * INTERAÇÃO COM BANCO:
     * - Realiza múltiplas buscas por ID para recuperar entidades
     *   relacionadas (Equipamento, Unidade e Usuario).
     *
     * @param request   requisição contendo dados do formulário.
     * @param isEdicao  define se a operação é edição ou cadastro.
     * @return Objeto Movimentacao pronto para persistência.
     */
    private Movimentacao montarMovimentacao(HttpServletRequest request, boolean isEdicao)
            throws Exception {

        // Define ID apenas em edição
        int idMov = isEdicao ? Integer.parseInt(request.getParameter("edt_id_mov")) : 0;

        int idEquipamento    = Integer.parseInt(request.getParameter("id_equip"));
        int idUnidadeOrigem  = Integer.parseInt(request.getParameter(isEdicao ? "edit_id_unidade_origem" : "id_unidade_origem"));
        int idUsuarioOrigem  = Integer.parseInt(request.getParameter(isEdicao ? "edit_id_usuario_origem" : "id_usuario_origem"));
        int idUnidadeDestino = Integer.parseInt(request.getParameter(isEdicao ? "edit_id_unidade_destino" : "id_unidade_destino"));
        int idUsuarioDestino = Integer.parseInt(request.getParameter(isEdicao ? "edit_id_usuario_destino" : "id_usuario_destino"));

        String tipoMov    = request.getParameter("tipo_movimentacao");
        String observacao = request.getParameter("observacao");

        // Busca o equipamento completo — o Fabricante está contido nele
        Equipamento equipamento = equipamentoDAO.buscarPorId(idEquipamento);

        // O fabricante é obtido a partir do equipamento já persistido no banco,
        // evitando parâmetro extra no formulário e garantindo consistência dos dados.
        Fabricante fabricante = equipamento.getFabricante();

        // Criação do objeto com entidades recuperadas do banco
        Movimentacao movimentacao = new Movimentacao(
                isEdicao ? idMov : 0,
                equipamento,
                fabricante,
                tipoMov,
                unidadeDAO.buscarPorId(idUnidadeOrigem),
                usuarioDAO.buscarPorId(idUsuarioOrigem),
                unidadeDAO.buscarPorId(idUnidadeDestino),
                usuarioDAO.buscarPorId(idUsuarioDestino),
                observacao,
                isEdicao ? null : Timestamp.valueOf(LocalDateTime.now())
        );

        return movimentacao;
    }

    // ==========================
    // BUSCAR LOCALIZAÇÃO ATUAL (AJAX)
    // ==========================

    /**
     * Responde requisições AJAX com a localização atual de um equipamento.
     *
     * REGRA DE NEGÓCIO:
     * - Ao selecionar um equipamento no modal de nova movimentação,
     *   o frontend consulta este endpoint para verificar se já existe
     *   histórico de movimentações.
     * - Se existir, retorna o último destino registrado (localização atual).
     * - Se não existir, retorna indicativo para manter campo livre.
     *
     * RETORNO:
     * - JSON com campos: encontrou (boolean), idUnidade (int), nomeUnidade (String).
     *
     * @param request  Requisição contendo parâmetro "id_equip".
     * @param response Resposta JSON ao frontend.
     */
    private void buscarLocalizacaoEquipamento(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();

        String idEquipStr = request.getParameter("id_equip");

        // Validação do parâmetro recebido
        if (idEquipStr == null || idEquipStr.trim().isEmpty()) {
            out.print("{\"encontrou\": false}");
            out.flush();
            return;
        }

        try {
            int idEquip = Integer.parseInt(idEquipStr.trim());
            br.com.patrimweb.model.Unidade unidade =
                    movimentacaoDAO.buscarUltimaUnidadeDestinoPorEquipamento(idEquip);

            if (unidade != null) {
                // Monta JSON com StringBuilder para evitar problemas de escaping
                String nomeEscapado = unidade.getNomeUnid()
                        .replace("\\", "\\\\")
                        .replace("\"", "\\\"");
                StringBuilder json = new StringBuilder();
                json.append("{");
                json.append("\"encontrou\": true,");
                json.append("\"idUnidade\": ").append(unidade.getIdUnid()).append(",");
                json.append("\"nomeUnidade\": \"").append(nomeEscapado).append("\"");
                json.append("}");
                out.print(json.toString());
            } else {
                out.print("{\"encontrou\": false}");
            }

        } catch (NumberFormatException e) {
            out.print("{\"encontrou\": false}");
        }

        out.flush();
    }
}