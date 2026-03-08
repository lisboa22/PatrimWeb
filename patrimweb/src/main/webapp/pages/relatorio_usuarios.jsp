<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<!-- 
    =====================================================================
    PÁGINA JSP: Relatório de Usuários - Sistema PatrimWeb
    =====================================================================

    PROPÓSITO DA PÁGINA:
    Esta JSP é responsável pela apresentação visual do relatório de usuários
    cadastrados no sistema PatrimWeb.

    RESPONSABILIDADES PRINCIPAIS:
    - Exibir filtros de consulta (período, nome e CPF).
    - Renderizar gráfico de cadastros por mês baseado em dados do servidor.
    - Listar usuários retornados pelo controller.
    - Permitir exportação dos dados em PDF e Excel.
    - Aplicar formatação visual e interações client-side (JavaScript).

    INTERAÇÃO COM BACK-END:
    - Recebe dados do RelatorioUsuarioController via atributos de request:
        • usuarios
        • usuariosPorMes
        • totalUsuariosAno
        • anos
        • anoAtual
        • filtros aplicados

    OBSERVAÇÃO:
    Esta página atua apenas como camada de apresentação (View),
    não contendo regras de persistência ou acesso direto ao banco.
-->

<c:set var="pageTitle" value="Usuários" scope="request" />

<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Relatório ${pageTitle} - PatrimWeb</title>

    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="css/patrimweb.css">

    <script src="https://cdnjs.cloudflare.com/ajax/libs/jspdf/2.5.1/jspdf.umd.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/jspdf-autotable/3.5.29/jspdf.plugin.autotable.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/xlsx/0.18.5/xlsx.full.min.js"></script>
</head>
<body>

    <script>
        /*
            ===============================================================
            INJEÇÃO DE DADOS DO SERVIDOR PARA O JAVASCRIPT
            ===============================================================
        */
        const usuariosPorMes = {
            <c:forEach var="entry" items="${usuariosPorMes}" varStatus="status">
                ${entry.key}: ${entry.value}<c:if test="${!status.last}">,</c:if>
            </c:forEach>
        };

        const totalUsuariosAno = ${totalUsuariosAno != null ? totalUsuariosAno : 0};
    </script>

    <div class="mobile-overlay" id="mobileOverlay" onclick="toggleSidebar()"></div>

    <jsp:include page="/includes/sidebar.jsp" />

