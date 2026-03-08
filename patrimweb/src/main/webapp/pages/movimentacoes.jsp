<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!--
    ============================================================
    PÁGINA JSP: GERENCIAMENTO DE MOVIMENTAÇÕES
    ============================================================

    Propósito da página:
    - Exibir o histórico de movimentações de equipamentos do sistema.
    - Permitir cadastro, edição e exclusão de movimentações.
    - Integrar dados vindos do backend (Controllers/DAOs) através
      de atributos disponibilizados no request.
    - Servir como camada de visualização (View) dentro do padrão MVC.

    Tecnologias utilizadas:
    - JSP + JSTL
    - Expression Language (EL)
    - JavaScript para interações dinâmicas
    - Controllers Java para persistência e regras de negócio.
-->

<!--
    Define o título da página em escopo de request.
    Regra de negócio visual:
    O sidebar utiliza essa variável para destacar o menu ativo.
-->
<c:set var="pageTitle" value="Movimentações" scope="request" />


<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">

    <!-- Configuração responsiva para dispositivos móveis -->
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <!-- Título dinâmico da página -->
    <title>Gerenciamento de ${pageTitle} - PatrimWeb</title>

    <!-- Biblioteca de ícones FontAwesome -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">

    <!-- Fonte padrão utilizada no sistema -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600&display=swap" rel="stylesheet">

    <!-- CSS principal do sistema -->
    <link rel="stylesheet" href="css/patrimweb.css">
    
</head>
<body>

    <!--
        Overlay utilizado no modo mobile.
        Regra visual:
        Ao clicar fora da sidebar, ela é fechada.
    -->
    <div class="mobile-overlay" id="mobileOverlay" onclick="toggleSidebar()"></div>

 <!-- INCLUIR SIDEBAR -->
    <!--
        Inclusão do menu lateral reutilizável.
        Centraliza navegação do sistema e evita duplicação de código.
    -->
    <jsp:include page="/includes/sidebar.jsp" />

