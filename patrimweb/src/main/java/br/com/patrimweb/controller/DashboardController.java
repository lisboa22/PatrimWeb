package br.com.patrimweb.controller;

import br.com.patrimweb.dao.EquipamentoDAO;
import br.com.patrimweb.dao.MovimentacaoDAO;
import br.com.patrimweb.dao.UnidadeDAO;
import br.com.patrimweb.model.Equipamento;
import br.com.patrimweb.model.Fabricante;
import br.com.patrimweb.model.Movimentacao;
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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller responsável por carregar e preparar todos os dados exibidos
 * na tela de Dashboard do sistema PatrimWeb.
 *
 * Responsabilidades principais:
 * - Validar sessão do usuário autenticado.
 * - Buscar dados consolidados no banco de dados através dos DAOs.
 * - Aplicar regras de negócio relacionadas a filtros de período.
 * - Preparar dados estatísticos para gráficos (rosca e barras).
 * - Encaminhar os dados para a view (dashboard.jsp).
 *
 * Esta classe atua como camada intermediária entre a camada de persistência (DAO)
 * e a camada de apresentação (JSP).
 */
@WebServlet("/DashboardController")
public class DashboardController extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    // DAOs responsáveis pela comunicação com o banco de dados
    private EquipamentoDAO equipamentoDAO;
    private MovimentacaoDAO movimentacaoDAO;
    private UnidadeDAO unidadeDAO;
    
    // Conexão com banco (não utilizada diretamente nesta classe, mas declarada)
    private Connection conexao;

    /**
     * Método executado automaticamente na inicialização do Servlet.
     *
     * Responsável por instanciar os DAOs com conexão ativa ao banco de dados.
     *
     * Regra crítica:
     * - Caso ocorra falha na criação dos DAOs, a aplicação interrompe a inicialização
     *   lançando ServletException.
     */
    @Override
    public void init() throws ServletException {
        try {
            equipamentoDAO = new EquipamentoDAO(Conexao.getConnection());
            movimentacaoDAO = new MovimentacaoDAO(Conexao.getConnection());
            unidadeDAO = new UnidadeDAO(Conexao.getConnection());
        } catch (Exception e) {
            throw new ServletException("Erro ao inicializar DAOs do Dashboard", e);
        }
    }

    /**
     * Processa requisições HTTP GET.
     *
     * Fluxo principal:
     * 1. Valida se existe sessão ativa.
     * 2. Impede cache da página do dashboard.
     * 3. Carrega dados necessários para exibição.
     *
     * @param request  objeto contendo dados da requisição HTTP
     * @param response objeto para construção da resposta HTTP
     * @throws ServletException em caso de erro interno
     * @throws IOException      em caso de falha de I/O
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
    	
    	// 🔒 Verificação de sessão
        // Regra de segurança: somente usuários autenticados podem acessar o dashboard.
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuarioLogado") == null) {
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }

        // 🧼 Evita cache do dashboard
        // Garante que o navegador não armazene a página, evitando exibição
        // de dados desatualizados após logout ou alterações.
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        
        
        try {
            carregarDadosDashboard(request, response);
            
        } catch (Exception e) {
            throw new ServletException("Erro ao carregar dados do dashboard", e);
        }
    }

    /**
     * Processa requisições HTTP POST.
     *
     * Regra de negócio:
     * - O dashboard trata requisições POST da mesma forma que GET.
     *
     * @param request  objeto da requisição
     * @param response objeto da resposta
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        doGet(request, response);
    }

    /**
     * Método responsável por buscar, consolidar e preparar todos os dados
     * necessários para exibição no Dashboard.
     *
     * Principais responsabilidades:
     * - Buscar totais para os cards superiores.
     * - Preparar dados para gráficos estatísticos.
     * - Aplicar filtro de período para movimentações.
     * - Selecionar últimas 10 movimentações.
     * - Encaminhar dados para a view.
     *
     * @param request  requisição HTTP
     * @param response resposta HTTP
     * @throws Exception em caso de erro na camada de persistência
     */
    private void carregarDadosDashboard(HttpServletRequest request, HttpServletResponse response) 
            throws Exception {
    	
    	// Obtém nova conexão com o banco
        Connection conexao = Conexao.getConnection(); 
        
        // Instancia DAO específico para consultas de movimentação
        MovimentacaoDAO movDAO = new MovimentacaoDAO(conexao);
        
        
        // ===============================
        // 1. CARDS DO TOPO
        // ===============================
        // Busca todos os equipamentos e calcula total
        List<Equipamento> equipamentos = equipamentoDAO.listarEquipamentos();
        int totalEquipamentos = equipamentos.size();
        request.setAttribute("totalEquipamentos", totalEquipamentos);
        
        // Busca todas as movimentações e calcula total
        List<Movimentacao> movimentacoes = movimentacaoDAO.listarMovimentacoes();
        int totalMovimentacoes = movimentacoes.size();
        request.setAttribute("totalMovimentacoes", totalMovimentacoes);
        
        // Busca todas as unidades cadastradas
        List<Unidade> unidades = unidadeDAO.listarUnidades();
        int unidadesAtendidas = unidades.size();
        request.setAttribute("unidadesAtendidas", unidadesAtendidas);
        
        // Conta equipamentos atualmente em manutenção
        // Regra de negócio: este valor representa ativos fora de operação
        int ativosManutencao = movimentacaoDAO.contarEquipamentosEmManutencao();
        request.setAttribute("ativosManutencao", ativosManutencao);

        
        // ===============================
        // 2. GRÁFICO DE ROSCA
        // ===============================
        // Recupera dados consolidados diretamente do DAO
        // Interação com banco: consulta agregada por tipo/fabricante
        Map<String, Integer> equipamentosPorTipo = movDAO.contarEquipamentosEmMovimentacao();
        request.setAttribute("equipamentosPorTipo", equipamentosPorTipo);
        
        // ===============================
        // 3. GRÁFICO DE BARRAS
        // ===============================
        // Filtro por período selecionado pelo usuário
        
        String periodoParam = request.getParameter("periodo");
        
        // Regra de negócio:
        // Caso não seja informado período, padrão será últimos 6 meses
        String periodoSelecionado = (periodoParam != null && !periodoParam.isEmpty()) 
                ? periodoParam 
                : "6";
        
        int mesesAtras = Integer.parseInt(periodoSelecionado);
        
        // Calcula data inicial com base no período
        LocalDateTime dataInicio = LocalDateTime.now().minusMonths(mesesAtras);
        Timestamp timestampInicio = Timestamp.valueOf(dataInicio);
        
        // Consulta movimentações filtradas pelo período
        List<Movimentacao> movimentacoesFiltradas = movDAO.listarMovimentacoesPorPeriodo(timestampInicio);
        
        // Consolida dados por unidade destino
        Map<String, Integer> solicitacoesPorUnidade = contarMovimentacoesPorUnidade(movimentacoesFiltradas);
        
        request.setAttribute("solicitacoesPorUnidade", solicitacoesPorUnidade);
        request.setAttribute("periodoSelecionado", periodoSelecionado);
        
        // ===============================
        // 4. TABELA - Últimas 10 movimentações
        // ===============================
        // Regra: exibir no máximo 10 registros mais recentes
        List<Movimentacao> atividadesRecentes = movimentacoes.size() > 10 
            ? movimentacoes.subList(0, 10) 
            : movimentacoes;
        
        request.setAttribute("atividadesRecentes", atividadesRecentes);
        
        // Encaminha requisição para a view JSP
        RequestDispatcher dispatcher = request.getRequestDispatcher("/pages/dashboard.jsp");
        dispatcher.forward(request, response);
    }
    
    
    /**
     * Conta movimentações por unidade destino.
     *
     * Regra de negócio:
     * - Consolida quantidade de movimentações agrupadas por nome da unidade.
     * - Limita o resultado aos 5 primeiros registros encontrados.
     *
     * Estrutura utilizada:
     * - LinkedHashMap para preservar ordem de inserção.
     *
     * @param movimentacoes lista de movimentações filtradas por período
     * @return mapa contendo nome da unidade como chave e total de movimentações como valor
     */
    private Map<String, Integer> contarMovimentacoesPorUnidade(List<Movimentacao> movimentacoes) {
        Map<String, Integer> mapa = new LinkedHashMap<>();
        
        // Percorre todas as movimentações recebidas
        // Estrutura de repetição responsável por consolidação estatística
        for (Movimentacao mov : movimentacoes) {
            String nomeUnidade = mov.getUnidadeDestino().getNomeUnid();
            
            // Incrementa contador da unidade
            mapa.put(nomeUnidade, mapa.getOrDefault(nomeUnidade, 0) + 1);
        }
        
        // Limita aos 5 primeiros resultados
        // Regra visual para não sobrecarregar gráfico
        Map<String, Integer> resultado = new LinkedHashMap<>();
        int count = 0;
        
        for (Map.Entry<String, Integer> entry : mapa.entrySet()) {
            if (count < 5) {
                resultado.put(entry.getKey(), entry.getValue());
                count++;
            } else {
                break;
            }
        }
        
        return resultado;
    }
}
