package com.p2.calculator_service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {
    private final JwtUtil jwtUtil;

    public JwtInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestUri = request.getRequestURI();

        if (requestUri.equals("/api/calculator") ||
                requestUri.startsWith("/api/calculator/")) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if(authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized: Missing or invalid token format.");
            return false;
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized: invalid token.");
            return false;
        }

        Long userId = jwtUtil.getUserIdFromToken(token);
        String userRole = jwtUtil.getRoleFromToken(token);

//        if (requestUri.startsWith("/api/admin")) {
//            if (!"ADMIN".equals(userRole)) {
//                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
//                response.getWriter().write("Forbidden: Administrator access required.");
//                return false;
//            }
//        }

        request.setAttribute("userId", userId);
        request.setAttribute("userRole", userRole);

        return true;
    }
}
