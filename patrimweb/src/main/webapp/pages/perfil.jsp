<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!--
    ============================================================================
    PÁGINA JSP: GERENCIAMENTO DE PERFIS
    ----------------------------------------------------------------------------
    Propósito:
    Interface de gerenciamento de perfis de acesso do sistema PatrimWeb.

    Responsabilidades:
    - Exibir lista de perfis cadastrados
    - Permitir cadastro de novos perfis
    - Permitir edição de perfis existentes
    - Permitir exclusão de perfis
    - Exibir mensagens de sucesso e erro vindas da sessão

    Observações:
    - Acessível somente por ADMINISTRADOR (controle no PerfilController).
    - Os dados são fornecidos pelo Controller via atributo "listaPerfis".
    ============================================================================
-->

<c:set var="pageTitle" value="Perfis" scope="request" />

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
                    <div class="table-title">Perfis Cadastrados</div>
                    <div class="header-actions">
                        <button class="btn btn-primary" onclick="openModal()">
                            <i class="fa-solid fa-plus"></i> Novo Perfil
                        </button>
                    </div>
                </div>

                <div class="table-responsive">
                    <table class="table-perfis">
                        <thead>
                            <tr>
                                <th style="width: 60px;">#ID</th>
                                <th>Nome do Perfil</th>
                                <th style="text-align: center;">Ações</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${empty perfis}">
                                    <tr>
                                        <td colspan="3" style="text-align: center; color: var(--text-muted); padding: 30px;">
                                            <i class="fa-solid fa-shield-halved" style="font-size: 24px; margin-bottom: 8px; display: block;"></i>
                                            Nenhum perfil cadastrado
                                        </td>
                                    </tr>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="p" items="${perfis}">
                                        <tr>
                                            <td>${p.idPerfil}</td>
                                            <td>
                                                <span class="badge-perfil">${p.nomePerfil}</span>
                                            </td>
                                            <td style="text-align: center;">
                                                <button class="btn-icon" title="Editar"
                                                    onclick="openModalEditar('${p.idPerfil}', '${p.nomePerfil}')">
                                                    <i class="fa-solid fa-pen"></i>
                                                </button>
                                                <button class="btn-icon delete" title="Excluir"
                                                    onclick="excluirPerfil(${p.idPerfil})">
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
                <h3 class="modal-title">Cadastrar Novo Perfil</h3>
                <button class="close-modal" onclick="closeModal()">&times;</button>
            </div>

            <div class="modal-body">
                <form id="formPerfil" action="${pageContext.request.contextPath}/PerfilController" method="post" autocomplete="off">
                    <input type="hidden" name="action" value="adicionar">

                    <div class="form-group full-width">
                        <label class="form-label">Nome do Perfil</label>
                        <input type="text" name="nome_perfil" class="form-input"
                               placeholder="Ex: ADMINISTRADOR, OPERADOR, TÉCNICO" required>
                    </div>
                </form>
            </div>

            <div class="modal-footer">
                <button class="btn btn-outline" onclick="closeModal()">Cancelar</button>
                <button class="btn btn-primary" type="submit" form="formPerfil">
                    <i class="fa-solid fa-floppy-disk"></i> Salvar Perfil
                </button>
            </div>
        </div>
    </div>


    <!-- ========================= MODAL EDIÇÃO ========================= -->
    <div id="modalEditar" class="modal-overlay">
        <div class="modal-box">
            <div class="modal-header">
                <h3 class="modal-title">Editar Perfil</h3>
                <button class="close-modal" onclick="closeModalEditar()">&times;</button>
            </div>

            <div class="modal-body">
                <form id="formEditarPerfil" action="${pageContext.request.contextPath}/PerfilController" method="post" autocomplete="off">
                    <input type="hidden" name="action" value="editar">
                    <input type="hidden" name="id_perfil" id="edit_id_perfil">

                    <div class="form-group full-width">
                        <label class="form-label">Nome do Perfil</label>
                        <input type="text" name="nome_perfil" id="edit_nome_perfil"
                               class="form-input" required>
                    </div>
                </form>
            </div>

            <div class="modal-footer">
                <button class="btn btn-outline" onclick="closeModalEditar()">Cancelar</button>
                <button class="btn btn-primary" type="submit" form="formEditarPerfil">
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

        function openModalEditar(id, nome) {
            document.getElementById('edit_id_perfil').value  = id;
            document.getElementById('edit_nome_perfil').value = nome;
            modalEditar.classList.add('show');
        }

        function closeModalEditar() {
            modalEditar.classList.remove('show');
        }

        window.addEventListener('click', function(e) {
            if (e.target === modalEditar) closeModalEditar();
        });

        // ── Exclusão ──
        function excluirPerfil(id) {
            if (confirm('Tem certeza que deseja excluir este perfil?\nAtenção: perfis vinculados a usuários não poderão ser excluídos.')) {
                const form = document.createElement('form');
                form.method = 'post';
                form.action = '${pageContext.request.contextPath}/PerfilController';

                const acao = document.createElement('input');
                acao.type = 'hidden'; acao.name = 'action'; acao.value = 'excluir';

                const idInput = document.createElement('input');
                idInput.type = 'hidden'; idInput.name = 'id_perfil'; idInput.value = id;

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
