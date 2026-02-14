<!DOCTYPE html>
<html lang="pt-br">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Configurações - PatrimWeb</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600&display=swap" rel="stylesheet">
    
    <style>
        /* --- ESTILOS GERAIS --- */
        :root {
            --sidebar-bg: #343a40;
            --sidebar-hover: #495057;
            --primary-blue: #3b82f6;
            --primary-hover: #2563eb;
            --bg-color: #f3f4f6;
            --text-dark: #1f2937;
            --text-muted: #6b7280;
            --white: #ffffff;
            --border-color: #e5e7eb;
            --danger: #ef4444;
            --success: #10b981;
        }

        * { margin: 0; padding: 0; box-sizing: border-box; font-family: 'Inter', sans-serif; }
        body { display: flex; background-color: var(--bg-color); height: 100vh; overflow: hidden; }

        /* Sidebar & Layout */
        .sidebar { width: 260px; background-color: var(--sidebar-bg); color: #c2c7d0; display: flex; flex-direction: column; transition: all 0.3s; z-index: 1000; }
        .logo-area { height: 60px; display: flex; align-items: center; padding: 0 20px; border-bottom: 1px solid #4b545c; background-color: var(--sidebar-bg); }
        .logo-icon { width: 35px; height: 35px; background-color: var(--primary-blue); border-radius: 5px; display: flex; align-items: center; justify-content: center; color: white; font-size: 20px; margin-right: 10px; }
        .nav-menu { list-style: none; margin-top: 20px; flex-grow: 1; overflow-y: auto; }
        .nav-item { padding: 12px 20px; cursor: pointer; display: flex; align-items: center; transition: background 0.2s; text-decoration: none; color: inherit; }
        .nav-item:hover, .nav-item.active { background-color: var(--sidebar-hover); color: var(--white); }
        .nav-item i { width: 25px; margin-right: 10px; text-align: center; }
        
        .main-content { flex: 1; display: flex; flex-direction: column; overflow-y: auto; position: relative; }
        header { background-color: var(--bg-color); padding: 20px 30px; display: flex; justify-content: space-between; align-items: center; flex-shrink: 0; }
        h1 { font-size: 24px; color: var(--text-dark); font-weight: 600; }
        
        /* --- ESTILOS DO MENU DE USUÁRIO (CORRIGIDO) --- */
        .user-menu-container { position: relative; } /* Necessário para posicionar o menu */
        
        .user-btn { background: white; border: 1px solid var(--border-color); padding: 8px 15px; border-radius: 20px; cursor: pointer; display: flex; align-items: center; gap: 10px; font-size: 14px; transition: 0.2s; }
        .user-btn:hover { background-color: #f9fafb; border-color: #d1d5db; }
        
        .user-dropdown {
            display: none; /* Escondido por padrão */
            position: absolute;
            right: 0;
            top: 50px;
            background: white;
            border: 1px solid var(--border-color);
            border-radius: 8px;
            box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
            width: 200px;
            z-index: 1001;
            overflow: hidden;
            animation: fadeIn 0.2s ease-out;
        }

        .user-dropdown.show { display: block; }

        .user-dropdown a {
            display: flex;
            align-items: center;
            padding: 12px 16px;
            color: var(--text-dark);
            text-decoration: none;
            font-size: 14px;
            transition: background 0.2s;
            gap: 10px;
        }

        .user-dropdown a:hover { background-color: #f3f4f6; color: var(--primary-blue); }
        .user-dropdown i { width: 18px; text-align: center; color: var(--text-muted); }
        .user-dropdown a:hover i { color: var(--primary-blue); }
        
        .dropdown-divider { height: 1px; background-color: var(--border-color); margin: 0; }
        .text-danger { color: var(--danger) !important; }
        .text-danger:hover { background-color: #fef2f2 !important; }
        .text-danger i { color: var(--danger) !important; }

        @keyframes fadeIn {
            from { opacity: 0; transform: translateY(-10px); }
            to { opacity: 1; transform: translateY(0); }
        }
        /* ------------------------------------------- */

        .dashboard-container { padding: 0 30px 30px 30px; }
        
        /* Botões */
        .btn { padding: 10px 20px; border-radius: 6px; border: none; font-size: 14px; font-weight: 500; cursor: pointer; transition: background 0.2s; display: inline-flex; align-items: center; justify-content: center; gap: 8px; }
        .btn-primary { background-color: var(--primary-blue); color: white; }
        .btn-primary:hover { background-color: var(--primary-hover); }
        .btn-outline { background-color: white; border: 1px solid var(--border-color); color: var(--text-dark); }
        .btn-outline:hover { background-color: #f3f4f6; }

        /* Formulário */
        .form-group { margin-bottom: 15px; }
        .form-label { display: block; margin-bottom: 6px; font-size: 14px; font-weight: 500; color: var(--text-dark); }
        .form-input { width: 100%; padding: 10px 12px; border: 1px solid var(--border-color); border-radius: 6px; font-size: 14px; color: var(--text-dark); transition: border-color 0.2s; }
        .form-input:focus { outline: none; border-color: var(--primary-blue); box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1); }

        /* --- ESTILOS NOVOS PARA CONFIGURAÇÕES --- */

        /* Container Branco */
        .settings-card {
            background: white;
            border-radius: 8px;
            box-shadow: 0 1px 3px rgba(0,0,0,0.05);
            overflow: hidden;
            display: flex;
            flex-direction: column;
            height: 100%; /* Ocupa altura disponível se necessário */
            max-height: fit-content;
        }

        /* Abas de Navegação */
        .settings-tabs {
            display: flex;
            border-bottom: 1px solid var(--border-color);
            padding: 0 20px;
            background-color: #f9fafb;
            overflow-x: auto; /* Rolagem horizontal no mobile */
            white-space: nowrap;
            -webkit-overflow-scrolling: touch;
        }

        .tab-btn {
            padding: 15px 20px;
            background: none;
            border: none;
            border-bottom: 2px solid transparent;
            font-size: 14px;
            font-weight: 500;
            color: var(--text-muted);
            cursor: pointer;
            transition: all 0.2s;
            flex-shrink: 0; /* Impede encolhimento no mobile */
        }

        .tab-btn:hover { color: var(--primary-blue); }
        .tab-btn.active { color: var(--primary-blue); border-bottom-color: var(--primary-blue); }

        /* Conteúdo das Abas */
        .tab-content { display: none; padding: 30px; }
        .tab-content.active { display: block; animation: fadeIn 0.3s; }
        @keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }

        /* Seção de Perfil */
        .profile-header { display: flex; align-items: center; gap: 20px; margin-bottom: 30px; }
        .profile-img-container { position: relative; width: 80px; height: 80px; }
        .profile-img { width: 100%; height: 100%; border-radius: 50%; object-fit: cover; border: 3px solid var(--bg-color); }
        .profile-edit-badge { position: absolute; bottom: 0; right: 0; background: var(--primary-blue); color: white; width: 28px; height: 28px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 12px; cursor: pointer; border: 2px solid white; }
        
        /* Grid de Configurações */
        .settings-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 30px; }
        .section-title { font-size: 16px; font-weight: 600; color: var(--text-dark); margin-bottom: 15px; border-bottom: 1px solid var(--border-color); padding-bottom: 10px; }

        /* Toggle Switch */
        .toggle-group { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; padding: 10px 0; }
        .toggle-info h4 { font-size: 14px; font-weight: 500; color: var(--text-dark); }
        .toggle-info p { font-size: 12px; color: var(--text-muted); margin-top: 2px; }
        
        .switch { position: relative; display: inline-block; width: 44px; height: 24px; flex-shrink: 0; margin-left: 10px; }
        .switch input { opacity: 0; width: 0; height: 0; }
        .slider { position: absolute; cursor: pointer; top: 0; left: 0; right: 0; bottom: 0; background-color: #ccc; transition: .4s; border-radius: 34px; }
        .slider:before { position: absolute; content: ""; height: 18px; width: 18px; left: 3px; bottom: 3px; background-color: white; transition: .4s; border-radius: 50%; }
        input:checked + .slider { background-color: var(--primary-blue); }
        input:checked + .slider:before { transform: translateX(20px); }

        /* Zona de Perigo */
        .danger-zone { border: 1px solid #fee2e2; background-color: #fef2f2; border-radius: 6px; padding: 20px; margin-top: 20px; }
        .danger-title { color: var(--danger); font-weight: 600; margin-bottom: 10px; display: flex; align-items: center; gap: 8px; }
        .btn-danger { background-color: white; border: 1px solid var(--danger); color: var(--danger); }
        .btn-danger:hover { background-color: var(--danger); color: white; }
        
        .danger-actions { display: flex; gap: 15px; }

        /* Overlay Mobile Menu */
        .mobile-overlay {
            display: none;
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(0, 0, 0, 0.5);
            z-index: 900;
        }
        .mobile-overlay.active { display: block; }

        .menu-toggle { display: none; cursor: pointer; font-size: 20px; margin-right: 15px; }

        /* --- MEDIA QUERIES --- */
        @media (max-width: 768px) {
            .sidebar { position: absolute; left: -260px; height: 100%; }
            .sidebar.active { left: 0; }
            .menu-toggle { display: block !important; }

            .dashboard-container { padding: 15px; }
            header { padding: 15px; }
            h1 { font-size: 18px; }
            .user-btn span { display: none; }

            /* Grid 1 coluna */
            .settings-grid { grid-template-columns: 1fr; gap: 15px; }
            
            /* Ajuste de Tabs */
            .tab-content { padding: 20px; }
            
            /* Perfil centralizado */
            .profile-header { flex-direction: column; text-align: center; }
            
            /* Botões Full Width */
            .btn { width: 100%; margin-top: 5px; }
            
            /* Zona de Perigo empilhada */
            .danger-actions { flex-direction: column; gap: 10px; }
        }
    </style>
</head>
<body>

    <div class="mobile-overlay" id="mobileOverlay" onclick="toggleSidebar()"></div>

    <aside class="sidebar" id="sidebar">
        <div class="logo-area">
            <svg class="logo-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"></path>
                <polyline points="3.27 6.96 12 12.01 20.73 6.96"></polyline>
                <line x1="12" y1="22.08" x2="12" y2="12"></line>
            </svg>
            <span style="font-weight: 600; color: white;">PatrimWeb</span>
        </div>
        <ul class="nav-menu">
            <a href="dashboard.html" class="nav-item"><i class="fa-solid fa-house"></i> Dashboard</a>
            <a href="equipamentos.html" class="nav-item"><i class="fa-solid fa-computer"></i> Equipamentos</a>
            <a href="${pageContext.request.contextPath}/UsuarioController" class="nav-item active"><i class="fa-solid fa-users"></i> Usu�rios</a>
            <a href="unidades.html" class="nav-item"><i class="fa-solid fa-building"></i> Unidades</a>
            <a href="fabricantes.html" class="nav-item"><i class="fa-solid fa-industry"></i> Fabricantes</a>
            <a href="movimentacoes.html" class="nav-item"><i class="fa-solid fa-truck-moving"></i> Movimentações</a>
            <!--<a href="relatorio_equipamentos.html" class="nav-item"><i class="fa-solid fa-file-lines"></i> Relatórios</a>-->
            <a href="configuracoes.html" class="nav-item active"><i class="fa-solid fa-gear"></i> Configurações</a>
        </ul>
    </aside>

    <main class="main-content">
        <header>
            <div style="display: flex; align-items: center;">
                <i class="fa-solid fa-bars menu-toggle" onclick="toggleSidebar()"></i>
                <h1>Configurações do Sistema</h1>
            </div>
            
            <div class="user-menu-container">
                <button class="user-btn" onclick="toggleUserMenu()">
                    <i class="fa-solid fa-circle-user" style="font-size: 18px;"></i>
                    <span>Admin</span>
                    <i class="fa-solid fa-chevron-down" style="font-size: 12px;"></i>
                </button>

                <div class="user-dropdown" id="userDropdown">
                    <a href="#"><i class="fa-regular fa-user"></i> Meu Perfil</a>
                    <a href="#"><i class="fa-solid fa-gear"></i> Configurações</a>
                    <a href="#"><i class="fa-regular fa-circle-question"></i> Ajuda</a>
                    <div class="dropdown-divider"></div>
                    <a href="#" class="text-danger"><i class="fa-solid fa-right-from-bracket"></i> Sair</a>
                </div>
            </div>
        </header>

        <div class="dashboard-container">
            
            <div class="settings-card">
                <div class="settings-tabs">
                    <button class="tab-btn active" onclick="openTab(event, 'tab-geral')">Geral</button>
                    <button class="tab-btn" onclick="openTab(event, 'tab-perfil')">Meu Perfil</button>
                    <button class="tab-btn" onclick="openTab(event, 'tab-notificacoes')">Notificações</button>
                    <button class="tab-btn" onclick="openTab(event, 'tab-seguranca')">Segurança</button>
                </div>

                <div id="tab-geral" class="tab-content active">
                    <div class="settings-grid">
                        <div>
                            <div class="section-title">Preferências de Exibição</div>
                            <div class="form-group">
                                <label class="form-label">Nome da Empresa</label>
                                <input type="text" class="form-input" value="PatrimWeb Corp">
                            </div>
                            <div class="form-group">
                                <label class="form-label">Idioma do Sistema</label>
                                <select class="form-input">
                                    <option>Português (Brasil)</option>
                                    <option>English (US)</option>
                                    <option>Español</option>
                                </select>
                            </div>
                        </div>
                        <div>
                            <div class="section-title">Personalização</div>
                            <div class="form-group">
                                <label class="form-label">Tema</label>
                                <select class="form-input">
                                    <option>Claro (Padrão)</option>
                                    <option>Escuro</option>
                                    <option>Sistema</option>
                                </select>
                            </div>
                            <div class="form-group">
                                <label class="form-label">Itens por Página (Tabelas)</label>
                                <select class="form-input">
                                    <option>10</option>
                                    <option selected>25</option>
                                    <option>50</option>
                                    <option>100</option>
                                </select>
                            </div>
                        </div>
                    </div>
                    <div style="margin-top: 20px; text-align: right;">
                        <button class="btn btn-primary">Salvar Alterações</button>
                    </div>
                </div>

                <div id="tab-perfil" class="tab-content">
                    <div class="profile-header">
                        <div class="profile-img-container">
                            <img src="https://ui-avatars.com/api/?name=Admin+User&background=3b82f6&color=fff" alt="Perfil" class="profile-img">
                            <div class="profile-edit-badge"><i class="fa-solid fa-camera"></i></div>
                        </div>
                        <div>
                            <h3 style="font-size: 18px; font-weight: 600;">Usuário Admin</h3>
                            <p style="color: var(--text-muted); font-size: 14px;">Administrador do Sistema</p>
                        </div>
                    </div>

                    <div class="settings-grid">
                        <div class="form-group">
                            <label class="form-label">Nome Completo</label>
                            <input type="text" class="form-input" value="Usuário Admin">
                        </div>
                        <div class="form-group">
                            <label class="form-label">E-mail</label>
                            <input type="email" class="form-input" value="admin@patrimweb.com">
                        </div>
                        <div class="form-group">
                            <label class="form-label">Telefone</label>
                            <input type="tel" class="form-input" value="(11) 99999-9999">
                        </div>
                        <div class="form-group">
                            <label class="form-label">Cargo</label>
                            <input type="text" class="form-input" value="Gerente de TI" disabled style="background-color: #f3f4f6;">
                        </div>
                    </div>
                    <div style="margin-top: 20px; text-align: right;">
                        <button class="btn btn-primary">Atualizar Perfil</button>
                    </div>
                </div>

                <div id="tab-notificacoes" class="tab-content">
                    <div class="section-title">Alertas por E-mail</div>
                    
                    <div class="toggle-group">
                        <div class="toggle-info">
                            <h4>Novos Usuários</h4>
                            <p>Receber e-mail quando um novo usuário for cadastrado.</p>
                        </div>
                        <label class="switch">
                            <input type="checkbox" checked>
                            <span class="slider"></span>
                        </label>
                    </div>

                    <div class="toggle-group">
                        <div class="toggle-info">
                            <h4>Movimentação de Equipamento</h4>
                            <p>Notificar quando houver transferência de patrimônio.</p>
                        </div>
                        <label class="switch">
                            <input type="checkbox" checked>
                            <span class="slider"></span>
                        </label>
                    </div>

                    <div class="toggle-group">
                        <div class="toggle-info">
                            <h4>Relatórios Mensais</h4>
                            <p>Receber resumo automático todo dia 01.</p>
                        </div>
                        <label class="switch">
                            <input type="checkbox">
                            <span class="slider"></span>
                        </label>
                    </div>
                </div>

                <div id="tab-seguranca" class="tab-content">
                    <div class="section-title">Alterar Senha</div>
                    <div class="settings-grid">
                        <div class="form-group">
                            <label class="form-label">Senha Atual</label>
                            <input type="password" class="form-input">
                        </div>
                        <div></div> <div class="form-group">
                            <label class="form-label">Nova Senha</label>
                            <input type="password" class="form-input">
                        </div>
                        <div class="form-group">
                            <label class="form-label">Confirmar Nova Senha</label>
                            <input type="password" class="form-input">
                        </div>
                    </div>
                    <button class="btn btn-outline" style="margin-top: 10px;">Atualizar Senha</button>

                    <div class="danger-zone">
                        <div class="danger-title">
                            <i class="fa-solid fa-triangle-exclamation"></i> Zona de Perigo
                        </div>
                        <p style="font-size: 14px; color: var(--text-dark); margin-bottom: 15px;">
                            Essas ações são irreversíveis. Tenha certeza antes de continuar.
                        </p>
                        <div class="danger-actions">
                            <button class="btn btn-danger">Limpar Cache do Sistema</button>
                            <button class="btn btn-danger">Resetar Configurações de Fábrica</button>
                        </div>
                    </div>
                </div>

            </div>

        </div>
    </main>

    <script>
        // Lógica de Troca de Abas
        function openTab(evt, tabName) {
            var i, tabContent, tabBtns;
            
            // Esconde todo o conteúdo das abas
            tabContent = document.getElementsByClassName("tab-content");
            for (i = 0; i < tabContent.length; i++) {
                tabContent[i].style.display = "none";
                tabContent[i].classList.remove("active");
            }

            // Remove a classe active de todos os botões
            tabBtns = document.getElementsByClassName("tab-btn");
            for (i = 0; i < tabBtns.length; i++) {
                tabBtns[i].className = tabBtns[i].className.replace(" active", "");
            }

            // Mostra a aba atual e adiciona classe active ao botão clicado
            document.getElementById(tabName).style.display = "block";
            // Pequeno delay para permitir a animação CSS
            setTimeout(() => {
                document.getElementById(tabName).classList.add("active");
            }, 10);
            
            evt.currentTarget.className += " active";
        }

        // Toggle Sidebar Mobile
        function toggleSidebar() {
            const sidebar = document.getElementById('sidebar');
            const overlay = document.getElementById('mobileOverlay');
            sidebar.classList.toggle('active');
            overlay.classList.toggle('active');
        }

        // ---------------------------------------------
        // --- LÓGICA DO MENU DO USUÁRIO (NOVA) ---
        // ---------------------------------------------
        function toggleUserMenu() {
            const dropdown = document.getElementById('userDropdown');
            dropdown.classList.toggle('show');
        }

        // Fechar o menu se clicar fora dele
        window.addEventListener('click', function(e) {
            const container = document.querySelector('.user-menu-container');
            const dropdown = document.getElementById('userDropdown');
            
            if (!container.contains(e.target)) {
                dropdown.classList.remove('show');
            }
        });
        // ---------------------------------------------
    </script>
</body>
</html>