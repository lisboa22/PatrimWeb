<%@ page contentType="text/html; charset=UTF-8" %>
<%
    /*
     * ============================================================
     * BLOCO DE CONTROLE DE ACESSO E POLÍTICA DE CACHE
     * ============================================================
     *
     * Objetivo:
     * - Impedir que usuários já autenticados acessem novamente
     *   a página de login.
     * - Evitar armazenamento em cache da página pelo navegador,
     *   garantindo que o usuário não consiga retornar ao login
     *   usando o botão "Voltar" após autenticação.
     */

    // 🔐 Recupera a sessão atual sem criar uma nova sessão caso não exista
    HttpSession sessao = request.getSession(false);

    /*
     * Regra de negócio:
     * Se existir uma sessão ativa E o atributo "usuarioLogado"
     * estiver presente, significa que o usuário já foi autenticado.
     *
     * A aplicação então redireciona automaticamente para o Dashboard,
     * evitando acesso indevido à tela de login.
     */
    if (sessao != null && sessao.getAttribute("usuarioLogado") != null) {
        response.sendRedirect("DashboardController");
        return; // Interrompe o processamento da página JSP
    }

    /*
     * 🧼 Configuração de headers HTTP para impedir cache:
     *
     * Cache-Control:
     *  - no-cache: força validação com o servidor
     *  - no-store: impede armazenamento local
     *  - must-revalidate: exige revalidação após expiração
     *
     * Pragma:
     *  - Compatibilidade com navegadores HTTP/1.0
     *
     * Expires:
     *  - Define data de expiração imediata (0)
     */
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
    response.setHeader("Pragma", "no-cache");
    response.setDateHeader("Expires", 0);
%>

<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">

    <!--
        Configuração responsiva para adaptação da interface
        em diferentes tamanhos de tela (desktop e mobile).
    -->
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <title>Login - PatrimWeb</title>

    <!--
        Arquivo CSS principal do sistema.
        Responsável pela identidade visual e layout da página de login.
    -->
    <link rel="stylesheet" href="css/patrimweb.css">

    <!--
        Biblioteca oficial do Google Identity Services.
        Necessária para renderização do botão e autenticação via Google OAuth.
    -->
    <script src="https://accounts.google.com/gsi/client" async defer></script>
</head>

