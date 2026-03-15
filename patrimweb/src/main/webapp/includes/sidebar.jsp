<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%--
    =====================================================================
    COMPONENTE JSP: SIDEBAR DE NAVEGAÇÃO DO SISTEMA PATRIMWEB
    =====================================================================

    PROPÓSITO:
    Este arquivo JSP representa o componente visual da barra lateral
    (sidebar) do sistema PatrimWeb. Ele é responsável por disponibilizar
    a navegação principal entre os módulos do sistema.

    RESPONSABILIDADES:
    - Exibir identidade visual do sistema (logo + nome).
    - Disponibilizar links de navegação para os principais controllers.
    - Destacar dinamicamente o módulo atualmente ativo.
    - Permitir interação responsiva em dispositivos móveis através
      do overlay e da função JavaScript toggleSidebar().

    REGRAS DE NEGÓCIO IMPLEMENTADAS:
    - O item ativo do menu é definido dinamicamente pela variável
      "pageTitle", enviada pelo controller responsável pela página.
    - A navegação utiliza o contextPath da aplicação para garantir
      funcionamento correto independentemente do ambiente de deploy.

    INTERAÇÃO COM BACK-END:
    - Utiliza Expression Language (EL) para acessar:
        • pageContext.request.contextPath
        • variável pageTitle definida no request.
    - Cada link aponta para um Controller Servlet responsável
      pelo carregamento do respectivo módulo.

    PONTOS CRÍTICOS:
    - A variável "pageTitle" deve ser definida corretamente pelos
      controllers, caso contrário o destaque visual do menu não
      funcionará.
    - O ID "sidebar" é utilizado por scripts JavaScript para controle
      de abertura/fechamento em dispositivos móveis.
    - O overlay depende da função JavaScript toggleSidebar(),
      definida em scripts comuns da aplicação.

    OBSERVAÇÃO:
    Este componente não possui acesso direto a banco de dados.
    Atua apenas como camada de apresentação (View).
--%>

