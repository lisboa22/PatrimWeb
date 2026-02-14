package br.com.patrimweb.controller;

import br.com.patrimweb.dao.EquipamentoDAO;
import br.com.patrimweb.dao.FabricanteDAO;
import br.com.patrimweb.model.Equipamento;
import br.com.patrimweb.model.Fabricante;
import br.com.patrimweb.utils.Conexao;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Controller responsável pela geração do relatório de equipamentos.
 *
 * RESPONSABILIDADES:
 * - Processar requisições de visualização do relatório.
 * - Aplicar filtros de pesquisa enviados pela interface.
 * - Consultar dados agregados para gráficos e listagens.
 * - Encaminhar dados preparados para a camada de visualização (JSP).
 *
 * REGRAS DE NEGÓCIO:
 * - O acesso ao relatório exige usuário autenticado.
 * - Filtros são opcionais e aplicados dinamicamente.
 * - O relatório pode ser filtrado por período, nome do equipamento e fabricante.
 * - O sistema apresenta também estatísticas anuais de cadastro.
 *
 * INTERAÇÃO COM BANCO:
 * - Utiliza EquipamentoDAO para consultas principais e estatísticas.
 * - Utiliza FabricanteDAO para carregar lista de fabricantes disponíveis.
 *
 * PONTOS CRÍTICOS:
 * - Conversão direta de parâmetros HTTP para datas e inteiros.
 * - Dependência da consistência dos parâmetros enviados pela interface.
 */