<main class="main-content">
    <header>
            <button class="menu-toggle" onclick="toggleSidebar()">
                <i class="fa-solid fa-bars"></i>
            </button>
            
            <h1>Relatório ${pageTitle}</h1>
            
            <jsp:include page="/includes/user-menu.jsp" />
        </header>

        <div class="dashboard-container">
            
            <!-- FORMULÁRIO DE FILTRO -->
            <div class="filter-card">
                <form class="filter-grid" action="RelatorioUsuarioController" method="get">
                    <div>
                        <label class="filter-label">Data Início</label>
                        <input type="date" name="dataInicio" class="filter-input" value="${filtroDataInicio}">
                    </div>

                    <div>
                        <label class="filter-label">Data Fim</label>
                        <input type="date" name="dataFim" class="filter-input" value="${filtroDataFim}">
                    </div>

                    <div>
                        <label class="filter-label">Nome</label>
                        <input type="text" name="nome" class="filter-input" placeholder="Nome" value="${filtroNome}">
                    </div>

                    <div>
                        <label class="filter-label">CPF</label>
                        <input type="text" name="cpf" id="cpf" class="filter-input" placeholder="CPF" value="${filtroCpf}">
                    </div>

                    <div>
                        <button class="btn btn-primary" type="submit" style="width: 100%;">
                            <i class="fa-solid fa-filter"></i> Filtrar
                        </button>
                    </div>
                </form>
            </div>

            <div class="charts-row">
                <div class="chart-card">
                    <div class="chart-header">
                        <div class="chart-title">Cadastros por Mês (${anoAtual})</div>

                        <form id="formAno" action="RelatorioUsuarioController" method="get">
                            <input type="hidden" name="dataInicio" value="${filtroDataInicio}">
                            <input type="hidden" name="dataFim" value="${filtroDataFim}">
                            <input type="hidden" name="nome" value="${filtroNome}">
                            <input type="hidden" name="cpf" value="${filtroCpf}">
                            
                            <select name="ano" id="ano" class="filter-input" style="width: 120px;" onchange="this.form.submit()">
                                <c:forEach var="anoItem" items="${anos}">
                                    <option value="${anoItem}" <c:if test="${anoItem == anoAtual}">selected</c:if>>
                                        ${anoItem}
                                    </option>
                                </c:forEach>
                            </select>
                        </form>
                    </div>

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

            <!-- TABELA DE RESULTADOS -->
            <div class="report-table-container">
                <div class="table-header-row">
                    <div class="table-title">Detalhamento</div>
                    <div style="font-size: 13px; color: var(--text-muted); margin-bottom: 15px;">Listagem Geral de Usuários</div>
                </div>

                <div class="table-responsive">
                    <table id="tabelaRelatorio">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Nome</th>
                                <th>CPF</th>
                                <th>Telefone</th>
                                <th>Email</th>
                                <th>Endereço</th>
                                <th>Data Inserção</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${empty usuarios}">
                                    <tr>
                                        <td colspan="7" style="text-align:center;">
                                            Nenhum usuário encontrado
                                        </td>
                                    </tr>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="u" items="${usuarios}">
                                        <tr>
                                            <td>${u.idUsu}</td>
                                            <td>${u.nomeUsu}</td>
                                            <td>${u.cpfUsu}</td>
                                            <td>${u.telefoneUsu}</td>
                                            <td>${u.emailUsu}</td>
                                            <td>${u.enderecoUsu}</td>
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

    <jsp:include page="/includes/scripts-comum.jsp" />
    
    <script>
        window.addEventListener('click', function(e) {
            const container = document.querySelector('.user-menu-container');
            const dropdown = document.getElementById('userDropdown');
            if (!container.contains(e.target)) {
                dropdown.classList.remove('show');
            }
        });

        /*
            ===============================================================
            EXPORTAÇÃO DE DADOS (PDF e Excel)
            ===============================================================
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
                    doc.text("Relatório de Usuários - PatrimWeb", 30, 15);
                    doc.setFontSize(10);
                    doc.text("Gerado em: " + new Date().toLocaleDateString(), 30, 22);

                    doc.autoTable({ 
                        html: '#tabelaRelatorio',
                        startY: 30,
                        theme: 'grid',
                        headStyles: { fillColor: [59, 130, 246] },
                        styles: { fontSize: 8 }
                    });

                    doc.save('relatorio_usuarios.pdf');
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
                const wb = XLSX.utils.table_to_book(tabela, {sheet: "Usuários"});
                XLSX.writeFile(wb, 'relatorio_usuarios.xlsx');
            }
        }
        
        /*
            Máscara dinâmica para CPF no campo de filtro.
        */
        const inputCPF = document.getElementById('cpf');
        if (inputCPF) {
            inputCPF.addEventListener('input', function(e) {
                let value = e.target.value.replace(/\D/g, "");
                value = value.replace(/(\d{3})(\d)/, "$1.$2");
                value = value.replace(/(\d{3})(\d)/, "$1.$2");
                value = value.replace(/(\d{3})(\d{1,2})$/, "$1-$2");
                e.target.value = value;
            });
        }
    </script>
    
    <script>
        /*
            ===============================================================
            GRÁFICO DE BARRAS — declaração única e unificada
            ---------------------------------------------------------------
            CORREÇÃO: A função estava declarada DUAS vezes no arquivo original.
            Em JavaScript isso causa um problema sutil: a segunda declaração
            sobrescreve a primeira via hoisting, então a chamada atualizarGrafico()
            entre as duas definições executava a versão incompleta (sem data-valor).
            Agora existe apenas UMA declaração com toda a lógica integrada.
            ===============================================================
        */
        const ALTURA_MAXIMA = 140;

        function atualizarGrafico() {
            document.querySelectorAll('.bar-group').forEach(function(barGroup) {
                const mes = parseInt(barGroup.dataset.mes);
                const qtdMes = (typeof usuariosPorMes !== 'undefined') ? (usuariosPorMes[mes] || 0) : 0;

                // Atributo lido pelo CSS para exibir tooltip com o valor sobre a barra
                barGroup.setAttribute('data-valor', qtdMes);

                let altura = 0;
                if (typeof totalUsuariosAno !== 'undefined' && totalUsuariosAno > 0) {
                    altura = (qtdMes * ALTURA_MAXIMA) / totalUsuariosAno;
                }

                // Garante visibilidade mínima quando existir valor positivo
                if (qtdMes > 0 && altura < 5) {
                    altura = 5;
                }

                const bar = barGroup.querySelector('.bar');
                if (bar) {
                    bar.style.height = altura + 'px';
                }
            });
        }

        // ✅ Chamada única, após a definição completa da função
        atualizarGrafico();
    </script>

</body>
</html>
