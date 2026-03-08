<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!--
    ============================================================================
    PÁGINA JSP: GERENCIAMENTO DE PERMISSÕES
    ----------------------------------------------------------------------------
    Propósito:
    Interface de gerenciamento de permissões por módulo do sistema PatrimWeb.

    Responsabilidades:
    - Exibir lista de permissões cadastradas
    - Permitir cadastro de novas permissões
    - Permitir edição de permissões existentes
    - Permitir exclusão de permissões
    - Exibir mensagens de sucesso e erro vindas da sessão

    Observações:
    - Acessível somente por ADMINISTRADOR (controle no PermissaoController).
    - Os dados são fornecidos pelo Controller via atributo "listaPermissoes".
    ============================================================================
-->

<c:set var="pageTitle" value="Permissões" scope="request" />

<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Gerenciamento de ${pageTitle} - PatrimWeb</title>

    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="css/patrimweb.css">
</head>
<body>

    <div class="mobile-overlay" id="mobileOverlay" onclick="toggleSidebar()"></div>

    <jsp:include page="/includes/sidebar.jsp" />

    <main class="main-content">
        <header>
            <button class="menu-toggle" onclick="toggleSidebar()">
                <i class="fa-solid fa-bars"></i>
            </button>

            <h1>${pageTitle}</h1>

            <div id="container-avisos">
                <c:if test="${not empty sessionScope.mensagemErro}">
                    <div class="alerta-custom alerta-erro">
                        <span><strong>⚠️ Atenção:</strong> ${sessionScope.mensagemErro}</span>
                        <button type="button" class="btn-fechar-alerta" onclick="this.parentElement.style.display='none'">&times;</button>
                    </div>
                    <% session.removeAttribute("mensagemErro"); %>
                </c:if>

                <c:if test="${not empty sessionScope.mensagemSucesso}">
                    <div class="alerta-custom alerta-sucesso">
                        <span><strong>✅ Sucesso!</strong> ${sessionScope.mensagemSucesso}</span>
                        <button type="button" class="btn-fechar-alerta" onclick="this.parentElement.style.display='none'">&times;</button>
                    </div>
                    <% session.removeAttribute("mensagemSucesso"); %>
                </c:if>
            </div>

            <jsp:include page="/includes/user-menu.jsp" />
        </header>

        <div class="dashboard-container">
            <div class="table-container">
                <div class="table-header-row">
                    <div class="table-title">Permissões Cadastradas</div>
                    <div class="header-actions">
                        <button class="btn btn-primary" onclick="openModal()">
                            <i class="fa-solid fa-plus"></i> Nova Permissão
                        </button>
                    </div>
                </div>

                <div class="table-responsive">
                    <table class="table-permissoes">
                        <thead>
                            <tr>
                                <th style="width: 60px;">#ID</th>
                                <th>Módulo</th>
                                <th>Ação</th>
                                <th>Descrição</th>
                                <th style="text-align: center;">Ações</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${empty permissoes}">
                                    <tr>
                                        <td colspan="5" style="text-align: center; color: var(--text-muted); padding: 30px;">
                                            <i class="fa-solid fa-key" style="font-size: 24px; margin-bottom: 8px; display: block;"></i>
                                            Nenhuma permissão cadastrada
                                        </td>
                                    </tr>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="p" items="${permissoes}">
                                        <tr>
                                            <td>${p.idPermissao}</td>
                                            <td>
                                                <span class="badge-modulo">${p.modulo}</span>
                                            </td>
                                            <td>${p.acao}</td>
                                            <td>${p.descricao}</td>
                                            <td style="text-align: center;">
                                                <button class="btn-icon" title="Editar"
                                                    onclick="openModalEditar('${p.idPermissao}', '${p.modulo}', '${p.acao}', '${p.descricao}')">
                                                    <i class="fa-solid fa-pen"></i>
                                                </button>
                                                <button class="btn-icon delete" title="Excluir"
                                                    onclick="excluirPermissao(${p.idPermissao})">
                                                    <i class="fa-solid fa-trash"></i>
                                                </button>
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


    <!-- ========================= MODAL CADASTRO ========================= -->
    <div id="modalCadastro" class="modal-overlay">
        <div class="modal-box">
            <div class="modal-header">
                <h3 class="modal-title">Cadastrar Nova Permissão</h3>
                <button class="close-modal" onclick="closeModal()">&times;</button>
            </div>

            <div class="modal-body">
                <form id="formPermissao" action="${pageContext.request.contextPath}/PermissaoController" method="post" autocomplete="off">
                    <input type="hidden" name="action" value="adicionar">

                    <div class="form-grid">
                        <div class="form-group">
                            <label class="form-label">Módulo</label>
                            <input type="text" name="modulo" class="form-input"
                                   placeholder="Ex: Equipamentos, Usuários" required>
                        </div>
                        <div class="form-group full-width">
						    <label class="form-label">Ação</label>
						    <select name="acao_permissao" class="form-input" required>
						        <option value="">Selecione...</option>
                                <option value="VISUALIZAR">VISUALIZAR</option>
                                <option value="INSERIR">INSERIR</option>
                                <option value="EDITAR">EDITAR</option>
                                <option value="EXCLUIR">EXCLUIR</option>
						    </select>
						</div>
                    </div>

                    <div class="form-group full-width">
                        <label class="form-label">Descrição</label>
                        <textarea name="descricao" class="form-input"
                                  placeholder="Descreva o que esta permissão permite fazer"></textarea>
                    </div>
                </form>
            </div>

            <div class="modal-footer">
                <button class="btn btn-outline" onclick="closeModal()">Cancelar</button>
                <button class="btn btn-primary" type="submit" form="formPermissao">
                    <i class="fa-solid fa-floppy-disk"></i> Salvar Permissão
                </button>
            </div>
        </div>
    </div>


    <!-- ========================= MODAL EDIÇÃO ========================= -->
    <div id="modalEditar" class="modal-overlay">
        <div class="modal-box">
            <div class="modal-header">
                <h3 class="modal-title">Editar Permissão</h3>
                <button class="close-modal" onclick="closeModalEditar()">&times;</button>
            </div>

            <div class="modal-body">
                <form id="formEditarPermissao" action="${pageContext.request.contextPath}/PermissaoController" method="post" autocomplete="off">
                    <input type="hidden" name="action" value="editar">
                    <input type="hidden" name="id_permissao" id="edit_id_permissao">

                    <div class="form-grid">
                        <div class="form-group">
                            <label class="form-label">Módulo</label>
                            <input type="text" name="modulo" id="edit_modulo"
                                   class="form-input" required>
                        </div>
                        <div class="form-group full-width">
						    <label class="form-label">Ação</label>
						    <select name="acao" id="edit_acao" class="form-input" required>
                                <option value="VISUALIZAR">VISUALIZAR</option>
                                <option value="INSERIR">INSERIR</option>
                                <option value="EDITAR">EDITAR</option>
                                <option value="EXCLUIR">EXCLUIR</option>
						    </select>
						</div>
                    </div>

                    <div class="form-group full-width">
                        <label class="form-label">Descrição</label>
                        <textarea name="descricao" id="edit_descricao" class="form-input"></textarea>
                    </div>
                </form>
            </div>

            <div class="modal-footer">
                <button class="btn btn-outline" onclick="closeModalEditar()">Cancelar</button>
                <button class="btn btn-primary" type="submit" form="formEditarPermissao">
                    <i class="fa-solid fa-floppy-disk"></i> Salvar Alterações
                </button>
            </div>
        </div>
    </div>


    <jsp:include page="/includes/scripts-comum.jsp" />

    <script>
        // ── Modal cadastro ──
        const modal = document.getElementById('modalCadastro');
        function openModal()  { modal.classList.add('show'); }
        function closeModal() { modal.classList.remove('show'); }
        window.onclick = function(e) { if (e.target === modal) closeModal(); }

        // ── Modal edição ──
        const modalEditar = document.getElementById('modalEditar');

        function openModalEditar(id, modulo, acao, descricao) {
            document.getElementById('edit_id_permissao').value = id;
            document.getElementById('edit_modulo').value       = modulo;
            document.getElementById('edit_acao').value         = acao;
            document.getElementById('edit_descricao').value    = descricao;
            modalEditar.classList.add('show');
        }

        function closeModalEditar() {
            modalEditar.classList.remove('show');
        }

        window.addEventListener('click', function(e) {
            if (e.target === modalEditar) closeModalEditar();
        });

        // ── Exclusão ──
        function excluirPermissao(id) {
            if (confirm('Tem certeza que deseja excluir esta permissão?')) {
                const form = document.createElement('form');
                form.method = 'post';
                form.action = '${pageContext.request.contextPath}/PermissaoController';

                const acao = document.createElement('input');
                acao.type = 'hidden'; acao.name = 'action'; acao.value = 'excluir';

                const idInput = document.createElement('input');
                idInput.type = 'hidden'; idInput.name = 'id_permissao'; idInput.value = id;

                form.appendChild(acao);
                form.appendChild(idInput);
                document.body.appendChild(form);
                form.submit();
            }
        }

        // ── Auto-dismiss alertas ──
        document.addEventListener('DOMContentLoaded', function() {
            document.querySelectorAll('.alerta-custom').forEach(function(a) {
                setTimeout(function() {
                    a.style.transition = 'opacity 0.6s ease';
                    a.style.opacity = '0';
                    setTimeout(() => a.remove(), 600);
                }, 5000);
            });
        });

        // ── Fecha dropdown usuário ao clicar fora ──
        window.addEventListener('click', function(e) {
            const container = document.querySelector('.user-menu-container');
            const dropdown  = document.getElementById('userDropdown');
            if (container && !container.contains(e.target)) {
                dropdown.classList.remove('show');
            }
        });
    </script>

</body>
</html>
