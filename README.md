# Documentação Técnica - PatrimWeb

**Sistema de Gerenciamento de Patrimônio e Movimentações**

---

## 1. Visão Geral do Sistema

### 1.1 Nome do Sistema
**PatrimWeb** – Sistema de Patrimônio e Movimentações

### 1.2 Objetivo Principal
PatrimWeb é uma aplicação web voltada ao gerenciamento centralizado de patrimônio organizacional, permitindo o registro, controle, movimentação e rastreamento de equipamentos e ativos em uma instituição.

### 1.3 Problema que o Sistema Resolve
Organizações enfrentam desafios significativos no controle de patrimônio:
- Falta de visibilidade sobre equipamentos e sua localização
- Dificuldade em rastrear movimentações de ativos
- Ausência de histórico detalhado de manutenção e movimentação
- Impossibilidade de gerar relatórios gerenciais sobre patrimônio
- Risco de perda ou deterioração de equipamentos sem acompanhamento

PatrimWeb centraliza essas operações em uma plataforma integrada, fornecendo rastreabilidade completa e relatórios gerenciais.

### 1.4 Contexto de Uso
O sistema é utilizado em ambientes corporativos e educacionais onde há necessidade de:
- Manutenção de inventário de equipamentos de tecnologia
- Acompanhamento de movimentações internas entre departamentos/unidades
- Geração de relatórios de controle patrimonial
- Auditoria de ativos organizacionais

### 1.5 Público-Alvo
- **Gerentes de TI/Patrimônio**: Tomam decisões baseadas em relatórios
- **Operadores do Sistema**: Registram equipamentos e movimentações
- **Administradores**: Gerenciam usuários e configurações do sistema
- **Auditores**: Consultam históricos e geram relatórios

### 1.6 Motivação do Projeto
O projeto foi desenvolvido como solução acadêmica para demonstrar competências em desenvolvimento web full-stack, aplicando padrões de arquitetura em camadas (MVC), segurança em aplicações web e integração com banco de dados relacional.

### 1.7 Benefícios Proporcionados
✅ **Centralização de Dados**: Informações de patrimônio em um único local seguro  
✅ **Rastreabilidade Completa**: Histórico detalhado de cada equipamento  
✅ **Relatórios Gerenciais**: Análises por período, unidade e equipamento  
✅ **Segurança de Acesso**: Autenticação obrigatória e controle de sessão  
✅ **Facilidade de Uso**: Interface intuitiva com navegação clara  

---

## 2. Visão Funcional

### 2.1 Descrição Geral das Funcionalidades
PatrimWeb é estruturado em módulos funcionais que cobrem o ciclo completo de gerenciamento patrimonial, desde o cadastro até a geração de relatórios.

### 2.2 Lista Completa de Funcionalidades

#### **Módulo de Autenticação**
- ✓ Login com credenciais (usuário/email + senha)
- ✓ Integração com Google Sign-In (preparada para implementação)
- ✓ Controle de sessão HTTP
- ✓ Proteção contra acesso não autenticado
- ✓ Logout com invalidação de sessão

#### **Módulo de Equipamentos**
- ✓ Cadastro de novo equipamento
- ✓ Edição de informações de equipamento
- ✓ Exclusão de equipamento
- ✓ Listagem com paginação
- ✓ Associação de equipamento a fabricante
- ✓ Visualização de histórico por equipamento

#### **Módulo de Fabricantes**
- ✓ Cadastro de fabricante
- ✓ Edição de dados de fabricante
- ✓ Exclusão com validação de referências (Foreign Key)
- ✓ Listagem completa de fabricantes
- ✓ Proteção contra exclusão se equipamentos vinculados

#### **Módulo de Usuários**
- ✓ Cadastro de novo usuário
- ✓ Edição de perfil de usuário
- ✓ Exclusão de usuário
- ✓ Listagem de todos os usuários
- ✓ Filtro por período de cadastro
- ✓ Relatório com exportação de dados

#### **Módulo de Unidades**
- ✓ Cadastro de unidade organizacional
- ✓ Edição de unidade
- ✓ Exclusão de unidade
- ✓ Listagem de unidades
- ✓ Atribuição de equipamentos a unidades

#### **Módulo de Movimentações**
- ✓ Registro de movimentação de equipamento
- ✓ Especificação de origem e destino
- ✓ Data e hora de movimentação
- ✓ Edição de movimentação
- ✓ Cancelamento/exclusão de movimentação
- ✓ Histórico completo de movimentações

#### **Módulo de Dashboard**
- ✓ Visão geral de estatísticas
- ✓ Quantidade de equipamentos
- ✓ Quantidade de movimentações
- ✓ Informações do usuário logado
- ✓ Acesso rápido aos módulos principais

#### **Módulo de Relatórios**
- ✓ Relatório de equipamentos por fabricante
- ✓ Relatório de usuários cadastrados
- ✓ Filtro por período
- ✓ Exportação em formato adequado
- ✓ Visualização tabulada com formatação

#### **Módulo de Configurações**
- ✓ Edição de perfil do usuário logado
- ✓ Alteração de preferências de visualização
- ✓ Ajustes de notificações (preparado)
- ✓ Informações do sistema

### 2.3 Explicação do que Cada Funcionalidade Realiza

**Cadastro de Equipamento**: Permite que operadores registrem novos equipamentos no sistema, associando-os a um fabricante e uma unidade. Gera timestamp automático de inserção para auditoria.

**Movimentação de Equipamento**: Registra quando um equipamento é deslocado entre unidades/departamentos, criando histórico rastreável para fins de auditoria e localização.

**Autenticação**: Valida credenciais do usuário contra o banco de dados, cria sessão HTTP segura e impede acesso a áreas protegidas do sistema.

**Relatórios**: Agrupa e apresenta dados conforme critérios de filtro (período, unidade, fabricante), permitindo análises gerenciais e tomada de decisão.

### 2.4 Fluxo Geral de Utilização pelo Usuário

```
┌─────────────┐
│  Acesso ao  │
│   Sistema   │
└──────┬──────┘
       │
       ▼
┌─────────────────┐
│   Tela Login    │
│ (index.jsp)     │
└──────┬──────────┘
       │ Autenticação
       │ LoginController
       ▼
┌─────────────────┐
│    Dashboard    │
│ Dashboard.jsp   │
└────┬──────┬────┬────┬──────┐
     │      │    │    │      │
     ▼      ▼    ▼    ▼      ▼
  Equip. Fabric. Usu. Movim. Relat.
  
     │      │    │    │      │
     └──────┴────┴────┴──────┘
            │
     Operações CRUD
     Controllers
            │
            ▼
     DAO Layer
            │
            ▼
    MySQL Database
```

