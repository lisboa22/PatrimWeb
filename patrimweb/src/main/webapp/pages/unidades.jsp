<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!-- 
    =====================================================================
    Página JSP: Gerenciamento de Unidades - PatrimWeb
    =====================================================================

    PROPÓSITO DA PÁGINA:
    Esta página é responsável pelo gerenciamento das Unidades cadastradas
    no sistema PatrimWeb, permitindo:

    • Listagem das unidades cadastradas
    • Inclusão de novas unidades
    • Edição de unidades existentes
    • Exclusão de unidades
    • Emissão de relatório de unidades

    REGRAS DE NEGÓCIO IMPORTANTES:
    - Os dados são recebidos do backend através do atributo "unidades".
    - As operações CRUD são delegadas ao UnidadeController.
    - Mensagens de sucesso e erro são controladas via sessão HTTP.
    - A data de inserção é formatada via JSTL fmt:formatDate.
    - Exclusões exigem confirmação do usuário.

    INTERAÇÕES COM BACKEND:
    - UnidadeController (CRUD)
    - RelatorioUnidadeController (exportação de relatório)

    TECNOLOGIAS UTILIZADAS:
    - JSP
    - JSTL (Core e Formatting)
    - JavaScript
    - CSS externo do sistema
    =====================================================================
-->

<!-- Define a variável pageTitle para ativar o menu correto -->
<c:set var="pageTitle" value="Unidades" scope="request" />

<!DOCTYPE html>
<html lang="pt-br">
<head>
    <!-- Configurações básicas de responsividade e charset -->
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Gerenciamento de ${pageTitle} - PatrimWeb</title>

    <!-- Bibliotecas externas de ícones e fontes -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600&display=swap" rel="stylesheet">

    <!-- CSS principal do sistema -->
    <link rel="stylesheet" href="css/patrimweb.css">
</head>
<body>

    <!-- Overlay utilizado no modo mobile para fechamento da sidebar -->
    <div class="mobile-overlay" id="mobileOverlay" onclick="toggleSidebar()"></div>

 <!-- INCLUIR SIDEBAR -->
    <!-- Componente reutilizável responsável pela navegação lateral -->
    <jsp:include page="/includes/sidebar.jsp" />

