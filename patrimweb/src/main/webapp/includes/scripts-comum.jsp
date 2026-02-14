<%@ page contentType="text/html; charset=UTF-8" %>

<script>
    /*
        =====================================================================
        SCRIPT COMUM - CONTROLE DE INTERFACE (MENU DO USUÁRIO E SIDEBAR)
        =====================================================================

        PROPÓSITO:
        Este script centraliza a lógica JavaScript responsável por controlar
        componentes interativos da interface do sistema PatrimWeb, incluindo:

        - Abertura e fechamento do menu dropdown do usuário.
        - Fechamento automático do menu ao clicar fora da área ativa.
        - Controle de exibição da sidebar em dispositivos móveis.
        - Gerenciamento seguro de eventos evitando conflitos com onclick inline.

        REGRAS DE NEGÓCIO DE INTERFACE:
        - Apenas um dropdown deve permanecer aberto visualmente.
        - Cliques fora do menu devem fechá-lo automaticamente.
        - A sidebar mobile funciona por alternância de classes CSS.
        - Eventos inline são removidos para padronizar o uso de addEventListener.

        PONTOS CRÍTICOS:
        - Os elementos HTML devem possuir os IDs/classes esperados:
            • userDropdown
            • user-menu-container
            • sidebar
            • mobileOverlay
            • menu-toggle
        - A ausência dos elementos é tratada com validações condicionais
          para evitar erros JavaScript em páginas onde eles não existam.
    */


    // --- LÓGICA DO MENU DO USUÁRIO ---

    /*
        FUNÇÃO: toggleUserMenu()

        PROPÓSITO:
        Alternar a visibilidade do dropdown do usuário autenticado.

        FUNCIONAMENTO:
        - Localiza o elemento com id "userDropdown".
        - Alterna a classe CSS "show".
        - A exibição visual é controlada exclusivamente por CSS.

        PONTO CRÍTICO:
        - Depende da existência da classe CSS ".show"
          para controlar visibilidade/animação do menu.
    */
    function toggleUserMenu() {
        const dropdown = document.getElementById('userDropdown');
        dropdown.classList.toggle('show');
    }

    /*
        EVENTO GLOBAL DE CLIQUE (window)

        PROPÓSITO:
        Fechar automaticamente o menu dropdown quando o usuário
        clicar fora da área do menu.

        REGRA DE DECISÃO:
        - Verifica se o clique ocorreu fora do container do menu.
        - Caso positivo, remove a classe "show", ocultando o dropdown.

        VALIDAÇÕES:
        - Garante que o container exista antes da verificação.
        - Evita erro caso o componente não esteja presente na página.

        IMPORTANTE:
        - Utiliza contains() para validar hierarquia DOM,
          evitando fechamento ao clicar dentro do próprio menu.
    */
    window.addEventListener('click', function(e) {
        const container = document.querySelector('.user-menu-container');
        const dropdown = document.getElementById('userDropdown');
        
        if (container && !container.contains(e.target)) {
            dropdown.classList.remove('show');
        }
    });

    // --- LÓGICA DA SIDEBAR MOBILE ---

    
    /*
        FUNÇÃO: toggleSidebar()

        PROPÓSITO:
        Controlar abertura e fechamento da sidebar em dispositivos móveis.

        FUNCIONAMENTO:
        - Alterna a classe "active" na sidebar.
        - Alterna a classe "active" no overlay escuro de fundo.
        - O comportamento visual é definido via CSS responsivo.

        VALIDAÇÕES:
        - Verifica se os elementos existem antes da manipulação,
          evitando exceções JavaScript.

        INTERAÇÃO ENTRE COMPONENTES:
        - sidebar: painel lateral de navegação.
        - mobileOverlay: camada escura que bloqueia o fundo
          e permite fechar o menu ao clicar fora.
    */
    // Manter APENAS no scripts-comum.jsp
function toggleSidebar() {
    const sidebar = document.getElementById('sidebar');
    const overlay = document.getElementById('mobileOverlay');
    if (sidebar) sidebar.classList.toggle('active');
    if (overlay) overlay.classList.toggle('active');
}

    /*
        EVENTO: DOMContentLoaded

        PROPÓSITO:
        Garantir que os manipuladores de eventos sejam registrados
        somente após o carregamento completo da estrutura HTML.

        REGRAS IMPLEMENTADAS:
        1) Remove eventos onclick inline existentes.
        2) Padroniza o uso de addEventListener.
        3) Evita duplicidade de execução de eventos.
        4) Melhora manutenção e desacoplamento entre HTML e JS.

        ELEMENTOS CONTROLADOS:
        - Botão de abertura da sidebar (.menu-toggle)
        - Overlay mobile (#mobileOverlay)
    */
document.addEventListener('DOMContentLoaded', function() {

    /*
        Seleciona o botão responsável por abrir a sidebar.
        Caso exista:
        - Remove onclick inline previamente definido.
        - Associa o evento click via JavaScript.
    */
    const menuToggle = document.querySelector('.menu-toggle');
    if (menuToggle) {
        // Remove o onclick inline e usa só o listener
        menuToggle.removeAttribute('onclick');
        menuToggle.addEventListener('click', toggleSidebar);
    }

    /*
        Configura o comportamento do overlay mobile.

        REGRA DE INTERFACE:
        - Ao clicar fora da sidebar (overlay),
          o menu lateral deve ser fechado.
    */
    const overlay = document.getElementById('mobileOverlay');
    if (overlay) {
        overlay.removeAttribute('onclick');
        overlay.addEventListener('click', toggleSidebar);
    }
});
</script>
