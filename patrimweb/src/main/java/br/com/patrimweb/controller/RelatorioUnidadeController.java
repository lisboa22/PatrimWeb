package br.com.patrimweb.controller;

import br.com.patrimweb.dao.UnidadeDAO;
import br.com.patrimweb.model.Unidade;
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
 * ao relatório de Unidades cadastradas no sistema.
 *
 * Propósito da classe:
 * - Controlar o fluxo de geração do relatório de unidades.
 * - Garantir que apenas usuários autenticados tenham acesso.
 * - Capturar filtros enviados pela interface web.
 * - Realizar consultas ao banco de dados através do UnidadeDAO.
 * - Preparar dados estatísticos utilizados em gráficos.
 * - Encaminhar os dados processados para a camada de visualização (JSP).
 *
 * Regras de negócio:
 * - O acesso ao relatório exige autenticação válida.
 * - Filtros são opcionais e aplicados dinamicamente.
 * - O gráfico utiliza o ano selecionado pelo usuário ou,
 *   na ausência deste, o primeiro ano disponível na base.
 */
@WebServlet("/RelatorioUnidadeController")
public class RelatorioUnidadeController extends HttpServlet {

    /**
     * Método responsável por processar requisições HTTP GET
     * para geração do relatório de unidades.
     *
     * Fluxo principal:
     * 1) Validação da sessão do usuário.
     * 2) Captura e conversão dos filtros recebidos.
     * 3) Consulta ao banco via DAO.
     * 4) Preparação de dados estatísticos.
     * 5) Encaminhamento para a página JSP.
     *
     * @param request  contém dados da requisição HTTP e parâmetros enviados pela interface
     * @param response responsável por construir a resposta HTTP ao cliente
     * @throws ServletException caso ocorra erro interno de processamento
     * @throws IOException      caso ocorra erro de entrada/saída
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // ===============================
        // VALIDAÇÃO DE SESSÃO (SEGURANÇA)
        // ===============================
        // Recupera a sessão existente sem criar uma nova.
        // Ponto crítico: evita criação automática de sessão para usuários não autenticados.
        HttpSession sessao = request.getSession(false);

        // Recupera o usuário autenticado armazenado na sessão.
        Usuario usuarioLogado = sessao != null
                ? (Usuario) sessao.getAttribute("usuarioLogado")
                : null;

        // Regra de negócio:
        // Caso não exista usuário autenticado, o acesso ao relatório é bloqueado.
        if (usuarioLogado == null) {
            response.sendRedirect("login.jsp");
            return; // Interrompe o fluxo para impedir acesso indevido.
        }

        // =====================================
        // LEITURA DOS PARÂMETROS DE FILTRO
        // =====================================
        // Parâmetros enviados pela interface (formulário GET).
        // Os filtros são opcionais e podem ser nulos.
        String dataInicioStr = request.getParameter("dataInicio");
        String dataFimStr    = request.getParameter("dataFim");
        String nome          = request.getParameter("nome");
        String endereco      = request.getParameter("endereco");

        // =====================================
        // CONVERSÃO DAS DATAS PARA TIMESTAMP
        // =====================================
        // Inicialmente definidos como null para permitir consultas sem filtro temporal.
        Timestamp dataInicio = null;
        Timestamp dataFim    = null;

        // Validação antes da conversão:
        // garante que somente valores válidos sejam transformados em Timestamp.
        if (dataInicioStr != null && !dataInicioStr.isEmpty()) {
            // Ajusta para início do dia, garantindo inclusão completa do período.
            dataInicio = Timestamp.valueOf(dataInicioStr + " 00:00:00");
        }

        if (dataFimStr != null && !dataFimStr.isEmpty()) {
            // Ajusta para final do dia, incluindo registros até 23:59:59.
            dataFim = Timestamp.valueOf(dataFimStr + " 23:59:59");
        }

        // =====================================
        // ACESSO AO BANCO DE DADOS
        // =====================================
        // try-with-resources garante fechamento automático da conexão,
        // evitando vazamento de recursos.
        try (Connection conn = Conexao.getConnection()) {

            // DAO responsável pelas operações de persistência da entidade Unidade.
            // Centraliza regras de acesso ao banco de dados.
            UnidadeDAO unidadeDAO = new UnidadeDAO(conn);

            // ======================================================
            // 1) CONSULTA FILTRADA PARA EXIBIÇÃO NA TABELA
            // ======================================================
            // Interação direta com banco de dados.
            // O DAO deve tratar filtros nulos como filtros opcionais.
            List<Unidade> unidades = unidadeDAO.filtrarUnidades(
                    dataInicio, dataFim, nome, endereco
            );

            // ======================================================
            // 2) OBTÉM ANOS DISPONÍVEIS PARA O GRÁFICO
            // ======================================================
            // Consulta anos distintos de cadastro existentes na base.
            // Utilizado para alimentar seletor do gráfico.
            List<Integer> anos = unidadeDAO.listarAnosCadastro();

            // ======================================================
            // 3) DEFINIÇÃO DO ANO SELECIONADO
            // ======================================================
            Integer anoSelecionado;
            String anoParam = request.getParameter("ano");

            // Estrutura de decisão relevante:
            // Prioriza ano escolhido pelo usuário.
            if (anoParam != null && !anoParam.isEmpty()) {
                anoSelecionado = Integer.parseInt(anoParam);
            } else {
                // Regra de fallback:
                // utiliza o primeiro ano disponível caso nenhum seja informado.
                anoSelecionado = anos.isEmpty() ? null : anos.get(0);
            }

            // ======================================================
            // 4) DADOS DO GRÁFICO (CADASTROS POR MÊS)
            // ======================================================
            // Map contendo estatísticas mensais:
            // chave -> mês (1 a 12)
            // valor -> quantidade de registros.
            Map<Integer, Integer> unidadesPorMes =
                    unidadeDAO.quantidadeUnidadesPorMes(anoSelecionado);

            // ======================================================
            // 5) TOTAL DE CADASTROS NO ANO
            // ======================================================
            // Soma agregada utilizando Stream API.
            // Importante para cálculo proporcional em gráficos.
            int totalUnidadesAno = unidadesPorMes.values()
                    .stream()
                    .mapToInt(Integer::intValue)
                    .sum();

            // =====================================
            // ENVIO DOS DADOS PARA A VIEW (JSP)
            // =====================================
            // Define atributos utilizados pela camada de apresentação.
            request.setAttribute("unidades",         unidades);
            request.setAttribute("anos",             anos);
            request.setAttribute("anoAtual",         anoSelecionado);
            request.setAttribute("anoSelecionado",   anoSelecionado);
            request.setAttribute("unidadesPorMes",   unidadesPorMes);
            request.setAttribute("totalUnidadesAno", totalUnidadesAno);

            // Mantém filtros preenchidos após submissão,
            // melhorando a experiência do usuário.
            request.setAttribute("filtroDataInicio", dataInicioStr);
            request.setAttribute("filtroDataFim",    dataFimStr);
            request.setAttribute("filtroNome",       nome);
            request.setAttribute("filtroEndereco",   endereco);

            // Encaminha requisição para JSP responsável pela renderização do relatório.
            request.getRequestDispatcher("/pages/relatorio_unidades.jsp")
                    .forward(request, response);

        } catch (Exception e) {
            // Ponto crítico:
            // Captura falhas de banco ou processamento e encapsula em ServletException
            // para padronizar tratamento de erros na aplicação.
            throw new ServletException("Erro no relatório de unidades", e);
        }
    }
}