<main class="main-content">
    <header>

            <!-- Botão exibido apenas em telas menores para abrir/fechar sidebar -->
            <button class="menu-toggle" onclick="toggleSidebar()">
                <i class="fa-solid fa-bars"></i>
            </button>

            <!-- Título dinâmico da página -->
            <h1>${pageTitle}</h1>

            <!-- 
                Container responsável por exibir mensagens de feedback ao usuário.
                As mensagens são armazenadas em sessão pelo backend.
            -->
            <div id="container-avisos" >

			    <%-- Mensagem de Erro --%>
                <!-- Exibe alerta caso exista mensagemErro na sessão -->
			    <c:if test="${not empty sessionScope.mensagemErro}">
			        <div class="alerta-custom alerta-erro">
			            <span>
			                <strong>⚠️ Atenção:</strong> ${sessionScope.mensagemErro}
			            </span>

                        <!-- Botão para fechamento manual do alerta -->
			            <button type="button" class="btn-fechar-alerta" onclick="this.parentElement.style.display='none'">&times;</button>
			        </div>

                    <!-- Remove a mensagem após exibição (controle de estado da sessão) -->
			        <% session.removeAttribute("mensagemErro"); %>
			    </c:if>

			    <%-- Mensagem de Sucesso --%>
                <!-- Exibe alerta quando operação foi executada com sucesso -->
			    <c:if test="${not empty sessionScope.mensagemSucesso}">
			        <div class="alerta-custom alerta-sucesso">
			            <span>
			                <strong>✅ Sucesso!</strong> ${sessionScope.mensagemSucesso}
			            </span>
			            <button type="button" class="btn-fechar-alerta" onclick="this.parentElement.style.display='none'">&times;</button>
			        </div>

                    <!-- Limpa atributo da sessão após uso -->
			        <% session.removeAttribute("mensagemSucesso"); %>
			    </c:if>
			</div>

            <!-- Menu do usuário logado (componente reutilizável) -->
            <jsp:include page="/includes/user-menu.jsp" />
        </header>

        <div class="dashboard-container">

            <!-- Container principal da tabela -->
            <div class="table-container">

                <!-- Cabeçalho com ações disponíveis -->
                <div class="table-header-row">
                    <div class="table-title">Unidades Cadastradas</div>

					<div class="header-actions">

                        <!-- Geração de relatório via controller específico -->
					<a href="${pageContext.request.contextPath}/RelatorioUnidadeController">
                        <button class="btn btn-outline">
                            <i class="fa-solid fa-file-export"></i> Relatório
                        </button>
                    </a>

                    <!-- Abertura do modal de cadastro -->
                    <button class="btn btn-primary" onclick="openModal()">
                        <i class="fa-solid fa-plus"></i> Nova Unidade
                    </button>
					</div>
                </div>

                <!-- Tabela responsiva -->
                <div class="table-responsive">
                    <table class="table-unidades">
                        <thead>
                            <tr>
                                <th style="width: 50px;">#ID</th>
                                <th>Nome da Unidade</th>
                                <th>Telefone</th>
                                <th>Email</th>
                                <th>Data Inserção</th>
                                <th style="text-align: center;">Ações</th>
                            </tr>
                        </thead>
                        <tbody>

                            <!-- 
                                Estrutura de repetição JSTL.
                                Percorre a coleção "unidades" enviada pelo backend.
                                Cada objeto representa uma entidade Unidade.
                            -->
                            <c:forEach var="u" items="${unidades}">
                                <tr>
                                    <td>${u.idUnid}</td>
                                    <td>${u.nomeUnid}</td>
                                    <td>${u.telefoneUnid}</td>
                                    <td>${u.emailUnid}</td>

                                    <!-- 
                                        Formatação da data utilizando JSTL fmt.
                                        Regra de apresentação: padrão brasileiro dd/MM/yyyy.
                                    -->
                                     <td> <fmt:formatDate value="${u.dataInsercao}" pattern="dd/MM/yyyy"/></td>

                                    <td style="text-align: center;">

                                        <!-- Botão de edição: envia dados da linha para o modal -->
                                        <button class="btn-icon" title="Editar" onclick="openModalEditar(
                                                                                            '${u.idUnid}',
                                                                                            '${u.nomeUnid}',
                                                                                            '${u.telefoneUnid}',
                                                                                            '${u.emailUnid}',
                                                                                            '${u.enderecoUnid}'
                                                                                        )">
                                            <i class="fa-solid fa-pen"></i>
                                        </button>

                                        <!-- Exclusão controlada via confirmação -->
                                        <button class="btn-icon delete" 
                                                title="Excluir"
                                                onclick="excluirUnidade(${u.idUnid})">
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

    <!-- ================= MODAL DE CADASTRO ================= -->
    <!-- Responsável por cadastrar novas unidades -->
    <div id="modalCadastro" class="modal-overlay">
        <div class="modal-box">
            <div class="modal-header">
                <h3 class="modal-title">Cadastrar Nova Unidade</h3>
                <button class="close-modal" onclick="closeModal()">&times;</button>
            </div>

            <div class="modal-body">

                <!-- 
                    Formulário enviado ao UnidadeController.
                    action=adicionar define operação de inserção.
                -->
                <form id="formUnidade" action="${pageContext.request.contextPath}/UnidadeController" method="post">
                    <input type="hidden" name="action" value="adicionar">

                    <div class="form-group full-width">
                        <label class="form-label">Nome</label>
                        <!-- Campo obrigatório conforme regra de negócio -->
                        <input type="text" name="nome_unid" class="form-input" placeholder="Ex: Matriz Administrativa" required>
                    </div>

                    <div class="form-grid">
                        <div class="form-group">
                            <label class="form-label">Telefone</label>
                            <input type="tel" name="telefone_unid" id="telefone_unid" class="form-input" placeholder="(00) 0000-0000" maxlength="15">
                        </div>
                        <div class="form-group">
                            <label class="form-label">E-mail</label>
                            <input type="email" name="email_unid" class="form-input" placeholder="unidade@email.com">
                        </div>
                    </div>

                    <div class="form-group full-width">
                        <label class="form-label">Endereço</label>
                        <textarea name="endereco_unid" class="form-input" placeholder="Rua, Número, Bairro, Cidade - UF"></textarea>
                    </div>
                </form>
            </div>

            <div class="modal-footer">
                <button class="btn btn-outline" onclick="closeModal()">Cancelar</button>

                <!-- Submissão externa vinculada ao formulário -->
                <button class="btn btn-primary" type="submit" form="formUnidade">Salvar Unidade</button>
            </div>
        </div>
    </div>

    <!-- ================= MODAL DE EDIÇÃO ================= -->
    <!-- Modal utilizado para alteração de registros existentes -->
    <div id="modalEditar" class="modal-overlay">
        <div class="modal-box">
            <div class="modal-header">
                <h3 class="modal-title">Editar Unidade</h3>
                <button class="close-modal" onclick="closeModalEditar()">&times;</button>
            </div>

            <div class="modal-body">

                <!-- action=editar determina operação de atualização -->
                <form id="formEditarUnidade" action="${pageContext.request.contextPath}/UnidadeController" method="post">
                    <input type="hidden" name="action" value="editar">

                    <!-- Identificador necessário para update no banco -->
                    <input type="hidden" name="id_unid" id="edit_id_unid">

                    <div class="form-group full-width">
                        <label class="form-label">Nome</label>
                        <input type="text" name="nome_unid" id="edit_nome_unid" class="form-input" required>
                    </div>

                    <div class="form-grid">
                        <div class="form-group">
                            <label class="form-label">Telefone</label>
                            <input type="tel" name="telefone_unid" id="edit_telefone_unid" class="form-input" maxlength="15">
                        </div>
                        <div class="form-group">
                            <label class="form-label">E-mail</label>
                            <input type="email" name="email_unid" id="edit_email_unid" class="form-input">
                        </div>
                    </div>

                    <div class="form-group full-width">
                        <label class="form-label">Endereço</label>
                        <textarea name="endereco_unid" id="edit_endereco_unid" class="form-input"></textarea>
                    </div>
                </form>
            </div>

            <div class="modal-footer">
                <button class="btn btn-outline" onclick="closeModalEditar()">Cancelar</button>
                <button class="btn btn-primary" type="submit" form="formEditarUnidade">Salvar Alterações</button>
            </div>
        </div>
    </div>

    <!-- Scripts compartilhados do sistema -->
    <jsp:include page="/includes/scripts-comum.jsp" />

    <script>
        /*
            ===================================================================
            CONTROLE DOS MODAIS
            Responsável por abrir/fechar modais de cadastro e edição.
            ===================================================================
        */
        const modal = document.getElementById('modalCadastro');
        const modalEditar = document.getElementById('modalEditar');

        function openModal() { modal.classList.add('show'); }
        function closeModal() { modal.classList.remove('show'); }

        /*
            Fecha o modal ao clicar fora da área interna.
            Ponto crítico de UX para evitar travamento visual.
        */
        window.onclick = function(event) {
            if (event.target == modal) closeModal();
            if (event.target == modalEditar) closeModalEditar();
        }

        /*
            Controle de fechamento automático do menu do usuário
            quando ocorre clique fora do container.
        */
        window.addEventListener('click', function(e) {
            const container = document.querySelector('.user-menu-container');
            if (!container.contains(e.target)) {
                document.getElementById('userDropdown').classList.remove('show');
            }
        });

        /*
            ===================================================================
            MÁSCARA DE TELEFONE
            Regra de apresentação aplicada em tempo real.
            Remove caracteres não numéricos e aplica padrão brasileiro.
            ===================================================================
        */
        function aplicarMascaraTelefone(el) {
            el.addEventListener('input', function(e) {
                let value = e.target.value.replace(/\D/g, "");
                value = value.replace(/^(\d{2})(\d)/g, "($1) $2");
                value = value.replace(/(\d)(\d{4})$/, "$1-$2");
                e.target.value = value;
            });
        }

        aplicarMascaraTelefone(document.getElementById('telefone_unid'));
        aplicarMascaraTelefone(document.getElementById('edit_telefone_unid'));

        /*
            Preenche o modal de edição com os dados selecionados.
            Evita nova consulta ao servidor (otimização de UX).
        */
        function openModalEditar(id, nome, telefone, email, endereco) {
            document.getElementById('edit_id_unid').value = id;
            document.getElementById('edit_nome_unid').value = nome;
            document.getElementById('edit_telefone_unid').value = telefone;
            document.getElementById('edit_email_unid').value = email;
            document.getElementById('edit_endereco_unid').value = endereco;
            modalEditar.classList.add('show');
        }

        function closeModalEditar() { modalEditar.classList.remove('show'); }

        /*
            ===================================================================
            EXCLUSÃO DE UNIDADE
            ===================================================================
            - Solicita confirmação do usuário.
            - Cria dinamicamente um formulário POST.
            - Envia requisição ao UnidadeController com action=deletar.
            - Evita uso de GET para operações destrutivas.
        */
        function excluirUnidade(idUnid) {
            if (confirm("Tem certeza que deseja excluir esta unidade?")) {
                const form = document.createElement("form");
                form.method = "post";
                form.action = "${pageContext.request.contextPath}/UnidadeController";

                const actionInput = document.createElement("input");
                actionInput.type = "hidden";
                actionInput.name = "action";
                actionInput.value = "deletar";

                const idInput = document.createElement("input");
                idInput.type = "hidden";
                idInput.name = "id_unid";
                idInput.value = idUnid;

                form.appendChild(actionInput);
                form.appendChild(idInput);
                document.body.appendChild(form);
                form.submit();
            }
        }
    </script>

    <script>
        /*
            Remove automaticamente alertas exibidos após 5 segundos.
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