**Fluxo típico do usuário:**
1. Acessa a URL raiz do sistema
2. Se não autenticado, vê a página de login (index.jsp)
3. Insere credenciais e submete formulário
4. LoginController valida credenciais e cria sessão
5. Se válido, redireciona para Dashboard
6. Usuário acessa módulos conforme necessidade
7. Cada ação (CRUD) passa pelo Controller correspondente
8. Controller interage com DAO para banco de dados
9. Resultado retorna à view (JSP)
10. Usuário pode consultar relatórios ou logout

---

## 3. Arquitetura do Sistema

### 3.1 Tipo de Arquitetura

PatrimWeb segue o **padrão de arquitetura em três camadas (MVC - Model-View-Controller)**:

```
┌─────────────────────────────────────┐
│     CAMADA DE APRESENTAÇÃO (VIEW)   │
│  JSP, CSS, JavaScript, HTML         │
├─────────────────────────────────────┤
│   CAMADA DE NEGÓCIO/CONTROLE        │
│  Servlets (Controllers)             │
├─────────────────────────────────────┤
│    CAMADA DE PERSISTÊNCIA (MODEL)   │
│  DAO, Entities, Banco de Dados      │
└─────────────────────────────────────┘
```

### 3.2 Organização dos Pacotes e Diretórios

```
patrimweb/
├── src/main/
│   ├── java/br/com/patrimweb/
│   │   ├── controller/       (Servlets - Controle)
│   │   │   ├── LoginController.java
│   │   │   ├── DashboardController.java
│   │   │   ├── EquipamentoController.java
│   │   │   ├── FabricanteController.java
│   │   │   ├── UsuarioController.java
│   │   │   ├── UnidadeController.java
│   │   │   ├── MovimentacaoController.java
│   │   │   └── RelatorioController.java
│   │   │
│   │   ├── model/           (Modelos de Dados - View Model)
│   │   │   ├── Usuario.java
│   │   │   ├── Equipamento.java
│   │   │   ├── Fabricante.java
│   │   │   ├── Unidade.java
│   │   │   └── Movimentacao.java
│   │   │
│   │   ├── dao/            (Acesso a Dados - Model)
│   │   │   ├── DAOGenerico.java
│   │   │   ├── UsuarioDAO.java
│   │   │   ├── EquipamentoDAO.java
│   │   │   ├── FabricanteDAO.java
│   │   │   ├── UnidadeDAO.java
│   │   │   └── MovimentacaoDAO.java
│   │   │
│   │   └── utils/          (Utilitários)
│   │       └── Conexao.java
│   │
│   └── webapp/
│       ├── index.jsp           (Login)
│       ├── dashboard.jsp       (Dashboard)
│       ├── equipamentos.jsp    (Gerenciamento de Equipamentos)
│       ├── fabricantes.jsp     (Gerenciamento de Fabricantes)
│       ├── usuarios.jsp        (Gerenciamento de Usuários)
│       ├── unidades.jsp        (Gerenciamento de Unidades)
│       ├── movimentacoes.jsp   (Movimentações)
│       ├── configuracoes.jsp   (Configurações)
│       ├── css/
│       │   └── patrimweb.css   (Estilos)
│       └── WEB-INF/
│           └── web.xml         (Configuração)
│
├── lib/
│   ├── mysql-connector-j-9.2.0/  (Driver MySQL)
│   └── configuracao/
│       └── database.properties    (Credenciais DB)
│
└── Servers/                    (Tomcat Server)
    └── Tomcat v9.0
```

### 3.3 Responsabilidade de Cada Camada

#### **Camada de Apresentação (View - JSP)**
- **Responsabilidade**: Exibir dados ao usuário e capturar entrada
- **Tecnologias**: JSP, HTML5, CSS3, JavaScript
- **Componentes**:
  - `index.jsp`: Página de login com formulário de autenticação
  - `dashboard.jsp`: Painel principal com menu de navegação
  - `equipamentos.jsp`: Tabela CRUD de equipamentos
  - `fabricantes.jsp`: Gerenciamento de fabricantes
  - `usuarios.jsp`: Gerenciamento de usuários com relatórios
  - `unidades.jsp`: Cadastro de unidades organizacionais
  - `movimentacoes.jsp`: Registro de movimentações
  - `configuracoes.jsp`: Configurações do usuário
- **Uso de JSTL**: Tags `<c:if>`, `<c:forEach>`, `<fmt:formatDate>` para lógica de apresentação

#### **Camada de Controle (Controller - Servlets)**
- **Responsabilidade**: Processar requisições, validar dados, coordenar fluxo
- **Tecnologias**: Java Servlets, Anotações @WebServlet
- **Padrão**: Front Controller (um servlet por entidade)
- **Componentes principais**:

```java
@WebServlet("/LoginController")
public class LoginController extends HttpServlet {
    // Processa POST do login
    // Valida credenciais
    // Cria sessão
    // Redireciona para Dashboard
}
```

**Responsabilidades de cada Controller**:
- Validar sessão do usuário
- Extrair parâmetros da requisição
- Determinar ação (CRUD) via parâmetro "action"
- Chamar métodos apropriados da camada DAO
- Tratar exceções
- Preparar dados para view
- Redirecionar ou forward para JSP

#### **Camada de Modelo/Persistência (DAO)**
- **Responsabilidade**: Acesso exclusivo ao banco de dados
- **Padrão**: DAO (Data Access Object)
- **Componentes**:
  - **DAOGenerico.java**: Classe utilitária com métodos estáticos para conexão
  - **UsuarioDAO.java**: CRUD de usuários, autenticação
  - **EquipamentoDAO.java**: CRUD de equipamentos
  - **FabricanteDAO.java**: CRUD de fabricantes
  - **UnidadeDAO.java**: CRUD de unidades
  - **MovimentacaoDAO.java**: CRUD de movimentações

**Características**:
- Uso de `PreparedStatement` contra SQL Injection
- Gerenciamento de conexão via `Connection` passada no construtor
- Operações CRUD separadas em métodos distintos
- Mapeamento de ResultSet para objetos de modelo

### 3.4 Comunicação Entre Componentes