@WebServlet("/RelatorioEquipamentoController")
public class RelatorioEquipamentoController extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /**
     * Processa requisições GET responsáveis pela geração do relatório.
     *
     * FLUXO PRINCIPAL:
     * 1. Valida sessão do usuário autenticado.
     * 2. Impede cache da página.
     * 3. Captura filtros enviados pela interface.
     * 4. Executa consultas no banco de dados.
     * 5. Calcula métricas agregadas.
     * 6. Encaminha dados para a JSP.
     *
     * @param request  Requisição HTTP contendo filtros opcionais.
     * @param response Resposta HTTP enviada ao navegador.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Validação de autenticação do usuário
        if (!validarSessao(request, response)) return;

        // Evita cache de páginas protegidas
        configurarNoCache(response);

        // Try-with-resources garante fechamento automático da conexão
        try (Connection conn = Conexao.getConnection()) {

            // DAOs responsáveis pelas consultas ao banco
            EquipamentoDAO equipamentoDAO = new EquipamentoDAO(conn);
            FabricanteDAO fabricanteDAO   = new FabricanteDAO(conn);

            // ==========================
            // CAPTURA FILTROS
            // ==========================

            // Parâmetros enviados via query string (GET)
            String dataInicioStr    = request.getParameter("dataInicio");
            String dataFimStr       = request.getParameter("dataFim");
            String nomeEquip        = request.getParameter("nomeEquip");
            String fabricanteIdStr  = request.getParameter("fabricanteId");

            // Conversões para tipos utilizados na camada DAO
            Timestamp dataInicio = converterDataInicio(dataInicioStr);
            Timestamp dataFim    = converterDataFim(dataFimStr);
            Integer fabricanteId = parseInteger(fabricanteIdStr);

            // ==========================
            // CONSULTAS
            // ==========================

            /**
             * Consulta principal do relatório.
             * Aplica filtros opcionais conforme valores informados.
             */
            List<Equipamento> equipamentos =
                    equipamentoDAO.filtrarEquipamentos(
                            dataInicio, dataFim, nomeEquip, fabricanteId
                    );

            // Lista completa de fabricantes para popular filtros da interface
            List<Fabricante> fabricantes = fabricanteDAO.listarFabricantes();

            // Lista de anos existentes com registros de equipamentos
            List<Integer> anos = equipamentoDAO.listarAnosCadastro();

            // Define qual ano será exibido no gráfico
            Integer anoSelecionado = definirAnoSelecionado(request, anos);

            /**
             * Consulta agregada:
             * Retorna quantidade de equipamentos cadastrados por mês
             * para o ano selecionado.
             */
            Map<Integer, Integer> equipamentosPorMes =
                    equipamentoDAO.quantidadeEquipamentosPorMes(anoSelecionado);

            /**
             * Soma total de equipamentos do ano.
             * Utiliza Stream API para agregação dos valores retornados.
             */
            int totalEquipamentosAno = equipamentosPorMes.values()
                    .stream()
                    .mapToInt(Integer::intValue)
                    .sum();

            // ==========================
            // ATRIBUTOS
            // ==========================

            /**
             * Atributos enviados para a JSP.
             * Servem tanto para exibição dos dados quanto para manter
             * os filtros selecionados na interface.
             */
            request.setAttribute("equipamentos", equipamentos);
            request.setAttribute("fabricantes", fabricantes);
            request.setAttribute("anos", anos);
            request.setAttribute("anoAtual", anoSelecionado);
            request.setAttribute("anoSelecionado", anoSelecionado);
            request.setAttribute("equipamentosPorMes", equipamentosPorMes);
            request.setAttribute("totalEquipamentosAno", totalEquipamentosAno);

            // Mantém filtros preenchidos após requisição
            request.setAttribute("filtroDataInicio", dataInicioStr);
            request.setAttribute("filtroDataFim", dataFimStr);
            request.setAttribute("filtroNomeEquip", nomeEquip);
            request.setAttribute("filtroFabricanteId", fabricanteIdStr);

            // Encaminha requisição para a página de relatório
            request.getRequestDispatcher("/pages/relatorio_equipamentos.jsp")
                    .forward(request, response);

        } catch (Exception e) {
            // Tratamento centralizado de erro da geração do relatório
            throw new ServletException("Erro no relatório de equipamentos", e);
        }
    }

    // ==========================
    // VALIDA SESSÃO
    // ==========================

    /**
     * Verifica se existe sessão ativa e usuário autenticado.
     *
     * REGRA DE SEGURANÇA:
     * - Impede acesso direto ao relatório sem login.
     *
     * @return true se sessão válida, false caso contrário.
     */
    private boolean validarSessao(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);

        // Estrutura condicional crítica de controle de acesso
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
     * - Evitar acesso a dados protegidos após logout utilizando
     *   navegação do navegador.
     */
    private void configurarNoCache(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
    }

    // ==========================
    // MÉTODOS AUXILIARES
    // ==========================

    /**
     * Converte a data inicial recebida da interface para Timestamp.
     *
     * REGRA:
     * - Define horário inicial do dia (00:00:00).
     *
     * @param data Data no formato ISO (yyyy-MM-dd).
     * @return Timestamp correspondente ou null caso vazio.
     */
    private Timestamp converterDataInicio(String data) {
        if (data == null || data.isEmpty()) return null;
        LocalDate localDate = LocalDate.parse(data);
        return Timestamp.valueOf(localDate.atStartOfDay());
    }

    /**
     * Converte a data final recebida da interface para Timestamp.
     *
     * REGRA:
     * - Ajusta horário para o final do dia (23:59:59),
     *   garantindo inclusão completa da data no filtro.
     *
     * @param data Data informada pelo usuário.
     * @return Timestamp correspondente ou null caso vazio.
     */
    private Timestamp converterDataFim(String data) {
        if (data == null || data.isEmpty()) return null;
        LocalDate localDate = LocalDate.parse(data);
        return Timestamp.valueOf(localDate.atTime(23, 59, 59));
    }

    /**
     * Converte String para Integer quando houver valor informado.
     *
     * VALIDAÇÃO:
     * - Retorna null quando parâmetro não é enviado,
     *   permitindo filtros opcionais.
     *
     * @param valor Valor recebido da requisição.
     * @return Integer convertido ou null.
     */
    private Integer parseInteger(String valor) {
        if (valor == null || valor.isEmpty()) return null;
        return Integer.parseInt(valor);
    }

    /**
     * Define qual ano será utilizado no relatório estatístico.
     *
     * REGRA:
     * - Prioriza ano informado pelo usuário.
     * - Caso não exista, utiliza o primeiro ano disponível.
     *
     * @param request requisição HTTP contendo possível parâmetro "ano".
     * @param anos lista de anos disponíveis no banco.
     * @return Ano selecionado ou null caso não exista registro.
     */
    private Integer definirAnoSelecionado(HttpServletRequest request, List<Integer> anos) {
        String anoParam = request.getParameter("ano");

        if (anoParam != null && !anoParam.isEmpty()) {
            return Integer.parseInt(anoParam);
        }

        return anos.isEmpty() ? null : anos.get(0);
    }
}
