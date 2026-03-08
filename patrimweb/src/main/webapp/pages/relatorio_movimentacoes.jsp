<%@ page contentType="text/html; charset=UTF-8" %>
<%-- 
    Define o tipo de conteúdo retornado ao cliente como HTML com codificação UTF-8.
    Garante suporte adequado a caracteres especiais utilizados na aplicação.
--%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%-- 
    Importa a biblioteca JSTL Core.
    Utilizada para estruturas de controle como:
    - <c:forEach>  → repetição
    - <c:if>       → condição simples
    - <c:choose>   → condição composta
    - <c:set>      → definição de variáveis
--%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%-- 
    Importa a biblioteca JSTL de formatação.
    Neste arquivo é utilizada para formatação de datas.
--%>

<!-- Define a variável pageTitle para ativar o menu correto -->
<c:set var="pageTitle" value="Movimentações" scope="request" />
<%-- 
    Define variável no escopo da requisição.
    Regra de negócio:
    - Utilizada para destacar o item correto no menu lateral.
    - Também compõe dinamicamente o título da página.
--%>

<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <%-- Define codificação da página --%>

    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <%-- Configuração de responsividade --%>

    <title>Relatório ${pageTitle} - PatrimWeb</title>
    <%-- Título dinâmico com base na variável pageTitle --%>

    <!-- Bibliotecas externas de layout e ícones -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="css/patrimweb.css">

    <!-- Bibliotecas para exportação de dados -->
    <script src="https://cdnjs.cloudflare.com/ajax/libs/jspdf/2.5.1/jspdf.umd.min.js"></script>
    <%-- Biblioteca para geração de PDF no client-side --%>

    <script src="https://cdnjs.cloudflare.com/ajax/libs/jspdf-autotable/3.5.29/jspdf.plugin.autotable.min.js"></script>
    <%-- Plugin para converter tabelas HTML em tabelas no PDF --%>

    <script src="https://cdnjs.cloudflare.com/ajax/libs/xlsx/0.18.5/xlsx.full.min.js"></script>
    <%-- Biblioteca para geração de arquivos Excel (.xlsx) --%>
    
</head>
<body>

    <%-- 
        Injeta dados vindos do Controller como variáveis JavaScript.
        Esses dados foram previamente processados no backend,
        possivelmente através de consultas ao banco de dados.
    --%>
    <script>
        const movimentacoesPorMes = {
            <c:forEach var="entry" items="${movimentacoesPorMes}" varStatus="status">
                ${entry.key}: ${entry.value}<c:if test="${!status.last}">,</c:if>
            </c:forEach>
        };
        <%-- 
            Estrutura de repetição que monta um objeto JS no formato:
            { mes: quantidade }
            Representa total de movimentações por mês.
        --%>

        const totalMovimentacoesAno = ${totalMovimentacoesAno != null ? totalMovimentacoesAno : 0};
        <%-- 
            Total anual utilizado para cálculo proporcional das barras.
            Validação defensiva:
            - Caso venha nulo do backend, assume 0 para evitar erro JS.
        --%>
    </script>

    <div class="mobile-overlay" id="mobileOverlay" onclick="toggleSidebar()"></div>
    <%-- Overlay para controle do menu lateral em dispositivos móveis --%>

 <!-- INCLUIR SIDEBAR -->
    <jsp:include page="/includes/sidebar.jsp" />
    <%-- Inclusão do menu lateral reutilizável --%>