```
USUÁRIO
  │
  ▼
┌──────────────────────────────┐
│    Browser (HTTP Request)    │
└──────────┬───────────────────┘
           │
           ▼
┌──────────────────────────────┐
│   Tomcat Web Container       │
│   Servlet Dispatcher         │
└──────────┬───────────────────┘
           │ Mapeia URL → Servlet
           ▼
┌──────────────────────────────┐
│    Controller Servlet        │
│  (ex: EquipamentoController) │
│  - Valida sessão             │
│  - Processa ação             │
│  - Prepara dados             │
└──────────┬───────────────────┘
           │ Chama método DAO
           ▼
┌──────────────────────────────┐
│    DAO (Data Access Object)  │
│  (ex: EquipamentoDAO)        │
│  - Monta SQL                 │
│  - Executa query             │
│  - Mapeia resultado          │
└──────────┬───────────────────┘
           │ JDBC Connection
           ▼
┌──────────────────────────────┐
│    MySQL Database            │
│    (Banco de Dados)          │
└──────────────────────────────┘
           │
           ▼ ResultSet
┌──────────────────────────────┐
│    DAO retorna dados         │
└──────────┬───────────────────┘
           │ Dados em objetos Java
           ▼
┌──────────────────────────────┐
│    Controller prepara        │
│    request/response          │
└──────────┬───────────────────┘
           │ Forward/Redirect
           ▼
┌──────────────────────────────┐
│    JSP (View)                │
│  - Renderiza HTML            │
│  - Aplica CSS/JavaScript     │
└──────────┬───────────────────┘
           │
           ▼
┌──────────────────────────────┐
│    HTTP Response             │
│    (HTML + CSS + JS)         │
└──────────┬   ──────────────────┘
           │
           ▼
        USUÁRIO (Browser)
```

### 3.5 Fluxo de Requisição e Resposta

**Exemplo: Cadastro de novo equipamento**

```
1. Usuário clica "Novo Equipamento"
   └─ Formulário em equipamentos.jsp

2. Preenche dados e submete
   └─ POST /EquipamentoController?action=adicionar
   └─ Parâmetros: nome, fabricante, unidade

3. EquipamentoController.doPost()
   └─ Valida sessão
   └─ Extrai parâmetros: action="adicionar"
   └─ Chama: adicionarEquipamento(request, response)
   └─ Monta objeto Equipamento
   └─ Chama: equipamentoDAO.adicionarEquipamento(equipamento)

4. EquipamentoDAO.adicionarEquipamento()
   └─ Prepara SQL: INSERT INTO equipamento (...)
   └─ Define parâmetros: stmt.setString(1, nome), etc
   └─ Executa: stmt.executeUpdate()
   └─ Fecha recursos (stmt, ResultSet)
   └─ Retorna ao Controller

5. Controller após sucesso
   └─ Define mensagem de sucesso na sessão
   └─ Redireciona: response.sendRedirect("/EquipamentoController")
   └─ Navega de volta para listagem

6. GET /EquipamentoController
   └─ EquipamentoController.doGet()
   └─ Chama: listarEquipamentos()
   └─ DAO retorna List<Equipamento>
   └─ RequestDispatcher.forward(request, response) 
   └─ Encaminha para: equipamentos.jsp

7. equipamentos.jsp
   └─ Recebe atributo "equipamentos" (List)
   └─ Renderiza tabela HTML com dados
   └─ Usa JSTL: <c:forEach var="equip" items="${equipamentos}">
   └─ Renderiza cada linha

8. Browser exibe página com novo equipamento na lista
```

---

## 4. Tecnologias Utilizadas

### 4.1 Linguagem de Programação

**Java 8 ou superior**
- **Versão utilizada**: Compatível com Tomcat 9.0
- **Justificativa**: 
  - Linguagem robusta e compilada para performance
  - Ampla compatibilidade com frameworks web
  - Suporte a Servlets e JSP
  - Segurança built-in (JDBC com PreparedStatement)

### 4.2 Frameworks e Bibliotecas

#### **Backend**
- **Jakarta Servlet API** (javax.servlet)
  - Implementação de Servlets para MVC
  - Gerenciamento de requisições HTTP
  - Controle de sessão
  
- **JSTL (JavaServer Pages Standard Tag Library)**
  - Lógica em JSP com tags como `<c:if>`, `<c:forEach>`
  - Formatação com `<fmt:formatDate>`
  - Evita scriptlets Java em JSP

- **MySQL Connector/J 9.2.0**
  - Driver JDBC para comunicação com MySQL
  - Included no repositório para portabilidade

#### **Frontend**
- **HTML5**: Estrutura semântica das páginas
- **CSS3**: Estilização visual (arquivo patrimweb.css)
- **JavaScript (Vanilla)**: 
  - Interatividade (toggle menu, modal, validações)
  - Não usa jQuery ou frameworks complexos
  - Foco em vanilla JS para máxima compatibilidade

### 4.3 Banco de Dados

**MySQL 5.7+ ou 8.0**

**Características**:
- Banco relacional que oferece ACID transactions
- Suporte a chaves estrangeiras (Foreign Keys)
- Suitable para aplicações web enterprise
- Configuração via arquivo `database.properties` em `/lib/configuracao/`

**Tabelas principais**:
```sql
usuario (id_usu, nome_usu, cpf_usu, email_usu, senha_usu, ...)
equipamento (id_equip, nome_equip, id_fabricante, id_unidade, ...)
fabricante (id_fab, nome_fab, data_insercao)
unidade (id_unidade, nome_unidade, data_insercao)
movimentacao (id_movimentacao, id_equip, id_unidade_orig, 
              id_unidade_dest, data_movimentacao)
```

### 4.4 Servidor de Aplicação

**Apache Tomcat 9.0.110**
- Servidor web/servlet container open-source
- Compatível com Servlet API 4.0
- Suporte a JSP 2.3
- Gerenciamento de sessões HTTP
- Deployment em arquivo .war

### 4.5 Ferramentas Auxiliares

- **IDE**: Eclipse IDE ou Apache NetBeans (conforme .metadata/)
- **Build Tool**: Maven (pom.xml se disponível) ou manual
- **Versionamento**: Git (repositório GitHub)
- **Gerenciamento de Dependências**: Maven (biblioteca MySQL incluída em `/lib/`)

### 4.6 Padrões Utilizados

