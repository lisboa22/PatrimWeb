package br.com.patrimweb.controller;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import br.com.patrimweb.utils.ConfigService;

@WebServlet("/IndexController")
public class IndexController extends HttpServlet {
	
	private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // ✅ envia o CLIENT_ID para o JSP
        request.setAttribute("clientId", ConfigService.getClientId());
      
        // abre a página de login
        request.getRequestDispatcher("/index.jsp").forward(request, response);
    }

}
