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
    
    <style>
        .label-filtro-historico {
            display: inline-flex;
            align-items: center;
            gap: 6px;
            font-size: 13px;
            font-weight: 500;
            color: var(--text-secondary, #555);
            cursor: pointer;
            user-select: none;
            white-space: nowrap;
        }
        .label-filtro-historico input[type="checkbox"] {
            width: 15px;
            height: 15px;
            cursor: pointer;
            accent-color: var(--primary, #4f6ef7);
        }
        .label-filtro-historico:hover span { color: var(--primary, #4f6ef7); }
        .btn-icon-historico {
            opacity: 0.28;
            cursor: not-allowed !important;
            pointer-events: none;
        }
    </style>
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
                    <div class="table-title">
                        <c:choose>
                            <c:when test="${exibirTodos}">Histórico Completo de Movimentações</c:when>
                            <c:otherwise>Última Movimentação por Equipamento</c:otherwise>
                        </c:choose>
                    </div>

                    <!-- Ações principais -->
                    <div class="header-actions">

                        <!--
                            Checkbox "Histórico de Movimentações".
                            Desmarcado (padrão): tela carrega apenas a última movimentação de cada equipamento.
                            Marcado: recarrega a página exibindo o histórico completo.
                            Estado preservado via atributo "exibirTodos" enviado pelo controller.
                        -->
                        <label class="label-filtro-historico" title="Marcar para exibir o histórico completo de todas as movimentações">
                            <input type="checkbox"
                                   id="chkHistorico"
                                   onchange="alternarHistorico(this.checked)"
                                   ${exibirTodos ? 'checked' : ''}>
                            <span>Histórico de Movimentações</span>
                        </label>

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
                                        <%--
                                            Regra por linha: "ultimosIds" contém o id_mov mais recente de
                                            cada equipamento. Apenas essa linha tem botões habilitados.
                                            Registros históricos ficam desabilitados visualmente.
                                        --%>
                                        <c:set var="isUltima" value="${ultimosIds.contains(m.idMov)}" />

                                        <c:choose>
                                            <c:when test="${isUltima}">
                                                <button class="btn-icon" title="Editar"
                                                    onclick="openModalEditar('${m.idMov}', '${m.equipamento.idEquip}', '${m.tipoMovimentacaoMov}', '${m.unidadeOrigem.idUnid}', '${m.unidadeDestino.idUnid}', '${m.usuarioOrigem.idUsu}', '${m.usuarioDestino.idUsu}', '${m.observacaoMov}')">
                                                    <i class="fa-solid fa-pen"></i>
                                                </button>
                                                <button class="btn-icon delete" title="Excluir"
                                                    onclick="excluirMovimentacao(${m.idMov})">
                                                    <i class="fa-solid fa-trash"></i>
                                                </button>
                                            </c:when>
                                            <c:otherwise>
                                                <button class="btn-icon btn-icon-historico"
                                                        title="Edição indisponível — não é a última movimentação deste equipamento"
                                                        disabled>
                                                    <i class="fa-solid fa-pen"></i>
                                                </button>
                                                <button class="btn-icon delete btn-icon-historico"
                                                        title="Exclusão indisponível — não é a última movimentação deste equipamento"
                                                        disabled>
                                                    <i class="fa-solid fa-trash"></i>
                                                </button>
                                            </c:otherwise>
                                        </c:choose>
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
                            <%-- Input hidden envia o ID real ao controller --%>
                            <input type="hidden" name="id_equip" id="cad_id_equip">
                            <%-- Input visível com datalist: exibe apenas nome e número de série --%>
                            <input type="text" id="cad_equip_input" class="form-input"
                                   placeholder="Digite para buscar..."
                                   list="listEquipamentos" autocomplete="off" required
                                   oninput="aoSelecionarEquipamento(this.value)">
                            <datalist id="listEquipamentos">
                                <c:forEach var="e" items="${equipamentos}">
                                    <option value="${e.nomeEquip} - ${e.numSerieEquip}"></option>
                                </c:forEach>
                            </datalist>
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
                            <label class="form-label">
                                Unidade Origem
                                <span id="cad_origem_hint" style="font-size:11px; color:#888; font-weight:normal; margin-left:6px; display:none;">
                                    📍 Localização atual
                                </span>
                            </label>
                            <select name="id_unidade_origem" id="cad_unidade_origem" class="form-input" required>
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
                            <%-- Input hidden envia o ID real ao controller --%>
                            <input type="hidden" name="id_equip" id="edit_id_equipamento">
                            <%-- Input visível com datalist: exibe apenas nome e número de série --%>
                            <input type="text" id="edit_equip_input" class="form-input"
                                   placeholder="Digite para buscar..."
                                   list="listEquipamentosEditar" autocomplete="off" required
                                   oninput="resolverIdEquipamento(this.value, 'edit_id_equipamento')">
                            <datalist id="listEquipamentosEditar">
                                <c:forEach var="e" items="${equipamentos}">
                                    <option value="${e.nomeEquip} - ${e.numSerieEquip}"></option>
                                </c:forEach>
                            </datalist>
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
                            <label class="form-label">Unidade Origem - 
                            	<span id="cad_origem_hint" style="font-size:11px; color:#888; font-weight:normal; margin-left:6px; display:inline;">
                                    📍 Localização atual
                                </span>	
                            </label>
                            <select name="edit_id_unidade_origem" id="edit_unidade_origem" class="form-input" disabled>
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
         * MAPA DE EQUIPAMENTOS
         * ============================================================
         * Gerado pelo servidor via JSTL.
         * Chave : "nomeEquip - numSerieEquip" (texto visível no datalist)
         * Valor : idEquip (enviado ao controller pelo input hidden)
         *
         * Necessário porque navegadores descartam atributos data-* das
         * options de <datalist>, impossibilitando leitura via JS.
         */
        const mapaEquipamentos = {};
        <c:forEach var="e" items="${equipamentos}">
        mapaEquipamentos["${e.nomeEquip} - ${e.numSerieEquip}"] = "${e.idEquip}";
        </c:forEach>

        /*
         * Resolve o ID oculto do equipamento a partir do texto selecionado no datalist.
         *
         * inputValue -> texto atual do campo visível
         * hiddenId   -> id do input hidden que receberá o ID do equipamento
         */
        function resolverIdEquipamento(inputValue, hiddenId) {
            const id = mapaEquipamentos.hasOwnProperty(inputValue) ? mapaEquipamentos[inputValue] : '';
            document.getElementById(hiddenId).value = id;
        }

        /*
         * ============================================================
         * SELEÇÃO DE EQUIPAMENTO — REGRA DE NEGÓCIO
         * ============================================================
         * Ao selecionar um equipamento no modal de nova movimentação:
         * 1. Resolve o ID no mapa e preenche o input hidden.
         * 2. Se o ID for válido, consulta via AJAX o controller para
         *    verificar se existe histórico de movimentações.
         * 3. Se existir histórico:
         *    - Preenche "Unidade Origem" com o último destino registrado
         *      (localização atual do equipamento).
         *    - Desabilita o select para evitar alteração indevida.
         *    - Exibe hint informando que é a localização atual.
         * 4. Se não existir histórico:
         *    - Mantém o select de "Unidade Origem" livre e habilitado.
         */
        function aoSelecionarEquipamento(inputValue) {
            // Passo 1: resolve ID no mapa
            resolverIdEquipamento(inputValue, 'cad_id_equip');

            const idEquip = document.getElementById('cad_id_equip').value;
            const selectOrigem = document.getElementById('cad_unidade_origem');
            const hint = document.getElementById('cad_origem_hint');

            // Se ainda não selecionou um equipamento válido, reabilita o select e sai
            if (!idEquip || idEquip.trim() === '') {
                selectOrigem.disabled = false;
                selectOrigem.value = '';
                hint.style.display = 'none';
                return;
            }

            // Passo 2: consulta AJAX ao controller
            const url = '${pageContext.request.contextPath}/MovimentacaoController'
                      + '?action=buscarLocalizacao&id_equip=' + encodeURIComponent(idEquip);

            fetch(url)
                .then(function(resp) {
                    if (!resp.ok) throw new Error('Falha na requisição');
                    return resp.json();
                })
                .then(function(data) {
                    if (data.encontrou) {
                        // Passo 3: equipamento tem histórico — preenche e bloqueia origem
                        selectOrigem.value = data.idUnidade;
                        selectOrigem.disabled = true;
                        hint.style.display = 'inline';
                    } else {
                        // Passo 4: sem histórico — mantém livre
                        selectOrigem.disabled = false;
                        selectOrigem.value = '';
                        hint.style.display = 'none';
                    }
                })
                .catch(function(err) {
                    // Em caso de erro na requisição, libera o campo para não bloquear o usuário
                    console.error('Erro ao buscar localização do equipamento:', err);
                    selectOrigem.disabled = false;
                    selectOrigem.value = '';
                    hint.style.display = 'none';
                });
        }

        /*
         * ============================================================
         * CONTROLE DOS MODAIS
         * ============================================================
         */
        const modal = document.getElementById('modalCadastro');
        const modalEditar = document.getElementById('modalEditar');

        function openModal() {
            // Limpa e reabilita campos ao abrir modal de cadastro
            document.getElementById('cad_equip_input').value = '';
            document.getElementById('cad_id_equip').value = '';
            const selectOrigem = document.getElementById('cad_unidade_origem');
            selectOrigem.disabled = false;
            selectOrigem.value = '';
            document.getElementById('cad_origem_hint').style.display = 'none';
            modal.classList.add('show');
        }
        function closeModal() { modal.classList.remove('show'); }

        /*
         * Preenche os campos do modal de edição.
         *
         * Parâmetros:
         * id          -> ID da movimentação
         * idEquip     -> ID do equipamento associado
         * tipo        -> tipo da movimentação
         * idUnOrigem  -> ID da unidade origem
         * idUnDestino -> ID da unidade destino
         * idUsOrigem  -> ID do usuário origem
         * idUsDestino -> ID do usuário destino
         * obs         -> observação registrada
         */
        function openModalEditar(id, idEquip, tipo, idUnOrigem, idUnDestino, idUsOrigem, idUsDestino, obs) {
            document.getElementById('edit_id_mov').value = id;

            // Preenche campo visível com o texto correspondente ao idEquip
            // e o campo hidden com o ID para envio correto ao controller
            const textoEquip = Object.keys(mapaEquipamentos).find(k => mapaEquipamentos[k] == idEquip) || '';
            document.getElementById('edit_equip_input').value = textoEquip;
            document.getElementById('edit_id_equipamento').value = idEquip;

            document.getElementById('edit_tipo_mov').value = tipo;
            document.getElementById('edit_unidade_origem').value = idUnDestino;
            document.getElementById('edit_unidade_destino').value = "";
            document.getElementById('edit_usuario_origem').value = idUsOrigem;
            document.getElementById('edit_usuario_destino').value = idUsDestino;
            document.getElementById('edit_obs').value = obs;

            modalEditar.classList.add('show');
        }

        function closeModalEditar() { modalEditar.classList.remove('show'); }

        /*
         * ============================================================
         * VALIDAÇÃO ANTES DO ENVIO
         * ============================================================
         * Impede submissão se equipamento não foi selecionado corretamente,
         * evitando NumberFormatException no controller por id_equip vazio.
         *
         * IMPORTANTE: o select de unidade origem pode estar disabled ao
         * submeter (quando equipamento tem histórico). Campos disabled não
         * são enviados pelo browser — por isso o select é reabilitado
         * momentaneamente antes do submit para garantir envio do valor.
         */
        document.addEventListener('DOMContentLoaded', function() {
            document.getElementById('formMovimentacao').addEventListener('submit', function(e) {
                const idEquip = document.getElementById('cad_id_equip').value;
                if (!idEquip || idEquip.trim() === '') {
                    e.preventDefault();
                    alert('Selecione um equipamento válido da lista antes de salvar.');
                    document.getElementById('cad_equip_input').focus();
                    return;
                }
                // Reabilita o select desabilitado para que o valor seja enviado no POST
                const selectOrigem = document.getElementById('cad_unidade_origem');
                if (selectOrigem.disabled) {
                    selectOrigem.disabled = false;
                }
            });

            document.getElementById('formEditarMovimentacao').addEventListener('submit', function(e) {
                const idEquip = document.getElementById('edit_id_equipamento').value;
                if (!idEquip || idEquip.trim() === '') {
                    e.preventDefault();
                    alert('Selecione um equipamento válido da lista antes de atualizar.');
                    document.getElementById('edit_equip_input').focus();
                }
            });
        });

        /*
         * ============================================================
         * EXCLUSÃO DE MOVIMENTAÇÃO
         * ============================================================
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
         * ============================================================
         * ALTERNÂNCIA DO CHECKBOX — HISTÓRICO DE MOVIMENTAÇÕES
         * ============================================================
         * Recarrega a página via GET passando exibirTodos ao controller.
         * true  → lista completa (listarMovimentacoes).
         * false → apenas últimas por equipamento (listarUltima...).
         */
        function alternarHistorico(marcado) {
            window.location.href = '${pageContext.request.contextPath}/MovimentacaoController'
                                 + '?exibirTodos=' + (marcado ? 'true' : 'false');
        }

        /*
         * Fecha modais ao clicar fora da área interna.
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