#### **Padrão MVC (Model-View-Controller)**
```
Model (M)    ← DAO + Entities + Banco de Dados
View (V)     ← JSP + HTML + CSS + JavaScript
Controller (C) ← Servlets (@WebServlet)
```

#### **Padrão DAO (Data Access Object)**
```
aplicacao → DAO → SQL → Banco de Dados
```
- Isola lógica de acesso aos dados
- Facilita testes unitários
- Reutilização de código SQL

#### **Padrão Front Controller**
```
Todas requisições HTTP → Servlet único (implícito no Tomcat)
                       → Determina ação via parâmetro
                       → Delega para método apropriado
```

#### **Padrão Session Management**
```
HttpSession → Armazena usuário autenticado
           → Validação em cada requisição protegida
           → Invalidação no logout
```

#### **Padrão Singleton (DAOGenerico)**
```
DAOGenerico.getConexao() → Método estático
                         → Lê configuração uma vez
                         → Retorna Connection compartilhada
```

### 4.7 Por que Essas Tecnologias são Adequadas

| Tecnologia | Justificativa |
|-----------|-------------|
| **Java** | Linguagem estabelecida para web, segurança, portabilidade |
| **Servlets/JSP** | Stack padrão em Java, suportado por Tomcat |
| **JSTL** | Evita scriptlets, melhora legibilidade do JSP |
| **MySQL** | SGBD relacional confiável, simples de configurar |
| **Tomcat** | Lightweight, rápido, excelente para aplicações pequenas/médias |
| **MVC** | Separação de responsabilidades, manutenibilidade |
| **DAO** | Abstração de BD, facilita manutenção e testes |

---

## 5. Estrutura do Projeto

### 5.1 Explicação Detalhada das Pastas Principais

```
patrimweb/                                  (Raiz do projeto)
│
├── src/main/java/br/com/patrimweb/
│   │
│   ├── controller/                         (CAMADA: Controle)
│   │   └── [Servlets HTTP - Processamento de requisições]
│   │
│   ├── model/                              (CAMADA: Modelo de Dados)
│   │   └── [Classes POJO - Entidades do domínio]
│   │
│   ├── dao/                                (CAMADA: Persistência/Dados)
│   │   └── [Classes DAO - Acesso ao banco]
│   │
│   └── utils/                              (UTILITÁRIOS)
│       └── Conexao.java [Gerenciar conexões]
│
├── src/main/webapp/                        (RAIZ WEB)
│   ├── index.jsp                           [Página de login]
│   ├── dashboard.jsp                       [Painel principal]
│   ├── equipamentos.jsp                    [CRUD Equipamentos]
│   ├── fabricantes.jsp                     [CRUD Fabricantes]
│   ├── usuarios.jsp                        [CRUD Usuários]
│   ├── unidades.jsp                        [CRUD Unidades]
│   ├── movimentacoes.jsp                   [CRUD Movimentações]
│   ├── configuracoes.jsp                   [Preferências]
│   ├── relatorio_*.jsp                     [Relatórios]
│   │
│   ├── css/
│   │   └── patrimweb.css                   [Estilos globais]
│   │
│   ├── js/                                 [Scripts JavaScript]
│   │   └── scripts.js (se separado)
│   │
│   └── WEB-INF/
│       ├── web.xml                         [Configuração servlet]
│       └── lib/                            [Bibliotecas JAR]
│
├── lib/
│   ├── mysql-connector-j-9.2.0/            [Driver MySQL]
│   └── configuracao/
│       └── database.properties             [Config. BD]
│
├── .metadata/                              [Metadados Eclipse]
├── Servers/                                [Tomcat configurado]
│
├── pom.xml (se usar Maven)                [Build configuration]
├── .gitignore                              [Git ignore file]
└── README.md                               [Documentação basic]
```

### 5.2 Função de Cada Diretório

| Diretório | Função |
|-----------|--------|
| `src/main/java/` | Código-fonte Java compilado |
| `src/main/webapp/` | Recursos web (JSP, CSS, JS, HTML) |
| `controller/` | Servlets que processam requisições HTTP |
| `model/` | Classes POJO representando entidades |
| `dao/` | Classes que acessam o banco de dados |
| `utils/` | Classes utilitárias (Conexão, helpers) |
| `WEB-INF/` | Configuração da aplicação web |
| `lib/` | Bibliotecas externas (MySQL driver) |
| `.metadata/` | Metadados da IDE Eclipse |
| `Servers/` | Servidor Tomcat configurado |

### 5.3 Organização dos Arquivos Importantes

#### **Arquivos de Configuração**
- `web.xml`: Define mapeamento de servlets e filtros
- `database.properties`: Credenciais de banco de dados
- `.gitignore`: Padrões de exclusão do Git

#### **Arquivo Principal de Estilos**
- `patrimweb.css`: Toda a estilização CSS centralizada
  - Variáveis CSS (cores, tamanhos)
  - Componentes (buttons, forms, cards)
  - Layout (grid, flexbox)
  - Responsividade (media queries)

#### **Classes Principais de Modelo**
```
Usuario.java         → idUsu, nomeUsu, emailUsu, senhaUsu, ...
Equipamento.java     → idEquip, nomeEquip, fabricante, ...
Fabricante.java      → idFab, nomeFab, dataInsercao
Unidade.java         → idUnidade, nomeUnidade, dataInsercao
Movimentacao.java    → idMovimentacao, equipamento, unidade, data
```

#### **Classes Principais de DAO**
```
UsuarioDAO.java      → CRUD + autenticar(login, senha)
EquipamentoDAO.java  → CRUD + buscarPorFabricante()
FabricanteDAO.java   → CRUD com validação FK
UnidadeDAO.java      → CRUD de unidades
MovimentacaoDAO.java → CRUD de movimentações
DAOGenerico.java     → Métodos estáticos compartilhados
```

### 5.4 Papel dos Principais Módulos do Sistema

#### **Módulo de Autenticação**
**Arquivo**: LoginController.java  
**Responsabilidade**: Validar credenciais e criar sessão  
**Fluxo**: 
1. Recebe POST com usuario/senha
2. UsuarioDAO.autenticar(usuario, senha)
3. Se válido → HttpSession.setAttribute("usuarioLogado", usuario)
4. Redireciona para DashboardController
5. Se inválido → Redireciona para index.jsp com mensagem erro

