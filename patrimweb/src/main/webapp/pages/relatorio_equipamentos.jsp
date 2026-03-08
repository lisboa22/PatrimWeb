<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%--
    ============================================================================
    PÁGINA JSP: RELATÓRIO DE EQUIPAMENTOS - SISTEMA PATRIMWEB
    ============================================================================

    PROPÓSITO:
    Esta página JSP é responsável pela apresentação do relatório gerencial
    de equipamentos cadastrados no sistema PatrimWeb.

    RESPONSABILIDADES:
    - Exibir filtros de consulta para geração do relatório.
    - Apresentar gráfico de aquisições por mês.
    - Listar detalhadamente os equipamentos retornados pelo controller.
    - Permitir exportação dos dados em PDF e Excel.
    - Permitir impressão direta da tela.
    - Integrar dados vindos do backend com visualizações em JavaScript.

    INTERAÇÃO COM BACK-END:
    - Dados são fornecidos pelo RelatorioEquipamentoController.
    - Utiliza JSTL e Expression Language (EL) para renderização dinâmica.
    - Recebe coleções e variáveis:
        • equipamentos
        • fabricantes
        • equipamentosPorMes
        • totalEquipamentosAno
        • anos
        • anoAtual
        • filtros aplicados (dataInicio, dataFim, nomeEquip, fabricanteId)

    REGRAS DE NEGÓCIO IMPORTANTES:
    - O menu ativo é definido via variável pageTitle.
    - Filtros são preservados entre requisições GET.
    - O gráfico é proporcional ao total anual de equipamentos.
    - Caso não existam registros, é exibida mensagem informativa.

    INTERAÇÃO COM BANCO:
    - Não ocorre acesso direto nesta página.
    - Dados já chegam processados pelo Controller/DAO.

    PONTOS CRÍTICOS:
    - Variáveis EL devem existir no request.
    - IDs HTML são utilizados por scripts JS para manipulação dinâmica.
    - Estrutura da tabela é reutilizada para exportação PDF/Excel.
--%>

<!-- Define a variável pageTitle para ativar o menu correto -->
<c:set var="pageTitle" value="Equipamentos" scope="request" />

<!DOCTYPE html>
<html lang="pt-br">
<head>
    <%-- Metadados básicos da página --%>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Relatório ${pageTitle} - PatrimWeb</title>

    <%-- Bibliotecas visuais e fontes --%>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="css/patrimweb.css">
    
    <%--
        Bibliotecas externas utilizadas para exportação de dados:
        - jsPDF: geração de PDF
        - autoTable: criação automática de tabelas em PDF
        - XLSX: exportação para Excel
    --%>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/jspdf/2.5.1/jspdf.umd.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/jspdf-autotable/3.5.29/jspdf.plugin.autotable.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/xlsx/0.18.5/xlsx.full.min.js"></script>
</head>
<body>

    <script>
        /*
            ===============================================================
            INTEGRAÇÃO DE DADOS BACK-END → FRONT-END
            ===============================================================

            Os dados são convertidos via JSTL para objetos JavaScript,
            permitindo uso direto na construção do gráfico.
        */

        // Dados agregados de equipamentos por mês enviados pelo servidor
        const equipamentosPorMes = {
            <c:forEach var="entry" items="${equipamentosPorMes}" varStatus="status">
                ${entry.key}: ${entry.value}<c:if test="${!status.last}">,</c:if>
            </c:forEach>
        };

        // Total anual utilizado como base proporcional do gráfico
        const totalEquipamentosAno = ${totalEquipamentosAno != null ? totalEquipamentosAno : 0};
    </script>

    <%-- Overlay utilizado no modo mobile para fechamento da sidebar --%>
    <div class="mobile-overlay" id="mobileOverlay" onclick="toggleSidebar()"></div>

 <!-- INCLUIR SIDEBAR -->
    <%-- Inclusão do componente reutilizável de navegação lateral --%>
    <jsp:include page="/includes/sidebar.jsp" />

