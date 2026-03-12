package br.com.patrimweb.controller;

import java.io.IOException;

import br.com.patrimweb.utils.ConfigService;

import java.sql.Connection;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import org.json.JSONObject;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import br.com.patrimweb.dao.PerfilDAO;
import br.com.patrimweb.dao.UsuarioDAO;
import br.com.patrimweb.model.Perfil;
import br.com.patrimweb.model.Usuario;
import br.com.patrimweb.utils.Conexao;

/**
 * Controller responsável pela autenticação de usuários utilizando
 * login social via Google (Google Sign-In).
 *
 * RESPONSABILIDADES PRINCIPAIS:
 * - Receber o token JWT enviado pelo frontend após autenticação no Google.
 * - Validar o token junto aos serviços oficiais do Google.
 * - Recuperar os dados básicos do usuário autenticado.
 * - Verificar se o usuário já existe no banco de dados.
 * - Criar automaticamente o usuário caso ainda não exista.
 * - Criar sessão HTTP autenticada no sistema PatrimWeb.
 * - Retornar resposta JSON indicando sucesso ou falha da autenticação.
 *
 * REGRAS DE NEGÓCIO IMPORTANTES:
 * - Apenas tokens válidos e destinados ao CLIENT_ID configurado são aceitos.
 * - Usuários autenticados via Google são criados automaticamente no sistema.
 * - A autenticação gera uma sessão HTTP padrão utilizada pelo restante do sistema.
 */
@WebServlet("/LoginGoogleController")
public class LoginGoogleController extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String CLIENT_ID = ConfigService.getClientId();

    /**
     * Processa requisições POST provenientes do frontend após autenticação via Google.
     *
     * Fluxo geral:
     * 1. Recebe JSON contendo o token do Google.
     * 2. Valida o token utilizando biblioteca oficial do Google.
     * 3. Extrai dados do usuário autenticado.
     * 4. Verifica existência do usuário no banco.
     * 5. Cria usuário automaticamente se necessário.
     * 6. ✅ Recarrega o usuário completo (com perfil) via buscarPorId antes de criar a sessão.
     * 7. Cria sessão autenticada com objeto completo.
     * 8. Retorna resposta JSON.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {

            // ============================================================
            // 1️⃣ Leitura do corpo da requisição HTTP
            // ============================================================
            String body = request.getReader()
                    .lines()
                    .collect(Collectors.joining());

            JSONObject json = new JSONObject(body);
            String idTokenString = json.getString("token");

            // ============================================================
            // 2️⃣ Criação do verificador oficial do Google
            // ============================================================
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance()
            )
            .setAudience(Collections.singletonList(CLIENT_ID))
            .build();

            // ============================================================
            // 3️⃣ Validação do token JWT
            // ============================================================
            GoogleIdToken idToken = verifier.verify(idTokenString);

            if (idToken == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write(
                    "{\"sucesso\": false, \"mensagem\": \"Token inválido ou expirado\"}"
                );
                return;
            }

            // ============================================================
            // Extração das informações do usuário autenticado
            // ============================================================
            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String nome  = (String) payload.get("name");
            Timestamp dataInsercao = Timestamp.valueOf(LocalDateTime.now());

            // ============================================================
            // 4️⃣ Acesso ao banco de dados
            // ============================================================
            try (Connection conn = Conexao.getConnection()) {

                UsuarioDAO usuarioDAO = new UsuarioDAO(conn);

                // Busca usuário pelo e-mail retornado pelo Google
                Usuario usuario = usuarioDAO.buscarPorEmail(email);

                Connection conexao = Conexao.getConnection();
                PerfilDAO perfilDAO = new PerfilDAO(conn);
                // ------------------------------------------------------------
                // Regra de negócio:
                // Se o usuário não existir, ele é criado automaticamente.
                // ------------------------------------------------------------
                if (usuario == null) {
                    usuario = new Usuario();
                    usuario.setNomeUsu(nome);
                    usuario.setEmailUsu(email);
                    usuario.setLoginGoogle(true);
                    usuario.setDataInsercao(dataInsercao);
                    Perfil perfil = perfilDAO.buscarPorId(2);
                    usuario.setPerfilUsu(perfil);

                    usuarioDAO.adicionarUsuario(usuario);

                    // Recarrega para obter o ID gerado pelo banco
                    usuario = usuarioDAO.buscarPorEmail(email);
                }

                // ============================================================
                // 5️⃣ Recarrega o usuário COMPLETO (com perfil) via buscarPorId
                // ============================================================
                // buscarPorEmail() retorna um objeto parcial — apenas id, nome,
                // email e loginGoogle — sem o perfil preenchido.
                // O perfil é necessário para que o controle de acesso (ex: sidebar
                // e UsuarioController) funcione corretamente após o login.
                if (usuario != null) {
                    Usuario usuarioCompleto = usuarioDAO.buscarPorId(usuario.getIdUsu());
                    if (usuarioCompleto != null) {
                        usuario = usuarioCompleto;
                    }
                }

                // ============================================================
                // 6️⃣ Criação da sessão autenticada com objeto completo
                // ============================================================
                HttpSession sessao = request.getSession(true);

                // ✅ Sessão criada com usuário completo (perfil incluído)
                sessao.setAttribute("usuarioLogado", usuario);
                sessao.setAttribute("nomeUsuario", usuario.getNomeUsu());

                response.getWriter().write("{\"sucesso\": true}");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(
                "{\"sucesso\": false, \"erro\": \"Erro interno no servidor\"}"
            );
        }
    }
}
