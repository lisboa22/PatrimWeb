<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!-- 
    Página JSP responsável pelo gerenciamento de Equipamentos no sistema PatrimWeb.

    Objetivo:
    - Listar todos os equipamentos cadastrados.
    - Permitir cadastro de novos equipamentos.
    - Permitir edição de equipamentos existentes.
    - Permitir exclusão de registros.
    - Exibir mensagens de feedback (sucesso/erro) provenientes da camada Controller.

    Observação:
    - Atua exclusivamente como camada de visualização (View).
    - Os dados são fornecidos pelo EquipamentoController via atributos no request/session.
-->

<!-- Define a variável pageTitle para ativar o menu correto -->
<!-- Utilizada para controle de navegação e destaque do menu ativo -->
<c:set var="pageTitle" value="Equipamentos" scope="request" />

<!DOCTYPE html>
<html lang="pt-br">
<head>
    <!-- Configurações básicas de codificação e responsividade -->
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Gerenciamento de ${pageTitle} - PatrimWeb</title>

    <!-- Bibliotecas externas para ícones e tipografia -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600&display=swap" rel="stylesheet">

    <!-- Arquivo CSS principal do sistema -->
    <link rel="stylesheet" href="css/patrimweb.css">
</head>
<body>

<!-- Overlay utilizado para controle da sidebar em dispositivos móveis -->
<div class="mobile-overlay" id="mobileOverlay" onclick="toggleSidebar()"></div>

<!-- INCLUIR SIDEBAR -->
<!-- Componente reutilizável de navegação -->
<jsp:include page="/includes/sidebar.jsp" />

<main class="main-content">
    <header>
            <!-- Botão de abertura do menu lateral em telas menores -->
            <button class="menu-toggle" onclick="toggleSidebar()">
                <i class="fa-solid fa-bars"></i>
            </button>
            
            <!-- Título dinâmico da página -->
            <h1>${pageTitle}</h1>
            
            <!-- Container responsável pela exibição de mensagens do sistema -->
	    <!-- As mensagens são armazenadas na sessão pelo Controller -->
	    <div id="container-avisos" >
			    <%-- Mensagem de Erro --%>
			    <!-- 
			        Regra de negócio:
			        Exibe mensagem de erro caso exista no escopo de sessão.
			        Após exibição, a mensagem é removida para evitar repetição.
			    -->
			    <c:if test="${not empty sessionScope.mensagemErro}">
			        <div class="alerta-custom alerta-erro">
			            <span>
			                <strong>⚠️ Atenção:</strong> ${sessionScope.mensagemErro}
			            </span>
			            <button type="button" class="btn-fechar-alerta" onclick="this.parentElement.style.display='none'">&times;</button>
			        </div>
			        <% session.removeAttribute("mensagemErro"); %>
			    </c:if>
			
			    <%-- Mensagem de Sucesso --%>
			    <!-- Mesmo comportamento aplicado para mensagens de sucesso -->
			    <c:if test="${not empty sessionScope.mensagemSucesso}">
			        <div class="alerta-custom alerta-sucesso">
			            <span>
			                <strong>✅ Sucesso!</strong> ${sessionScope.mensagemSucesso}
			            </span>
			            <button type="button" class="btn-fechar-alerta" onclick="this.parentElement.style.display='none'">&times;</button>
			        </div>
			        <% session.removeAttribute("mensagemSucesso"); %>
			    </c:if>
			</div>
            
            <!-- INCLUIR MENU DE USUÁRIO -->
            <!-- Componente responsável pelo controle da sessão do usuário -->
            <jsp:include page="/includes/user-menu.jsp" />
        </header>

    <div class="dashboard-container">
    
	    

        <!-- Container principal da tabela de equipamentos -->
        <div class="table-container">
            <div class="table-header-row">
                <div class="table-title">Equipamentos Cadastrados</div>

                <!-- Ações principais da página -->
                <div class="header-actions">

                    <!-- Geração de relatório -->
                    <!-- Redireciona para RelatorioEquipamentoController -->
                    <a href="${pageContext.request.contextPath}/RelatorioEquipamentoController">
                        <button class="btn btn-outline">
                            <i class="fa-solid fa-file-export"></i> Relatório
                        </button>
                    </a>

                    <!-- Abre modal de cadastro -->
                    <button class="btn btn-primary" onclick="openModal()">
                        <i class="fa-solid fa-plus"></i> Novo Equipamento
                    </button>
                </div>
            </div>

            <div class="table-responsive">
                <table>
                    <thead>
                        <tr>
                            <th style="width: 80px;">#ID</th>
                            <th>Nome do Equipamento</th>
                            <th>Fabricante</th>
                            <th>Data Inserção</th>
                            <th style="text-align: center;">Ações</th>
                        </tr>
                    </thead>
                    <tbody>
                        <!-- 
                            Estrutura de repetição:
                            Percorre a lista "equipamentos" enviada pelo Controller.
                            Cada item representa um objeto Equipamento.
                        -->
                        <c:forEach var="e" items="${equipamentos}">
                            <tr>
                                <td>${e.idEquip}</td>
                                <td>${e.nomeEquip}</td>
                                <td>${e.fabricante.nomeFab}</td>

                                <!-- Formatação de data no padrão brasileiro -->
                                <td>
                                    <fmt:formatDate value="${e.dataInsercao}" pattern="dd/MM/yyyy"/>
                                </td>

                                <!-- Ações de edição e exclusão -->
                                <td style="text-align: center;">

                                    <!-- 
                                        Botão de edição:
                                        Envia dados do registro para preenchimento do modal.
                                        Observação: os dados são passados como parâmetros JavaScript.
                                    -->
                                    <button class="btn-icon" title="Editar" 
                                        onclick="openModalEditar('${e.idEquip}', '${e.nomeEquip}', '${e.fabricante.idFab}')">
                                        <i class="fa-solid fa-pen"></i>
                                    </button>

                                    <!-- 
                                        Botão de exclusão:
                                        Dispara função JavaScript que envia requisição POST ao Controller.
                                    -->
                                    <button class="btn-icon delete" title="Excluir" onclick="excluirEquipamento(${e.idEquip})">
                                        <i class="fa-solid fa-trash"></i>
                                    </button>

                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</main>

