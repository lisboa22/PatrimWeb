package br.com.patrimweb.controller;

import java.io.IOException;
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

import br.com.patrimweb.dao.UsuarioDAO;
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

    /**
     * Serial version UID padrão para controle de serialização da Servlet.
     */
    private static final long serialVersionUID = 1L;

    /**
     * CLIENT_ID da aplicação configurada no Google Cloud Console.
     * Utilizado para validar se o token recebido foi realmente emitido
     * para esta aplicação específica.
     */
    private static final String CLIENT_ID = 
        "";

    /**
     * Método responsável por processar requisições POST provenientes
     * do frontend após autenticação via Google.
     *
     * Fluxo geral:
     * 1. Recebe JSON contendo o token do Google.
     * 2. Valida o token utilizando biblioteca oficial do Google.
     * 3. Extrai dados do usuário autenticado.
     * 4. Verifica existência do usuário no banco.
     * 5. Cria usuário automaticamente se necessário.
     * 6. Cria sessão autenticada.
     * 7. Retorna resposta JSON.
     *
     * @param request  Requisição HTTP contendo o token Google em formato JSON.
     * @param response Resposta HTTP enviada ao cliente em formato JSON.
     *
     * @throws ServletException em caso de erro interno da Servlet.
     * @throws IOException      em caso de erro de leitura/escrita HTTP.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Define que a resposta será JSON e com codificação UTF-8
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {

            // ============================================================
            // 1️⃣ Leitura do corpo da requisição HTTP
            // ============================================================
            // O frontend envia um JSON contendo o token JWT retornado pelo Google.
            // Aqui o corpo da requisição é lido integralmente e convertido em String.
            String body = request.getReader()
                    .lines()
                    .collect(Collectors.joining());

            // Converte o JSON recebido em objeto manipulável
            JSONObject json = new JSONObject(body);

            // Extrai o token JWT enviado pelo Google Sign-In
            String idTokenString = json.getString("token");

            // ============================================================
            // 2️⃣ Criação do verificador oficial do Google
            // ============================================================
            // Este objeto valida criptograficamente o token recebido,
            // garantindo autenticidade e integridade.
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance()
            )
            // Define o CLIENT_ID esperado (proteção contra uso indevido do token)
            .setAudience(Collections.singletonList(CLIENT_ID))
            .build();

            // ============================================================
            // 3️⃣ Validação do token JWT
            // ============================================================
            // Caso o token seja inválido, expirado ou adulterado,
            // o método verify retorna null.
            GoogleIdToken idToken = verifier.verify(idTokenString);

            // Validação crítica de segurança
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

            // Email validado pelo Google (identificador principal do usuário)
            String email = payload.getEmail();

            // Nome exibido na conta Google
            String nome = (String) payload.get("name");

            // Data/hora atual utilizada como data de inserção no sistema
            Timestamp dataInsercao = Timestamp.valueOf(LocalDateTime.now());

            // ============================================================
            // 4️⃣ Acesso ao banco de dados
            // ============================================================
            // Uso de try-with-resources garante fechamento automático da conexão,
            // evitando vazamentos de recursos.
            try (Connection conn = Conexao.getConnection()) {

                // DAO responsável pelas operações da entidade Usuario
                UsuarioDAO usuarioDAO = new UsuarioDAO(conn);

                // Busca usuário pelo e-mail retornado pelo Google
                Usuario usuario = usuarioDAO.buscarPorEmail(email);

                // ------------------------------------------------------------
                // Regra de negócio:
                // Se o usuário não existir, ele é criado automaticamente.
                // ------------------------------------------------------------
                if (usuario == null) {
                    usuario = new Usuario();

                    // Preenche dados básicos vindos do Google
                    usuario.setNomeUsu(nome);
                    usuario.setEmailUsu(email);

                    // Marca que o usuário foi criado via login social
                    usuario.setLoginGoogle(true);

                    // Define data de criação do registro
                    usuario.setDataInsercao(dataInsercao);

                    // Persistência do novo usuário no banco
                    usuarioDAO.adicionarUsuario(usuario);

                    // Nova consulta para recuperar o usuário já com ID gerado
                    usuario = usuarioDAO.buscarPorEmail(email);
                }

                // ============================================================
                // 5️⃣ Criação da sessão autenticada
                // ============================================================
                // Cria (ou recupera) sessão HTTP do usuário autenticado.
                HttpSession sessao = request.getSession(true);

                // Armazena o objeto usuário na sessão para controle de acesso
                sessao.setAttribute("usuarioLogado", usuario);

                // Retorna sucesso ao frontend
                response.getWriter().write("{\"sucesso\": true}");
            }

        } catch (Exception e) {
            // ============================================================
            // Tratamento genérico de exceções
            // ============================================================
            // Qualquer erro inesperado durante validação, banco ou sessão
            // resulta em erro interno do servidor.
            e.printStackTrace();

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(
                "{\"sucesso\": false, \"erro\": \"Erro interno no servidor\"}"
            );
        }
    }
}
