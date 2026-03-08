<%@ page contentType="text/html; charset=UTF-8" %>
<%-- 
    Diretiva JSP responsável por definir o tipo de conteúdo retornado ao cliente.
    Neste caso, HTML com codificação UTF-8 para suportar corretamente caracteres especiais.
--%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%-- 
    Importa a biblioteca JSTL Core.
    Utilizada para estruturas de controle como:
    - <c:forEach> (estrutura de repetição)
    - <c:if> (estrutura condicional)
    - <c:choose>, <c:when>, <c:otherwise> (estrutura condicional composta)
    - <c:set> (definição de variáveis)
--%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%-- 
    Importa a biblioteca JSTL de formatação.
    Utilizada neste arquivo para formatação de datas via <fmt:formatDate>.
--%>

<!-- Define a variável pageTitle para ativar o menu correto -->
<c:set var="pageTitle" value="Fabricantes" scope="request" />
<%-- 
    Define a variável "pageTitle" no escopo de requisição.
    Regra de negócio implícita:
    - Esta variável é utilizada para destacar o item correto no menu lateral.
    - Também compõe dinamicamente o título da página.
--%>

<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <%-- Define codificação da página para UTF-8 --%>

    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <%-- Responsividade para dispositivos móveis --%>

    <title>Relatório ${pageTitle} - PatrimWeb</title>
    <%-- 
        Título dinâmico da página.
        Utiliza a variável "pageTitle" definida anteriormente.
    --%>

    <!-- Bibliotecas externas para ícones e fontes -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <%-- Biblioteca de ícones (Font Awesome) --%>

    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600&display=swap" rel="stylesheet">
    <%-- Fonte padrão da aplicação --%>

    <link rel="stylesheet" href="css/patrimweb.css">
    <%-- Arquivo CSS principal do sistema --%>
    
    <!-- Bibliotecas JavaScript para exportação de dados -->
    <script src="https://cdnjs.cloudflare.com/ajax/libs/jspdf/2.5.1/jspdf.umd.min.js"></script>
    <%-- Biblioteca para geração de PDF no lado cliente --%>

    <script src="https://cdnjs.cloudflare.com/ajax/libs/jspdf-autotable/3.5.29/jspdf.plugin.autotable.min.js"></script>
    <%-- Plugin do jsPDF para geração automática de tabelas --%>

    <script src="https://cdnjs.cloudflare.com/ajax/libs/xlsx/0.18.5/xlsx.full.min.js"></script>
    <%-- Biblioteca para geração de arquivos Excel (XLSX) --%>
    
</head>
<body>

    <script>
        // ================================
        // DADOS RECEBIDOS DO BACK-END
        // ================================

        // Objeto JavaScript montado dinamicamente via JSTL.
        // Estrutura esperada:
        // { mes: quantidadeDeFabricantes }
        // Regra de negócio:
        // - Representa a quantidade de fabricantes cadastrados por mês.
        // - Dados previamente calculados no Controller.
        const fabricantesPorMes = {
            <c:forEach var="entry" items="${fabricantesPorMes}" varStatus="status">
                ${entry.key}: ${entry.value}<c:if test="${!status.last}">,</c:if>
            </c:forEach>
        };

        // Total de fabricantes no ano selecionado.
        // Validação:
        // - Caso venha nulo do backend, assume 0 para evitar erro JavaScript.
        const totalFabricantesAno = ${totalFabricantesAno != null ? totalFabricantesAno : 0};
    </script>

    <div class="mobile-overlay" id="mobileOverlay" onclick="toggleSidebar()"></div>
    <%-- Overlay utilizado para comportamento responsivo do menu lateral --%>

 <!-- INCLUIR SIDEBAR -->
    <jsp:include page="/includes/sidebar.jsp" />
    <%-- 
        Inclusão do menu lateral.
        Separação de responsabilidades e reutilização de layout.
    --%>