<!-- Modal de Cadastro -->
<!-- Responsável por cadastrar novo equipamento -->
<div id="modalCadastro" class="modal-overlay">
    <div class="modal-box">
        <div class="modal-header">
            <h3 class="modal-title">Cadastrar Novo Equipamento</h3>
            <button class="close-modal" onclick="closeModal()">&times;</button>
        </div>
        <div class="modal-body">

            <!-- 
                Formulário enviado via POST para EquipamentoController
                action=adicionar define operação no backend
            -->
            <form id="formEquipamento" action="${pageContext.request.contextPath}/EquipamentoController" method="post">
                <input type="hidden" name="action" value="adicionar">

                <!-- Campo obrigatório para nome do equipamento -->
                <div class="form-group full-width">
                    <label class="form-label">Nome do Equipamento</label>
                    <input type="text" name="nome_equip" class="form-input" placeholder="Ex: Notebook Dell Inspiron" required>
                </div>

                <!-- Seleção de fabricante -->
                <!-- Lista dinâmica proveniente do backend -->
                <div class="form-group full-width">
				    <label class="form-label">Fabricante</label>
				    <select name="id_fabricante" class="form-input" required>
				        <option value="">Selecione...</option>
				        <c:forEach var="f" items="${fabricantes}">
				            <option value="${f.idFab}">${f.nomeFab}</option>
				        </c:forEach>
				    </select>
				</div>
            </form>
        </div>

        <!-- Rodapé do modal -->
        <div class="modal-footer">
            <button class="btn btn-outline" onclick="closeModal()">Cancelar</button>
            <button class="btn btn-primary" type="submit" form="formEquipamento">Salvar</button>
        </div>
    </div>
</div>