<main class="main-content">
    <header>
            <%-- Botão responsável por abrir/fechar sidebar em dispositivos móveis --%>
            <button class="menu-toggle" onclick="toggleSidebar()">
                <i class="fa-solid fa-bars"></i>
            </button>
            
            <%-- Título dinâmico baseado na variável pageTitle --%>
            <h1>Relatório ${pageTitle}</h1>
            
            <%-- Inclusão do menu de usuário autenticado --%>
            <jsp:include page="/includes/user-menu.jsp" />
        </header>

        <div class="dashboard-container">
            
            <%--
                ==========================================================
                FORMULÁRIO DE FILTROS DO RELATÓRIO
                ==========================================================
                Envia requisição GET ao controller preservando estado
                dos filtros para nova consulta.
            --%>
            <div class="filter-card">
                <form class="filter-grid" action="RelatorioEquipamentoController" method="get">
                    <div>
                        <label class="filter-label">Data Início</label>
                        <%-- Filtro por data inicial de inserção --%>
                        <input type="date" name="dataInicio" class="filter-input" value="${filtroDataInicio}">
                    </div>

                    <div>
                        <label class="filter-label">Data Fim</label>
                        <%-- Filtro por data final de inserção --%>
                        <input type="date" name="dataFim" class="filter-input" value="${filtroDataFim}">
                    </div>

                    <div>
                        <label class="filter-label">Nome Equipamento</label>
                        <%-- Busca textual pelo nome do equipamento --%>
                        <input type="text" name="nomeEquip" class="filter-input" placeholder="Nome do Equipamento" value="${filtroNomeEquip}">
                    </div>

                    <div>
                        <label class="filter-label">Fabricante</label>
                        <%--
                            Lista dinâmica de fabricantes.
                            Estrutura de repetição JSTL responsável por
                            popular o select com dados vindos do backend.
                        --%>
                        <select name="fabricanteId" class="filter-input">
                            <option value="">Todos</option>
                            <c:forEach var="fab" items="${fabricantes}">
                                <option value="${fab.idFab}" 
                                    <c:if test="${filtroFabricanteId == fab.idFab}">selected</c:if>>
                                    ${fab.nomeFab}
                                </option>
                            </c:forEach>
                        </select>
                    </div>

                    <div>
                        <%-- Disparo da filtragem do relatório --%>
                        <button class="btn btn-primary" type="submit" style="width: 100%;">
                            <i class="fa-solid fa-filter"></i> Filtrar
                        </button>
                    </div>
                </form>
            </div>

            <%-- ================= GRÁFICOS E EXPORTAÇÃO ================= --%>
            <div class="charts-row">
                <div class="chart-card">
                    <div class="chart-header">
                        <div class="chart-title">Aquisições por Mês (${anoAtual})</div>

                        <%--
                            Seleção de ano do relatório.
                            Ao alterar o valor, o formulário é submetido automaticamente.
                            Os filtros existentes são preservados via campos hidden.
                        --%>
                        <form id="formAno" action="RelatorioEquipamentoController" method="get">
                            <input type="hidden" name="dataInicio" value="${filtroDataInicio}">
                            <input type="hidden" name="dataFim" value="${filtroDataFim}">
                            <input type="hidden" name="nomeEquip" value="${filtroNomeEquip}">
                            <input type="hidden" name="fabricanteId" value="${filtroFabricanteId}">
                            
                            <select name="ano" id="ano" class="filter-input" style="width: 120px;" onchange="this.form.submit()">
                                <c:forEach var="anoItem" items="${anos}">
                                    <option value="${anoItem}" <c:if test="${anoItem == anoAtual}">selected</c:if>>
                                        ${anoItem}
                                    </option>
                                </c:forEach>
                            </select>
                        </form>
                    </div>

                    <%--
                        Estrutura visual do gráfico de barras.
                        Cada grupo representa um mês e será atualizado
                        dinamicamente via JavaScript.
                    --%>
                    <div class="bar-chart-container">
                        <div class="bar-group" data-mes="1"><div class="bar" style="height: 0px;"></div><span class="bar-label">Jan</span></div>
                        <div class="bar-group" data-mes="2"><div class="bar" style="height: 0px;"></div><span class="bar-label">Fev</span></div>
                        <div class="bar-group" data-mes="3"><div class="bar" style="height: 0px;"></div><span class="bar-label">Mar</span></div>
                        <div class="bar-group" data-mes="4"><div class="bar" style="height: 0px;"></div><span class="bar-label">Abr</span></div>
                        <div class="bar-group" data-mes="5"><div class="bar" style="height: 0px;"></div><span class="bar-label">Mai</span></div>
                        <div class="bar-group" data-mes="6"><div class="bar" style="height: 0px;"></div><span class="bar-label">Jun</span></div>
                        <div class="bar-group" data-mes="7"><div class="bar" style="height: 0px;"></div><span class="bar-label">Jul</span></div>
                        <div class="bar-group" data-mes="8"><div class="bar" style="height: 0px;"></div><span class="bar-label">Ago</span></div>
                        <div class="bar-group" data-mes="9"><div class="bar" style="height: 0px;"></div><span class="bar-label">Set</span></div>
                        <div class="bar-group" data-mes="10"><div class="bar" style="height: 0px;"></div><span class="bar-label">Out</span></div>
                        <div class="bar-group" data-mes="11"><div class="bar" style="height: 0px;"></div><span class="bar-label">Nov</span></div>
                        <div class="bar-group" data-mes="12"><div class="bar" style="height: 0px;"></div><span class="bar-label">Dez</span></div>
                    </div>
                </div>
                
                <%-- Área de exportação e impressão --%>
                <div class="chart-card" style="display: flex; flex-direction: column; justify-content: center;">
                    <div class="chart-title" style="margin-bottom: 20px;">Exportar Dados</div>
                    <div style="display: flex; flex-direction: column; gap: 10px;">
                        <button class="btn btn-outline" onclick="exportData('PDF')">
                            <i class="fa-solid fa-file-pdf" style="color: var(--danger);"></i> Baixar PDF
                        </button>
                        <button class="btn btn-outline" onclick="exportData('Excel')">
                            <i class="fa-solid fa-file-excel" style="color: var(--success);"></i> Baixar Excel (CSV)
                        </button>
                        <%-- Impressão direta da página via navegador --%>
                        <button class="btn btn-outline" onclick="window.print()">
                            <i class="fa-solid fa-print"></i> Imprimir Tela
                        </button>
                    </div>
                </div>
            </div>

            <%-- ================= TABELA DE RESULTADOS ================= --%>
            <div class="report-table-container">
                <div class="table-header-row">
                    <div class="table-title">Detalhamento</div>
                    <div style="font-size: 13px; color: var(--text-muted); margin-bottom: 15px;">Listagem Geral de Equipamentos</div>
                </div>

                <div class="table-responsive">
                    <table id="tabelaRelatorio">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Nome do Equipamento</th>
                                <th>Fabricante</th>
                                <th>Data Inserção</th>
                            </tr>
                        </thead>
                        <tbody>

                            <%--
                                Estrutura condicional:
                                - Se não houver equipamentos, exibe mensagem.
                                - Caso contrário, lista registros.
                            --%>
                            <c:choose>
                                <c:when test="${empty equipamentos}">
                                    <tr>
                                        <td colspan="4" style="text-align:center;">
                                            Nenhum equipamento encontrado
                                        </td>
                                    </tr>
                                </c:when>
                                <c:otherwise>
                                    <%-- Iteração sobre lista de equipamentos retornada pelo controller --%>
                                    <c:forEach var="equip" items="${equipamentos}">
                                        <tr>
                                            <td>${equip.idEquip}</td>
                                            <td>${equip.nomeEquip}</td>
                                            <td>${equip.fabricante.nomeFab}</td>
                                            <td>
                                                <%--
                                                    Formatação de data utilizando JSTL fmt.
                                                    Converte objeto Date para padrão brasileiro.
                                                --%>
                                                <fmt:formatDate value="${equip.dataInsercao}" pattern="dd/MM/yyyy"/>
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
    <%-- Scripts globais compartilhados (menu, sidebar, etc.) --%>
    <jsp:include page="/includes/scripts-comum.jsp" />
    
    <script>
        /*
            Controle de fechamento do menu de usuário ao clicar fora.
            Estrutura condicional verifica se o clique ocorreu fora
            do container do menu.
        */
        window.addEventListener('click', function(e) {
            const container = document.querySelector('.user-menu-container');
            const dropdown = document.getElementById('userDropdown');
            if (!container.contains(e.target)) {
                dropdown.classList.remove('show');
            }
        });

        /*
            ==========================================================
            FUNÇÃO: exportData(type)
            ==========================================================
            PROPÓSITO:
            Exportar os dados exibidos na tabela para PDF ou Excel.

            PARÂMETRO:
            type -> String indicando o formato ("PDF" ou "Excel")

            REGRAS:
            - PDF utiliza jsPDF + autoTable.
            - Excel utiliza biblioteca XLSX convertendo tabela HTML.
        */
        function exportData(type) {
            if (type === 'PDF') {
                const { jsPDF } = window.jspdf;
                const doc = new jsPDF();

                const logoImg = new Image();
                logoImg.src = 'assets/images/logo.png';

                const gerarPDF = function() {
                    try {
                        const canvas = document.createElement('canvas');
                        canvas.width  = logoImg.naturalWidth  || logoImg.width;
                        canvas.height = logoImg.naturalHeight || logoImg.height;
                        const ctx = canvas.getContext('2d');
                        ctx.drawImage(logoImg, 0, 0);
                        const logoBase64 = canvas.toDataURL('image/png');

                        const maxLogoWidth  = 40;
                        const maxLogoHeight = 15;
                        const ratio  = Math.min(maxLogoWidth / canvas.width, maxLogoHeight / canvas.height);
                        const logoW  = canvas.width  * ratio;
                        const logoH  = canvas.height * ratio;

                        doc.addImage(logoBase64, 'PNG', 14, 8, logoW, logoH);
                    } catch (e) {
                        console.warn('Não foi possível adicionar o logo ao PDF:', e);
                    }

                    doc.setFontSize(18);
                    doc.text("Relatório de Equipamentos - PatrimWeb", 30, 15);

                    doc.setFontSize(10);
                    doc.text("Gerado em: " + new Date().toLocaleDateString(), 30, 22);

                    doc.autoTable({ 
                        html: '#tabelaRelatorio',
                        startY: 30,
                        theme: 'grid',
                        headStyles: { fillColor: [59, 130, 246] },
                        styles: { fontSize: 8 }
                    });

                    doc.save('relatorio_equipamentos.pdf');
                };

                if (logoImg.complete && logoImg.naturalWidth > 0) {
                    gerarPDF();
                } else {
                    logoImg.onload  = gerarPDF;
                    logoImg.onerror = gerarPDF;
                }
                return;
            }
            else if (type === 'Excel') {
                const tabela = document.getElementById('tabelaRelatorio');
                const wb = XLSX.utils.table_to_book(tabela, {sheet: "Equipamentos"});
                XLSX.writeFile(wb, 'relatorio_equipamentos.xlsx');
            }
        }
    </script>
    
    <script>
        /*
            ALTURA_MAXIMA:
            Define o limite visual máximo das barras do gráfico.
        */
        const ALTURA_MAXIMA = 140;

        /*
            ==========================================================
            FUNÇÃO: atualizarGrafico()
            ==========================================================
            PROPÓSITO:
            Ajustar dinamicamente a altura das barras do gráfico
            proporcionalmente ao total anual de equipamentos.

            LÓGICA:
            - Percorre todos os meses (bar-group).
            - Obtém quantidade de equipamentos por mês.
            - Calcula altura proporcional.
            - Garante altura mínima para visualização.
        */
        function atualizarGrafico() {
            document.querySelectorAll('.bar-group').forEach(barGroup => {
                const mes = parseInt(barGroup.dataset.mes);
                const equipamentosMes = equipamentosPorMes[mes] || 0;

                let altura = 0;

                if (totalEquipamentosAno > 0) {
                    altura = (equipamentosMes * ALTURA_MAXIMA) / totalEquipamentosAno;
                }

                if (equipamentosMes > 0 && altura < 5) {
                    altura = 5;
                }

                barGroup.querySelector('.bar').style.height = altura + 'px';
            });
        }

        /*
            Execução automática após carregamento da página
            para renderização inicial do gráfico.
        */
        atualizarGrafico();
        
        
        /*
            Segunda implementação da função atualizarGrafico.
            Mantida conforme código original.
            Adiciona atributo data-valor contendo o total mensal,
            possibilitando uso futuro em tooltips ou interações visuais.
        */
        function atualizarGrafico() {
            document.querySelectorAll('.bar-group').forEach(barGroup => {
                const mes = parseInt(barGroup.dataset.mes);
                const equipamentosMes = equipamentosPorMes[mes] || 0;

                barGroup.setAttribute('data-valor', equipamentosMes); 

                let altura = 0;
                if (totalEquipamentosAno > 0) {
                    altura = (equipamentosMes * ALTURA_MAXIMA) / totalEquipamentosAno;
                }

                if (equipamentosMes > 0 && altura < 5) {
                    altura = 5;
                }

                barGroup.querySelector('.bar').style.height = altura + 'px';
            });
        }
    </script>

</body>
</html>