<main class="main-content">
    <header>
            <!-- Botão menu mobile -->
            <button class="menu-toggle" onclick="toggleSidebar()">
                <i class="fa-solid fa-bars"></i>
            </button>
            
            <h1>Relatório ${pageTitle}</h1>
            
            <!-- INCLUIR MENU DE USUÁRIO -->
            <jsp:include page="/includes/user-menu.jsp" />
            <%-- Inclusão do menu do usuário autenticado --%>
        </header>

        <div class="dashboard-container">

            <%-- ========================= -->
            <%-- FILTROS DE CONSULTA      -->
            <%-- ========================= --%>
            <div class="filter-card">
                <form class="filter-grid" action="RelatorioMovimentacaoController" method="get">
                    <%-- 
                        Envia requisição GET para o Controller.
                        Interação com backend:
                        - Controller recebe parâmetros
                        - Executa filtros na base de dados
                        - Retorna lista filtrada
                    --%>

                    <div>
                        <label class="filter-label">Data Início</label>
                        <input type="date" name="dataInicio" class="filter-input" value="${filtroDataInicio}">
                        <%-- Filtro por data inicial da movimentação --%>
                    </div>

                    <div>
                        <label class="filter-label">Data Fim</label>
                        <input type="date" name="dataFim" class="filter-input" value="${filtroDataFim}">
                        <%-- Filtro por data final da movimentação --%>
                    </div>

                    <div>
                        <label class="filter-label">Tipo de Movimentação</label>
                        <select name="tipoMovimentacao" class="filter-input">
                            <%-- 
                                Estrutura condicional para manter opção selecionada.
                                Regra de negócio:
                                - Permite filtrar por tipo específico ou listar todas.
                            --%>
                            <option value="">Todas</option>
                            <option value="Transferência"  <c:if test="${filtroTipo == 'Transferência'}">selected</c:if>>Transferência</option>
                            <option value="Empréstimo"     <c:if test="${filtroTipo == 'Empréstimo'}">selected</c:if>>Empréstimo</option>
                            <option value="Manutenção"     <c:if test="${filtroTipo == 'Manutenção'}">selected</c:if>>Manutenção</option>
                            <option value="Devolução"      <c:if test="${filtroTipo == 'Devolução'}">selected</c:if>>Devolução</option>
                        </select>
                    </div>

                    <div>
                        <label class="filter-label">Equipamento (Nome ou Serial)</label>
                        <input type="text" name="equipamento" class="filter-input"
                               placeholder="Ex: Notebook ou SN-001" value="${filtroEquipamento}">
                        <%-- Permite busca textual por nome do equipamento ou número de série --%>
                    </div>

                    <div>
                        <button class="btn btn-primary" type="submit" style="width: 100%;">
                            <i class="fa-solid fa-filter"></i> Filtrar
                        </button>
                    </div>
                </form>
            </div>

            <%-- ========================= -->
            <%-- GRÁFICO E EXPORTAÇÃO     -->
            <%-- ========================= --%>
            <div class="charts-row">
                <div class="chart-card">
                    <div class="chart-header">
                        <div class="chart-title">Movimentações por Mês (${anoAtual})</div>

                        <%-- 
                            Seletor de ano.
                            Regra importante:
                            - Preserva os filtros ativos através de campos hidden.
                        --%>
                        <form id="formAno" action="RelatorioMovimentacaoController" method="get">
                            <input type="hidden" name="dataInicio"       value="${filtroDataInicio}">
                            <input type="hidden" name="dataFim"          value="${filtroDataFim}">
                            <input type="hidden" name="tipoMovimentacao" value="${filtroTipo}">
                            <input type="hidden" name="equipamento"      value="${filtroEquipamento}">

                            <select name="ano" id="ano" class="filter-input" style="width: 120px;" onchange="this.form.submit()">
                                <%-- Estrutura de repetição para listar anos disponíveis --%>
                                <c:forEach var="anoItem" items="${anos}">
                                    <option value="${anoItem}" <c:if test="${anoItem == anoAtual}">selected</c:if>>
                                        ${anoItem}
                                    </option>
                                </c:forEach>
                            </select>
                        </form>
                    </div>

                    <%-- Estrutura estática das 12 barras (meses do ano) --%>
                    <div class="bar-chart-container">
                        <div class="bar-group" data-mes="1" ><div class="bar" style="height:0px;"></div><span class="bar-label">Jan</span></div>
                        <div class="bar-group" data-mes="2" ><div class="bar" style="height:0px;"></div><span class="bar-label">Fev</span></div>
                        <div class="bar-group" data-mes="3" ><div class="bar" style="height:0px;"></div><span class="bar-label">Mar</span></div>
                        <div class="bar-group" data-mes="4" ><div class="bar" style="height:0px;"></div><span class="bar-label">Abr</span></div>
                        <div class="bar-group" data-mes="5" ><div class="bar" style="height:0px;"></div><span class="bar-label">Mai</span></div>
                        <div class="bar-group" data-mes="6" ><div class="bar" style="height:0px;"></div><span class="bar-label">Jun</span></div>
                        <div class="bar-group" data-mes="7" ><div class="bar" style="height:0px;"></div><span class="bar-label">Jul</span></div>
                        <div class="bar-group" data-mes="8" ><div class="bar" style="height:0px;"></div><span class="bar-label">Ago</span></div>
                        <div class="bar-group" data-mes="9" ><div class="bar" style="height:0px;"></div><span class="bar-label">Set</span></div>
                        <div class="bar-group" data-mes="10"><div class="bar" style="height:0px;"></div><span class="bar-label">Out</span></div>
                        <div class="bar-group" data-mes="11"><div class="bar" style="height:0px;"></div><span class="bar-label">Nov</span></div>
                        <div class="bar-group" data-mes="12"><div class="bar" style="height:0px;"></div><span class="bar-label">Dez</span></div>
                    </div>
                </div>

                <%-- Card de exportação de dados --%>
                <div class="chart-card" style="display: flex; flex-direction: column; justify-content: center;">
                    <div class="chart-title" style="margin-bottom: 20px;">Exportar Dados</div>
                    <div style="display: flex; flex-direction: column; gap: 10px;">
                        <button class="btn btn-outline" onclick="exportData('PDF')">
                            <i class="fa-solid fa-file-pdf" style="color: var(--danger);"></i> Baixar PDF
                        </button>
                        <button class="btn btn-outline" onclick="exportData('Excel')">
                            <i class="fa-solid fa-file-excel" style="color: var(--success);"></i> Baixar Excel
                        </button>
                        <button class="btn btn-outline" onclick="window.print()">
                            <i class="fa-solid fa-print"></i> Imprimir Tela
                        </button>
                    </div>
                </div>
            </div>

            <%-- ========================= -->
            <%-- TABELA DE RESULTADOS     -->
            <%-- ========================= --%>
            <div class="report-table-container">
                <div class="table-header-row">
                    <div class="table-title">Detalhamento</div>
                    <div style="font-size: 13px; color: var(--text-muted); margin-bottom: 15px;">
                        Listagem Geral de Movimentações
                    </div>
                </div>

                <div class="table-responsive">
                    <table id="tabelaRelatorio">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Equipamento (N/S)</th>
                                <th>Tipo</th>
                                <th>Origem (Unid. / Usuário)</th>
                                <th>Destino (Unid. / Usuário)</th>
                                <th>Data</th>
                            </tr>
                        </thead>
                        <tbody>
                            <%-- Tratamento para lista vazia --%>
                            <c:choose>
                                <c:when test="${empty movimentacoes}">
                                    <tr>
                                        <td colspan="6" style="text-align:center;">
                                            Nenhuma movimentação encontrada
                                        </td>
                                    </tr>
                                </c:when>
                                <c:otherwise>
                                    <%-- 
                                        Estrutura de repetição:
                                        Itera sobre lista de movimentações retornada pelo backend.
                                        Dados provavelmente obtidos via consulta ao banco.
                                    --%>
                                    <c:forEach var="m" items="${movimentacoes}">
                                        <tr>
                                            <td>${m.idMov}</td>

                                            <td>
                                                ${m.equipamento.nomeEquip}<br>
                                                <span style="font-size: 11px; color: #9ca3af;">SN: ${m.equipamento.numSerieEquip}</span>
                                            </td>

                                            <td>
                                                <%-- 
                                                    Estrutura condicional para aplicar badge conforme tipo.
                                                    Regra de negócio:
                                                    - Cada tipo possui identidade visual específica.
                                                --%>
                                                <c:choose>
                                                    <c:when test="${m.tipoMovimentacaoMov == 'Transferência'}">
                                                        <span class="badge badge-transferencia">Transferência</span>
                                                    </c:when>
                                                    <c:when test="${m.tipoMovimentacaoMov == 'Manutenção'}">
                                                        <span class="badge badge-manutencao">Manutenção</span>
                                                    </c:when>
                                                    <c:when test="${m.tipoMovimentacaoMov == 'Empréstimo'}">
                                                        <span class="badge badge-emprestimo">Empréstimo</span>
                                                    </c:when>
                                                    <c:when test="${m.tipoMovimentacaoMov == 'Devolução'}">
                                                        <span class="badge badge-devolucao">Devolução</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="badge badge-outro">${m.tipoMovimentacaoMov}</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>

                                            <td>
                                                ${m.unidadeOrigem.nomeUnid}<br>
                                                <span style="font-size: 11px;">${m.usuarioOrigem.nomeUsu}</span>
                                            </td>

                                            <td>
                                                ${m.unidadeDestino.nomeUnid}<br>
                                                <span style="font-size: 11px;">${m.usuarioDestino.nomeUsu}</span>
                                            </td>

                                            <td>
                                                <fmt:formatDate value="${m.dataInsercao}" pattern="dd/MM/yyyy HH:mm"/>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
            </div>

        </div>
    </main>
    
    <!-- INCLUIR SCRIPTS COMUNS -->
    <jsp:include page="/includes/scripts-comum.jsp" />

    <script>
        window.addEventListener('click', function(e) {
            const container = document.querySelector('.user-menu-container');
            const dropdown  = document.getElementById('userDropdown');

            // Fecha dropdown ao clicar fora do menu
            if (!container.contains(e.target)) {
                dropdown.classList.remove('show');
            }
        });

        // =====================================
        // FUNÇÃO DE EXPORTAÇÃO
        // Parâmetro:
        //   type (String) → 'PDF' ou 'Excel'
        // Retorno:
        //   Não possui retorno. Gera arquivo para download.
        // =====================================
        function exportData(type) {
            if (type === 'PDF') {
                const { jsPDF } = window.jspdf;

                // Orientação paisagem para comportar todas as colunas
                const doc = new jsPDF('l', 'mm', 'a4');

                // =====================================
                // INSERÇÃO DO LOGO
                // Carrega a imagem do logo a partir do caminho relativo do projeto.
                // O logo é convertido em base64 via canvas para ser embutido no PDF.
                // =====================================
                const logoImg = new Image();
                logoImg.src = 'assets/images/logo.png';

                const gerarPDF = function() {
                    try {
                        // Desenha a imagem em um canvas temporário para obter o base64
                        const canvas = document.createElement('canvas');
                        canvas.width  = logoImg.naturalWidth  || logoImg.width;
                        canvas.height = logoImg.naturalHeight || logoImg.height;
                        const ctx = canvas.getContext('2d');
                        ctx.drawImage(logoImg, 0, 0);
                        const logoBase64 = canvas.toDataURL('image/png');

                        // Calcula proporção para encaixar o logo em no máximo 40mm de largura
                        const maxLogoWidth  = 40;
                        const maxLogoHeight = 15;
                        const ratio  = Math.min(maxLogoWidth / canvas.width, maxLogoHeight / canvas.height);
                        const logoW  = canvas.width  * ratio;
                        const logoH  = canvas.height * ratio;

                        // Posiciona o logo no canto superior esquerdo
                        doc.addImage(logoBase64, 'PNG', 14, 8, logoW, logoH);
                    } catch (e) {
                        // Caso a imagem não possa ser lida (ex: CORS), ignora e continua
                        console.warn('Não foi possível adicionar o logo ao PDF:', e);
                    }

                    // Título e data deslocados para não sobrepor o logo
                    doc.setFontSize(18);
                    doc.text("Relatório de Movimentações - PatrimWeb", 30, 15);

                    doc.setFontSize(10);
                    doc.text("Gerado em: " + new Date().toLocaleDateString(), 30, 22);

                    doc.autoTable({
                        html: '#tabelaRelatorio',
                        startY: 30,
                        theme: 'grid',
                        headStyles: { fillColor: [59, 130, 246] },
                        styles: { fontSize: 8, cellPadding: 2 }
                    });

                    doc.save('relatorio_movimentacoes.pdf');
                };

                // Se a imagem já estiver carregada, gera o PDF imediatamente;
                // caso contrário, aguarda o evento de carregamento.
                if (logoImg.complete && logoImg.naturalWidth > 0) {
                    gerarPDF();
                } else {
                    logoImg.onload  = gerarPDF;
                    logoImg.onerror = gerarPDF; // Continua mesmo se o logo não carregar
                }
                return; // Encerra aqui; gerarPDF() fará o save assincronamente
            }
            else if (type === 'Excel') {
                const tabela = document.getElementById('tabelaRelatorio');
                const wb = XLSX.utils.table_to_book(tabela, { sheet: "Movimentacoes" });
                XLSX.writeFile(wb, 'relatorio_movimentacoes.xlsx');
            }
        }
    </script>
    <script>
        // Altura máxima das barras do gráfico
        const ALTURA_MAXIMA = 140;

        // =====================================
        // Atualiza o gráfico de barras
        // Retorno:
        //   Não retorna valor.
        //   Atualiza visualmente as barras com base nos dados.
        // =====================================
        

        atualizarGrafico();
        
        
        function atualizarGrafico() {
            document.querySelectorAll('.bar-group').forEach(function(barGroup) {
                const mes = parseInt(barGroup.dataset.mes);

                // Validação defensiva para evitar erro caso variável não exista
                const qtdMes = (typeof movimentacoesPorMes !== 'undefined') ? (movimentacoesPorMes[mes] || 0) : 0;

                // Define atributo usado pelo CSS para exibição do valor
                barGroup.setAttribute('data-valor', qtdMes);

                let altura = 0;

                if (typeof totalMovimentacoesAno !== 'undefined' && totalMovimentacoesAno > 0) {
                    altura = (qtdMes * ALTURA_MAXIMA) / totalMovimentacoesAno;
                }

                if (qtdMes > 0 && altura < 5) {
                    altura = 5;
                }

                const bar = barGroup.querySelector('.bar');
                if (bar) {
                    bar.style.height = altura + 'px';
                }
            });
        }
    </script>
    
</body>
</html>