<!-- Modal de Edição -->
<!-- Permite atualizar dados existentes -->
<div id="modalEditar" class="modal-overlay">
    <div class="modal-box">
        <div class="modal-header">
            <h3 class="modal-title">Editar Equipamento</h3>
            <button class="close-modal" onclick="closeModalEditar()">&times;</button>
        </div>
        <div class="modal-body">

            <!-- 
                Formulário enviado via POST
                action=editar define operação de atualização no Controller
            -->
            <form id="formEditarEquipamento" action="${pageContext.request.contextPath}/EquipamentoController" method="post">
                <input type="hidden" name="action" value="editar">
                <input type="hidden" name="id_equip" id="edit_id_equip">
                
                <div class="form-group full-width">
                    <label class="form-label">Nome do Equipamento</label>
                    <input type="text" name="nome_equip" id="edit_nome_equip" class="form-input" required>
                </div>

                <!-- Select preenchido dinamicamente -->
                <div class="form-group full-width">
                    <label class="form-label">Fabricante</label>
                    <select name="id_fabricante" id="edit_id_fabricante" class="form-input" required>
					    <option value="">Selecione...</option>
					    <c:forEach var="f" items="${fabricantes}">
					        <option value="${f.idFab}">${f.nomeFab}</option>
					    </c:forEach>
					</select>
                </div>
            </form>
        </div>

        <div class="modal-footer">
            <button class="btn btn-outline" onclick="closeModalEditar()">Cancelar</button>
            <button class="btn btn-primary" type="submit" form="formEditarEquipamento">Salvar Alterações</button>
        </div>
    </div>
</div>

<!-- INCLUIR SCRIPTS COMUNS -->
<jsp:include page="/includes/scripts-comum.jsp" />

<script>
    // Referências aos elementos modais
    const modal = document.getElementById('modalCadastro');
    const modalEditar = document.getElementById('modalEditar');

    // Controle de abertura/fechamento do modal de cadastro
    function openModal() { modal.classList.add('show'); }
    function closeModal() { modal.classList.remove('show'); }

    /*
        Função responsável por preencher o modal de edição.
        Parâmetros:
        - id: identificador do equipamento
        - nome: nome atual do equipamento
        - idFabricante: fabricante vinculado
    */
    function openModalEditar(id, nome, idFabricante) {
        document.getElementById('edit_id_equip').value = id;
        document.getElementById('edit_nome_equip').value = nome;
        document.getElementById('edit_id_fabricante').value = idFabricante;
        modalEditar.classList.add('show');
    }

    function closeModalEditar() { modalEditar.classList.remove('show'); }

    // Fecha modal ao clicar fora da área principal
    window.onclick = function(event) {
        if(event.target === modal) closeModal();
        if(event.target === modalEditar) closeModalEditar();
    }

    // Fecha dropdown do usuário ao clicar fora
    window.addEventListener('click', function(e) {
        const container = document.querySelector('.user-menu-container');
        if (!container.contains(e.target)) {
            document.getElementById('userDropdown').classList.remove('show');
        }
    });

    /*
        Função responsável pela exclusão de equipamento.
        Parâmetro:
        - idEquip: identificador do equipamento a ser excluído.

        Regra de negócio:
        - Solicita confirmação antes de enviar requisição.
        - Envia requisição POST ao EquipamentoController com action=deletar.
    */
    function excluirEquipamento(idEquip) {
        if(confirm("Tem certeza que deseja excluir este equipamento?")) {
            const form = document.createElement("form");
            form.method = "post";
            form.action = "${pageContext.request.contextPath}/EquipamentoController";

            const action = document.createElement("input");
            action.type = "hidden"; action.name = "action"; action.value = "deletar";

            const id = document.createElement("input");
            id.type = "hidden"; id.name = "id_equip"; id.value = idEquip;

            form.appendChild(action);
            form.appendChild(id);
            document.body.appendChild(form);
            form.submit();
        }
    }
</script>

<script>
    /*
        Script responsável por ocultar automaticamente alertas após 5 segundos.
        Melhora experiência do usuário evitando poluição visual.
    */
    document.addEventListener("DOMContentLoaded", function() {
        const alertas = document.querySelectorAll('.alerta-custom');
        alertas.forEach(function(alerta) {
            setTimeout(function() {
                alerta.style.transition = "opacity 0.6s ease";
                alerta.style.opacity = "0";
                setTimeout(() => alerta.remove(), 600);
            }, 5000);
        });
    });
</script>
		
</body>
</html>
