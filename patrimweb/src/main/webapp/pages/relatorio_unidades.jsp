<%@ page contentType="text/html; charset=UTF-8" %>
<%-- 
    Define o tipo de conteúdo da resposta como HTML com codificação UTF-8.
    Garante correta exibição de caracteres especiais.
--%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%-- 
    Importa a biblioteca JSTL Core.
    Utilizada para:
    - Estruturas de repetição (<c:forEach>)
    - Estruturas condicionais (<c:if>, <c:choose>)
    - Definição de variáveis (<c:set>)
--%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%-- 
    Importa a biblioteca JSTL de formatação.
    Neste JSP é utilizada para formatação de datas.
--%>

<!-- Define a variável pageTitle para ativar o menu correto -->
<c:set var="pageTitle" value="Unidades" scope="request" />
<%-- 
    Define variável no escopo da requisição.
    Regra de negócio:
    - Utilizada para ativar o item correto no menu lateral.
    - Também compõe dinamicamente o título da página.
--%>

<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <%-- Define codificação da página --%>

    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <%-- Configuração para responsividade em dispositivos móveis --%>

    <title>Relatório ${pageTitle} - PatrimWeb</title>
    <%-- Título dinâmico baseado na variável pageTitle --%>

    <!-- Bibliotecas visuais -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="css/patrimweb.css">

    <!-- Bibliotecas para exportação -->
    <script src="https://cdnjs.cloudflare.com/ajax/libs/jspdf/2.5.1/jspdf.umd.min.js"></script>
    <%-- Biblioteca para geração de PDF no lado cliente --%>

    <script src="https://cdnjs.cloudflare.com/ajax/libs/jspdf-autotable/3.5.29/jspdf.plugin.autotable.min.js"></script>
    <%-- Plugin para converter tabelas HTML em PDF automaticamente --%>

    <script src="https://cdnjs.cloudflare.com/ajax/libs/xlsx/0.18.5/xlsx.full.min.js"></script>
    <%-- Biblioteca para geração de planilhas Excel (.xlsx) --%>
</head>
<body>

    <%-- 
        Injeta dados do backend como variáveis JavaScript.
        Esses dados foram previamente calculados no Controller
        através de consultas ao banco de dados.
    --%>
    <script>
    const unidadesPorMes = {
        <c:forEach var="entry" items="${unidadesPorMes}" varStatus="status">
            "${entry.key}": ${entry.value}<c:if test="${!status.last}">,</c:if>
        </c:forEach>
    };
    <%-- 
        Estrutura de repetição que monta um objeto JavaScript no formato:
        { mes: quantidade }
        Representa a quantidade de unidades cadastradas por mês.
    --%>

    const totalUnidadesAno = ${totalUnidadesAno != null ? totalUnidadesAno : 0};
    <%-- 
        Total anual utilizado no cálculo proporcional do gráfico.
        Validação defensiva:
        - Caso o valor venha nulo do backend, assume 0 para evitar erro JS.
    --%>