<body class="login-page"> 

    <!--
        Container principal da tela de login.
        Centraliza visualmente o conteúdo da autenticação.
    -->
    <div class="login-container">

        <!--
            Cabeçalho institucional do sistema.
            Exibe logotipo em SVG e nome do sistema.
        -->
        <header class="login-header">
            <svg class="logo-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"></path>
                <polyline points="3.27 6.96 12 12.01 20.73 6.96"></polyline>
                <line x1="12" y1="22.08" x2="12" y2="12"></line>
            </svg>
            <div class="system-name">
                Sistema de Patrimônio<br>e Movimentações
            </div>
        </header>

        <!--
            Card principal contendo os mecanismos de autenticação.
        -->
        <main class="login-card">
            <h2 class="card-title">Acesso ao Sistema</h2>
            
            <!--
                Configuração inicial do Google Sign-In.
                data-client_id:
                    Identificador da aplicação registrado no Google Cloud.
                data-callback:
                    Função JavaScript executada após autenticação bem-sucedida.
            -->
            <div id="g_id_onload"
                 data-client_id="${clientId}"
                 data-callback="handleGoogleLogin">
            </div>
           
            <!--
                Renderização do botão padrão de login do Google.
                Os atributos data-* controlam aparência e comportamento.
            -->
            <div class="google-login">
                <div class="g_id_signin" data-type="standard" data-shape="rectangular" data-theme="outline" data-text="signin_with" data-size="large" data-width="100%"></div>
            </div>
            
            <!--
                Formulário de autenticação tradicional (usuário e senha).
                action:
                    Envia requisição POST para LoginController.
                contextPath:
                    Garante funcionamento independente do contexto da aplicação.
            -->
            <form id="formLogin" action="${pageContext.request.contextPath}/LoginController" method="post">

                <!--
                    Campo oculto utilizado para identificar a ação
                    a ser executada no controller.
                    Regra de negócio definida no backend.
                -->
                <input type="hidden" name="action" value="adicionar">
    
                <!--
                    Campo de entrada do usuário (email ou username).
                    required:
                        Validação HTML obrigando preenchimento antes do envio.
                -->
                <div class="login-input-group">
                    <svg class="input-icon-left" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
                    </svg>
                    <input name="usuario" type="text" placeholder="E-mail ou Nome de Usuário" required>
                </div>

                <!--
                    Campo de senha com funcionalidade de alternar visibilidade.
                    Inclui ícone SVG clicável controlado via JavaScript.
                -->
                <div class="login-input-group">
                    <svg class="input-icon-left" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                    </svg>
                    <input name="senha" type="password" id="password" placeholder="Senha" required>

                    <!--
                        Ícone responsável por alternar entre senha visível e oculta.
                    -->
                    <svg id="togglePassword" class="toggle-password" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                    </svg>
                </div>

                <!-- Área reservada para futuras opções adicionais -->
                <div class="login-options">
                    </div>

                <!--
                    Botão responsável por submeter o formulário
                    e iniciar o processo de autenticação no backend.
                -->
                <button type="submit" class="btn-login-submit">Entrar</button>

                <!-- Área reservada para links auxiliares -->
                <div class="login-footer-links">
                    </div>
            </form>
        </main>
    </div>

    <script>
        /*
         * ============================================================
         * CONTROLE DE VISUALIZAÇÃO DA SENHA
         * ============================================================
         *
         * Permite ao usuário alternar entre visualizar ou ocultar
         * o conteúdo do campo de senha.
         */

        // Seleciona elementos do DOM utilizados na funcionalidade
        const togglePassword = document.querySelector('#togglePassword');
        const passwordInput = document.querySelector('#password');

        /*
         * Evento de clique responsável por alternar o tipo do input:
         * password -> text (visível)
         * text -> password (oculto)
         */
        togglePassword.addEventListener('click', function () {

            // Estrutura condicional que alterna dinamicamente o tipo do campo
            const type = passwordInput.getAttribute('type') === 'password' ? 'text' : 'password';

            // Atualiza atributo do campo input
            passwordInput.setAttribute('type', type);

            /*
             * Regra visual:
             * Altera a cor do ícone indicando estado ativo/inativo,
             * mantendo consistência com variáveis visuais do sistema.
             */
            this.style.color = type === 'text' ? '#3B82F6' : '#718096';
        });

        /*
         * ============================================================
         * CALLBACK DE AUTENTICAÇÃO GOOGLE
         * ============================================================
         *
         * Função executada automaticamente após login bem-sucedido
         * via Google Identity Services.
         *
         * Parâmetro:
         * response -> objeto retornado pelo Google contendo o
         *             token JWT de autenticação do usuário.
         */
        function handleGoogleLogin(response) {

            /*
             * Envia o token recebido para o backend através do
             * LoginGoogleController para validação server-side.
             *
             * Interação com backend:
             * - Método HTTP: POST
             * - Content-Type: application/json
             * - Corpo: token JWT do Google
             */
            fetch("${pageContext.request.contextPath}/LoginGoogleController", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ token: response.credential })
            })
            .then(res => res.json())
            .then(data => {

                /*
                 * Regra de negócio:
                 * Caso o backend valide o usuário com sucesso,
                 * redireciona para o Dashboard do sistema.
                 */
                if (data.sucesso) {
                    window.location.href = "${pageContext.request.contextPath}/DashboardController";
                } else {
                    // Tratamento simples de falha de autenticação
                    alert("Erro ao autenticar com o Google");
                }
            });
        }
    </script>
</body>
</html>
