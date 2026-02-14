<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!-- 
    =====================================================================================
    PÁGINA: fabricantes.jsp
    SISTEMA: PatrimWeb
    CAMADA: View (JSP)

    PROPÓSITO DA PÁGINA:
    - Realizar o gerenciamento completo de Fabricantes cadastrados no sistema.
    - Exibir listagem de fabricantes.
    - Permitir cadastro, edição e exclusão de registros.
    - Exibir mensagens de feedback provenientes do Controller.

    RESPONSABILIDADES:
    - Renderizar dados recebidos do FabricanteController.
    - Enviar requisições HTTP (POST) para operações de inserção, edição e exclusão.
    - Formatar dados para exibição (ex: datas).
    - Controlar modais e interações de interface via JavaScript.

    OBSERVAÇÃO IMPORTANTE:
    - Não contém regras de persistência.
    - Interage com banco de dados indiretamente via Controller.
    =====================================================================================
-->

<!-- 
    Define variável pageTitle no escopo de request.
    Finalidade:
    - Permitir ativação visual do menu correspondente na sidebar.
    - Utilizada também no título dinâmico da página.
-->
<c:set var="pageTitle" value="Fabricantes" scope="request" />

<!DOCTYPE html>
<html lang="pt-br">
<head>
    <!-- Configuração de codificação e responsividade -->
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Gerenciamento de ${pageTitle} - PatrimWeb</title>

    <!-- Bibliotecas externas para ícones e tipografia -->
    <!-- Dependências visuais utilizadas pela interface -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600&display=swap" rel="stylesheet">

    <!-- Arquivo CSS principal do sistema -->
    <link rel="stylesheet" href="css/patrimweb.css">
    
</head>
<body>

    <!-- 
        Overlay utilizado para bloquear interação com conteúdo principal 
        quando a sidebar estiver aberta em dispositivos móveis.
    -->
    <div class="mobile-overlay" id="mobileOverlay" onclick="toggleSidebar()"></div>

    <!-- Inclusão da sidebar (componente reutilizável de navegação) -->
    <jsp:include page="/includes/sidebar.jsp" />

