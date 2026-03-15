<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%--
    =====================================================================================
    PAGINA   : configuracoes.jsp
    SISTEMA  : PatrimWeb
    CAMADA   : View (JSP)

    ABAS:
      1. Perfil       - Dados pessoais do usuario logado
      2. Notificacoes - Preferencias de alertas por e-mail
      3. Seguranca    - Alteracao de senha e zona de perigo
      4. Permissoes   - Matriz de controle de acesso por perfil  *** NOVA ABA ***

    DADOS ESPERADOS PELO CONTROLLER  (aba Permissoes):
      perfis           -> List<Perfil>
      permissoes       -> List<Permissao>  (idPermissao, modulo, acao, descricao)
      perfilPermissoes -> Map<Integer, List<Integer>>  idPerfil -> [idPermissao, ...]

    FUNCIONAMENTO DA ABA PERMISSOES:
      - Painel esquerdo : lista clicavel de perfis com badge contador
      - Painel direito  : matriz modulo/acoes com checkboxes customizados
      - Botoes "Conceder Tudo" e "Revogar Tudo" no cabecalho
      - Indicador "alteracoes nao salvas" ao mudar qualquer checkbox
      - Salvamento via AJAX (fetch) sem recarregar a pagina
      - Toast de feedback apos salvar (sucesso ou erro)
      - Contadores atualizados localmente apos cada save

    CONTROLE DE ACESSO:
      Pagina restrita a usuarios com perfil ADMINISTRADOR.
    =====================================================================================
