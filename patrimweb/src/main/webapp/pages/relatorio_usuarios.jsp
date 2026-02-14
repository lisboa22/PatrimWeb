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

<!-- Define a variável pageTitle para ativar o menu correto -->
<!-- Regra de navegação: utilizada pelos includes para destacar o menu ativo -->
<c:set var="pageTitle" value="Usuários" scope="request" />

<!DOCTYPE html>
<html lang="pt-br">
<head>
    <!-- Configurações básicas de renderização -->
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Relatório ${pageTitle} - PatrimWeb</title>

    <!-- Biblioteca de ícones utilizada na interface -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">

    <!-- Fonte padrão do sistema -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600&display=swap" rel="stylesheet">

    <!-- CSS principal do sistema -->
    <link rel="stylesheet" href="css/patrimweb.css">
    
    <!-- Bibliotecas externas utilizadas para exportação de relatórios -->
    <!-- jsPDF: geração de PDF -->
    <script src="https://cdnjs.cloudflare.com/ajax/libs/jspdf/2.5.1/jspdf.umd.min.js"></script>

    <!-- Plugin para criação automática de tabelas em PDF -->
    <script src="https://cdnjs.cloudflare.com/ajax/libs/jspdf-autotable/3.5.29/jspdf.plugin.autotable.min.js"></script>

    <!-- Biblioteca XLSX para exportação Excel -->
    <script src="https://cdnjs.cloudflare.com/ajax/libs/xlsx/0.18.5/xlsx.full.min.js"></script>
</head>
<body>

    <script>
        /*
            ===============================================================
            INJEÇÃO DE DADOS DO SERVIDOR PARA O JAVASCRIPT
            ===============================================================

            Os dados abaixo são enviados pelo controller e convertidos
            para objetos JavaScript para permitir renderização dinâmica
            do gráfico de barras no cliente.

            usuariosPorMes:
                Estrutura chave/valor onde:
                chave = mês (1..12)
                valor = quantidade de usuários cadastrados no mês

            totalUsuariosAno:
                Total geral utilizado como base proporcional do gráfico.
        */
        const usuariosPorMes = {
            <c:forEach var="entry" items="${usuariosPorMes}" varStatus="status">
                ${entry.key}: ${entry.value}<c:if test="${!status.last}">,</c:if>
            </c:forEach>
        };

        // Garante valor padrão 0 caso não exista dado vindo do servidor
        const totalUsuariosAno = ${totalUsuariosAno != null ? totalUsuariosAno : 0};
    </script>

    <!-- Overlay utilizado em dispositivos móveis para fechar sidebar -->
    <div class="mobile-overlay" id="mobileOverlay" onclick="toggleSidebar()"></div>

 <!-- INCLUIR SIDEBAR -->
 <!-- Componente reutilizável de navegação lateral -->
    <jsp:include page="/includes/sidebar.jsp" />

