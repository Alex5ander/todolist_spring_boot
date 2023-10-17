package br.com.example.todolist.filter;

import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import at.favre.lib.crypto.bcrypt.BCrypt.Result;
import br.com.example.todolist.user.IUserRepository;
import br.com.example.todolist.user.UserModel;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class FilterTaskAuth extends OncePerRequestFilter {

  @Autowired
  IUserRepository repository;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

    String serveletPath = request.getServletPath();
    if(serveletPath.startsWith("/tasks/")) {
      String authorization = request.getHeader("Authorization");
      String authEncoded = authorization.substring("Basic".length()).trim();
      byte[] authDecoded = Base64.getDecoder().decode(authEncoded);
      String authString = new String(authDecoded);
      String[] credentials = authString.split(":");
      String username = credentials[0];
      String password = credentials[1];

      UserModel user = repository.findByUsername(username);
      if(user == null) {
        response.sendError(401);
      }else {
        Result result = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword().toCharArray());

        if(result.verified) {
          request.setAttribute("idUser", user.getId());
          filterChain.doFilter(request, response);  
        }else {
          response.sendError(401);
        }
      }
    }else {
       filterChain.doFilter(request, response);  
    }

  }

}