<main class="main-content">
    <header>
            <!--
                Botão exibido apenas em telas menores.
                Responsável por abrir/fechar o menu lateral.
            -->
            <button class="menu-toggle" onclick="toggleSidebar()">
                <i class="fa-solid fa-bars"></i>
            </button>
            
            
            <!-- Título principal da página -->
            <h1>${pageTitle}</h1>
            
            <!--
                Container responsável por exibir mensagens
                de feedback vindas da sessão HTTP.
                Utilizado após operações CRUD.
            -->
            <div id="container-avisos" >
			    <%-- Mensagem de Erro --%>
                <!--
                    Exibida quando o backend define "mensagemErro" na sessão.
                    Estrutura condicional JSTL.
                -->
			    <c:if test="${not empty sessionScope.mensagemErro}">
			        <div class="alerta-custom alerta-erro">
			            <span>
			                <strong>⚠️ Atenção:</strong> ${sessionScope.mensagemErro}
			            </span>

                        <!-- Fecha visualmente o alerta -->
			            <button type="button" class="btn-fechar-alerta" onclick="this.parentElement.style.display='none'">&times;</button>
			        </div>

                    <!--
                        Regra importante:
                        Remove a mensagem da sessão após exibição,
                        evitando reaparecimento em refresh.
                    -->
			        <% session.removeAttribute("mensagemErro"); %>
			    </c:if>
			
			    <%-- Mensagem de Sucesso --%>
			    <c:if test="${not empty sessionScope.mensagemSucesso}">
			        <div class="alerta-custom alerta-sucesso">
			            <span>
			                <strong>✅ Sucesso!</strong> ${sessionScope.mensagemSucesso}
			            </span>
			            <button type="button" class="btn-fechar-alerta" onclick="this.parentElement.style.display='none'">&times;</button>
			        </div>

                    <!-- Remove mensagem após uso -->
			        <% session.removeAttribute("mensagemSucesso"); %>
			    </c:if>
			</div>
            
            <!-- INCLUIR MENU DE USUÁRIO -->
            <!-- Menu contendo informações do usuário autenticado -->
            <jsp:include page="/includes/user-menu.jsp" />
        </header>

        <div class="dashboard-container">
		
            <div class="table-container">

                <!-- Cabeçalho da tabela -->
                <div class="table-header-row">
                    <div class="table-title">Histórico de Movimentações</div>

                    <!-- Ações principais -->
                    <div class="header-actions">

                        <!--
                            Geração de relatório.
                            Redireciona para controller responsável pela exportação.
                        -->
                        <a href="${pageContext.request.contextPath}/RelatorioMovimentacaoController">
                        <button class="btn btn-outline"><i class="fa-solid fa-file-export"></i> Relatório</button></a>

                        <!-- Abre modal de cadastro -->
                        <button class="btn btn-primary" onclick="openModal()"><i class="fa-solid fa-plus"></i> Nova Movimentação</button>
                    </div>
                </div>

                <!-- Container responsivo da tabela -->
                <div class="table-responsive">
                    <table>
                        <thead>
                            <tr>
                                <th style="width: 50px;">#ID</th>
                                <th>Equipamento/Número Série</th>
                                <th>Fabricante</th>
                                <th>Tipo</th>
                                <th>Origem &rarr; Destino</th>
                                <th>Libração &rarr; Recepção</th>
                                <th>Observação</th>
                                <th>Data</th>
                                <th style="text-align: center;">Ações</th>
                            </tr>
                        </thead>
                        <tbody>

                            <!--
                                Estrutura de repetição JSTL.
                                Percorre a coleção "movimentacoes"
                                enviada pelo Controller.
                            -->
                              <c:forEach var="m" items="${movimentacoes}">
                                <tr>
                                	
                                    <!-- Dados da movimentação -->
                                    <td>${m.idMov}</td>
                                    <td>${m.equipamento.nomeEquip} &rarr; ${m.equipamento.numSerieEquip}</td>
                                    <td>${m.fabricante.nomeFab}</td>
                                    <td>${m.tipoMovimentacaoMov}</td>

                                    <!-- Regra de visualização:
                                         mostra fluxo origem → destino -->
                                    <td>${m.unidadeOrigem.nomeUnid} &rarr; ${m.unidadeDestino.nomeUnid}</td>
                                    <td>${m.usuarioOrigem.nomeUsu} &rarr; ${m.usuarioDestino.nomeUsu}</td>

                                    <td>${m.observacaoMov}</td>

                                    <!--
                                        Formatação de data usando JSTL fmt.
                                        Responsável pela conversão para padrão brasileiro.
                                    -->
                                     <td> <fmt:formatDate value="${m.dataInsercao}" pattern="dd/MM/yyyy"/></td>
                                    
                                    <td style="text-align: center;">

                                        <!--
                                            Botão de edição.
                                            Envia dados da linha para preenchimento do modal.
                                            Ponto crítico:
                                            valores são passados via parâmetros JS.
                                        -->
                                        <button class="btn-icon" title="Editar" 
                                            onclick="openModalEditar('${m.idMov}', '${m.equipamento.idEquip}', '${m.tipoMovimentacaoMov}', '${m.unidadeOrigem.idUnid}', '${m.unidadeDestino.idUnid}', '${m.usuarioOrigem.idUsu}', '${m.usuarioDestino.idUsu}', '${m.observacaoMov}')">
                                            <i class="fa-solid fa-pen"></i>
                                        </button>

                                        <!-- Exclusão da movimentação -->
                                        <button class="btn-icon delete" title="Excluir" onclick="excluirMovimentacao(${m.idMov})">
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

    <!-- ============================================================
         MODAL DE CADASTRO
         ============================================================ -->
    <div id="modalCadastro" class="modal-overlay">
        <div class="modal-box">
            <div class="modal-header">
                <h3 class="modal-title">Registrar Nova Movimentação</h3>
                <button class="close-modal" onclick="closeModal()">&times;</button>
            </div>

            <div class="modal-body">

                <!--
                    Formulário enviado ao MovimentacaoController.
                    action=adicionar define operação CRUD no backend.
                -->
                <form id="formMovimentacao" action="${pageContext.request.contextPath}/MovimentacaoController" method="post">
                    <input type="hidden" name="action" value="adicionar">

                    <!-- Grid visual dos campos -->
                    <div class="form-grid">

                        <!-- Lista de equipamentos carregada do backend -->
                        <div class="form-group full-width">
                            <label class="form-label">Equipamento - Número Série</label>
                            <select name="id_equip" class="form-input" required>
                                <option value="">Selecione...</option>
                                <c:forEach var="e" items="${equipamentos}">
                                    <option value="${e.idEquip}">${e.nomeEquip} - ${e.numSerieEquip}</option>
                                </c:forEach>
                            </select>
                        </div>

                        <!-- Número de série 
                        <div class="form-group full-width">
	                        <label class="form-label">Número de Série</label>
	                        <input type="text" name="numero_serie" class="form-input" placeholder="Ex: A258M3583" required>
	                    </div>-->

                        <!-- Tipo de movimentação -->
                        <div class="form-group full-width">
                            <label class="form-label">Tipo de Movimentação</label>
                            <select name="tipo_movimentacao" class="form-input" required>
                            	<option value="">Selecione...</option>
                                <option value="Transferencia">Transferência</option>
                                <option value="Transferencia">Empréstimo</option>
                                <option value="Manutencao">Manutenção</option>
                                <option value="Baixa">Devolução</option>
                            </select>
                        </div>

                        <!-- Unidade origem -->
                        <div class="form-group">
                            <label class="form-label">Unidade Origem</label>
                            <select name="id_unidade_origem" class="form-input" required>
                            	<option value="">Selecione...</option>
                                <c:forEach var="un" items="${unidades}">
                                    <option value="${un.idUnid}">${un.nomeUnid}</option>
                                </c:forEach>
                            </select>
                        </div>

                        <!-- Unidade destino -->
                        <div class="form-group">
                            <label class="form-label">Unidade Destino</label>
                            <select name="id_unidade_destino" class="form-input" required>
                            	<option value="">Selecione...</option>
                                <c:forEach var="u" items="${unidades}">
                                    <option value="${u.idUnid}">${u.nomeUnid}</option>
                                </c:forEach>
                            </select>
                        </div>

                        <!-- Usuário origem -->
                        <div class="form-group">
                            <label class="form-label">Usuário Origem</label>
                            <select name="id_usuario_origem" class="form-input" required>
                            	<option value="">Selecione...</option>
                                <c:forEach var="u" items="${usuarios}">
                                    <option value="${u.idUsu}">${u.nomeUsu}</option>
                                </c:forEach>
                            </select>
                        </div>

                        <!-- Usuário destino -->
                        <div class="form-group">
                            <label class="form-label">Usuário Destino</label>
                            <select name="id_usuario_destino" class="form-input" required>
                            	<option value="">Selecione...</option>
                                <c:forEach var="u" items="${usuarios}">
                                    <option value="${u.idUsu}">${u.nomeUsu}</option>
                                </c:forEach>
                            </select>
                        </div>
                    </div>

                    <!-- Observação livre -->
                    <div class="form-group full-width">
                        <label class="form-label">Observação</label>
                        <textarea name="observacao" class="form-input"></textarea>
                    </div>
                        
                </form>
            </div>

            <!-- Botões de ação -->
            <div class="modal-footer">
                <button class="btn btn-outline" onclick="closeModal()">Cancelar</button>
                <button class="btn btn-primary" type="submit" form="formMovimentacao">Salvar</button>
            </div>
        </div>
    </div>

    <!-- ============================================================
         MODAL DE EDIÇÃO
         ============================================================ -->
    <div id="modalEditar" class="modal-overlay">
        <div class="modal-box">
            <div class="modal-header">
                <h3 class="modal-title">Editar Movimentação</h3>
                <button class="close-modal" onclick="closeModalEditar()">&times;</button>
            </div>

            <div class="modal-body">

                <!--
                    Formulário de atualização.
                    action=editar identifica operação no Controller.
                -->
                <form id="formEditarMovimentacao" action="${pageContext.request.contextPath}/MovimentacaoController" method="post">
                    <input type="hidden" name="action" value="editar">
                    <input type="hidden" name="edt_id_mov" id="edit_id_mov">

                    <div class="form-grid">

                        <!-- Equipamento -->
                        <div class="form-group full-width">
                            <label class="form-label">Equipamento - Número Série</label>
                            <select name="id_equip" id="edit_id_equipamento" class="form-input" required>
                                <c:forEach var="e" items="${equipamentos}">
                                    <option value="${e.idEquip}">${e.nomeEquip} - ${e.numSerieEquip}</option>
                                </c:forEach>
                            </select>
                        </div>

                        <!-- Tipo -->
                        <div class="form-group full-width">
                            <label class="form-label">Tipo de Movimentação</label>
                            <select name="tipo_movimentacao" id="edit_tipo_mov" class="form-input" required>
                            	<option value="">Selecione...</option>
                                <option value="Transferência">Transferência</option>
                                <option value="Empréstimo">Empréstimo</option>
                                <option value="Manutenção">Manutenção</option>
                                <option value="Devolução">Devolução</option>
                            </select>
                        </div>
                    

                    <!-- Unidades e usuários -->
                    <div class="form-group">
                            <label class="form-label">Unidade Origem</label>
                            <select name="edit_id_unidade_origem" id="edit_unidade_origem" class="form-input" required>
                            	<option value="">Selecione...</option>
                                <c:forEach var="uno" items="${unidades}">
                                    <option value="${uno.idUnid}">${uno.nomeUnid}</option>
                                </c:forEach>
                            </select>
                        </div>

                        <div class="form-group">
                            <label class="form-label">Unidade Destino</label>
                            <select name="edit_id_unidade_destino" id="edit_unidade_destino" class="form-input" required>
                            	<option value="">Selecione...</option>
                                <c:forEach var="und" items="${unidades}">
                                    <option value="${und.idUnid}">${und.nomeUnid}</option>
                                </c:forEach>
                            </select>
                        </div>

                        <div class="form-group">
                            <label class="form-label">Usuário Origem</label>
                            <select name="edit_id_usuario_origem" id="edit_usuario_origem"class="form-input" required>
                            	<option value="">Selecione...</option>
                                <c:forEach var="uso" items="${usuarios}">
                                    <option value="${uso.idUsu}">${uso.nomeUsu}</option>
                                </c:forEach>
                            </select>
                        </div>

                        <div class="form-group">
                            <label class="form-label">Usuário Destino</label>
                            <select name="edit_id_usuario_destino" id="edit_usuario_destino" class="form-input" required>
                            	<option value="">Selecione...</option>
                                <c:forEach var="u" items="${usuarios}">
                                    <option value="${u.idUsu}">${u.nomeUsu}</option>
                                </c:forEach>
                            </select>
                        </div>
                    
                    <!-- Observação -->
                    <div class="form-group full-width">
                        <label class="form-label">Observação</label>
                        <textarea name="observacao" id="edit_obs" class="form-input"></textarea>
                    </div>
                    </div>
                    </div>
                </form>

                <!-- Ações -->
                <div class="modal-footer">
	                <button class="btn btn-outline" onclick="closeModalEditar()">Cancelar</button>
	                <button class="btn btn-primary" type="submit" form="formEditarMovimentacao">Atualizar</button>
	            </div>
            </div>
        </div>
    </div>
    
  
  	<!-- INCLUIR SCRIPTS COMUNS -->
    <!-- Scripts reutilizáveis do sistema (sidebar, eventos globais, etc.) -->
    <jsp:include page="/includes/scripts-comum.jsp" />
    
    <script>
        /*
         * ============================================================
         * CONTROLE DOS MODAIS
         * ============================================================
         * Responsável por abrir e fechar modais de cadastro e edição.
         */

        const modal = document.getElementById('modalCadastro');
        const modalEditar = document.getElementById('modalEditar');

        function openModal() { modal.classList.add('show'); }
        function closeModal() { modal.classList.remove('show'); }

        /*
         * Preenche os campos do modal de edição.
         *
         * Parâmetros:
         * id             -> ID da movimentação
         * idEquip        -> equipamento associado
         * tipo           -> tipo da movimentação
         * idUnOrigem     -> unidade origem
         * idUnDestino    -> unidade destino
         * idUsOrigem     -> usuário origem
         * idUsDestino    -> usuário destino
         * obs            -> observação registrada
         */
        function openModalEditar(id, idEquip, tipo, idUnOrigem, idUnDestino, idUsOrigem, idUsDestino, obs) {
            document.getElementById('edit_id_mov').value = id;
            document.getElementById('edit_id_equipamento').value = idEquip;
            document.getElementById('edit_tipo_mov').value = tipo;
            
            document.getElementById('edit_unidade_origem').value = idUnOrigem;
            document.getElementById('edit_unidade_destino').value = idUnDestino;
            document.getElementById('edit_usuario_origem').value = idUsOrigem;
            document.getElementById('edit_usuario_destino').value = idUsDestino;
            
            document.getElementById('edit_obs').value = obs;

            // Exibe modal após preenchimento
            modalEditar.classList.add('show');
        }

        function closeModalEditar() { modalEditar.classList.remove('show'); }

        /*
         * ============================================================
         * EXCLUSÃO DE MOVIMENTAÇÃO
         * ============================================================
         *
         * Regra de negócio:
         * - Solicita confirmação do usuário.
         * - Cria formulário dinamicamente.
         * - Envia POST para MovimentacaoController com action=deletar.
         */
        function excluirMovimentacao(idMov) {
            if(confirm("Deseja excluir este registro de movimentação?")) {
                const form = document.createElement("form");
                form.method = "post";
                form.action = "${pageContext.request.contextPath}/MovimentacaoController";

                const action = document.createElement("input");
                action.type = "hidden"; 
                action.name = "action"; 
                action.value = "deletar";

                const id = document.createElement("input");
                id.type = "hidden"; 
                id.name = "id_mov"; 
                id.value = idMov;

                form.appendChild(action);
                form.appendChild(id);
                
                document.body.appendChild(form);
                form.submit();
            }
        }

        /*
         * Fecha modais ao clicar fora da área interna.
         * Estrutura condicional baseada no elemento alvo do evento.
         */
        window.onclick = function(event) {
            if (event.target == modal) closeModal();
            if (event.target == modalEditar) closeModalEditar();
        }
    </script>
    
    <script>
        /*
         * ============================================================
         * AUTO-OCULTAÇÃO DE ALERTAS
         * ============================================================
         * Remove automaticamente mensagens de feedback após 5 segundos.
         * Melhora UX evitando poluição visual.
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
