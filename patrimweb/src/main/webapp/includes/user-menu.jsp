<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%-- 
    =====================================================================
    COMPONENTE JSP: MENU DE USUÁRIO (USER MENU DROPDOWN)
    =====================================================================

    PROPÓSITO:
    Este trecho JSP representa o componente visual responsável por exibir
    o menu do usuário autenticado no sistema PatrimWeb. Ele apresenta o
    nome do usuário logado e disponibiliza ações relacionadas à sessão,
    como o logout.

    CONTEXTO DE USO:
    - Normalmente incluído em layouts principais (header/navbar).
    - Depende da existência de um objeto "usuarioLogado" armazenado
      no escopo de sessão (sessionScope).
    - Utiliza JSTL e Expression Language (EL) para renderização dinâmica.

    REGRAS DE NEGÓCIO IMPORTANTES:
    - O nome exibido no botão é obtido da sessão do usuário autenticado.
    - Caso não exista usuário logado ou o nome esteja vazio, o sistema
      apresenta o valor padrão "Admin".
    - O logout é realizado via requisição ao LogoutController.

    INTERAÇÕES COM BACK-END:
    - Leitura direta do objeto sessionScope.usuarioLogado.
    - Redirecionamento para controllers através do contextPath da aplicação.

    PONTOS CRÍTICOS:
    - A variável de sessão "usuarioLogado" deve estar previamente definida
      pelo processo de autenticação.
    - Caso a sessão expire, o fallback "Admin" evita erro de renderização.
    - O funcionamento do dropdown depende da função JavaScript
      toggleUserMenu(), que deve existir em arquivo JS carregado na página.

    TECNOLOGIAS UTILIZADAS:
    - JSP (Java Server Pages)
    - JSTL Core
    - Expression Language (EL)
    - Font Awesome (ícones)
    - JavaScript (controle de exibição do menu)
--%>

<div class="user-menu-container">
    
    <%-- 
        Botão principal do menu do usuário.
        Ao clicar, executa a função JavaScript toggleUserMenu(),
        responsável por abrir/fechar o dropdown.
    --%>
    <button class="user-btn" onclick="toggleUserMenu()">
        
        <%-- Ícone visual do usuário autenticado --%>
        <i class="fa-solid fa-circle-user"></i>
        
        <%-- 
            Exibição dinâmica do nome do usuário logado.

            REGRA DE DECISÃO (Expression Language):
            - Verifica se sessionScope.usuarioLogado.nomeUsu NÃO está vazio.
            - Se existir → mostra o nome do usuário.
            - Caso contrário → exibe "Admin" como valor padrão.

            Isso evita falhas visuais caso a sessão não possua usuário válido.
        --%>
        <span>${not empty sessionScope.usuarioLogado.nomeUsu ? sessionScope.usuarioLogado.nomeUsu : 'Admin'}</span>

        <%-- Ícone indicativo de menu expansível (dropdown) --%>
        <i class="fa-solid fa-chevron-down"></i>
    </button>

    <%-- 
        Container do menu dropdown.
        A visibilidade normalmente é controlada via JavaScript/CSS.
    --%>
    <div class="user-dropdown" id="userDropdown">

        <%-- 
            LINKS FUTUROS (FUNCIONALIDADES AINDA NÃO IMPLEMENTADAS)

            Estes links estão comentados propositalmente.
            Representam extensões planejadas do sistema:

            - Perfil do usuário
            - Configurações
            - Ajuda

            Cada opção aponta para um Controller específico.
            O uso de ${pageContext.request.contextPath} garante que
            a URL funcione independentemente do contexto da aplicação.
        --%>

        <!-- Descomente as linhas abaixo quando implementar as funcionalidades -->
        <!-- 
        <a href="${pageContext.request.contextPath}/PerfilController">
            <i class="fa-regular fa-user"></i> Meu Perfil
        </a>
        <a href="${pageContext.request.contextPath}/ConfiguracoesController">
            <i class="fa-solid fa-gear"></i> Configurações
        </a>
        <a href="${pageContext.request.contextPath}/AjudaController">
            <i class="fa-regular fa-circle-question"></i> Ajuda
        </a>
        <div class="dropdown-divider"></div>
        -->

        <%-- 
            AÇÃO DE LOGOUT

            REGRA DE NEGÓCIO:
            - Encerrar a sessão do usuário autenticado.
            - O LogoutController deve invalidar a sessão HTTP
              e redirecionar o usuário para a tela de login.

            O uso do contextPath evita problemas caso o sistema
            esteja implantado em subdiretórios do servidor.
        --%>
        <a href="${pageContext.request.contextPath}/LogoutController" class="text-danger">
            <i class="fa-solid fa-right-from-bracket"></i> Sair
        </a>
    </div>
</div>
