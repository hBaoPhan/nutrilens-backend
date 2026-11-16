package com.nutrilens.gateway.filter;

import com.nutrilens.common.security.JwtUtils;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    @Value("${jwt.secret:9a7f8e6d5c4b3a210fedcba9876543210123456789abcdef0123456789abcdef}")
    private String jwtSecret;

    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/api/auth/login",
            "/api/v1/auth/login",
            "/api/auth/register",
            "/api/v1/auth/register",
            "/api/nutrition/foods",
            "/api/v1/nutrition/foods",
            "/api/nutrition/tips",
            "/api/v1/tips",
            "/api/nutrition/resolve-volume",
            "/api/v1/nutrition/resolve-volume",
            "/api/nutrition/resolve-volume-multi",
            "/api/v1/nutrition/resolve-volume-multi",
            "/api/ai/",
            "/predict"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 1. Allow public endpoints
        boolean isPublic = PUBLIC_ENDPOINTS.stream().anyMatch(path::startsWith);
        if (isPublic) {
            return chain.filter(exchange);
        }

        // 2. Extract Authorization header
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);

        // 3. Validate Token
        if (!JwtUtils.validateToken(token, jwtSecret) || JwtUtils.isTokenExpired(token, jwtSecret)) {
            return onError(exchange, "Token is expired or invalid", HttpStatus.UNAUTHORIZED);
        }

        // 4. Token Relay: Extract claims and pass down to downstream services
        try {
            Claims claims = JwtUtils.extractAllClaims(token, jwtSecret);
            String username = claims.getSubject();
            String role = (String) claims.get("role");

            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-User-Username", username != null ? username : "")
                    .header("X-User-Role", role != null ? role : "")
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        } catch (Exception e) {
            return onError(exchange, "Failed to parse JWT claims", HttpStatus.UNAUTHORIZED);
        }
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        return response.setComplete();
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