<main class="main-content">
    <header>
            <!-- 
                Botão responsável por abrir/fechar a sidebar em telas menores.
                Interação controlada via JavaScript.
            -->
            <button class="menu-toggle" onclick="toggleSidebar()">
                <i class="fa-solid fa-bars"></i>
            </button>
            
            <!-- Título dinâmico baseado na variável pageTitle -->
            <h1>${pageTitle}</h1>
            
            <!-- 
                Container de mensagens do sistema.

                REGRA DE NEGÓCIO:
                - As mensagens são definidas no Controller.
                - São armazenadas na sessão.
                - Após renderização, são removidas da sessão para evitar reapresentação.
            -->
            <div id="container-avisos" >

			    <%-- Mensagem de Erro --%>
			    <c:if test="${not empty sessionScope.mensagemErro}">
			        <!-- 
                        Exibe mensagem de erro caso exista no escopo de sessão.
                        Ponto crítico:
                        - Remove atributo após exibição para evitar duplicidade.
                    -->
			        <div class="alerta-custom alerta-erro">
			            <span>
			                <strong>⚠️ Atenção:</strong> ${sessionScope.mensagemErro}
			            </span>
			            <button type="button" class="btn-fechar-alerta" onclick="this.parentElement.style.display='none'">&times;</button>
			        </div>
			        <% session.removeAttribute("mensagemErro"); %>
			    </c:if>
			
			    <%-- Mensagem de Sucesso --%>
			    <c:if test="${not empty sessionScope.mensagemSucesso}">
			        <!-- Mesmo comportamento aplicado para mensagens de sucesso -->
			        <div class="alerta-custom alerta-sucesso">
			            <span>
			                <strong>✅ Sucesso!</strong> ${sessionScope.mensagemSucesso}
			            </span>
			            <button type="button" class="btn-fechar-alerta" onclick="this.parentElement.style.display='none'">&times;</button>
			        </div>
			        <% session.removeAttribute("mensagemSucesso"); %>
			    </c:if>
			</div>
            
            <!-- Inclusão do menu de usuário (controle de sessão e logout) -->
            <jsp:include page="/includes/user-menu.jsp" />
        </header>

        <div class="dashboard-container">
            
            

            <!-- Container principal da listagem -->
            <div class="table-container">
                <div class="table-header-row">
                    <div class="table-title">Fabricantes Cadastrados</div>

					<div class="header-actions">

                        <!-- 
                            Geração de relatório.
                            Interação:
                            - Requisição GET ao RelatorioFabricanteController.
                            - Controller será responsável por gerar o relatório (ex: PDF/Excel).
                        -->
						<a href="${pageContext.request.contextPath}/RelatorioFabricanteController">
                            <button class="btn btn-outline">
                                <i class="fa-solid fa-file-export"></i> Relatório
                            </button>
                        </a>

                        <!-- Abre modal de cadastro -->
                        <button class="btn btn-primary" onclick="openModal()">
                            <i class="fa-solid fa-plus"></i> Novo Fabricante
                        </button>

					</div>
                </div>

                <div class="table-responsive">
                    <table>
                        <thead>
                            <tr>
                                <th style="width: 80px;">#ID</th>
                                <th>Nome do Fabricante</th>
                                <th>Data de Inserção</th>
                                <th style="text-align: center;">Ações</th>
                            </tr>
                        </thead>
                        <tbody>

						    <!-- 
						        Estrutura de repetição (JSTL).
						        
                                Fonte dos dados:
                                - Lista "fabricantes" enviada pelo FabricanteController.
                                - Cada elemento representa um objeto do tipo Fabricante.
                                
                                Ponto crítico:
                                - Caso a lista esteja vazia, nenhuma linha será renderizada.
						    -->
						    <c:forEach var="f" items="${fabricantes}">
						        <tr>
						            <td>${f.idFab}</td>
						            <td>${f.nomeFab}</td>

                                    <!-- 
                                        Formatação da data:
                                        - Utiliza tag fmt:formatDate.
                                        - Converte para padrão brasileiro (dd/MM/yyyy).
                                        - A propriedade dataInsercao deve ser do tipo Date.
                                    -->
						            <td>
                                        <fmt:formatDate value="${f.dataInsercao}" pattern="dd/MM/yyyy"/>
                                    </td>

						            <td style="text-align: center;">

						            	<!-- 
                                            Botão de edição.

                                            Regra:
                                            - Abre modal de edição.
                                            - Pré-carrega ID e nome nos campos ocultos/inputs.
                                        -->
						            	<button class="btn-icon" title="Editar" onclick="openModalEditar(
																					        '${f.idFab}',
																					        '${f.nomeFab}'  
																					    )">
					                        <i class="fa-solid fa-pen"></i>
					                    </button>

                                        <!-- 
                                            Botão de exclusão.

                                            Regra de negócio:
                                            - Solicita confirmação do usuário.
                                            - Envia requisição POST para exclusão.
                                            - A exclusão é processada pelo Controller.
                                        -->
						                <button class="btn-icon delete" 
										        title="Excluir"
										        onclick="excluirFabricante(${f.idFab})">
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
    <div id="modalCadastro" class="modal-overlay">
        <div class="modal-box">
            <div class="modal-header">
                <h3 class="modal-title">Cadastrar Novo Fabricante</h3>
                <button class="close-modal" onclick="closeModal()">&times;</button>
            </div>
            
            <div class="modal-body">

            	<!-- 
                    Formulário de cadastro.

                    Método: POST
                    Destino: /patrimweb/FabricanteController
                    Parâmetro action=adicionar define operação de inserção.

                    Interação com banco:
                    - Controller chamará DAO para persistência.
                -->
            	<form id="formFabricante" action="/patrimweb/FabricanteController" method="post">
                
    			    <input type="hidden" name="action" value="adicionar">
              
                    <div class="form-group full-width">
                        <label class="form-label">Nome</label>

                        <!-- 
                            Campo obrigatório.
                            Validação client-side via atributo required.
                        -->
                        <input type="text" name="nome_fab" class="form-input" placeholder="Ex: Dell, Samsung, LG..." required>
                    </div>

                </form>
            </div>
    
            <div class="modal-footer">
                <button class="btn btn-outline" onclick="closeModal()">Cancelar</button>
                <!-- Submete formulário externo através do atributo form -->
                <button class="btn btn-primary" type="submit" form="formFabricante">Salvar Fabricante</button>
            </div>
        </div>
    </div>
    
    <!-- Modal de Edição -->
    <div id="modalEditar" class="modal-overlay">
	    <div class="modal-box">
	        <div class="modal-header">
	            <h3 class="modal-title">Editar Fabricante</h3>
	            <button class="close-modal" onclick="closeModalEditar()">&times;</button>
	        </div>
			        
	        <div class="modal-body">

                <!-- 
                    Formulário de edição.

                    Método: POST
                    action=editar define atualização.
                    id_fab identifica registro a ser alterado.
                -->
	            <form id="formEditarFabricante" action="/patrimweb/FabricanteController" method="post">
			                
	                <input type="hidden" name="action" value="editar">
	                <input type="hidden" name="id_fab" id="edit_id_fab">
			
	                <div class="form-group full-width">
	                    <label class="form-label">Nome</label>
	                    <input type="text" name="nome_fab" id="edit_nome_fab" class="form-input" required>
	                </div>
			
	            </form>
	        </div>
			        
	        <div class="modal-footer">
	            <button class="btn btn-outline" onclick="closeModalEditar()">Cancelar</button>
	            <button class="btn btn-primary" type="submit" form="formEditarFabricante">Salvar Alterações</button>
	        </div>
	   	</div>
	</div>

	<!-- Inclusão de scripts comuns do sistema -->
    <jsp:include page="/includes/scripts-comum.jsp" />
    
    <script>
        /*
            Controle de exibição do modal de cadastro.

            Função: openModal()
            - Exibe o modal adicionando classe CSS 'show'.

            Função: closeModal()
            - Oculta o modal removendo classe CSS.
        */
        const modal = document.getElementById('modalCadastro');

        function openModal() {
            modal.classList.add('show');
        }

        function closeModal() {
            modal.classList.remove('show');
        }

        // Fecha modal ao clicar fora da área principal
        window.onclick = function(event) {
            if (event.target == modal) {
                closeModal();
            }
        }

        /*
            Controle de fechamento do dropdown do usuário.
            Regra:
            - Caso clique fora do container, remove classe 'show'.
        */
        window.addEventListener('click', function(e) {
            const container = document.querySelector('.user-menu-container');
            const dropdown = document.getElementById('userDropdown');
            
            if (!container.contains(e.target)) {
                dropdown.classList.remove('show');
            }
        });
        // ---------------------------------------------

        /*
            Controle do modal de edição.

            Parâmetros:
            - id: identificador do fabricante.
            - nome: nome atual do fabricante.

            Responsabilidade:
            - Preencher campos do formulário antes da exibição.
        */
        const modalEditar = document.getElementById('modalEditar');

        function openModalEditar(id, nome) {
            document.getElementById('edit_id_fab').value = id;
            document.getElementById('edit_nome_fab').value = nome;

            modalEditar.classList.add('show');
        }

        function closeModalEditar() {
            modalEditar.classList.remove('show');
        }

        // Fecha modal de edição ao clicar fora
        window.addEventListener('click', function(event) {
            if (event.target === modalEditar) {
                closeModalEditar();
            }
        });
        
    </script>
    
    <script>
        /*
            Função: excluirFabricante(idFab)

            Parâmetro:
            - idFab: identificador do fabricante a ser excluído.

            Regra de negócio:
            - Solicita confirmação ao usuário.
            - Cria dinamicamente formulário POST.
            - Envia parâmetros:
                action=deletar
                id_fab=idFab

            Interação com banco:
            - Exclusão será processada no Controller e DAO.
        */
		function excluirFabricante(idFab) {
		    if (confirm("Tem certeza que deseja excluir este fabricante?")) {
		        const form = document.createElement("form");
		        form.method = "post";
		        form.action = "/patrimweb/FabricanteController";
		
		        const action = document.createElement("input");
		        action.type = "hidden";
		        action.name = "action";
		        action.value = "deletar";
		
		        const id = document.createElement("input");
		        id.type = "hidden";
		        id.name = "id_fab";
		        id.value = idFab;
		
		        form.appendChild(action);
		        form.appendChild(id);
		        document.body.appendChild(form);
		        form.submit();
		    }
		}
		</script>
		
		<script>
            /*
                Script responsável por remover automaticamente alertas após 5 segundos.

                Objetivo:
                - Melhorar experiência do usuário.
                - Evitar acúmulo visual de mensagens antigas.
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