--%>
<c:set var="pageTitle" value="Configurações" scope="request" />

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
                <%-- Mensagem de Erro --%>
                <c:if test="${not empty sessionScope.mensagemErro}">
                    <div class="alerta-custom alerta-erro">
                        <span><strong>&#9888;&#65039; Atenção:</strong> ${sessionScope.mensagemErro}</span>
                        <button type="button" class="btn-fechar-alerta" onclick="this.parentElement.style.display='none'">&times;</button>
                    </div>
                    <% session.removeAttribute("mensagemErro"); %>
                </c:if>
                <%-- Mensagem de Sucesso --%>
                <c:if test="${not empty sessionScope.mensagemSucesso}">
                    <div class="alerta-custom alerta-sucesso">
                        <span><strong>&#9989; Sucesso!</strong> ${sessionScope.mensagemSucesso}</span>
                        <button type="button" class="btn-fechar-alerta" onclick="this.parentElement.style.display='none'">&times;</button>
                    </div>
                    <% session.removeAttribute("mensagemSucesso"); %>
                </c:if>
            </div>

            <jsp:include page="/includes/user-menu.jsp" />
        </header>

        <div class="dashboard-container">
            <div class="settings-card">

                <!-- ================================================
                     BARRA DE ABAS
                     ================================================ -->
                <div class="settings-tabs">
                    <button class="tab-btn active" data-aba="perfil" onclick="openTab(event, 'tab-perfil')">
                        <i class="fa-solid fa-user"></i> Perfil
                    </button>
                    <!--  <button class="tab-btn" data-aba="notificacoes" onclick="openTab(event, 'tab-notificacoes')">
                        <i class="fa-solid fa-bell"></i> Notificacoes
                    </button>-->
                    <button class="tab-btn" data-aba="seguranca" onclick="openTab(event, 'tab-seguranca')">
                        <i class="fa-solid fa-lock"></i> Segurança
                    </button>
                    <!--  <button class="tab-btn" data-aba="permissoes" onclick="openTab(event, 'tab-permissoes')">
                        <i class="fa-solid fa-shield-halved"></i> Permissões
                    </button>-->
                </div>


                <!-- ================================================
                     ABA: PERFIL
                     ================================================ -->
                <div id="tab-perfil" class="tab-content active">
                    <div class="profile-header">
                        <div class="profile-img-container">

                            <%--
                                Foto servida da pasta estatica: imagens/perfil/{idUsu}.jpg
                                
                                

                                O parâmetro ?v= é o timestamp gravado na sessão quando
                                o usuário troca a foto. Isso força o navegador a baixar
                                a imagem nova em vez de exibir a versão em cache.

                                Se o arquivo não existir, o onerror ativa o avatar
                                com as iniciais do nome como fallback.
                            --%>
                            <c:set var="idUsu"    value="${sessionScope.usuarioLogado.idUsu}" />
                            <c:set var="fotoVers" value="${not empty sessionScope.fotoPerfil_v ? sessionScope.fotoPerfil_v : '0'}" />

                            <img id="previewFoto"
                                 src="${pageContext.request.contextPath}/imagens/perfil/${idUsu}.jpg?v=${fotoVers}"
                                 alt="Foto de Perfil"
                                 class="profile-img"
                                 onerror="this.style.display='none';document.getElementById('avatarFallback').style.display='flex';">

                            <div id="avatarFallback" class="profile-img"
                                 style="display:none;align-items:center;justify-content:center;
                                        background:linear-gradient(135deg,#3b82f6,#1d4ed8);
                                        color:#fff;font-size:32px;font-weight:700;border-radius:50%">
                                ${fn:toUpperCase(fn:substring(sessionScope.usuarioLogado.nomeUsu,0,1))}
                            </div>

                            <%-- Ícone de câmera — abre o seletor de arquivo ao clicar --%>
                            <div class="profile-edit-badge"
                                 onclick="document.getElementById('inputFoto').click()"
                                 title="Alterar foto de perfil"
                                 style="cursor:pointer">
                                <i class="fa-solid fa-camera"></i>
                            </div>
                        </div>

                        <div>
                            <h3 style="font-size:18px;font-weight:600">
                                ${not empty sessionScope.usuarioLogado.nomeUsu ? sessionScope.usuarioLogado.nomeUsu : 'Usuário Admin'}
                            </h3>
                            <p style="color:#6b7280;font-size:14px">
                                ${not empty sessionScope.usuarioLogado.perfilUsu.nomePerfil ? sessionScope.usuarioLogado.perfilUsu.nomePerfil : 'Administrador do Sistema'}
                            </p>
                            <%-- Mostra o nome do arquivo enquanto o upload acontece --%>
                            <p id="lblArquivoSelecionado"
                               style="display:none;font-size:12px;color:#3b82f6;margin-top:4px">
                                <i class="fa-solid fa-spinner fa-spin"></i>
                                <span id="nomeArquivoSelecionado"></span>
                            </p>
                        </div>
                    </div>

                    <%--
                        Formulário exclusivo de upload de foto.
                        enctype="multipart/form-data" é obrigatório para envio de arquivo —
                        sem ele o servidor recebe o campo vazio e o upload não funciona.
                    --%>
                    <form id="formFoto"
                          action="${pageContext.request.contextPath}/ConfiguracaoController"
                          method="post"
                          enctype="multipart/form-data">
                        <input type="hidden" name="action" value="atualizarFoto">
                        <%-- Input oculto — acionado pelo clique no ícone de câmera --%>
                        <input type="file"
                               id="inputFoto"
                               name="foto_perfil"
                               accept="image/jpeg,image/png,image/webp"
                               style="display:none"
                               onchange="previewESalvar(this)">
                    </form>
                    <form id="formPerfil" action="${pageContext.request.contextPath}/ConfiguracaoController" method="post">
                        <input type="hidden" name="action" value="atualizarPerfil">
                        <div class="settings-grid">
                            <div class="form-group">
                                <label class="form-label">Nome Completo</label>
                                <input type="text" name="nome_usu" class="form-input"
                                       value="${not empty sessionScope.usuarioLogado.nomeUsu ? sessionScope.usuarioLogado.nomeUsu : ''}"
                                       placeholder="Nome completo" required>
                            </div>
                            <div class="form-group">
                                <label class="form-label">E-mail</label>
                                <input type="email" name="email_usu" class="form-input"
                                       value="${not empty sessionScope.usuarioLogado.emailUsu ? sessionScope.usuarioLogado.emailUsu : ''}"
                                       placeholder="exemplo@patrimweb.com" required>
                            </div>
                            <div class="form-group">
                                <label class="form-label">Telefone</label>
                                <input type="tel" name="telefone_usu" class="form-input"
                                       value="${not empty sessionScope.usuarioLogado.telefoneUsu ? sessionScope.usuarioLogado.telefoneUsu : ''}"
                                       placeholder="(00) 00000-0000">
                            </div>
                            <div class="form-group">
                                <label class="form-label">CPF</label>
                                <input type="text" name="cpf_usu" class="form-input"
                                       value="${not empty sessionScope.usuarioLogado.cpfUsu ? sessionScope.usuarioLogado.cpfUsu : ''}"
                                       placeholder="000.000.000-00"
                                       maxlength="14">
                            </div>
                            <div class="form-group" style="grid-column: 1 / -1;">
                                <label class="form-label">Endereço</label>
                                <input type="text" name="endereco_usu" class="form-input"
                                       value="${not empty sessionScope.usuarioLogado.enderecoUsu ? sessionScope.usuarioLogado.enderecoUsu : ''}"
                                       placeholder="Rua, número, bairro, cidade">
                            </div>
                            <div class="form-group">
                                <label class="form-label">
                                    Cargo
                                    <span style="font-size:11px;font-weight:400;color:#9ca3af;margin-left:6px">
                                        <i class="fa-solid fa-lock" style="font-size:10px"></i> Alterável somente em Usuários
                                    </span>
                                </label>
                                <input type="text" class="form-input"
                                       value="${not empty sessionScope.usuarioLogado.perfilUsu.nomePerfil ? sessionScope.usuarioLogado.perfilUsu.nomePerfil : ''}"
                                       disabled style="background-color:#f3f4f6;cursor:not-allowed;color:#6b7280">
                            </div>
                        </div>
                        <div style="margin-top:20px;text-align:right">
                            <button type="submit" class="btn btn-primary">Atualizar Perfil</button>
                        </div>
                    </form>
                </div>
                <!-- FIM ABA: PERFIL -->


                <!-- ================================================
                     ABA: NOTIFICACOES
                     ================================================ -->
                <div id="tab-notificacoes" class="tab-content">
                    <div class="section-title">Alertas por E-mail</div>
                    <div class="toggle-group">
                        <div class="toggle-info">
                            <h4>Novos Usuários</h4>
                            <p>Receber e-mail quando um novo usuário for cadastrado.</p>
                        </div>
                        <label class="switch"><input type="checkbox" checked><span class="slider"></span></label>
                    </div>
                    <div class="toggle-group">
                        <div class="toggle-info">
                            <h4>Movimentação de Equipamento</h4>
                            <p>Notificar quando houver transferência de patrimônio.</p>
                        </div>
                        <label class="switch"><input type="checkbox" checked><span class="slider"></span></label>
                    </div>
                    <div class="toggle-group">
                        <div class="toggle-info">
                            <h4>Relatórios Mensais</h4>
                            <p>Receber resumo automático todo dia 01.</p>
                        </div>
                        <label class="switch"><input type="checkbox"><span class="slider"></span></label>
                    </div>
                </div>
                <!-- FIM ABA: NOTIFICACOES -->


                <!-- ================================================
                     ABA: SEGURANCA
                     ================================================ -->
                <div id="tab-seguranca" class="tab-content">

                    <%-- Alteração de senha: visível apenas para usuários sem login Google --%>
                    <c:choose>
                        <c:when test="${not sessionScope.usuarioLogado.loginGoogle}">
                            <div class="section-title">Alterar Senha</div>
                            <form id="formSenha" action="${pageContext.request.contextPath}/ConfiguracaoController" method="post">
                                <input type="hidden" name="action" value="alterarSenha">
                                <div class="settings-grid">
                                    <div class="form-group">
                                        <label class="form-label">Senha Atual</label>
                                        <input type="password" name="senha_atual" class="form-input"
                                               placeholder="Digite sua senha atual" required>
                                    </div>
                                    <div></div>
                                    <div class="form-group">
                                        <label class="form-label">Nova Senha</label>
                                        <input type="password" name="nova_senha" class="form-input"
                                               placeholder="Digite a nova senha" required>
                                    </div>
                                    <div class="form-group">
                                        <label class="form-label">Confirmar Nova Senha</label>
                                        <input type="password" name="confirmar_senha" class="form-input"
                                               placeholder="Repita a nova senha" required>
                                    </div>
                                </div>
                                <button type="submit" class="btn btn-outline" style="margin-top:10px">Atualizar Senha</button>
                            </form>
                        </c:when>
                        <c:otherwise>
                            <%-- Usuário Google: exibe aviso no lugar do formulário de senha --%>
                            <div class="section-title">Alterar Senha</div>
                            <div style="display:flex;align-items:center;gap:12px;background:#f0f9ff;border:1px solid #bae6fd;border-radius:8px;padding:14px 18px;margin-bottom:24px;">
                                <i class="fa-brands fa-google" style="color:#4285F4;font-size:22px;flex-shrink:0"></i>
                                <p style="font-size:13px;color:#0369a1;margin:0;">
                                    Sua conta utiliza o <strong>Login com Google</strong>. A senha é gerenciada diretamente pela sua conta Google e não pode ser alterada aqui.
                                </p>
                            </div>
                        </c:otherwise>
                    </c:choose>
                    <!--  <div class="danger-zone">
                        <div class="danger-title">
                            <i class="fa-solid fa-triangle-exclamation"></i> Zona de Perigo
                        </div>
                        <p style="font-size:14px;color:#1f2937;margin-bottom:15px">
                            Essas ações são irreversíveis. Tenha certeza antes de continuar.
                        </p>
                        <div class="danger-actions">
                            <button class="btn btn-danger"
                                    onclick="confirmarAcaoPerigo('${pageContext.request.contextPath}/ConfiguracaoController?action=limparCache','Tem certeza que deseja limpar o cache do sistema?')">
                                Limpar Cache do Sistema
                            </button>
                            <button class="btn btn-danger"
                                    onclick="confirmarAcaoPerigo('${pageContext.request.contextPath}/ConfiguracaoController?action=resetarConfiguracoes','Atencao: esta acao resetara todas as configuracoes. Deseja continuar?')">
                                Resetar Configuracões de Fábrica
                            </button>
                        </div>
                    </div>-->
                </div>
                <!-- FIM ABA: SEGURANCA -->


                <!-- ================================================
                     ABA: PERMISSOES
                     ================================================ -->
                <div id="tab-permissoes" class="tab-content">

                    <div class="section-title" style="margin-bottom:18px">
                        <i class="fa-solid fa-shield-halved" style="color:var(--primary-blue)"></i>
                        Controle de Acesso por Perfil
                        <span style="margin-left:auto;font-size:11px;font-weight:400;color:var(--text-muted)">
                            Clique em um perfil para gerenciar suas permissões
                        </span>
                    </div>

                    <div class="perm-layout">

                        <!-- ---- Painel esquerdo: lista de perfis ---- -->
                       <div class="perm-sidebar">
                            <div class="perm-sidebar-hd">
                                <i class="fa-solid fa-user-shield"></i> Perfis de Acesso
                            </div>

                            <c:choose>
                                <c:when test="${not empty perfis}">
                                    <c:forEach var="p" items="${perfis}" varStatus="st">
                                        <c:set var="cor" value="${st.index mod 7 == 0 ? '#3b82f6' :
                                                                   st.index mod 7 == 1 ? '#8b5cf6' :
                                                                   st.index mod 7 == 2 ? '#10b981' :
                                                                   st.index mod 7 == 3 ? '#f59e0b' :
                                                                   st.index mod 7 == 4 ? '#ef4444' :
                                                                   st.index mod 7 == 5 ? '#06b6d4' :
                                                                                         '#ec4899'}" />
                                        <div class="perm-profile-item ${st.first ? 'ativo' : ''}"
                                             data-perfil-id="${p.idPerfil}"
                                             data-perfil-nome="${p.nomePerfil}"
                                             onclick="selecionarPerfil(this)">
                                            <div class="perm-avatar" style="background:${cor}">
                                                <c:out value="${fn:toUpperCase(fn:substring(p.nomePerfil, 0, 1))}" />
                                            </div>
                                            <span class="perm-role-name">${p.nomePerfil}</span>
                                            <span class="perm-cnt" id="cnt-${p.idPerfil}">0</span>
                                        </div>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <div style="padding:24px 16px;font-size:13px;color:var(--text-muted);text-align:center">
                                        <i class="fa-solid fa-circle-info" style="display:block;font-size:20px;margin-bottom:6px"></i>
                                        Nenhum perfil cadastrado.
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                        <%-- fim perm-sidebar --%>


                        <!-- ---- Painel direito: matriz de permissoes ---- -->
                       <div class="perm-main">

                            <div class="perm-main-hd">
                                <div class="perm-main-title">
                                    <strong id="matrizTitulo">Selecione um perfil</strong>
                                    <span   id="matrizSub">Escolha um perfil ao lado para visualizar e editar as permissões</span>
                                </div>
                                <div class="perm-bulk" id="bulkActions">
                                    <button type="button" class="btn-all-grant" onclick="toggleTodos(true)">
                                        <i class="fa-solid fa-check-double"></i> Conceder Tudo
                                    </button>
                                    <button type="button" class="btn-all-revoke" onclick="toggleTodos(false)">
                                        <i class="fa-solid fa-ban"></i> Revogar Tudo
                                    </button>
                                </div>
                            </div>

                            <div class="perm-empty" id="permVazio">
                                <div class="perm-empty-ico">
                                    <i class="fa-solid fa-hand-pointer"></i>
                                </div>
                                <p>Selecione um perfil na lista ao lado<br>para visualizar e configurar as permissões de acesso.</p>
                            </div>

                            <%--
                                O atributo "action" do form foi removido propositalmente.
                                A URL do POST e montada diretamente no fetch via URLSearchParams,
                                evitando o conflito com input[name="action"] que causava o erro 400.
                            --%>
                            <form id="formPerm"
                                  method="post"
                                  style="display:none;flex-direction:column;flex:1"
                                  onsubmit="submitPermissoes(event)">

                                <input type="hidden" name="id_perfil" id="hdnIdPerfil">

                                <div class="perm-body">
                                    <c:choose>
                                        <c:when test="${not empty permissoes}">

                                            <c:set var="modAtual" value="__NONE__" />

                                            <c:forEach var="perm" items="${permissoes}">

                                                <c:if test="${perm.modulo != modAtual}">
                                                    <div><c:if test="${modAtual != '__NONE__'}"></c:if></div>
                                                    <c:set var="modAtual" value="${perm.modulo}" />
                                                    <div class="perm-module">
                                                        <div class="perm-module-hd">
                                                            <i class="${perm.modulo == 'Equipamentos'   ? 'fa-solid fa-computer'    :
                                                                        perm.modulo == 'Usuarios'       ? 'fa-solid fa-users'        :
                                                                        perm.modulo == 'Unidades'       ? 'fa-solid fa-building'     :
                                                                        perm.modulo == 'Fabricantes'    ? 'fa-solid fa-industry'     :
                                                                        perm.modulo == 'Movimentacoes'  ? 'fa-solid fa-truck-moving' :
                                                                        perm.modulo == 'Relatorios'     ? 'fa-solid fa-file-lines'   :
                                                                        perm.modulo == 'Dashboard'      ? 'fa-solid fa-house'        :
                                                                        perm.modulo == 'Configuracoes'  ? 'fa-solid fa-gear'         :
                                                                                                          'fa-solid fa-puzzle-piece'}"></i>
                                                            ${perm.modulo}
                                                        </div>
                                                    </div>
                                                </c:if>

                                                <label class="perm-row">
			                                    <input type="checkbox"
			                                           class="perm-cb"
			                                           data-perm-id="${perm.idPermissao}"
			                                           onchange="marcarDirty()">
			
			                                    <span class="perm-row-info">
			                                        <span class="perm-row-name">${perm.modulo}</span>
			                                    </span>
			                                    
			                                    
			                                    
			                                    <!--  <input type="checkbox"
			                                           class="perm-cb"
			                                           data-perm-id="${perm.modulo}"
			                                           onchange="marcarDirty()">
			
			                                    <span class="perm-row-info">
			                                        <span class="perm-row-name">Visualizar</span>
			                                    </span>
			                                    
			                                    
			                                    
			                                    <input type="checkbox"
			                                           class="perm-cb"
			                                           data-perm-id="${perm.idPermissao}"
			                                           onchange="marcarDirty()">
			
			                                    <span class="perm-row-info">
			                                        <span class="perm-row-name">Inserir</span>
			                                    </span>
			                                    
			                                    
			                                    
			                                    <input type="checkbox"
			                                           class="perm-cb"
			                                           data-perm-id="${perm.idPermissao}"
			                                           onchange="marcarDirty()">
			
			                                    <span class="perm-row-info">
			                                        <span class="perm-row-name">Editar</span>
			                                    </span>
			                                    
			                                    
			                                    
			                                    <input type="checkbox"
			                                           class="perm-cb"
			                                           data-perm-id="${perm.idPermissao}"
			                                           onchange="marcarDirty()">
			
			                                    <span class="perm-row-info">
			                                        <span class="perm-row-name">Excluir</span>
			                                    </span>-->
			                                    
			                                    
			                                    
			                            
			
			                                    <span class="perm-badge">${perm.modulo}</span>
			                                </label>

                                            </c:forEach>
                                            <c:if test="${not empty permissoes}"></c:if>

                                        </c:when>
                                        
                                        
                                        <c:otherwise>
                                            <div style="padding:40px 20px;text-align:center;color:var(--text-muted)">
                                                <i class="fa-solid fa-circle-exclamation"
                                                   style="font-size:28px;color:#d1d5db;display:block;margin-bottom:10px"></i>
                                                Nenhuma permissão cadastrada.<br>
                                                <a href="${pageContext.request.contextPath}/PermissaoController"
                                                   style="color:var(--primary-blue);font-size:13px;margin-top:8px;display:inline-block">
                                                    Cadastrar permissões &rarr;
                                                </a>
                                            </div>
                                        </c:otherwise>
                                    </c:choose>
                                    
                                </div>
                                <%-- fim perm-body --%>

                                <div class="perm-footer">
                                    <div class="perm-footer-left">
                                        <span class="perm-hint">
                                            <i class="fa-solid fa-circle-info"></i>
                                            Alterações são aplicadas imediatamente após salvar.
                                        </span>
                                        <span class="perm-dirty" id="permDirty">
                                            <i class="fa-solid fa-circle-dot"></i>
                                            Ha alteções não salvas
                                        </span>
                                    </div>
                                    <button type="submit" class="btn btn-primary" style="min-width:155px">
                                        <i class="fa-solid fa-floppy-disk"></i> Salvar Permissões
                                    </button>
                                </div>

                            </form>
                            <%-- fim formPerm --%>

                        </div>
                        <%-- fim perm-main --%>

                    </div>
                    <%-- fim perm-layout --%>

                </div>
                <!-- FIM ABA: PERMISSOES -->

            </div><%-- fim settings-card --%>
        </div><%-- fim dashboard-container --%>
    </main>

    <!-- Toast de feedback AJAX -->
    <div class="pw-toast" id="pwToast">
        <i class="fa-solid fa-circle-check"></i>
        <span id="pwToastMsg">Permissoes salvas!</span>
    </div>

    <script>
        const permData = {
            <c:forEach var="entry" items="${perfilPermissoes}" varStatus="s">
                "${entry.key}": [<c:forEach var="pid" items="${entry.value}" varStatus="ps">${pid}<c:if test="${!ps.last}">,</c:if></c:forEach>]<c:if test="${!s.last}">,</c:if>
            </c:forEach>
        };

        /* URL do controller resolvida no servidor, sem ambiguidade com input[name="action"] */
        var CONTROLLER_URL = '${pageContext.request.contextPath}/ConfiguracaoController';
    </script>

    <jsp:include page="/includes/scripts-comum.jsp" />

    <script>
        /* ============================================================
           TROCA DE ABAS
           ============================================================ */
        function openTab(evt, tabName) {
            var els  = document.getElementsByClassName('tab-content');
            var btns = document.getElementsByClassName('tab-btn');
            for (var i = 0; i < els.length;  i++) { els[i].style.display  = 'none'; els[i].classList.remove('active'); }
            for (var i = 0; i < btns.length; i++) { btns[i].className = btns[i].className.replace(' active', ''); }
            var panel = document.getElementById(tabName);
            panel.style.display = 'block';
            setTimeout(function () { panel.classList.add('active'); }, 10);
            evt.currentTarget.className += ' active';
        }

        function confirmarAcaoPerigo(url, mensagem) {
            if (confirm(mensagem)) window.location.href = url;
        }

        window.addEventListener('click', function (e) {
            var c = document.querySelector('.user-menu-container');
            var d = document.getElementById('userDropdown');
            if (c && !c.contains(e.target)) d.classList.remove('show');
        });

        document.addEventListener('DOMContentLoaded', function () {
            document.querySelectorAll('.alerta-custom').forEach(function (a) {
                setTimeout(function () {
                    a.style.transition = 'opacity 0.6s ease';
                    a.style.opacity    = '0';
                    setTimeout(function () { a.remove(); }, 600);
                }, 5000);
            });
            refreshContadores();
            var primeiro = document.querySelector('.perm-profile-item');
            if (primeiro) selecionarPerfil(primeiro);

            // Abre a aba indicada pelo parâmetro ?aba= na URL (ex: após erro em Segurança)
            var params  = new URLSearchParams(window.location.search);
            var abaAlvo = params.get('aba');
            if (abaAlvo) {
                var btnAlvo = document.querySelector('.tab-btn[data-aba="' + abaAlvo + '"]');
                if (btnAlvo) btnAlvo.click();
            }
        });


        /* ============================================================
           LOGICA DA ABA PERMISSOES
           ============================================================ */

        var perfilAtivoId   = null;
        var perfilAtivoNome = null;

        function selecionarPerfil(el) {
            document.querySelectorAll('.perm-profile-item').forEach(function (i) { i.classList.remove('ativo'); });
            el.classList.add('ativo');

            perfilAtivoId   = el.dataset.perfilId;
            perfilAtivoNome = el.dataset.perfilNome;

            document.getElementById('matrizTitulo').textContent = perfilAtivoNome;
            document.getElementById('matrizSub').textContent    = 'Gerencie as permissoes de acesso deste perfil';
            document.getElementById('bulkActions').classList.add('visivel');
            document.getElementById('hdnIdPerfil').value = perfilAtivoId;

            var ativos = permData[perfilAtivoId] || [];
            document.querySelectorAll('.perm-cb').forEach(function (cb) {
                cb.checked = ativos.indexOf(parseInt(cb.dataset.permId)) !== -1;
            });

            document.getElementById('permVazio').style.display = 'none';
            document.getElementById('formPerm').style.display  = 'flex';
            document.getElementById('permDirty').classList.remove('visivel');
        }

        function toggleTodos(marcar) {
            document.querySelectorAll('.perm-cb').forEach(function (cb) { cb.checked = marcar; });
            marcarDirty();
        }

        function marcarDirty() {
            document.getElementById('permDirty').classList.add('visivel');
        }

        function refreshContadores() {
            document.querySelectorAll('.perm-profile-item').forEach(function (item) {
                var total = (permData[item.dataset.perfilId] || []).length;
                var el    = document.getElementById('cnt-' + item.dataset.perfilId);
                if (el) el.textContent = total;
            });
        }

        /**
         * submitPermissoes(e)
         *
         * Envia o POST via URLSearchParams (application/x-www-form-urlencoded).
         * Isso garante que request.getParameter("action") funcione corretamente
         * no Servlet, evitando o erro 400 causado pelo uso de FormData (multipart).
         *
         * A URL e lida da variavel CONTROLLER_URL (definida acima via JSP EL),
         * evitando o conflito classico onde form.action retornava o elemento
         * input[name="action"] em vez da URL do formulario.
         */
        function submitPermissoes(e) {
            e.preventDefault();

            /* Coleta IDs dos checkboxes marcados */
            var marcados = [];
            document.querySelectorAll('.perm-cb:checked').forEach(function (cb) {
                marcados.push(cb.dataset.permId);
            });

            /* Monta payload como URLSearchParams */
            var params = new URLSearchParams();
            params.append('action',    'salvarPermissoes');
            params.append('id_perfil', perfilAtivoId);
            marcados.forEach(function (id) {
                params.append('permissao_ids', id);
            });

            fetch(CONTROLLER_URL, {
                method:  'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body:    params.toString()
            })
            .then(function (res) {
                if (res.ok) {
                    permData[perfilAtivoId] = marcados.map(Number);
                    refreshContadores();
                    document.getElementById('permDirty').classList.remove('visivel');
                    mostrarToast('Permissoes de <strong>' + perfilAtivoNome + '</strong> salvas com sucesso!', 'ok');
                } else {
                    mostrarToast('Erro ao salvar. Tente novamente.', 'err');
                }
            })
            .catch(function () {
                mostrarToast('Erro de conexao. Verifique a rede e tente novamente.', 'err');
            });
        }

        function mostrarToast(msg, tipo) {
            var toast = document.getElementById('pwToast');
            var icon  = toast.querySelector('i');
            document.getElementById('pwToastMsg').innerHTML = msg;
            toast.className = 'pw-toast ' + tipo;
            icon.className  = tipo === 'ok' ? 'fa-solid fa-circle-check' : 'fa-solid fa-circle-xmark';
            setTimeout(function () { toast.classList.add('show'); }, 10);
            setTimeout(function () { toast.classList.remove('show'); }, 3800);
        }

        /* ============================================================
           UPLOAD DE FOTO DE PERFIL
           ============================================================
           Fluxo:
             1. Usuário clica no ícone de câmera → abre o seletor de arquivo
                (pode navegar para qualquer pasta do computador)
             2. Ao selecionar, esta função valida tipo e tamanho no lado cliente
                (validação rápida antes de enviar ao servidor)
             3. Mostra preview imediato da foto escolhida
             4. Submete o formulário automaticamente para o servidor
        */
        function previewESalvar(input) {
            if (!input.files || input.files.length === 0) return;

            var arquivo = input.files[0];

            // ── Validação no cliente (feedback rápido antes de enviar) ──
            var tiposPermitidos = ['image/jpeg', 'image/png', 'image/webp'];
            if (tiposPermitidos.indexOf(arquivo.type) === -1) {
                alert('Formato inválido. Use apenas JPG, PNG ou WEBP.');
                input.value = '';
                return;
            }

            var maxBytes = 5 * 1024 * 1024; // 5 MB
            if (arquivo.size > maxBytes) {
                alert('Arquivo muito grande. O tamanho máximo é 5 MB.');
                input.value = '';
                return;
            }

            // ── Preview imediato usando FileReader ──
            //    FileReader lê o arquivo localmente (sem enviar ao servidor ainda)
            //    e gera uma URL temporária para exibir a imagem na tela
            var reader = new FileReader();
            reader.onload = function (e) {
                var preview   = document.getElementById('previewFoto');
                var fallback  = document.getElementById('avatarFallback');
                preview.src   = e.target.result;
                preview.style.display  = 'block';
                fallback.style.display = 'none';
            };
            reader.readAsDataURL(arquivo);

            // ── Exibe nome do arquivo selecionado abaixo do nome do usuário ──
            var lblContainer = document.getElementById('lblArquivoSelecionado');
            var lblNome      = document.getElementById('nomeArquivoSelecionado');
            lblNome.textContent    = arquivo.name;
            lblContainer.style.display = 'block';

            // ── Submete o formulário automaticamente ──
            //    O servidor valida novamente (nunca confiar só no cliente)
            document.getElementById('formFoto').submit();
        }
    </script>

</body>
</html>
