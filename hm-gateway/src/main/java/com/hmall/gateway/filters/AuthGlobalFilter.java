package com.hmall.gateway.filters;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.AntPathMatcher;
import com.hmall.common.exception.UnauthorizedException;
import com.hmall.gateway.config.AuthProperties;
import com.hmall.gateway.utils.JwtTool;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
@Component
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final AuthProperties authProperties;

    private final JwtTool jwtTool;

    //AntPattern，类似于正则表达式：/path/**
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        if(isExclude(request.getPath())){ //放行
            return chain.filter(exchange);
        }
        List<String> authorization = request.getHeaders().get("Authorization");
        String token = null;
        if(authorization != null && !authorization.isEmpty()){
            token = authorization.get(0);
        }
        Long userId = null;
        try{
            userId = jwtTool.parseToken(token);
        } catch(UnauthorizedException e){
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete(); //返回的是Mono。这样子就不往下走了！
        }

        //传输userID
        String userIdString = userId.toString();
        ServerWebExchange newExchange = exchange.mutate()
                .request(builder -> {
                    builder.header("userId", userIdString);
                }).build();

        return chain.filter(newExchange);
    }

    private boolean isExclude(RequestPath path) {
        List<String> excludePaths = authProperties.getExcludePaths();
        for(String pattern: excludePaths){
            if (antPathMatcher.match(pattern, path.toString())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
