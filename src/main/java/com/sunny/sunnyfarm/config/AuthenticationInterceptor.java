package com.sunny.sunnyfarm.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthenticationInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // OPTIONS 요청은 인증 검사 생략
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        Object userId = request.getSession().getAttribute("userId");

        System.out.println("===============");
        System.out.println(userId);
        System.out.println("===============");

        if (userId == null) {
            System.out.println("로그인 필요");

            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"message\": \"로그인이 필요합니다.\"}");
            response.getWriter().flush();

            return false;
        }
        return true;
    }
}