#### **Módulo de Equipamentos**
**Arquivo**: EquipamentoController.java  
**Responsabilidade**: Operações CRUD de equipamentos  
**Métodos**:
- `listarEquipamentos()`: GET lista todos
- `adicionarEquipamento()`: POST insere novo
- `editarEquipamento()`: POST atualiza existente
- `deletarEquipamento()`: POST remove equipamento

#### **Módulo de Relatórios**
**Arquivo**: RelatorioController.java  
**Responsabilidade**: Gerar relatórios com filtros  
**Funcionalidades**:
- Filtro por período (data início/fim)
- Filtro por fabricante
- Filtro por unidade
- Exportação de dados

#### **Módulo de Configurações**
**Arquivo**: configuracoes.jsp  
**Responsabilidade**: Ajustes de preferência do usuário  
**Abas**:
- Geral (tema, idioma, itens por página)
- Meu Perfil (dados pessoais)
- Notificações (preferências)

---

## 6. Modelagem e Dados

### 6.1 Estrutura do Banco de Dados

#### **Diagrama Entidade-Relacionamento (MER)**

```
┌──────────────────┐
│    USUARIO       │
├──────────────────┤
│ id_usu (PK)      │
│ nome_usu         │
│ cpf_usu          │
│ email_usu        │
│ telefone_usu     │
│ endereco_usu     │
│ senha_usu        │
│ login_google     │
│ data_insercao    │
└──────────────────┘

┌──────────────────┐      ┌─────────────────┐
│   FABRICANTE     │──┬───│ EQUIPAMENTO     │
├──────────────────┤  │   ├─────────────────┤
│ id_fab (PK)      │  └───│ id_equip (PK)   │
│ nome_fab         │      │ nome_equip      │
│ data_insercao    │      │ id_fabricante*  │
└──────────────────┘      │ id_unidade*     │
                          │ data_insercao   │
                          └─────────────────┘
                                  │
                                  │ 1..N
                                  │
                          ┌───────┴────────┐
                          │  MOVIMENTACAO  │
                          ├────────────────┤
                          │ id_movimentacao│
                          │ id_equip*      │
                          │ id_unidade_orig│
                          │ id_unidade_dest│
                          │ data_movim     │
                          └────────────────┘

┌──────────────────┐
│    UNIDADE       │
├──────────────────┤
│ id_unidade (PK)  │
│ nome_unidade     │
│ data_insercao    │
└──────────────────┘
```

### 6.2 Entidades Principais

#### **1. USUARIO**
```sql
CREATE TABLE usuario (
    id_usu INT PRIMARY KEY AUTO_INCREMENT,
    nome_usu VARCHAR(100) NOT NULL,
    cpf_usu VARCHAR(11) UNIQUE,
    email_usu VARCHAR(100) UNIQUE NOT NULL,
    telefone_usu VARCHAR(20),
    endereco_usu VARCHAR(255),
    senha_usu VARCHAR(255) NOT NULL,
    login_google BOOLEAN DEFAULT FALSE,
    data_insercao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**Atributos**:
- `id_usu`: Identificador único do usuário
- `nome_usu`: Nome completo (obrigatório)
- `cpf_usu`: CPF único (validação extra)
- `email_usu`: Email único (usado para login)
- `telefone_usu`: Contato telefônico
- `endereco_usu`: Endereço residencial
- `senha_usu`: Senha (idealmente hashada em produção)
- `login_google`: Flag para Google Sign-In
- `data_insercao`: Timestamp de criação (auditoria)

#### **2. FABRICANTE**
```sql
CREATE TABLE fabricante (
    id_fab INT PRIMARY KEY AUTO_INCREMENT,
    nome_fab VARCHAR(100) NOT NULL UNIQUE,
    data_insercao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**Atributos**:
- `id_fab`: Identificador único
- `nome_fab`: Nome do fabricante (ex: Dell, HP, Apple)
- `data_insercao`: Registro de criação

#### **3. UNIDADE**
```sql
CREATE TABLE unidade (
    id_unidade INT PRIMARY KEY AUTO_INCREMENT,
    nome_unidade VARCHAR(100) NOT NULL UNIQUE,
    data_insercao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**Atributos**:
- `id_unidade`: Identificador único
- `nome_unidade`: Nome da unidade (ex: Almoxarifado, TI, Administrativo)
- `data_insercao`: Registro de criação

#### **4. EQUIPAMENTO**
```sql
CREATE TABLE equipamento (
    id_equip INT PRIMARY KEY AUTO_INCREMENT,
    nome_equip VARCHAR(100) NOT NULL,
    id_fabricante INT NOT NULL,
    id_unidade INT NOT NULL,
    data_insercao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_fabricante) REFERENCES fabricante(id_fab),
    FOREIGN KEY (id_unidade) REFERENCES unidade(id_unidade)
);
```

**Atributos**:
- `id_equip`: Identificador único
- `nome_equip`: Descrição do equipamento (ex: Notebook Dell)
- `id_fabricante`: Referência ao fabricante (FK)
- `id_unidade`: Unidade onde está localizado (FK)
- `data_insercao`: Timestamp de cadastro

#### **5. MOVIMENTACAO**
```sql
CREATE TABLE movimentacao (
    id_movimentacao INT PRIMARY KEY AUTO_INCREMENT,
    id_equip INT NOT NULL,
    id_unidade_orig INT NOT NULL,
    id_unidade_dest INT NOT NULL,
    data_movimentacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_equip) REFERENCES equipamento(id_equip),
    FOREIGN KEY (id_unidade_orig) REFERENCES unidade(id_unidade),
    FOREIGN KEY (id_unidade_dest) REFERENCES unidade(id_unidade)
);
```

**Atributos**:
- `id_movimentacao`: Identificador único do registro de movimento
- `id_equip`: Equipamento sendo movido (FK)
- `id_unidade_orig`: Unidade de origem (FK)
- `id_unidade_dest`: Unidade de destino (FK)
- `data_movimentacao`: Data/hora do movimento (auditoria)

### 6.3 Relacionamentos Entre Tabelas

**Relacionamentos:**
1. **FABRICANTE → EQUIPAMENTO** (1:N)
   - Um fabricante pode ter N equipamentos
   - Equipamento obrigatoriamente tem um fabricante
   - Exclusão de fabricante é bloqueada se tiver equipamentos (FK constraint)

2. **UNIDADE → EQUIPAMENTO** (1:N)
   - Uma unidade pode ter N equipamentos
   - Equipamento localizado em uma unidade

3. **EQUIPAMENTO → MOVIMENTACAO** (1:N)
   - Um equipamento pode ter N movimentações
   - Cada movimentação é rastreável

4. **UNIDADE → MOVIMENTACAO** (1:N múltiplas)
   - Uma unidade pode ser origem de N movimentações
   - Uma unidade pode ser destino de N movimentações

### 6.4 Responsabilidade dos Modelos/Classes

Cada classe modelo POJO (Plain Old Java Object) representa uma entidade:

```java
public class Usuario {
    private int idUsu;
    private String nomeUsu;
    private String cpfUsu;
    private String emailUsu;
    private String senhaUsu;
    // ... getters/setters
}
```

**Responsabilidades do Modelo**:
1. **Encapsulamento de Dados**: Atributos privados com getters/setters
2. **Validação Lógica**: Regras de domínio (em produção)
3. **Serialização**: Podem ser convertidos para JSON (em produção)
4. **Mapeamento O/R**: Campos Java ↔ Colunas SQL

**Padrão:**
- Um modelo por entidade (POJO)
- Construtor vazio e construtor completo
- Getters e setters para todos os atributos
- Sem lógica de negócio complexa (responsabilidade do DAO/Controller)

---

## 7. Fluxo de Funcionamento do Sistema

### 7.1 Inicialização e Acesso ao Sistema

```
1. USUÁRIO ACESSA A APLICAÇÃO
   └─ Digita: http://localhost:8080/patrimweb/
   
