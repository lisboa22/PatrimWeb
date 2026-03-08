<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!-- 
    ============================================================================
    PÁGINA JSP: GERENCIAMENTO DE USUÁRIOS
    ----------------------------------------------------------------------------
    Propósito:
    Esta página é responsável pela interface de gerenciamento de usuários
    do sistema PatrimWeb.

    Responsabilidades principais:
    - Exibir lista de usuários cadastrados
    - Permitir cadastro de novos usuários
    - Permitir edição de usuários existentes
    - Permitir exclusão de usuários
    - Exibir mensagens de sucesso e erro vindas da sessão
    - Integrar com UsuarioController para operações CRUD

    Observações:
    - Os dados são fornecidos pelo Controller via atributo "usuarios".
    - Operações persistentes (CRUD) são realizadas no backend.
    ============================================================================
-->

<!-- Define a variável pageTitle para ativar o menu correto -->
<!-- Regra de negócio: a sidebar utiliza essa variável para destacar o módulo ativo -->
<c:set var="pageTitle" value="Usuários" scope="request" />


<!DOCTYPE html>

<html lang="pt-br">

<head>

    <!-- Configuração básica de charset e responsividade -->
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <!-- Título dinâmico baseado no módulo -->
    <title>Gerenciamento de ${pageTitle} - PatrimWeb</title>

    <!-- Bibliotecas visuais externas -->
    <!-- Font Awesome: ícones do sistema -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">

    <!-- Fonte padrão da interface -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600&display=swap" rel="stylesheet">

    <!-- CSS principal do sistema -->
    <link rel="stylesheet" href="css/patrimweb.css">

    <style>
        /* ─── Campo de senha com botão mostrar/ocultar ─── */
        .input-senha-wrapper {
            position: relative;
            display: flex;
            align-items: center;
        }
        .input-senha-wrapper .form-input {
            flex: 1;
            padding-right: 2.5rem;
        }
        .btn-toggle-senha {
            position: absolute;
            right: 0.65rem;
            background: none;
            border: none;
            cursor: pointer;
            color: #6b7280;
            padding: 0.25rem;
            display: flex;
            align-items: center;
            transition: color 0.2s;
        }
        .btn-toggle-senha:hover { color: #374151; }

        /* ─── Divisor de seção no modal ─── */
        .form-section-divider {
            grid-column: 1 / -1;
            display: flex;
            align-items: center;
            gap: 0.75rem;
            margin: 0.5rem 0 0.25rem;
            color: #6b7280;
            font-size: 0.8rem;
            font-weight: 600;
            text-transform: uppercase;
            letter-spacing: 0.05em;
        }
        .form-section-divider::before,
        .form-section-divider::after {
            content: '';
            flex: 1;
            height: 1px;
            background-color: #e5e7eb;
        }
        .form-section-divider small {
            font-size: 0.72rem;
            font-weight: 400;
            text-transform: none;
            color: #9ca3af;
        }

        /* ─── Mensagem de erro de confirmação de senha ─── */
        .msg-erro-senha {
            color: #dc2626;
            font-size: 0.78rem;
            margin-top: 0.3rem;
            display: block;
        }
    </style>

</head>
<body>

    <!-- Overlay utilizado no modo mobile para fechamento da sidebar -->
    <!-- Interação controlada por função toggleSidebar() definida em scripts comuns -->
    <div class="mobile-overlay" id="mobileOverlay" onclick="toggleSidebar()"></div>

 <!-- INCLUIR SIDEBAR -->
 <!-- Componente reutilizável contendo navegação principal do sistema -->
    <jsp:include page="/includes/sidebar.jsp" />

<main class="main-content">
    <header>

            <!-- Botão responsável por abrir/fechar sidebar em dispositivos móveis -->
            <button class="menu-toggle" onclick="toggleSidebar()">
                <i class="fa-solid fa-bars"></i>
            </button>
            
            
            <!-- Título da página renderizado dinamicamente -->
            <h1>${pageTitle}</h1>
            
            <div id="container-avisos" >

			    <%-- 
			        Exibição de mensagens de erro armazenadas na sessão.
			        Regra de negócio:
			        Controllers enviam mensagens via sessionScope após operações CRUD.
			        Após exibir, a mensagem é removida para evitar reapresentação.
			    --%>
			    <c:if test="${not empty sessionScope.mensagemErro}">
			        <div class="alerta-custom alerta-erro">
			            <span>
			                <strong>⚠️ Atenção:</strong> ${sessionScope.mensagemErro}
			            </span>
			            <!-- Permite fechar manualmente o alerta -->
			            <button type="button" class="btn-fechar-alerta" onclick="this.parentElement.style.display='none'">&times;</button>
			        </div>
			        <% session.removeAttribute("mensagemErro"); %>
			    </c:if>
			
			    <%-- 
			        Exibição de mensagens de sucesso vindas do backend.
			        Indica conclusão correta de operações como cadastro, edição ou exclusão.
			    --%>
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
            <!-- Contém informações do usuário logado e opções de conta -->
            <jsp:include page="/includes/user-menu.jsp" />
        </header>

        <div class="dashboard-container">
            
            <div class="table-container">
                <div class="table-header-row">

                    <!-- Título da tabela -->
                    <div class="table-title">Usuários Cadastrados</div>

					<div class="header-actions">

                        <!-- Geração de relatório via controller -->
                        <!-- Interação backend responsável por exportação de dados -->
					    <a href="${pageContext.request.contextPath}/RelatorioUsuarioController">
                            <button class="btn btn-outline">
                                <i class="fa-solid fa-file-export"></i> Relatório
                            </button>
                        </a>

                        <!-- Abre modal de cadastro -->
                        <button class="btn btn-primary" onclick="openModal()">
                            <i class="fa-solid fa-plus"></i> Novo Usuário
                        </button>

					</div>
                </div>

                <div class="table-responsive">
                    <table>
                        <thead>
                            <tr>
                                <th style="width: 50px;">#ID</th>
                                <th>Nome</th>
                                <th>CPF</th>
                                <th>Email</th>
                                <th>Telefone</th>
                                <th>Data Inserção</th>
                                <th style="text-align: center;">Ações</th>
                            </tr>
                        </thead>
                        <tbody>

						    <%-- 
						        Estrutura de repetição JSTL:
						        Percorre a coleção "usuarios" enviada pelo Controller.
						        Cada objeto representa um registro recuperado do banco.
						    --%>
						    <c:forEach var="u" items="${usuarios}">
						        <tr>

                                    <!-- Exibição direta dos atributos do modelo -->
						            <td>${u.idUsu}</td>
						            <td>${u.nomeUsu}</td>
						            <td>${u.cpfUsu}</td>
						            <td>${u.emailUsu}</td>
						            <td>${u.telefoneUsu}</td>

                                    <!-- 
                                        Formatação de data utilizando JSTL fmt
                                        Regra de apresentação:
                                        padroniza data no formato brasileiro.
                                    -->
						            <td>
                                        <fmt:formatDate value="${u.dataInsercao}" pattern="dd/MM/yyyy"/>
                                    </td>

						            <td style="text-align: center;">

                                        <!-- Botão editar:
                                             Envia dados atuais para preenchimento automático do modal -->
						            	<button class="btn-icon" title="Editar" onclick="openModalEditar(
																					        '${u.idUsu}',
																					        '${u.nomeUsu}',
																					        '${u.cpfUsu}',
																					        '${u.emailUsu}',
																					        '${u.telefoneUsu}',
																					        '${u.enderecoUsu}',
																					        '${u.perfilUsu.idPerfil}'
																					    )">
					                        <i class="fa-solid fa-pen"></i>
					                    </button>

                                        <!-- Botão excluir:
                                             Dispara confirmação antes de enviar requisição POST -->
						                <button class="btn-icon delete" 
										        title="Excluir"
										        onclick="excluirUsuario(${u.idUsu})">
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

    <!-- ========================= MODAL CADASTRO ========================= -->
    <!-- Responsável pela criação de novos usuários -->
    <div id="modalCadastro" class="modal-overlay">
        <div class="modal-box">
            <div class="modal-header">
                <h3 class="modal-title">Cadastrar Novo Usuário</h3>
                <button class="close-modal" onclick="closeModal()">&times;</button>
            </div>
            
            <div class="modal-body">

                <!-- 
                    Formulário enviado ao UsuarioController.
                    Regra de negócio:
                    O campo hidden "action" determina qual operação o controller executará.
                -->
                <form id="formUsuario" action="/patrimweb/UsuarioController" method="post" autocomplete="off">
                
    			<input type="hidden" name="action" value="adicionar">
    
                    <div class="form-group full-width">
                        <label class="form-label">Nome</label>
                        <!-- Validação HTML obrigatória -->
                        <input type="text" name="nome_usu" class="form-input" placeholder="Ex: João da Silva" required>
                    </div>

                    <div class="form-grid">
                        <div class="form-group">
                            <label class="form-label">CPF</label>
                            <!-- Campo com máscara aplicada via JavaScript -->
                            <input type="text" name="cpf_usu" id="cpf_usu" class="form-input" placeholder="000.000.000-00" maxlength="14" required>
                        </div>
                        <div class="form-group">
                            <label class="form-label">Telefone</label>
                            <input type="tel" name="telefone_usu" id="telefone_usu" class="form-input" placeholder="(00) 00000-0000" maxlength="15">
                        </div>
                    </div>

                    <div class="form-group full-width">
                        <label class="form-label">E-mail</label>
                        <input type="email" name="email_usu" class="form-input" placeholder="usuario@email.com" required autocomplete="off">
                    </div>

                    <div class="form-group full-width">
                        <label class="form-label">Endereço</label>
                        <textarea name="endereco_usu" class="form-input" placeholder="Rua, Número, Bairro, Cidade - UF"></textarea>
                    </div>
                    
                    <div class="form-group full-width">
                        <label class="form-label">Senha</label>
                        <!-- Campo obrigatório no cadastro. type=password oculta a digitação. -->
                        <div class="input-senha-wrapper">
                            <input type="password" name="senha_usu" id="senha_usu" class="form-input" placeholder="Mínimo 6 caracteres" required autocomplete="new-password">
                            <button type="button" class="btn-toggle-senha" onclick="toggleSenha('senha_usu', 'icone-senha-add')" tabindex="-1">
                                <i id="icone-senha-add" class="fa-solid fa-eye"></i>
                            </button>
                        </div>
                    </div>
                    
                    <div class="form-group full-width">
					    <label class="form-label">Perfil</label>
					    <select name="id_perfil" class="form-input" required>
					        <option value="">Selecione...</option>
					        <c:forEach var="p" items="${perfis}">
					            <option value="${p.idPerfil}">${p.nomePerfil}</option>
					        </c:forEach>
					    </select>
					</div>
                    
                </form>
            </div>
            
            <div class="modal-footer">
                <button class="btn btn-outline" onclick="closeModal()">Cancelar</button>
                <!-- Submit vinculado ao formulário -->
                <button class="btn btn-primary" type="submit" form="formUsuario">Salvar Usuário</button>
            </div>
        </div>
    </div>
    
    
    <!-- ========================= MODAL EDIÇÃO ========================= -->
    <!-- Responsável por alterar dados existentes -->
    <div id="modalEditar" class="modal-overlay">
    <div class="modal-box">
        <div class="modal-header">
            <h3 class="modal-title">Editar Usuário</h3>
            <button class="close-modal" onclick="closeModalEditar()">&times;</button>
        </div>
        
        <div class="modal-body">

            <!-- Formulário de edição -->
            <form id="formEditarUsuario" action="/patrimweb/UsuarioController" method="post" autocomplete="off">
                
                <!-- Define operação de edição -->
                <input type="hidden" name="action" value="editar">

                <!-- Identificador do usuário (ponto crítico para atualização correta no banco) -->
                <input type="hidden" name="id_usu" id="edit_id_usu">

                <div class="form-group full-width">
                    <label class="form-label">Nome</label>
                    <input type="text" name="nome_usu" id="edit_nome_usu" class="form-input" required>
                </div>

                <div class="form-grid">
                    <div class="form-group">
                        <label class="form-label">CPF</label>
                        <input type="text" name="cpf_usu" id="edit_cpf_usu" class="form-input" maxlength="14" required>
                    </div>
                    <div class="form-group">
                        <label class="form-label">Telefone</label>
                        <input type="tel" name="telefone_usu_edt" id="edit_telefone_usu" class="form-input" maxlength="15">
                    </div>
                </div>

                <div class="form-group full-width">
                    <label class="form-label">E-mail</label>
                    <input type="email" name="email_usu" id="edit_email_usu" class="form-input" required>
                </div>

                <div class="form-group full-width">
                    <label class="form-label">Endereço</label>
                    <textarea name="endereco_usu_edt" id="edit_endereco_usu" class="form-input"></textarea>
                </div>

                <div class="form-group full-width">
                    <label class="form-label">Perfil</label>
                    <select name="id_perfil" id="edit_id_perfil" class="form-input" required>
                        <option value="">Selecione...</option>
                        <c:forEach var="p" items="${perfis}">
                            <option value="${p.idPerfil}">${p.nomePerfil}</option>
                        </c:forEach>
                    </select>
                </div>

                <%-- 
                    ─────────────────────────────────────────────────────
                    SEÇÃO: ALTERAÇÃO DE SENHA
                    ─────────────────────────────────────────────────────
                    Regra de negócio:
                    - O campo é OPCIONAL. Se deixado em branco, a senha
                      atual do usuário é mantida no banco sem alteração.
                    - Se preenchido, a nova senha é criptografada com
                      BCrypt no Controller antes de persistir.
                    - A confirmação é validada por JavaScript no front-end,
                      impedindo o envio em caso de divergência.
                    ─────────────────────────────────────────────────────
                --%>
                <div class="form-section-divider">
                    <span>Alterar Senha <small>(opcional)</small></span>
                </div>

                <div class="form-grid">
                    <div class="form-group">
                        <label class="form-label">Nova Senha</label>
                        <div class="input-senha-wrapper">
                            <input type="password" name="senha_usu" id="edit_senha_usu"
                                   class="form-input"
                                   placeholder="Deixe em branco para não alterar"
                                   autocomplete="new-password"
                                   oninput="validarConfirmacaoSenha()">
                            <button type="button" class="btn-toggle-senha" onclick="toggleSenha('edit_senha_usu', 'icone-senha-edt')" tabindex="-1">
                                <i id="icone-senha-edt" class="fa-solid fa-eye"></i>
                            </button>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-label">Confirmar Nova Senha</label>
                        <div class="input-senha-wrapper">
                            <input type="password" id="edit_confirmar_senha"
                                   class="form-input"
                                   placeholder="Repita a nova senha"
                                   autocomplete="new-password"
                                   oninput="validarConfirmacaoSenha()">
                            <button type="button" class="btn-toggle-senha" onclick="toggleSenha('edit_confirmar_senha', 'icone-senha-conf')" tabindex="-1">
                                <i id="icone-senha-conf" class="fa-solid fa-eye"></i>
                            </button>
                        </div>
                        <!-- Mensagem de erro exibida quando as senhas não coincidem -->
                        <small id="msg-senha-divergente" class="msg-erro-senha" style="display:none;">
                            ⚠️ As senhas não coincidem.
                        </small>
                    </div>
                </div>
            </form>
        </div>
        
        <div class="modal-footer">
            <button class="btn btn-outline" onclick="closeModalEditar()">Cancelar</button>
            <button class="btn btn-primary" type="submit" form="formEditarUsuario">Salvar Alterações</button>
        </div>
    </div>
