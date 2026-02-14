package br.com.patrimweb.controller;

import br.com.patrimweb.dao.UsuarioDAO;
import br.com.patrimweb.model.Usuario;
import br.com.patrimweb.utils.Conexao;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * Controller responsável por processar requisições relacionadas
 * ao relatório de Usuários cadastrados no sistema.
 *
 * Propósito da classe:
 * - Controlar o fluxo de geração do relatório de usuários.
 * - Garantir que somente usuários autenticados tenham acesso.
 * - Receber filtros enviados pela interface web.
 * - Realizar consultas ao banco através da camada DAO.
 * - Preparar dados estatísticos para exibição gráfica.
 * - Encaminhar os dados para a camada de visualização (JSP).
 *
 * Regras de negócio:
 * - O relatório só pode ser acessado por usuários autenticados.
 * - Os filtros são opcionais e aplicados dinamicamente na consulta.
 * - O gráfico utiliza o ano selecionado pelo usuário ou,
 *   caso não informado, o primeiro ano disponível retornado pelo banco.
 */
@WebServlet("/RelatorioUsuarioController")
public class RelatorioUsuarioController extends HttpServlet {

    /**
     * Processa requisições HTTP GET responsáveis pela geração
     * do relatório de usuários cadastrados.
     *
     * Fluxo de execução:
     * 1) Validação de autenticação via sessão.
     * 2) Captura dos parâmetros de filtro.
     * 3) Conversão de dados para tipos adequados.
     * 4) Consulta ao banco utilizando DAO.
     * 5) Preparação de dados estatísticos.
     * 6) Encaminhamento para JSP de apresentação.
     *
     * @param request  objeto contendo dados da requisição HTTP e parâmetros enviados pelo cliente
     * @param response objeto responsável por construir a resposta HTTP
     * @throws ServletException em caso de erro interno durante o processamento
     * @throws IOException      em caso de falha de comunicação I/O
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // ===============================
        // VALIDAÇÃO DE SESSÃO (SEGURANÇA)
        // ===============================
        // Recupera a sessão existente sem criar uma nova.
        // Ponto crítico de segurança: impede criação automática de sessão para usuários não autenticados.
        HttpSession sessao = request.getSession(false);

        // Recupera o usuário autenticado armazenado na sessão.
        Usuario usuarioLogado = sessao != null
                ? (Usuario) sessao.getAttribute("usuarioLogado")
                : null;

        // Regra de negócio:
        // Caso não exista usuário logado, o acesso ao relatório é negado.
        if (usuarioLogado == null) {
            response.sendRedirect("login.jsp");
            return; // Interrompe o fluxo para evitar acesso indevido.
        }

        // =====================================
        // LEITURA DOS PARÂMETROS DE FILTRO
        // =====================================
        // Parâmetros enviados via query string (requisição GET).
        // Todos os filtros são opcionais.
        String dataInicioStr = request.getParameter("dataInicio");
        String dataFimStr = request.getParameter("dataFim");
        String nome = request.getParameter("nome");
        String cpf = request.getParameter("cpf");

        // =====================================
        // CONVERSÃO DAS DATAS PARA TIMESTAMP
        // =====================================
        // Inicialmente definidos como null para permitir consulta sem restrição temporal.
        Timestamp dataInicio = null;
        Timestamp dataFim = null;

        // Validação: converte apenas quando o valor estiver presente e não vazio.
        // Ajusta para início do dia garantindo abrangência total do período.
        if (dataInicioStr != null && !dataInicioStr.isEmpty()) {
            dataInicio = Timestamp.valueOf(dataInicioStr + " 00:00:00");
        }

        // Ajusta para final do dia para incluir registros até 23:59:59.
        if (dataFimStr != null && !dataFimStr.isEmpty()) {
            dataFim = Timestamp.valueOf(dataFimStr + " 23:59:59");
        }

        // =====================================
        // ACESSO AO BANCO DE DADOS
        // =====================================
        // try-with-resources garante fechamento automático da conexão,
        // evitando vazamento de recursos.
        try (Connection conn = Conexao.getConnection()) {

            // DAO responsável pelas operações de persistência da entidade Usuario.
            // Centraliza toda interação com o banco de dados.
            UsuarioDAO usuarioDAO = new UsuarioDAO(conn);

            // ======================================================
            // 1) CONSULTA FILTRADA PARA EXIBIÇÃO NA TABELA
            // ======================================================
            // Interação com banco de dados.
            // O DAO deve tratar parâmetros nulos como filtros opcionais.
            List<Usuario> usuarios = usuarioDAO.filtrarUsuarios(
                    dataInicio, dataFim, nome, cpf
            );

            // ======================================================
            // 2) OBTÉM LISTA DE ANOS DISPONÍVEIS
            // ======================================================
            // Recupera anos distintos de cadastro existentes na base.
            // Utilizado para alimentar seletor do gráfico.
            List<Integer> anos = usuarioDAO.listarAnosCadastro();

            // ======================================================
            // 3) DEFINIÇÃO DO ANO SELECIONADO
            // ======================================================
            Integer anoSelecionado;
            String anoParam = request.getParameter("ano");

            // Estrutura de decisão relevante:
            // Prioriza o ano informado pelo usuário.
            if (anoParam != null && !anoParam.isEmpty()) {
                anoSelecionado = Integer.parseInt(anoParam);
            } else {
                // Regra de fallback:
                // utiliza o primeiro ano disponível caso nenhum seja informado.
                anoSelecionado = anos.isEmpty() ? null : anos.get(0);
            }

            // ======================================================
            // 4) DADOS ESTATÍSTICOS PARA O GRÁFICO
            // ======================================================
            // Map contendo estatísticas mensais:
            // chave -> mês (1 a 12)
            // valor -> quantidade de usuários cadastrados no mês.
            Map<Integer, Integer> usuariosPorMes =
                    usuarioDAO.quantidadeUsuariosPorMes(anoSelecionado);

            // ======================================================
            // 5) CÁLCULO DO TOTAL DE USUÁRIOS NO ANO
            // ======================================================
            // Soma agregada utilizando Stream API.
            // Utilizado para métricas e representação gráfica proporcional.
            int totalUsuariosAno = usuariosPorMes.values()
                    .stream()
                    .mapToInt(Integer::intValue)
                    .sum();

            // =====================================
            // ENVIO DOS DADOS PARA A VIEW (JSP)
            // =====================================
            // Define atributos utilizados pela camada de apresentação.
            request.setAttribute("usuarios", usuarios);
            request.setAttribute("anos", anos);
            request.setAttribute("anoAtual", anoSelecionado);
            request.setAttribute("anoSelecionado", anoSelecionado);
            request.setAttribute("usuariosPorMes", usuariosPorMes);
            request.setAttribute("totalUsuariosAno", totalUsuariosAno);

            // Mantém os filtros preenchidos após submissão,
            // preservando estado da interface do usuário.
            request.setAttribute("filtroDataInicio", dataInicioStr);
            request.setAttribute("filtroDataFim", dataFimStr);
            request.setAttribute("filtroNome", nome);
            request.setAttribute("filtroCpf", cpf);

            // Encaminha requisição para JSP responsável pela renderização do relatório.
            request.getRequestDispatcher("/pages/relatorio_usuarios.jsp")
                    .forward(request, response);

        } catch (Exception e) {
            // Ponto crítico:
            // Encapsula exceções oriundas de banco ou lógica
            // e as propaga como ServletException para tratamento padronizado pelo container.
            throw new ServletException("Erro no relatório de usuários", e);
        }
    }
}