2. SERVIDOR TOMCAT PROCESSA
   └─ Localiza index.jsp como página padrão (welcome-file)
   
3. index.jsp EXECUTA
   └─ Verifica: session.getAttribute("usuarioLogado")
   └─ Se existe → Redireciona para DashboardController
   └─ Se não existe → Exibe formulário de login
   
4. FORMULÁRIO DE LOGIN
   └─ <form action="LoginController" method="post">
   └─ Campos: usuario (texto), senha (password)
   └─ Botão: "Entrar"
```

### 7.2 Processo de Autenticação

```
1. USUÁRIO SUBMETE CREDENCIAIS
   └─ POST /LoginController
   └─ Parâmetros: usuario=admin, senha=1234
   
2. LOGINCONTROLLER.doPost() EXECUTADO
   └─ Captura: String usuarioInput = request.getParameter("usuario");
   └─ Captura: String senha = request.getParameter("senha");
   
3. CRIA CONEXÃO COM BANCO
   └─ Connection conn = Conexao.getConnection();
   └─ Lê arquivo: database.properties
   └─ Cria DriverManager.getConnection(url, user, pass)
   
4. INSTANCIA DAO E VALIDA
   └─ UsuarioDAO usuarioDAO = new UsuarioDAO(conn);
   └─ Usuario usuario = usuarioDAO.autenticar(usuarioInput, senha);
   
5. USUARIODAO.AUTENTICAR() CONSULTA BD
   └─ SQL: SELECT * FROM usuario WHERE (email_usu = ? OR nome_usu = ?) AND senha_usu = ?
   └─ PreparedStatement stmt = conexao.prepareStatement(sql);
   └─ stmt.setString(1, login); // Email ou nome
   └─ stmt.setString(2, login);
   └─ stmt.setString(3, senha);
   └─ ResultSet rs = stmt.executeQuery();
   
6. RESULTADO DA AUTENTICAÇÃO
   └─ Se rs.next() → Usuário encontrado
   │   └─ Cria objeto Usuario
   │   └─ Retorna ao Controller
   └─ Se !rs.next() → Usuário não existe
       └─ Retorna null ao Controller
   
7. CONTROLLER TRATA RESULTADO
   └─ If usuario != null:
   │   ├─ HttpSession session = request.getSession(true);
   │   ├─ session.setAttribute("usuarioLogado", usuario);
   │   ├─ session.setMaxInactiveInterval(1800); // 30 min
   │   └─ response.sendRedirect("DashboardController");
   └─ If usuario == null:
       ├─ RequestDispatcher rd = request.getRequestDispatcher("index.jsp");
       ├─ request.setAttribute("erro", "Credenciais inválidas");
       └─ rd.forward(request, response);
```

### 7.3 Fluxo de Uma Operação CRUD (Exemplo: Adicionar Equipamento)

```
┌─────────────────────────────────────────────────────────┐
│ 1. USUÁRIO INTERAGE COM INTERFACE                       │
└──────────────────┬──────────────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────────────┐
│ 2. FORMULÁRIO EM equipamentos.jsp                        │
│    - Nome: <input name="nomeEquip">                     │
│    - Fabricante: <select name="idFabricante">           │
│    - Unidade: <select name="idUnidade">                 │
│    └─ Submete: POST /EquipamentoController?action=add  │
└──────────────────┬──────────────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────────────┐
│ 3. EQUIPAMENTOCONTROLLER.doPost()                       │
│    - String action = request.getParameter("action");    │
│    - switch(action) {                                   │
│      case "adicionar":                                  │
│        adicionarEquipamento(request, response);         │
│      }                                                  │
└──────────────────┬──────────────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────────────┐
│ 4. VALIDAÇÃO E MONTAGEM DE OBJETO                       │
│    - String nome = request.getParameter("nomeEquip");   │
│    - int idFab = Integer.parseInt(...);                 │
│    - Equipamento equip = new Equipamento();             │
│    - equip.setNomeEquip(nome);                          │
│    - equip.setFabricante(fab);                          │
└──────────────────┬──────────────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────────────┐
│ 5. CHAMA DAO                                             │
│    - equipamentoDAO.adicionarEquipamento(equip);        │
└──────────────────┬──────────────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────────────┐
│ 6. EQUIPAMENTODAO.adicionarEquipamento()                │
│    - String sql = "INSERT INTO equipamento (...)"       │
│    - PreparedStatement stmt = ...prepareStatement(sql)  │
│    - stmt.setString(1, equipamento.getNomeEquip());    │
│    - stmt.setInt(2, equipamento.getIdFabricante());    │
│    - int resultado = stmt.executeUpdate();              │
│    - rs.close(); stmt.close();                          │
│    └─ Retorna int (linhas afetadas)                     │
└──────────────────┬──────────────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────────────┐
│ 7. BANCO DE DADOS                                        │
│    INSERT INTO equipamento                              │
│    (nome_equip, id_fabricante, id_unidade, data_inser)  │
│    VALUES ('Notebook Dell', 1, 2, NOW());               │
│                                                          │
│    Resultado: 1 linha inserida                           │
└──────────────────┬──────────────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────────────┐
│ 8. RETORNO AO CONTROLLER                                │
│    if (resultado > 0) {                                  │
│      request.getSession().setAttribute(                 │
│        "mensagem", "Equipamento adicionado com sucesso" │
│      );                                                 │
│    }                                                    │
└──────────────────┬──────────────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────────────┐
│ 9. REDIRECIONAMENTO                                      │
│    response.sendRedirect(                               │
│      request.getContextPath() + "/EquipamentoController" │
│    );                                                   │
│    └─ Redireciona para GET (após-redirect-get pattern)  │
└──────────────────┬──────────────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────────────┐
│ 10. NOVA REQUISIÇÃO GET                                 │
│     GET /EquipamentoController                          │
│     └─ Carrega lista atualizada do banco                │
└──────────────────┬──────────────────────────────────────┘
                   │
                   ▼
