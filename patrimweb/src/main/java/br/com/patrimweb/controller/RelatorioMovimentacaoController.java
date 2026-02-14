package br.com.patrimweb.controller;

import br.com.patrimweb.dao.MovimentacaoDAO;
import br.com.patrimweb.model.Movimentacao;
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
 * Controller responsável por processar as requisições do relatório de movimentações.
 *
 * Objetivo da classe:
 * - Controlar o fluxo de geração do relatório de movimentações do sistema.
 * - Garantir acesso apenas a usuários autenticados.
 * - Capturar filtros informados pela interface.
 * - Realizar consultas ao banco de dados através do MovimentacaoDAO.
 * - Preparar dados estatísticos utilizados em gráficos e tabelas.
 * - Encaminhar os dados processados para a camada de visualização (JSP).
 *
 * Regras de negócio principais:
 * - O relatório só pode ser acessado por usuários logados.
 * - Filtros são opcionais e aplicados dinamicamente nas consultas.
 * - Datas informadas devem considerar o intervalo completo do dia.
 */
@WebServlet("/RelatorioMovimentacaoController")
public class RelatorioMovimentacaoController extends HttpServlet {

    /**
     * Processa requisições HTTP GET para geração do relatório de movimentações.
     *
     * Fluxo geral:
     * 1. Valida existência de sessão ativa.
     * 2. Captura filtros enviados pela interface.
     * 3. Converte parâmetros para tipos adequados.
     * 4. Consulta dados no banco através do DAO.
     * 5. Calcula estatísticas para gráficos.
     * 6. Encaminha dados para a JSP responsável pela apresentação.
     *
     * @param request  objeto contendo os dados da requisição HTTP
     * @param response objeto utilizado para construção da resposta HTTP
     * @throws ServletException em caso de erro interno na servlet
     * @throws IOException      em caso de erro de entrada/saída
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // ==========================
        // VALIDAÇÃO DE SESSÃO
        // ==========================
        // Recupera sessão existente sem criar nova.
        // Ponto crítico: evita criação indevida de sessão para usuários não autenticados.
        HttpSession sessao = request.getSession(false);

        // Recupera usuário autenticado armazenado na sessão.
        Usuario usuarioLogado = sessao != null
                ? (Usuario) sessao.getAttribute("usuarioLogado")
                : null;

        // Regra de segurança:
        // Caso não exista usuário autenticado, o acesso ao relatório é bloqueado.
        if (usuarioLogado == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        // ==========================
        // CAPTURA DOS FILTROS
        // ==========================
        // Parâmetros enviados via query string ou formulário.
        // Estes filtros são opcionais e utilizados para restringir resultados.
        String dataInicioStr    = request.getParameter("dataInicio");
        String dataFimStr       = request.getParameter("dataFim");
        String tipoMovimentacao = request.getParameter("tipoMovimentacao");
        String equipamento      = request.getParameter("equipamento");

        // ==========================
        // CONVERSÃO DE DATAS
        // ==========================
        // Conversão necessária pois o banco trabalha com Timestamp.
        // Ajusta horários para incluir o dia completo no filtro.
        Timestamp dataInicio = null;
        Timestamp dataFim    = null;

        // Validação de existência da data inicial antes da conversão.
        if (dataInicioStr != null && !dataInicioStr.isEmpty()) {
            // Define início do dia para garantir inclusão de registros desde 00:00:00.
            dataInicio = Timestamp.valueOf(dataInicioStr + " 00:00:00");
        }

        // Validação de existência da data final antes da conversão.
        if (dataFimStr != null && !dataFimStr.isEmpty()) {
            // Define final do dia para incluir todos os registros até 23:59:59.
            dataFim = Timestamp.valueOf(dataFimStr + " 23:59:59");
        }

        // ==========================
        // BLOCO PRINCIPAL DE CONSULTA
        // ==========================
        // Abre conexão com banco utilizando try-with-resources,
        // garantindo fechamento automático da conexão.
        try (Connection conn = Conexao.getConnection()) {

            // DAO responsável por todas as operações relacionadas às movimentações.
            // Centraliza regras de acesso ao banco de dados.
            MovimentacaoDAO movimentacaoDAO = new MovimentacaoDAO(conn);

            // ------------------------------------------------------
            // 1. LISTA FILTRADA PARA EXIBIÇÃO NA TABELA
            // ------------------------------------------------------
            // Consulta principal do relatório.
            // Interação direta com banco de dados.
            // O DAO deve tratar filtros nulos como opcionais.
            List<Movimentacao> movimentacoes = movimentacaoDAO.filtrarMovimentacoes(
                    dataInicio, dataFim, tipoMovimentacao, equipamento
            );

            // ------------------------------------------------------
            // 2. ANOS DISPONÍVEIS PARA O GRÁFICO
            // ------------------------------------------------------
            // Recupera anos existentes no banco para popular seletor de período.
            List<Integer> anos = movimentacaoDAO.listarAnosCadastro();

            // ------------------------------------------------------
            // 3. DEFINIÇÃO DO ANO SELECIONADO
            // ------------------------------------------------------
            // Estrutura condicional responsável por determinar qual ano será utilizado.
            Integer anoSelecionado;
            String anoParam = request.getParameter("ano");

            // Prioriza escolha manual do usuário.
            if (anoParam != null && !anoParam.isEmpty()) {
                anoSelecionado = Integer.parseInt(anoParam);
            } else {
                // Caso não informado, utiliza o primeiro ano disponível.
                // Regra implícita: geralmente representa o ano mais recente.
                anoSelecionado = anos.isEmpty() ? null : anos.get(0);
            }

            // ------------------------------------------------------
            // 4. DADOS DO GRÁFICO
            // ------------------------------------------------------
            // Consulta estatística que retorna quantidade de movimentações por mês.
            // Estrutura Map:
            // chave   -> mês
            // valor   -> quantidade de movimentações
            Map<Integer, Integer> movimentacoesPorMes =
                    movimentacaoDAO.quantidadeMovimentacoesPorMes(anoSelecionado);

            // ------------------------------------------------------
            // 5. TOTAL DO ANO
            // ------------------------------------------------------
            // Soma todos os valores do Map utilizando Stream API.
            // Importante para cálculos proporcionais em gráficos.
            int totalMovimentacoesAno = movimentacoesPorMes.values()
                    .stream()
                    .mapToInt(Integer::intValue)
                    .sum();

            // ==========================
            // ENVIO DE ATRIBUTOS PARA A JSP
            // ==========================
            // Dados principais utilizados pela camada de apresentação.
            request.setAttribute("movimentacoes",         movimentacoes);
            request.setAttribute("anos",                  anos);
            request.setAttribute("anoAtual",              anoSelecionado);
            request.setAttribute("anoSelecionado",        anoSelecionado);
            request.setAttribute("movimentacoesPorMes",   movimentacoesPorMes);
            request.setAttribute("totalMovimentacoesAno", totalMovimentacoesAno);

            // Preserva valores dos filtros após submissão,
            // melhorando experiência do usuário ao manter estado da busca.
            request.setAttribute("filtroDataInicio",    dataInicioStr);
            request.setAttribute("filtroDataFim",       dataFimStr);
            request.setAttribute("filtroTipo",          tipoMovimentacao);
            request.setAttribute("filtroEquipamento",   equipamento);

            // Encaminha requisição para JSP responsável pela renderização do relatório.
            request.getRequestDispatcher("/pages/relatorio_movimentacoes.jsp")
                    .forward(request, response);

        } catch (Exception e) {
            // Ponto crítico:
            // Captura qualquer falha relacionada a banco ou processamento.
            // A exceção original é encapsulada para facilitar rastreamento.
            throw new ServletException("Erro no relatório de movimentações", e);
        }
    }
}