<main class="main-content">
    <header>
            <!-- Botão exibido apenas em layout mobile -->
            <button class="menu-toggle" onclick="toggleSidebar()">
                <i class="fa-solid fa-bars"></i>
            </button>
            
            <!-- Título dinâmico baseado na variável pageTitle -->
            <h1>Relatório ${pageTitle}</h1>
            
            <!-- Menu do usuário autenticado -->
            <jsp:include page="/includes/user-menu.jsp" />
        </header>

        <div class="dashboard-container">
            
            <!-- 
                ==========================================================
                FORMULÁRIO DE FILTRO
                ==========================================================
                Envia requisição GET ao controller responsável pelo relatório.
                Os valores preenchidos retornam preservados após a consulta.
            -->
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
                        <!-- Filtro textual aplicado pelo controller -->
                        <input type="text" name="nome" class="filter-input" placeholder="Nome" value="${filtroNome}">
                    </div>

                    <div>
                        <label class="filter-label">CPF</label>
                        <!-- Campo com máscara aplicada via JavaScript -->
                        <input type="text" name="cpf" id="cpf" class="filter-input" placeholder="CPF" value="${filtroCpf}">
                    </div>

                    <div>
                        <!-- Dispara nova consulta aplicando filtros -->
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

                        <!-- 
                            Formulário responsável por troca de ano do gráfico.
                            Mantém filtros existentes através de campos hidden,
                            preservando o contexto da busca.
                        -->
                        <form id="formAno" action="RelatorioUsuarioController" method="get">
                            <input type="hidden" name="dataInicio" value="${filtroDataInicio}">
                            <input type="hidden" name="dataFim" value="${filtroDataFim}">
                            <input type="hidden" name="nome" value="${filtroNome}">
                            <input type="hidden" name="cpf" value="${filtroCpf}">
                            
                            <!-- Alteração do ano dispara submissão automática -->
                            <select name="ano" id="ano" class="filter-input" style="width: 120px;" onchange="this.form.submit()">
                                <c:forEach var="anoItem" items="${anos}">
                                    <option value="${anoItem}" <c:if test="${anoItem == anoAtual}">selected</c:if>>
                                        ${anoItem}
                                    </option>
                                </c:forEach>
                            </select>
                        </form>
                    </div>

                    <!-- 
                        Estrutura base do gráfico de barras.
                        Cada grupo representa um mês.
                        A altura real é calculada dinamicamente via JavaScript.
                    -->
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
                
                <!-- Área de exportação de dados -->
                <div class="chart-card" style="display: flex; flex-direction: column; justify-content: center;">
                    <div class="chart-title" style="margin-bottom: 20px;">Exportar Dados</div>
                    <div style="display: flex; flex-direction: column; gap: 10px;">
                        <!-- Exportação PDF -->
                        <button class="btn btn-outline" onclick="exportData('PDF')">
                            <i class="fa-solid fa-file-pdf" style="color: var(--danger);"></i> Baixar PDF
                        </button>

                        <!-- Exportação Excel -->
                        <button class="btn btn-outline" onclick="exportData('Excel')">
                            <i class="fa-solid fa-file-excel" style="color: var(--success);"></i> Baixar Excel (CSV)
                        </button>

                        <!-- Impressão nativa do navegador -->
                        <button class="btn btn-outline" onclick="window.print()">
                            <i class="fa-solid fa-print"></i> Imprimir Tela
                        </button>
                    </div>
                </div>
            </div>

            <!-- ==========================================================
                 TABELA DE RESULTADOS
                 ==========================================================
                 Apresenta listagem retornada pelo controller.
                 -->
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
                            <!-- 
                                Estrutura condicional JSTL:
                                - Caso não haja registros, exibe mensagem informativa.
                                - Caso contrário, percorre lista de usuários.
                            -->
                            <c:choose>
                                <c:when test="${empty usuarios}">
                                    <tr>
                                        <td colspan="7" style="text-align:center;">
                                            Nenhum usuário encontrado
                                        </td>
                                    </tr>
                                </c:when>
                                <c:otherwise>
                                    <!-- Iteração sobre coleção enviada pelo controller -->
                                    <c:forEach var="u" items="${usuarios}">
                                        <tr>
                                            <td>${u.idUsu}</td>
                                            <td>${u.nomeUsu}</td>
                                            <td>${u.cpfUsu}</td>
                                            <td>${u.telefoneUsu}</td>
                                            <td>${u.emailUsu}</td>
                                            <td>${u.enderecoUsu}</td>
                                            <td>
                                                <!-- Formatação de data via JSTL fmt -->
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

	<!-- Scripts comuns reutilizados pelo sistema -->
    <jsp:include page="/includes/scripts-comum.jsp" />
    
    <script>
        /*
            Fecha o dropdown do menu do usuário quando ocorre clique
            fora do container.

            REGRA DE INTERAÇÃO:
            Evita que o menu permaneça aberto após perda de foco.
        */
        window.addEventListener('click', function(e) {
            const container = document.querySelector('.user-menu-container');
            const dropdown = document.getElementById('userDropdown');
            if (!container.contains(e.target)) {
                dropdown.classList.remove('show');
            }
        });

        /*
            ===============================================================
            FUNÇÃO: exportData
            ===============================================================
            Responsável por exportar a tabela HTML para diferentes formatos.

            Parâmetro:
                type -> define o formato de saída ('PDF' ou 'Excel')

            Regras:
                - PDF usa jsPDF + autoTable.
                - Excel converte diretamente a tabela HTML.
        */
        function exportData(type) {
            if (type === 'PDF') {
                const { jsPDF } = window.jspdf;
                const doc = new jsPDF();

                // Cabeçalho do documento
                doc.setFontSize(18);
                doc.text("Relatório de Usuários - PatrimWeb", 14, 15);

                // Data de geração
                doc.setFontSize(10);
                doc.text("Gerado em: " + new Date().toLocaleDateString(), 14, 22);

                // Conversão automática da tabela HTML
                doc.autoTable({ 
                    html: '#tabelaRelatorio',
                    startY: 30,
                    theme: 'grid',
                    headStyles: { fillColor: [59, 130, 246] },
                    styles: { fontSize: 8 }
                });

                // Download do arquivo
                doc.save('relatorio_usuarios.pdf');
            }
            else if (type === 'Excel') {
                const tabela = document.getElementById('tabelaRelatorio');

                // Converte tabela HTML em workbook Excel
                const wb = XLSX.utils.table_to_book(tabela, {sheet: "Usuários"});

                // Gera arquivo XLSX
                XLSX.writeFile(wb, 'relatorio_usuarios.xlsx');
            }
        }
        
        /*
            Máscara dinâmica para CPF.
            REGRA:
            - Remove caracteres não numéricos.
            - Aplica formatação padrão XXX.XXX.XXX-XX.
            - Atua apenas na camada visual (frontend).
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
            ALTURA_MAXIMA:
            Define o limite visual máximo das barras do gráfico.
            Utilizado como base proporcional para cálculo de altura.
        */
        const ALTURA_MAXIMA = 140;

        /*
            ===============================================================
            FUNÇÃO: atualizarGrafico
            ===============================================================
            Responsável por ajustar dinamicamente a altura das barras
            com base na quantidade de usuários por mês.

            LÓGICA:
            - Percorre todos os meses do gráfico.
            - Obtém quantidade correspondente no objeto usuariosPorMes.
            - Calcula proporção em relação ao total anual.
            - Define altura mínima visual para valores pequenos.
        */
        function atualizarGrafico() {
            document.querySelectorAll('.bar-group').forEach(barGroup => {
                const mes = parseInt(barGroup.dataset.mes);
                const usuariosMes = usuariosPorMes[mes] || 0;

                let altura = 0;

                if (totalUsuariosAno > 0) {
                    altura = (usuariosMes * ALTURA_MAXIMA) / totalUsuariosAno;
                }

                // Garante visibilidade mínima quando existir valor
                if (usuariosMes > 0 && altura < 5) {
                    altura = 5;
                }

                barGroup.querySelector('.bar').style.height = altura + 'px';
            });
        }

        // Executa automaticamente após carregamento da página
        atualizarGrafico();
        
        
        /*
            Segunda implementação da função atualizarGrafico.
            Acrescenta atributo data-valor utilizado por tooltip CSS.
            Mantém cálculo proporcional da altura das barras.
        */
        function atualizarGrafico() {
            document.querySelectorAll('.bar-group').forEach(function(barGroup) {
                const mes = parseInt(barGroup.dataset.mes);
                
                const qtdMes = (typeof usuariosPorMes !== 'undefined') ? (usuariosPorMes[mes] || 0) : 0;

                // Valor exibido visualmente via CSS (tooltip)
                barGroup.setAttribute('data-valor', qtdMes);

                let altura = 0;
                if (typeof totalUsuariosAno !== 'undefined' && totalUsuariosAno > 0) {
                    altura = (qtdMes * ALTURA_MAXIMA) / totalUsuariosAno;
                }

                // Altura mínima visual
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