</div>

<!-- INCLUIR SCRIPTS COMUNS -->
<!-- Contém funções globais reutilizadas pelo sistema -->
    <jsp:include page="/includes/scripts-comum.jsp" />

    <script>
        /*
            ============================================================================
            CONTROLE DO MODAL DE CADASTRO
            Responsável por abrir e fechar o modal de criação de usuários.
            ============================================================================
        */
        const modal = document.getElementById('modalCadastro');

        function openModal() {
            modal.classList.add('show');
        }

        function closeModal() {
            modal.classList.remove('show');
        }

        /*
            Estrutura de decisão:
            Fecha o modal caso o clique ocorra fora da área interna.
        */
        window.onclick = function(event) {
            if (event.target == modal) {
                closeModal();
            }
        }

        /*
            Fecha dropdown do menu do usuário ao clicar fora.
            Ponto crítico UX: evita menus abertos indevidamente.
        */
        window.addEventListener('click', function(e) {
            const container = document.querySelector('.user-menu-container');
            const dropdown = document.getElementById('userDropdown');
            
            if (!container.contains(e.target)) {
                dropdown.classList.remove('show');
            }
        });

        // ========================= MÁSCARAS DE INPUT =========================

        const inputCPF = document.getElementById('cpf_usu');
        const inputTelefone = document.getElementById('telefone_usu');
        

        /*
            Máscara dinâmica de CPF.
            Regra de apresentação: força padrão XXX.XXX.XXX-XX.
        */
        inputCPF.addEventListener('input', function(e) {
            let value = e.target.value.replace(/\D/g, "");
            value = value.replace(/(\d{3})(\d)/, "$1.$2");
            value = value.replace(/(\d{3})(\d)/, "$1.$2");
            value = value.replace(/(\d{3})(\d{1,2})$/, "$1-$2");
            e.target.value = value;
        });

        /*
            Máscara dinâmica de telefone.
            Padroniza entrada antes do envio ao backend.
        */
        inputTelefone.addEventListener('input', function(e) {
            let value = e.target.value.replace(/\D/g, "");
            value = value.replace(/^(\d{2})(\d)/g, "($1) $2");
            value = value.replace(/(\d)(\d{4})$/, "$1-$2");
            e.target.value = value;
        });
        
            
            const inputCPFEdt = document.getElementById('edit_cpf_usu');
            const inputTelefoneEdt = document.getElementById('edit_telefone_usu');

            /*
                Máscara dinâmica de CPF.
                Regra de apresentação: força padrão XXX.XXX.XXX-XX.
            */
            inputCPFEdt.addEventListener('input', function(e) {
                let value = e.target.value.replace(/\D/g, "");
                value = value.replace(/(\d{3})(\d)/, "$1.$2");
                value = value.replace(/(\d{3})(\d)/, "$1.$2");
                value = value.replace(/(\d{3})(\d{1,2})$/, "$1-$2");
                e.target.value = value;
            });

            /*
                Máscara dinâmica de telefone.
                Padroniza entrada antes do envio ao backend.
            */
            inputTelefoneEdt.addEventListener('input', function(e) {
                let value = e.target.value.replace(/\D/g, "");
                value = value.replace(/^(\d{2})(\d)/g, "($1) $2");
                value = value.replace(/(\d)(\d{4})$/, "$1-$2");
                e.target.value = value;
            });
                
        
        /*
            ============================================================================
            CONTROLE DO MODAL DE EDIÇÃO
            Preenche automaticamente os campos com dados do usuário selecionado.
            ============================================================================
        */
        const modalEditar = document.getElementById('modalEditar');

        /**
         * Abre modal de edição preenchendo dados existentes.
         * @param id Identificador do usuário
         * @param nome Nome do usuário
         * @param cpf CPF do usuário
         * @param email Email do usuário
         * @param telefone Telefone do usuário
         * @param endereco Endereço do usuário
         * @param idPerfil ID do perfil vinculado ao usuário
         */
        function openModalEditar(id, nome, cpf, email, telefone, endereco, idPerfil) {
            document.getElementById('edit_id_usu').value = id;
            document.getElementById('edit_nome_usu').value = nome;
            document.getElementById('edit_cpf_usu').value = cpf;
            document.getElementById('edit_email_usu').value = email;
            document.getElementById('edit_telefone_usu').value = telefone;
            document.getElementById('edit_endereco_usu').value = endereco;

            // ✅ Seleciona o perfil correspondente ao registro do usuário
            const selectPerfil = document.getElementById('edit_id_perfil');
            if (selectPerfil && idPerfil) {
                selectPerfil.value = idPerfil;
            }

            modalEditar.classList.add('show');
        }

        /*
            ============================================================================
            TOGGLE MOSTRAR / OCULTAR SENHA
            Alterna o tipo do input entre "password" e "text"
            e troca o ícone de olho aberto/fechado.
            ============================================================================
        */
        function toggleSenha(inputId, iconeId) {
            const input = document.getElementById(inputId);
            const icone = document.getElementById(iconeId);
            if (input.type === 'password') {
                input.type = 'text';
                icone.classList.replace('fa-eye', 'fa-eye-slash');
            } else {
                input.type = 'password';
                icone.classList.replace('fa-eye-slash', 'fa-eye');
            }
        }

        /*
            ============================================================================
            VALIDAÇÃO DE CONFIRMAÇÃO DE SENHA (MODAL EDIÇÃO)
            ----------------------------------------------------------------------------
            Regra de negócio:
            - Campo opcional: se deixado em branco, a senha atual é mantida no banco.
            - Se preenchido, a confirmação deve ser idêntica à nova senha.
            - Enquanto divergirem, o botão "Salvar Alterações" fica desabilitado.
            ============================================================================
        */
        function validarConfirmacaoSenha() {
            const nova        = document.getElementById('edit_senha_usu').value;
            const confirmacao = document.getElementById('edit_confirmar_senha').value;
            const msgErro     = document.getElementById('msg-senha-divergente');
            const btnSalvar   = document.querySelector('button[form="formEditarUsuario"]');

            if (nova === '') {
                msgErro.style.display = 'none';
                btnSalvar.disabled    = false;
                return;
            }

            if (nova !== confirmacao) {
                msgErro.style.display = 'inline';
                btnSalvar.disabled    = true;
            } else {
                msgErro.style.display = 'none';
                btnSalvar.disabled    = false;
            }
        }

        /*
            ============================================================================
            FECHAR MODAL DE EDIÇÃO
            Limpa os campos de senha ao fechar para evitar que dados sensíveis
            fiquem visíveis caso o modal seja reaberto para outro usuário.
            ============================================================================
        */
        function closeModalEditar() {
            document.getElementById('edit_senha_usu').value       = '';
            document.getElementById('edit_confirmar_senha').value  = '';
            document.getElementById('msg-senha-divergente').style.display = 'none';
            document.querySelector('button[form="formEditarUsuario"]').disabled = false;
            modalEditar.classList.remove('show');
        }

        /*
            Fecha modal de edição ao clicar fora.
        */
        window.addEventListener('click', function(event) {
            if (event.target === modalEditar) {
                closeModalEditar();
            }
        });

    </script>
    
    <script>
        /*
            ============================================================================
            EXCLUSÃO DE USUÁRIO
            ----------------------------------------------------------------------------
            Fluxo:
            1. Solicita confirmação ao usuário.
            2. Cria formulário POST dinamicamente.
            3. Envia action=deletar para UsuarioController.
            4. Controller executa remoção no banco de dados.
            ============================================================================
        */
		function excluirUsuario(idUsu) {
		    if (confirm("Tem certeza que deseja excluir este usuário?")) {
		        const form = document.createElement("form");
		        form.method = "post";
		        form.action = "/patrimweb/UsuarioController";
		
		        const action = document.createElement("input");
		        action.type = "hidden";
		        action.name = "action";
		        action.value = "deletar";
		
		        const id = document.createElement("input");
		        id.type = "hidden";
		        id.name = "id_usu";
		        id.value = idUsu;
		
		        form.appendChild(action);
		        form.appendChild(id);
		        document.body.appendChild(form);
		        form.submit();
		    }
		}
		</script>
		
		<script>
            /*
                ============================================================================
                CONTROLE AUTOMÁTICO DE ALERTAS
                ----------------------------------------------------------------------------
                Regra de UX:
                - Alertas permanecem visíveis por 5 segundos.
                - Após o tempo, ocorre fade-out e remoção do DOM.
                ============================================================================
            */
		    document.addEventListener("DOMContentLoaded", function() {
		        const alertas = document.querySelectorAll('.alerta-custom');

                // Estrutura de repetição para tratar múltiplos alertas simultaneamente
		        alertas.forEach(function(alerta) {
		            setTimeout(function() {
		                alerta.style.transition = "opacity 0.6s ease";
		                alerta.style.opacity = "0";
		                setTimeout(() => alerta.remove(), 600);
		            }, 5000); // 5 segundos
		        });
		    });
		</script>
	
</body>
</html>