┌──────────────────────────────────────   ──────────────────┐
│ 11. EQUIPAMENTOCONTROLLER.doGet()                       │
│     - listarEquipamentos(request, response);            │
│     - List<Equipamento> lista = equipamentoDAO.listar   │
│     - request.setAttribute("equipamentos", lista);      │
│     - RequestDispatcher rd =                            │
│       request.getRequestDispatcher("equipamentos.jsp"); │
│     - rd.forward(request, response);                    │
└──────────────────┬──────────────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────────────┐
│ 12. equipamentos.jsp RENDERIZA                          │
│     - Recebe atributo: ${equipamentos}                  │
│     - <c:forEach var="equip" items="${equipamentos}">  │
│     - <tr><td>${equip.nomeEquip}</td></tr>             │
│     - Renderiza tabela HTML                             │
└──────────────────┬──────────────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────────────┐
│ 13. NAVEGADOR EXIBE RESULTADO                           │
│     - Página atualizada com novo equipamento na tabela  │
│     - Mensagem de sucesso exibida                       │
└─────────────────────────────────────────────────────────┘
```

### 7.4 Fluxo de Relatórios

```
USUÁRIO CLICA "GERAR RELATÓRIO"
   └─ RelatorioController?type=usuarios&dataInicio=...&dataFim=...
   
CONTROLLER RECEBE FILTROS
   └─ Timestamp dataInicio = Timestamp.valueOf(...);
   └─ Timestamp dataFim = Timestamp.valueOf(...);
   
DAO EXECUTA QUERY COM FILTROS
   └─ SELECT * FROM usuario 
      WHERE data_insercao BETWEEN ? AND ?
   
RESULTADO AGRUPADO/FORMATADO
   └─ LinkedHashMap<Data, Integer> para estatísticas
   └─ List<Usuario> para relatório detalhado
   
JSP RENDERIZA RESULTADO
   └─ Tabela HTML com dados
   └─ Gráficos (se implementado)
   └─ Opção de exportação
```

---

## 8. Interface do Usuário

### 8.1 Descrição das Telas Existentes

#### **1. Tela de Login (index.jsp)**
**Objetivo**: Autenticar o usuário no sistema

**Elementos**:
- Logo/Header com "PatrimWeb"
- Formulário com:
  - Campo "E-mail ou Nome de Usuário"
  - Campo "Senha"
  - Checkbox "Lembrar-me"
  - Botão "Entrar"
- Integração Google Sign-In (UI preparada)
- Links "Esqueci senha" e "Registre-se" (placeholders)

**Estilos**:
- Background gradient azul
- Card centralizado com shadow
- Inputs com ícones
- Botão com hover effect

#### **2. Dashboard (dashboard.jsp)**
**Objetivo**: Visão geral do sistema e acesso aos módulos

**Elementos**:
- **Header**: Logo + Menu usuário (Perfil, Configurações, Sair)
- **Sidebar**: Menu de navegação
  - Dashboard (home)
  - Equipamentos
  - Fabricantes
  - Usuários
  - Unidades
  - Movimentações
  - Configurações
- **Main Content**:
  - Cards com estatísticas:
    - Total de Equipamentos
    - Total de Movimentações
    - Usuários Cadastrados
  - Tabela com últimas movimentações
- **Footer**: Informações do sistema

**Responsividade**: Menu togglable em mobile

#### **3. Gerenciamento de Equipamentos (equipamentos.jsp)**
**Objetivo**: CRUD de equipamentos

**Elementos**:
- Botão "+ Novo Equipamento"
- Filtros: Por fabricante, por unidade
- Tabela com:
  - Coluna: Nome do Equipamento
  - Coluna: Fabricante
  - Coluna: Unidade Atual
  - Coluna: Data de Cadastro
  - Coluna: Ações (Editar, Deletar, Ver Histórico)
- Modal/formulário para adicionar/editar
- Paginação da tabela

#### **4. Gerenciamento de Fabricantes (fabricantes.jsp)**
**Objetivo**: CRUD de fabricantes

**Elementos**:
- Botão "+ Novo Fabricante"
- Tabela:
  - Nome do Fabricante
  - Data de Cadastro
  - Ações (Editar, Deletar)
- Modal para criar/editar
- Validação: Não permite deletar se tiver equipamentos associados

#### **5. Gerenciamento de Usuários (usuarios.jsp)**
**Objetivo**: CRUD de usuários

**Elementos**:
- Botão "+ Novo Usuário"
- Filtros: Por período, por nome
- Tabela:
  - Nome
  - CPF
  - Email
  - Telefone
  - Data Cadastro
  - Ações
- Botão "Relatório" (exporta dados)
- Modal de adicionar/editar usuário

#### **6. Movimentações (movimentacoes.jsp)**
**Objetivo**: Registrar e acompanhar movimentações

**Elementos**:
- Botão "+ Nova Movimentação"
- Formulário:
  - Seleção de equipamento
  - Unidade de Origem
  - Unidade de Destino
  - Data/Hora (auto-preenchida)
- Tabela de histórico:
  - Equipamento
  - Origem → Destino
  - Data/Hora
  - Ações (Editar, Deletar)
- Busca por equipamento

#### **7. Configurações (configuracoes.jsp)**
**Objetivo**: Preferências do usuário

**Abas**:
- **Geral**:
  - Nome empresa
  - Idioma
  - Tema (claro/escuro)
  - Itens por página em tabelas
  
- **Meu Perfil**:
  - Nome completo
  - Email
  - Telefone
  - Cargo
  - Avatar
  - Botão "Atualizar Perfil"

- **Notificações**:
  - Email sobre movimentações
  - Alertas de equipamento
  - Frequência de notificações

#### **8. Relatórios**
**Objetivo**: Análises e exportação de dados

**Tipos**:
- **Relatório de Equipamentos**:
  - Filtro: Por fabricante, por período
  - Exibe: Lista com estatísticas
  - Opção de imprimir/PDF

- **Relatório de Usuários**:
  - Filtro: Por período, CPF
  - Exibe: Usuários cadastrados
  - Gráfico de cadastros por mês

- **Relatório de Movimentações**:
  - Filtro: Por equipamento, por período
  - Timeline de movimentações
  - Localização atual do equipamento

### 8.2 Objetivo de Cada Tela

| Tela | Objetivo |
|------|----------|
| Login | Autenticar usuário |
| Dashboard | Visão geral e navegação |
| Equipamentos | Gerenciar patrimônio |
| Fabricantes | Manter lista de fabricantes |
| Usuários | Gerenciar usuários do sistema |
| Unidades | Gerenciar departamentos/locais |
| Movimentações | Rastrear equipamentos |
| Configurações | Preferências pessoais |
| Relatórios | Análises e exportação |

### 8.3 Interações Disponíveis

**Em todas as tabelas**:
- Clicar em linha para expandir detalhes
- Ícones de ação (editar/deletar)
- Busca/filtro
- Ordenação por coluna
- Paginação

**Em formulários**:
- Validação client-side (HTML5)
- Validação server-side (Java)
- Feedback visual de erro
- Auto-preenchimento de campos

**Em modais**:
- Sobreposição da página
- Botão fechar (X)
- Clique fora para fechar
- Transição suave

### 8.4 Experiência do Usuário (UX) Adotada

**Princípios Aplicados**:

1. **Simplicidade**
   - Menu claro e intuitivo
   - Evita jargão técnico
   - Informações organizadas por prioridade

2. **Consistência**
   - Mesmo design em todas as páginas
   - Padrão de cores unificado
   - Botões e ícones padronizados

3. **Feedback Visual**
   - Hover effects em elementos interativos
   - Mensagens de sucesso/erro
   - Loading indicators
   - Confirmação antes de deletar

4. **Acessibilidade**
   - Contraste adequado de cores
   - Ícones com labels/títulos
   - Navegação por teclado (Tab)
   - Responsividade mobile

5. **Performance**
   - Carregamento rápido de páginas
   - Paginação de tabelas grandes
   - Imagens otimizadas
   - Sem bloqueios na UI

**Design Visual**:
- Paleta: Azul primário (#2F70B8), tons de cinza
- Typography: Fonts modernas, leitura clara
- Espaçamento: Uso consistente de padding/margin
- Responsividade: Breakpoints para mobile/tablet/desktop

---

## 9. Segurança e Controle

### 9.1 Autenticação

**Mecanismo**: Sessão HTTP com validação de credenciais

```java
// Validação de credenciais contra banco
Usuario usuario = usuarioDAO.autenticar(login, senha);

