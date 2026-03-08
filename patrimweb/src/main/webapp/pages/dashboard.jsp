<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<!-- 
    Página JSP responsável pela exibição do Dashboard do Sistema de Patrimônio (PatrimWeb).

    Objetivo:
    - Apresentar indicadores estratégicos consolidados do sistema.
    - Exibir métricas resumidas (cards).
    - Demonstrar dados estatísticos em formato gráfico.
    - Listar movimentações recentes para acompanhamento operacional.

    Observação:
    - Os dados são fornecidos pelo Controller (ex: DashboardController) via atributos
      adicionados ao escopo request.
    - Esta página atua exclusivamente como camada de visualização (View).
-->

<!-- Define a variável pageTitle para ativar o menu correto -->
<!-- Regra de navegação: o valor é utilizado na sidebar para destacar o menu ativo -->
<c:set var="pageTitle" value="Dashboard" scope="request" />

<!DOCTYPE html>
<html lang="pt-br">
<head>
    <!-- Configurações básicas de codificação e responsividade -->
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${pageTitle} - Sistema de Patrimônio</title>

    <!-- Bibliotecas externas para ícones e tipografia -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600&display=swap" rel="stylesheet">

    <!-- Arquivo CSS principal do sistema -->
    <link rel="stylesheet" href="css/patrimweb.css">

    <!-- Biblioteca Chart.js para renderização dos gráficos -->
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
</head>
<body>

	<!-- Overlay utilizado para dispositivos móveis ao abrir a sidebar -->
	<!-- Regra de usabilidade: fecha o menu lateral ao clicar fora -->
	<div class="mobile-overlay" id="mobileOverlay" onclick="toggleSidebar()"></div>
	

    <!-- INCLUIR SIDEBAR -->
    <!-- Componente reutilizável de navegação lateral -->
    <jsp:include page="/includes/sidebar.jsp" />

    <main class="main-content">
        <header>
            <!-- Botão menu mobile -->
            <!-- Visível apenas em telas menores -->
            <button class="menu-toggle" onclick="toggleSidebar()">
                <i class="fa-solid fa-bars"></i>
            </button>
            
            <!-- Título dinâmico da página -->
            <h1>${pageTitle}</h1>
            
            <!-- INCLUIR MENU DE USUÁRIO -->
            <!-- Componente responsável por informações do usuário logado -->
            <jsp:include page="/includes/user-menu.jsp" />
        </header>

        <div class="dashboard-container">
            
            <!-- CARDS COM DADOS DINÂMICOS -->
            <!-- Indicadores estratégicos principais do sistema -->
            <!-- Os valores são carregados do backend via atributos no request -->
            <div class="cards-grid">
                <div class="card">
                    <div class="card-icon"><i class="fa-solid fa-desktop"></i></div>
                    <div class="card-info">
                        <!-- Total de equipamentos cadastrados no sistema -->
                        <h3>${totalEquipamentos}</h3>
                        <p>Total Equipamentos</p>
                    </div>
                </div>
                <div class="card">
                    <div class="card-icon"><i class="fa-solid fa-rotate"></i></div>
                    <div class="card-info">
                        <!-- Total de movimentações registradas -->
                        <h3>${totalMovimentacoes}</h3>
                        <p>Total Movimentações</p>
                    </div>
                </div>
                <div class="card">
                    <div class="card-icon"><i class="fa-solid fa-clipboard-check"></i></div>
                    <div class="card-info">
                        <!-- Total de unidades cadastradas no sistema -->
                        <h3>${unidadesAtendidas}</h3>
                        <p>Unidades Cadastradas</p>
                    </div>
                </div>
                <div class="card">
                    <div class="card-icon"><i class="fa-solid fa-screwdriver-wrench"></i></div>
                    <div class="card-info">
                        <!-- Quantidade de equipamentos atualmente em manutenção -->
                        <h3>${ativosManutencao}</h3>
                        <p>Ativos em Manutenção</p>
                    </div>
                </div>
            </div>

            <div class="charts-row">
                <!-- Gráfico 1: Equipamentos mais utilizados -->
                <!-- Fonte de dados: Map equipamentosPorTipo (fornecido pelo backend) -->
                <div class="chart-container">
                    <div class="chart-header">
                        <span class="chart-title">Equipamentos Mais Utilizados</span>
                    </div>
                    <div class="chart-canvas-wrapper doughnut-wrapper">
                        <canvas id="doughnutChart"></canvas>
                    </div>
                </div>

                <!-- Gráfico 2: Solicitações por Unidade -->
                <!-- Permite filtragem por período -->
                <div class="chart-container">
                    <div class="chart-header">
                        <span class="chart-title">Solicitações por Unidade</span>
                        <div class="chart-filter">
                            <!-- 
                                Formulário responsável por filtrar os dados por período.
                                Método GET para permitir reprocessamento da página.
                                Envia o parâmetro "periodo" ao DashboardController.
                            -->
                            <form id="formPeriodo" action="DashboardController" method="get" style="margin: 0;">
                                <select name="periodo" onchange="this.form.submit()">
                                    <!-- 
                                        Regra de negócio:
                                        O período selecionado é mantido após reload da página
                                        utilizando comparação com a variável periodoSelecionado.
                                    -->
                                    <option value="6" ${periodoSelecionado == '6' ? 'selected' : ''}>Últimos 6 meses</option>
                                    <option value="12" ${periodoSelecionado == '12' ? 'selected' : ''}>Último ano</option>
                                    <option value="3" ${periodoSelecionado == '3' ? 'selected' : ''}>Últimos 3 meses</option>
                                    <option value="1" ${periodoSelecionado == '1' ? 'selected' : ''}>Último mês</option>
                                </select>
                            </form>
                        </div>
                    </div>
                    <div class="chart-canvas-wrapper bar-wrapper">
                        <canvas id="barChart"></canvas>
                    </div>
                </div>
            </div>

            <!-- Tabela de movimentações recentes -->
            <!-- Fonte de dados: Lista atividadesRecentes -->
            <div class="table-container">
                <div class="table-title">Lista de Atividades Recentes</div>
                
                <div class="table-responsive">
                    <table>
                        <thead>
                            <tr>
                                <th>Equipamento/Número Série</th>
                                <th>Fabricante</th>
                                <th>Tipo</th>
                                <th>Unidade origem → destino</th>
                                <th>Usuário</th>
                                <th>Data da movimentação</th>
                                <th>Observação</th>
                            </tr>
                        </thead>
                        <tbody>
                            <!-- 
                                Estrutura de repetição JSTL:
                                Itera sobre a lista atividadesRecentes.
                                Cada item representa uma movimentação registrada no sistema.
                            -->
                            <c:forEach var="atividade" items="${atividadesRecentes}">
                                <tr>
                                    <!-- Acesso a propriedades encadeadas do objeto -->
                                    <td>${atividade.equipamento.nomeEquip} → ${atividade.equipamento.numSerieEquip}</td>
                                    <td>${atividade.fabricante.nomeFab}</td>
                                    <td>${atividade.tipoMovimentacaoMov}</td>
                                    <td>${atividade.unidadeOrigem.nomeUnid} → ${atividade.unidadeDestino.nomeUnid}</td>
                                    <td>${atividade.usuarioOrigem.nomeUsu} → ${atividade.usuarioDestino.nomeUsu}</td>

                                    <!-- Formatação de data utilizando JSTL fmt -->
                                    <!-- Padrão brasileiro: dd/MM/yyyy -->
                                    <td><fmt:formatDate value="${atividade.dataInsercao}" pattern="dd/MM/yyyy" /></td>

                                    <!-- 
                                        Regra de apresentação:
                                        Caso não exista observação, exibe hífen.
                                    -->
                                    <td>${empty atividade.observacaoMov ? '-' : atividade.observacaoMov}</td>
                                </tr>
                            </c:forEach>
                            
                            <!-- 
                                Estrutura condicional:
                                Caso a lista esteja vazia, exibe mensagem informativa.
                            -->
                            <c:if test="${empty atividadesRecentes}">
                                <tr>
                                    <td colspan="5" style="text-align: center; padding: 20px; color: #9ca3af;">
                                        Nenhuma movimentação registrada
                                    </td>
                                </tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>
            </div>

        </div>
    </main>


	
    
    <!-- INCLUIR SCRIPTS COMUNS -->
    <!-- Scripts globais do sistema (funções utilitárias, sidebar, etc.) -->
    <jsp:include page="/includes/scripts-comum.jsp" />

	
    
    <!-- Scripts específicos do dashboard -->
    <script>
        // --- Configuração Responsiva dos Gráficos ---
        // Define comportamento visual baseado na largura da tela
        const isMobile = window.innerWidth < 768;
        const legendPos = isMobile ? 'bottom' : 'right';
        const barThick = isMobile ? 15 : 30;

        // --- DADOS DO GRÁFICO DE ROSCA (fornecidos pelo backend) ---
        // Map equipamentosPorTipo convertido para arrays JavaScript
        const tiposLabels = [];
        const tiposData = [];
        <c:forEach var="entry" items="${equipamentosPorTipo}">
            // entry.key = tipo do equipamento
            // entry.value = quantidade
            tiposLabels.push('${entry.key}');
            tiposData.push(${entry.value});
        </c:forEach>

        // Instanciação do gráfico de rosca
        const ctxDoughnut = document.getElementById('doughnutChart').getContext('2d');
        new Chart(ctxDoughnut, {
            type: 'doughnut',
            data: {
                // Regra defensiva: caso não haja dados, exibe valor padrão
                labels: tiposLabels.length > 0 ? tiposLabels : ['Sem dados'],
                datasets: [{
                    data: tiposData.length > 0 ? tiposData : [1],
                    backgroundColor: ['#4285F4', '#EA4335', '#FBBC05', '#34A853', '#9C27B0'],
                    borderWidth: 0,
                    hoverOffset: 4
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                cutout: '70%',
                plugins: {
                    legend: {
                        position: legendPos,
                        labels: {
                            usePointStyle: true,
                            padding: 20
                        }
                    }
                },
                layout: {
                    padding: isMobile ? 10 : 0
                }
            }
        });

        // --- DADOS DO GRÁFICO DE BARRAS (fornecidos pelo backend) ---
        const unidadesLabels = [];
        const unidadesData = [];
        <c:forEach var="entry" items="${solicitacoesPorUnidade}">
            unidadesLabels.push('${entry.key}');
            unidadesData.push(${entry.value});
        </c:forEach>

        // Instanciação do gráfico de barras
        const ctxBar = document.getElementById('barChart').getContext('2d');
        new Chart(ctxBar, {
            type: 'bar',
            data: {
                labels: unidadesLabels.length > 0 ? unidadesLabels : ['Sem dados'],
                datasets: [{
                    label: 'Solicitações',
                    data: unidadesData.length > 0 ? unidadesData : [0],
                    backgroundColor: '#4285F4',
                    borderRadius: 4,
                    barThickness: barThick
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { display: false }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        // Grade horizontal para melhor leitura
                        grid: { color: '#f0f0f0' }
                    },
                    x: {
                        grid: { display: false },
                        ticks: {
                            font: {
                                size: isMobile ? 10 : 12
                            }
                        }
                    }
                }
            }
        });
    </script>
    
</body>
</html>
