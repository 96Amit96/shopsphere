package com.shopsphere.authservice.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopsphere.authservice.dto.response.ApiResponse;
import com.shopsphere.authservice.entity.AuthUser;
import com.shopsphere.authservice.security.service.CustomUserDetailsService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
         @NonNull   HttpServletRequest request,
         @NonNull HttpServletResponse response,
         @NonNull   FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(7);

        try {
            String email = jwtService.extractUsername(jwt);
            UserDetails userDetails =
                    customUserDetailsService.loadUserByUsername(email);

            if (jwtService.isTokenValid(jwt, (AuthUser) userDetails)) {
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                SecurityContextHolder.getContext()
                        .setAuthentication(authenticationToken);
            }
            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException ex) {
            sendUnauthorizedResponse(
                    response,
                    "Access token has expired"
            );
        } catch (JwtException | IllegalArgumentException ex){
            sendUnauthorizedResponse(
                    response,
                    "Invalid access token"
            );
        }
    }

    private void sendUnauthorizedResponse(
            HttpServletResponse response,
            String message
    ) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ApiResponse<Void> apiResponse =
                ApiResponse.failure(message);

        response.getWriter().write(
                objectMapper.writeValueAsString(apiResponse)
        );
    }
}