if (usuario != null) {
    HttpSession session = request.getSession(true);
    session.setAttribute("usuarioLogado", usuario);
    session.setMaxInactiveInterval(1800); // 30 minutos
}
```

**Características**:
- Verificação no banco com PreparedStatement (contra SQL Injection)
- Sessão HTTP com timeout de 30 minutos
- Cookie JSESSIONID gerenciado pelo Tomcat
- Validação obrigatória em cada requisição protegida

**Melhorias Futuras**:
- Hash bcrypt para senhas (em produção, usar BCrypt em lugar de plain text)
- 2FA com SMS/Email
- OAuth 2.0 com Google (UI já preparada)
- Rate limiting para tentativas de login

### 9.2 Autorização

**Implementação**: Validação de sessão em cada Controller

```java
protected void doGet(HttpServletRequest request, HttpServletResponse response) 
        throws ServletException, IOException {
    
    HttpSession session = request.getSession(false);
    if (session == null || session.getAttribute("usuarioLogado") == null) {
        response.sendRedirect(request.getContextPath() + "/");
        return;
    }
    
    // Código protegido aqui
}
```

**Fluxo**:
1. Usuário não autenticado → Redireciona para login
2. Usuário com sessão válida → Acesso concedido
3. Sessão expirada → Redireciona para login

### 9.3 Controle de Permissões

**Atual**: Modelo básico (todos usuários autenticados têm acesso a tudo)

**Melhorias Futuras**:
- Roles/Papéis (Admin, Operador, Visualizador)
- Permissions por funcionalidade
- Controle granular por módulo
- Auditoria de ações por usuário

**Exemplo de implementação futura**:
```java
if (!temPermissao(usuario, "DELETAR_EQUIPAMENTO")) {
    response.setStatus(403); // Forbidden
    return;
}
```

### 9.4 Proteções Implementadas

#### **1. Contra SQL Injection**
```java
// ❌ VULNERÁVEL (não usado no projeto)
String sql = "SELECT * FROM usuario WHERE email = '" + email + "'";

// ✓ SEGURO (usado no projeto)
String sql = "SELECT * FROM usuario WHERE email = ?";
PreparedStatement stmt = conexao.prepareStatement(sql);
stmt.setString(1, email);
```

**Como Funciona**: PreparedStatement separa SQL do dado, compilando o comando antes de inserir parâmetros.

#### **2. Contra Session Hijacking**
```java
// Session timeout automático
session.setMaxInactiveInterval(1800); // 30 min

// Invalidação no logout
session.invalidate();
```

#### **3. Contra Cache de Páginas Protegidas**
```java
// Em cada página protegida
response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
response.setHeader("Pragma", "no-cache");
response.setDateHeader("Expires", 0);
```

#### **4. Validação de Entrada**
```java
// Client-side (HTML5)
<input type="email" required>
<input type="number" min="0">

// Server-side (Java)
if (nome == null || nome.trim().isEmpty()) {
    throw new Exception("Nome obrigatório");
}
```

#### **5. Foreign Key Constraints**
```java
// Impede exclusão de fabricante com equipamentos
try {
    fabricanteDAO.deletar(idFabricante);
} catch (SQLException e) {
    // Violação de FK - equipamentos associados
    session.setAttribute("erro", "Fabricante possui equipamentos");
}
```

### 9.5 Recomendações Adicionais de Segurança

Para **ambiente de produção**, implementar:

1. **