package br.com.patrimweb.controller;

import br.com.patrimweb.dao.FabricanteDAO;
import br.com.patrimweb.model.Fabricante;
import br.com.patrimweb.utils.Conexao;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Controller responsável pela geração do relatório de fabricantes do sistema.
 *
 * Objetivos principais:
 * - Validar se o usuário possui sessão ativa antes de acessar o relatório.
 * - Capturar filtros enviados pela interface (período e nome).
 * - Consultar dados no banco de dados através do FabricanteDAO.
 * - Preparar dados estatísticos para exibição (quantidade por mês e total anual).
 * - Encaminhar os dados processados para a JSP responsável pela renderização.
 *
 * Regras de negócio relevantes:
 * - O relatório só pode ser acessado por usuários autenticados.
 * - Os filtros são opcionais e aplicados dinamicamente.
 * - O sistema calcula automaticamente o total anual baseado nos valores mensais retornados.
 */
@WebServlet("/RelatorioFabricanteController")
public class RelatorioFabricanteController extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /**
     * Método responsável por atender requisições HTTP GET para geração
     * do relatório de fabricantes.
     *
     * Fluxo principal:
     * 1. Valida sessão do usuário.
     * 2. Configura headers para impedir cache da página.
     * 3. Abre conexão com o banco de dados.
     * 4. Captura e converte filtros recebidos via request.
     * 5. Executa consultas no DAO.
     * 6. Calcula estatísticas necessárias ao relatório.
     * 7. Envia os dados para a JSP via atributos da requisição.
     *
     * @param request  objeto contendo dados da requisição HTTP
     * @param response objeto responsável pela resposta HTTP
     * @throws ServletException em caso de erro de processamento do servlet
     * @throws IOException      em caso de falha de entrada/saída
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Valida se existe sessão ativa e usuário autenticado.
        // Caso contrário, interrompe a execução do fluxo.
        if (!validarSessao(request, response)) return;

        // Define cabeçalhos HTTP para evitar cache da página,
        // garantindo que o relatório sempre seja atualizado.
        configurarNoCache(response);

        // Abre conexão com banco utilizando try-with-resources,
        // garantindo fechamento automático da conexão.
        try (Connection conn = Conexao.getConnection()) {

            // DAO responsável por todas as operações relacionadas a Fabricante.
            FabricanteDAO fabricanteDAO = new FabricanteDAO(conn);

            // ==========================
            // CAPTURA FILTROS
            // ==========================
            // Recupera parâmetros enviados pela interface (GET).
            String dataInicioStr = request.getParameter("dataInicio");
            String dataFimStr    = request.getParameter("dataFim");
            String nome          = request.getParameter("nome");

            // Conversão das datas recebidas em String para Timestamp,
            // permitindo filtragem correta nas consultas SQL.
            Timestamp dataInicio = converterDataInicio(dataInicioStr);
            Timestamp dataFim    = converterDataFim(dataFimStr);

            // ==========================
            // CONSULTAS
            // ==========================
            // Busca lista de fabricantes aplicando filtros informados.
            // Interação direta com banco via DAO.
            List<Fabricante> fabricantes =
                    fabricanteDAO.filtrarFabricantes(dataInicio, dataFim, nome);

            // Recupera anos distintos de cadastro existentes no banco.
            List<Integer> anos = fabricanteDAO.listarAnosCadastro();

            // Define qual ano será utilizado para geração do gráfico/estatística.
            Integer anoSelecionado = definirAnoSelecionado(request, anos);

            // Consulta quantidade de fabricantes cadastrados por mês no ano selecionado.
            Map<Integer, Integer> fabricantesPorMes =
                    fabricanteDAO.quantidadeFabricantesPorMes(anoSelecionado);

            // Calcula o total anual somando todos os valores retornados no mapa.
            // Uso de Stream API para agregação dos dados.
            int totalFabricantesAno = fabricantesPorMes.values()
                    .stream()
                    .mapToInt(Integer::intValue)
                    .sum();

            // ==========================
            // ATRIBUTOS
            // ==========================
            // Dados enviados para a camada de visualização (JSP).
            request.setAttribute("fabricantes", fabricantes);
            request.setAttribute("anos", anos);
            request.setAttribute("anoAtual", anoSelecionado);
            request.setAttribute("anoSelecionado", anoSelecionado);
            request.setAttribute("fabricantesPorMes", fabricantesPorMes);
            request.setAttribute("totalFabricantesAno", totalFabricantesAno);

            // Mantém filtros preenchidos após requisição,
            // permitindo persistência visual no formulário.
            request.setAttribute("filtroDataInicio", dataInicioStr);
            request.setAttribute("filtroDataFim", dataFimStr);
            request.setAttribute("filtroNome", nome);

            // Encaminha requisição para página JSP responsável pela exibição do relatório.
            request.getRequestDispatcher("/pages/relatorio_fabricantes.jsp")
                    .forward(request, response);

        } catch (Exception e) {
            // Tratamento centralizado de exceção.
            // Encapsula erro original dentro de ServletException.
            throw new ServletException("Erro no relatório de fabricantes", e);
        }
    }

    // ==========================
    // VALIDAÇÃO DE SESSÃO
    // ==========================

    /**
     * Valida se existe sessão ativa e se há usuário autenticado.
     *
     * Regra de segurança:
     * - O acesso ao relatório é permitido apenas para usuários logados.
     *
     * @param request  requisição HTTP atual
     * @param response resposta HTTP para possível redirecionamento
     * @return true se sessão válida; false caso contrário
     * @throws IOException em caso de erro no redirecionamento
     */
    private boolean validarSessao(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        // Recupera sessão existente sem criar nova.
        HttpSession session = request.getSession(false);

        // Valida existência da sessão e atributo de autenticação.
        if (session == null || session.getAttribute("usuarioLogado") == null) {
            // Redireciona para página inicial caso não autenticado.
            response.sendRedirect(request.getContextPath() + "/");
            return false;
        }

        return true;
    }

    // ==========================
    // NO CACHE
    // ==========================

    /**
     * Configura headers HTTP para impedir armazenamento em cache.
     *
     * Objetivo:
     * Garantir que relatórios sensíveis não sejam reutilizados
     * após logout ou navegação anterior.
     *
     * @param response objeto de resposta HTTP
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
     * Converte uma data no formato String (yyyy-MM-dd)
     * para Timestamp representando o início do dia.
     *
     * Regra aplicada:
     * - Caso a data não seja informada, retorna null,
     *   permitindo consultas sem filtro inicial.
     *
     * @param data data recebida da requisição
     * @return Timestamp correspondente ao início do dia ou null
     */
    private Timestamp converterDataInicio(String data) {
        if (data == null || data.isEmpty()) return null;

        // Conversão para LocalDate e definição do horário 00:00:00
        LocalDate localDate = LocalDate.parse(data);
        return Timestamp.valueOf(localDate.atStartOfDay());
    }

    /**
     * Converte uma data no formato String (yyyy-MM-dd)
     * para Timestamp representando o final do dia.
     *
     * Regra aplicada:
     * - Define horário 23:59:59 para garantir inclusão
     *   completa dos registros do dia informado.
     *
     * @param data data recebida da requisição
     * @return Timestamp correspondente ao fim do dia ou null
     */
    private Timestamp converterDataFim(String data) {
        if (data == null || data.isEmpty()) return null;

        LocalDate localDate = LocalDate.parse(data);
        return Timestamp.valueOf(localDate.atTime(23, 59, 59));
    }

    /**
     * Define qual ano será utilizado no relatório.
     *
     * Regras:
     * - Se o usuário informar o parâmetro "ano", ele tem prioridade.
     * - Caso contrário, utiliza o primeiro ano retornado pela consulta.
     * - Se não houver anos cadastrados, retorna null.
     *
     * @param request requisição HTTP contendo parâmetros
     * @param anos lista de anos disponíveis no banco
     * @return ano selecionado para geração do relatório
     */
    private Integer definirAnoSelecionado(HttpServletRequest request, List<Integer> anos) {
        String anoParam = request.getParameter("ano");

        // Verifica se o usuário selecionou manualmente um ano.
        if (anoParam != null && !anoParam.isEmpty()) {
            return Integer.parseInt(anoParam);
        }

        // Define ano padrão baseado nos dados disponíveis.
        return anos.isEmpty() ? null : anos.get(0);
    }
}