<main class="main-content">
    <header>
            <!-- Botão menu mobile -->
            <button class="menu-toggle" onclick="toggleSidebar()">
                <i class="fa-solid fa-bars"></i>
            </button>
            
            <h1>Relatório ${pageTitle}</h1>
            
            <!-- INCLUIR MENU DE USUÁRIO -->
            <jsp:include page="/includes/user-menu.jsp" />
            <%-- Inclusão do menu do usuário logado --%>
        </header>

        <div class="dashboard-container">
            
            <!-- ============================= -->
            <!-- FORMULÁRIO DE FILTROS -->
            <!-- ============================= -->
            <div class="filter-card">
                <form class="filter-grid" action="RelatorioFabricanteController" method="get">
                    <%-- 
                        Envia requisição GET para o Controller responsável.
                        Interação com backend:
                        - Controller processa filtros
                        - Consulta banco de dados
                        - Retorna lista filtrada
                    --%>

                    <div>
                        <label class="filter-label">Data Início</label>
                        <input type="date" name="dataInicio" class="filter-input" value="${filtroDataInicio}">
                        <%-- Filtro por data inicial de inserção --%>
                    </div>

                    <div>
                        <label class="filter-label">Data Fim</label>
                        <input type="date" name="dataFim" class="filter-input" value="${filtroDataFim}">
                        <%-- Filtro por data final de inserção --%>
                    </div>

                    <div>
                        <label class="filter-label">Nome do Fabricante</label>
                        <input type="text" name="nome" class="filter-input" placeholder="Nome do Fabricante..." value="${filtroNome}">
                        <%-- Filtro textual por nome --%>
                    </div>

                    <div>
                        <button class="btn btn-primary" type="submit" style="width: 100%;">
                            <i class="fa-solid fa-filter"></i> Filtrar
                        </button>
                    </div>
                </form>
            </div>

            <!-- ============================= -->
            <!-- GRÁFICO E EXPORTAÇÃO -->
            <!-- ============================= -->
            <div class="charts-row">
                <div class="chart-card">
                    <div class="chart-header">
                        <div class="chart-title">Cadastros por Mês (${anoAtual})</div>
                        <%-- Exibe o ano atualmente selecionado --%>

                        <form id="formAno" action="RelatorioFabricanteController" method="get">
                            <%-- 
                                Permite troca de ano mantendo os filtros ativos.
                                Regra importante:
                                - Preserva os filtros atuais via campos hidden.
                            --%>

                            <input type="hidden" name="dataInicio" value="${filtroDataInicio}">
                            <input type="hidden" name="dataFim" value="${filtroDataFim}">
                            <input type="hidden" name="nome" value="${filtroNome}">
                            
                            <select name="ano" id="ano" class="filter-input" style="width: 120px;" onchange="this.form.submit()">
                                <%-- 
                                    Estrutura de repetição:
                                    - Lista todos os anos disponíveis.
                                    - Marca como selected o ano atual.
                                --%>
                                <c:forEach var="anoItem" items="${anos}">
                                    <option value="${anoItem}" <c:if test="${anoItem == anoAtual}">selected</c:if>>
                                        ${anoItem}
                                    </option>
                                </c:forEach>
                            </select>
                        </form>
                    </div>

                    <%-- Estrutura visual do gráfico de barras (12 meses fixos) --%>
                    <div class="bar-chart-container">
                        <%-- Cada .bar-group representa um mês do ano --%>
                        <div class="bar-group" data-mes="1">
                            <div class="bar" style="height: 0px;"></div>
                            <span class="bar-label">Jan</span>
                        </div>
                        <%-- Demais meses seguem mesma estrutura até Dezembro --%>
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

                <%-- Card de exportação --%>
                <div class="chart-card" style="display: flex; flex-direction: column; justify-content: center;">
                    <div class="chart-title" style="margin-bottom: 20px;">Exportar Dados</div>
                    <div style="display: flex; flex-direction: column; gap: 10px;">
                        <button class="btn btn-outline" onclick="exportData('PDF')">
                            <i class="fa-solid fa-file-pdf" style="color: var(--danger);"></i> Baixar PDF
                        </button>

                        <button class="btn btn-outline" onclick="exportData('Excel')">
                            <i class="fa-solid fa-file-excel" style="color: var(--success);"></i> Baixar Excel (CSV)
                        </button>

                        <button class="btn btn-outline" onclick="window.print()">
                            <i class="fa-solid fa-print"></i> Imprimir Tela
                        </button>
                    </div>
                </div>
            </div>

            <!-- ============================= -->
            <!-- TABELA DE DETALHAMENTO -->
            <!-- ============================= -->
            <div class="report-table-container">
                <div class="table-header-row">
                    <div class="table-title">Detalhamento</div>
                    <div style="font-size: 13px; color: var(--text-muted); margin-bottom: 15px;">Listagem Geral de Fabricantes</div>
                </div>

                <div class="table-responsive">
                    <table id="tabelaRelatorio">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Nome do Fabricante</th>
                                <th>Data Inserção</th>
                            </tr>
                        </thead>
                        <tbody>
                            <%-- Estrutura condicional para tratar lista vazia --%>
                            <c:choose>
                                <c:when test="${empty fabricantes}">
                                    <%-- Regra de negócio:
                                         Caso nenhum registro seja encontrado após filtros,
                                         exibe mensagem informativa ao usuário.
                                    --%>
                                    <tr>
                                        <td colspan="3" style="text-align:center;">
                                            Nenhum fabricante encontrado
                                        </td>
                                    </tr>
                                </c:when>
                                <c:otherwise>
                                    <%-- 
                                        Estrutura de repetição:
                                        Itera sobre lista "fabricantes" enviada pelo Controller.
                                        Dados provavelmente provenientes de consulta ao banco.
                                    --%>
                                    <c:forEach var="f" items="${fabricantes}">
                                        <tr>
                                            <td>${f.idFab}</td>
                                            <td>${f.nomeFab}</td>
                                            <td>
                                                <%-- Formatação da data no padrão brasileiro --%>
                                                <fmt:formatDate value="${f.dataInsercao}" pattern="dd/MM/yyyy"/>
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
        // Fecha dropdown do usuário ao clicar fora
        window.addEventListener('click', function(e) {
            const container = document.querySelector('.user-menu-container');
            const dropdown = document.getElementById('userDropdown');
            
            // Validação para evitar erro ao clicar fora do container
            if (!container.contains(e.target)) {
                dropdown.classList.remove('show');
            }
        });

        // =====================================
        // FUNÇÃO DE EXPORTAÇÃO DE DADOS
        // =====================================
        function exportData(type) {

            // Exportação para PDF
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
                    doc.text("Relatório de Fabricantes - PatrimWeb", 30, 15);

                    doc.setFontSize(10);
                    doc.text("Gerado em: " + new Date().toLocaleDateString(), 30, 22);

                    // Geração automática da tabela a partir do HTML
                    doc.autoTable({ 
                        html: '#tabelaRelatorio',
                        startY: 30,
                        theme: 'grid',
                        headStyles: { fillColor: [59, 130, 246] },
                        styles: { fontSize: 10 }
                    });

                    // Salva arquivo localmente
                    doc.save('relatorio_fabricantes.pdf');
                };

                if (logoImg.complete && logoImg.naturalWidth > 0) {
                    gerarPDF();
                } else {
                    logoImg.onload  = gerarPDF;
                    logoImg.onerror = gerarPDF;
                }
                return;
            }

            // Exportação para Excel
            else if (type === 'Excel') {
                const tabela = document.getElementById('tabelaRelatorio');

                // Converte tabela HTML em workbook
                const wb = XLSX.utils.table_to_book(tabela, {sheet: "Fabricantes"});

                // Gera arquivo XLSX
                XLSX.writeFile(wb, 'relatorio_fabricantes.xlsx');
            }
        }
    </script>

    <script>
        // Altura máxima em pixels para barras do gráfico
        const ALTURA_MAXIMA = 140;

        // =====================================
        // FUNÇÃO DE ATUALIZAÇÃO DO GRÁFICO
        // =====================================
        function atualizarGrafico() {
            document.querySelectorAll('.bar-group').forEach(function(barGroup) {

                // Obtém o mês a partir do atributo data-mes
                const mes = parseInt(barGroup.dataset.mes);
                
                // Recupera quantidade de fabricantes do mês
                // Validação defensiva contra variáveis indefinidas
                const qtdMes = (typeof fabricantesPorMes !== 'undefined') ? (fabricantesPorMes[mes] || 0) : 0;

                // Define atributo usado pelo CSS para exibir valor
                barGroup.setAttribute('data-valor', qtdMes);

                let altura = 0;

                // Regra de cálculo proporcional:
                // altura = (valor do mês / total do ano) * altura máxima
                if (typeof totalFabricantesAno !== 'undefined' && totalFabricantesAno > 0) {
                    altura = (qtdMes * ALTURA_MAXIMA) / totalFabricantesAno;
                }

                // Regra visual:
                // Caso exista valor, mas altura calculada seja muito pequena,
                // define altura mínima de 5px para garantir visibilidade.
                if (qtdMes > 0 && altura < 5) {
                    altura = 5;
                }

                const bar = barGroup.querySelector('.bar');
                if (bar) {
                    bar.style.height = altura + 'px';
                }
            });
        }

        // Executa atualização do gráfico ao carregar a página
        atualizarGrafico();
    </script>

</body>
</html>