<aside class="sidebar" id="sidebar">
     <%-- Área de identificação visual do sistema (logo e nome) --%>
     <div class="logo-area">
          <%-- Ícone SVG utilizado como logomarca do sistema --%>
          <svg class="logo-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"></path>
              <polyline points="3.27 6.96 12 12.01 20.73 6.96"></polyline>
              <line x1="12" y1="22.08" x2="12" y2="12"></line>
          </svg>

          <%-- Nome do sistema exibido ao lado do logo --%>
          <span style="font-weight: 600; color: white;">PatrimWeb</span>
      </div>

    <%-- Menu principal de navegação do sistema --%>
    <ul class="nav-menu">

        <%--
            Link para o Dashboard.
            A classe CSS "active" é aplicada dinamicamente quando
            pageTitle == 'Dashboard', indicando visualmente a página atual.
        --%>
        <a href="${pageContext.request.contextPath}/DashboardController" class="nav-item ${pageTitle == 'Dashboard' ? 'active' : ''}">
            <i class="fa-solid fa-house"></i> Dashboard
        </a>

        <%--
            Acesso ao gerenciamento de usuários do sistema.
            Controller responsável: UsuarioController.

            CONTROLE DE ACESSO:
            O item só é exibido para usuários com perfil ADMINISTRADOR.
            Isso evita que links indevidos apareçam para outros perfis,
            complementando a proteção já existente no UsuarioController.
        --%>
        <c:if test="${not empty sessionScope.usuarioLogado
                      and not empty sessionScope.usuarioLogado.perfilUsu
                      and sessionScope.usuarioLogado.perfilUsu.nomePerfil eq 'ADMINISTRADOR'}">
            <a href="${pageContext.request.contextPath}/UsuarioController" class="nav-item ${pageTitle == 'Usuários' ? 'active' : ''}">
                <i class="fa-solid fa-users"></i> Usuários
            </a>
        </c:if>

		<c:if test="${not empty sessionScope.usuarioLogado
                      and not empty sessionScope.usuarioLogado.perfilUsu
                      and sessionScope.usuarioLogado.perfilUsu.nomePerfil ne 'VISITANTE'}">
         <%--
            Acesso ao módulo de Equipamentos.
            Controller responsável: EquipamentoController.
        --%>
        <a href="${pageContext.request.contextPath}/EquipamentoController" class="nav-item ${pageTitle == 'Equipamentos' ? 'active' : ''}">
            <i class="fa-solid fa-computer"></i> Equipamentos
        </a>
        
        <%--
            Acesso ao cadastro e gerenciamento de unidades
            organizacionais ou locais vinculados ao patrimônio.
            Controller responsável: UnidadeController.
        --%>
        <a href="${pageContext.request.contextPath}/UnidadeController" class="nav-item ${pageTitle == 'Unidades' ? 'active' : ''}">
            <i class="fa-solid fa-building"></i> Unidades
        </a>

        <%--
            Acesso ao módulo de fabricantes de equipamentos.
            Controller responsável: FabricanteController.
        --%>
        <a href="${pageContext.request.contextPath}/FabricanteController" class="nav-item ${pageTitle == 'Fabricantes' ? 'active' : ''}">
            <i class="fa-solid fa-industry"></i> Fabricantes
        </a>

        <%--
            Acesso ao módulo de movimentações patrimoniais.
            Responsável por registrar transferências e alterações
            de localização dos equipamentos.
            Controller responsável: MovimentacaoController.
        --%>
        <a href="${pageContext.request.contextPath}/MovimentacaoController" class="nav-item ${pageTitle == 'Movimentações' ? 'active' : ''}">
            <i class="fa-solid fa-truck-moving"></i> Movimentações
        </a>
        
        <a href="${pageContext.request.contextPath}/ConfiguracaoController" class="nav-item ${pageTitle == 'Configurações' ? 'active' : ''}">
            <i class="fa-solid fa-gear"></i> Configurações
        </a>
        
        </c:if>
        
        <%--
            ─────────────────────────────────────────────────────
            SEÇÃO ADMINISTRATIVA — visível apenas para ADMINISTRADOR
            ─────────────────────────────────────────────────────
            Agrupa módulos de configuração e controle de acesso:
            Perfis, Permissões e Configurações do sistema.
        --%>
        <c:if test="${not empty sessionScope.usuarioLogado
                      and not empty sessionScope.usuarioLogado.perfilUsu
                      and sessionScope.usuarioLogado.perfilUsu.nomePerfil eq 'ADMINISTRADOR'}">

            <!-- <div class="nav-section-label">Administração</div> -->

            <a href="${pageContext.request.contextPath}/PerfilController" class="nav-item ${pageTitle == 'Perfis' ? 'active' : ''}">
                <i class="fa-solid fa-shield-halved"></i> Perfis
            </a>

              <a href="${pageContext.request.contextPath}/PermissaoController" class="nav-item ${pageTitle == 'Permissões' ? 'active' : ''}">
                <i class="fa-solid fa-key"></i> Permissões
            </a>

            <%--
                Acesso ao módulo de Configurações do sistema.

                CONTROLE DE ACESSO:
                Visível exclusivamente para usuários com perfil ADMINISTRADOR.
                Permite gerenciamento de perfil do usuário e parâmetros do sistema.
                Controller responsável: ConfiguracaoController.
            --%>
            

        </c:if>
    </ul>
</aside>

<%--
    Overlay utilizado em dispositivos móveis.

    FUNÇÃO:
    - Escurecer o restante da tela quando a sidebar estiver aberta.
    - Permitir fechamento da sidebar ao clicar fora dela.

    INTERAÇÃO:
    - Dispara a função JavaScript toggleSidebar(), responsável
      por alternar o estado visual da sidebar.

    PONTO CRÍTICO:
    - O ID deve permanecer consistente com o utilizado nos scripts
      JavaScript para garantir o funcionamento correto.
--%>
<div class="mobile-overlay" id="overlay" onclick="toggleSidebar()"></div>