</script>

   <div class="mobile-overlay" id="mobileOverlay" onclick="toggleSidebar()"></div>
   <%-- Overlay utilizado para controle do menu lateral em dispositivos móveis --%>

 <!-- INCLUIR SIDEBAR -->
    <jsp:include page="/includes/sidebar.jsp" />
    <%-- Inclusão do menu lateral reutilizável da aplicação --%>

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

            <%-- ============================= -->
            <%-- FILTROS DE CONSULTA          -->
            <%-- ============================= --%>
            <div class="filter-card">
                <form class="filter-grid" action="RelatorioUnidadeController" method="get">
                    <%-- 
                        Envia requisição GET para o Controller responsável.
                        Interação com backend:
                        - Controller recebe parâmetros
                        - Executa filtros na base de dados
                        - Retorna lista filtrada
                    --%>

                    <div>
                        <label class="filter-label">Data Cadastro (Início)</label>
                        <input type="date" name="dataInicio" class="filter-input" value="${filtroDataInicio}">
                        <%-- Filtro por data inicial de cadastro da unidade --%>
                    </div>

                    <div>
                        <label class="filter-label">Data Cadastro (Fim)</label>
                        <input type="date" name="dataFim" class="filter-input" value="${filtroDataFim}">
                        <%-- Filtro por data final de cadastro da unidade --%>
                    </div>

                    <div>
                        <label class="filter-label">Nome</label>
                        <input type="text" name="nome" class="filter-input" placeholder="Nome da unidade" value="${filtroNome}">
                        <%-- Filtro textual por nome da unidade --%>
                    </div>

                    <div>
                        <label class="filter-label">Endereço</label>
                        <input type="text" name="endereco" class="filter-input" placeholder="Endereço" value="${filtroEndereco}">
                        <%-- Filtro textual por endereço da unidade --%>
                    </div>

                    <div>
                        <button class="btn btn-primary" type="submit" style="width: 100%;">
                            <i class="fa-solid fa-filter"></i> Filtrar
                        </button>
                    </div>
                </form>
            </div>

            <%-- ============================= -->
            <%-- GRÁFICO E EXPORTAÇÃO         -->
            <%-- ============================= --%>
            <div class="charts-row">
                <div class="chart-card">
                    <div class="chart-header">
                        <div class="chart-title">Cadastros por Mês (${anoAtual})</div>

                        <%-- 
                            Seletor de ano.
                            Regra de negócio:
                            - Preserva filtros ativos através de campos hidden.
                        --%>
                        <form id="formAno" action="RelatorioUnidadeController" method="get">
                            <input type="hidden" name="dataInicio" value="${filtroDataInicio}">
                            <input type="hidden" name="dataFim"    value="${filtroDataFim}">
                            <input type="hidden" name="nome"       value="${filtroNome}">
                            <input type="hidden" name="endereco"   value="${filtroEndereco}">

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

                    <%-- Estrutura fixa das 12 barras (meses do ano) --%>
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

                <%-- Card de exportação --%>
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

            <%-- ============================= -->
            <%-- TABELA DE RESULTADOS         -->
            <%-- ============================= --%>
            <div class="report-table-container">
                <div class="table-header-row">
                    <div class="table-title">Detalhamento</div>
                    <div style="font-size: 13px; color: var(--text-muted); margin-bottom: 15px;">
                        Listagem Geral de Unidades
                    </div>
                </div>

                <div class="table-responsive">
                    <table id="tabelaRelatorio">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Nome da Unidade</th>
                                <th>Telefone</th>
                                <th>Email</th>
                                <th>Endereço</th>
                                <th>Data Cadastro</th>
                            </tr>
                        </thead>
                        <tbody>
                            <%-- Estrutura condicional para tratar lista vazia --%>
                            <c:choose>
                                <c:when test="${empty unidades}">
                                    <tr>
                                        <td colspan="6" style="text-align:center;">
                                            Nenhuma unidade encontrada
                                        </td>
                                    </tr>
                                </c:when>
                                <c:otherwise>
                                    <%-- 
                                        Estrutura de repetição:
                                        Itera sobre lista "unidades" retornada pelo Controller.
                                        Dados provavelmente provenientes de consulta ao banco.
                                    --%>
                                    <c:forEach var="u" items="${unidades}">
                                        <tr>
                                            <td>${u.idUnid}</td>
                                            <td>${u.nomeUnid}</td>
                                            <td>${u.telefoneUnid}</td>
                                            <td>${u.emailUnid}</td>
                                            <td>${u.enderecoUnid}</td>
                                            <td>
                                                <fmt:formatDate value="${u.dataInsercao}" pattern="dd/MM/yyyy"/>
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

        /*
         * Função: exportData
         * Parâmetro:
         *   type (String) → 'PDF' ou 'Excel'
         * Retorno:
         *   Não retorna valor. Gera arquivo para download.
         * Regra de negócio:
         *   Exporta os dados atualmente exibidos na tabela.
         */
        function exportData(type) {
            if (type === 'PDF') {
                const { jsPDF } = window.jspdf;

                // Orientação paisagem para comportar endereços longos
                const doc = new jsPDF('l', 'mm', 'a4');

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
                    doc.text("Relatório de Unidades - PatrimWeb", 30, 15);

                    doc.setFontSize(10);
                    doc.text("Gerado em: " + new Date().toLocaleDateString(), 30, 22);

                    doc.autoTable({
                        html: '#tabelaRelatorio',
                        startY: 30,
                        theme: 'grid',
                        headStyles: { fillColor: [59, 130, 246] },
                        styles: { fontSize: 9 }
                    });

                    doc.save('relatorio_unidades.pdf');
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
                const wb = XLSX.utils.table_to_book(tabela, { sheet: "Unidades" });
                XLSX.writeFile(wb, 'relatorio_unidades.xlsx');
            }
        }
    </script>

    <script>
        // Altura máxima das barras do gráfico
        const ALTURA_MAXIMA = 140;

        /*
         * Função: atualizarGrafico
         * Retorno:
         *   Não retorna valor.
         * Responsabilidade:
         *   Calcula e aplica dinamicamente a altura das barras
         *   com base na quantidade mensal proporcional ao total anual.
         */
        function atualizarGrafico() {
            document.querySelectorAll('.bar-group').forEach(function(barGroup) {

                const mes          = parseInt(barGroup.dataset.mes);
                const qtdMes       = unidadesPorMes[mes] || 0;

                let altura = 0;

                // Cálculo proporcional da altura
                if (totalUnidadesAno > 0) {
                    altura = (qtdMes * ALTURA_MAXIMA) / totalUnidadesAno;
                }

                // Garante altura mínima visível quando houver registro
                if (qtdMes > 0 && altura < 5) {
                    altura = 5;
                }

                barGroup.querySelector('.bar').style.height = altura + 'px';
            });
        }

        atualizarGrafico();
        
        
        function atualizarGrafico() {
            document.querySelectorAll('.bar-group').forEach(function(barGroup) {
                const mes = parseInt(barGroup.dataset.mes);
                
                // Validação defensiva para evitar erro caso variável não exista
                const qtdMes = (typeof unidadesPorMes !== 'undefined') ? (unidadesPorMes[mes] || 0) : 0;

                // Define atributo utilizado pelo CSS para exibição de valor
                barGroup.setAttribute('data-valor', qtdMes);

                let altura = 0;

                if (typeof totalUnidadesAno !== 'undefined' && totalUnidadesAno > 0) {
                    altura = (qtdMes * ALTURA_MAXIMA) / totalUnidadesAno;
                }

                // Altura mínima visual para barras com valor cadastrado
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
