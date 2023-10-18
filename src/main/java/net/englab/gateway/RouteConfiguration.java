package net.englab.gateway;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;

@Configuration
@RequiredArgsConstructor
public class RouteConfiguration {
    private final ClientAddressResolver clientAddressResolver;
    @Value("${gateway.context-searcher.url}")
    private String contextSearcherUrl;
    @Value("${gateway.feedback-collector.url}")
    private String feedbackCollectorUrl;

    @Bean
    public RouteLocator myRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                // Available to everyone (with rate limits and captcha)
                .route(r -> r
                        .path("/search")
                        .filters(f -> f
                                .setPath("/api/v1/searcher/search")
                                .requestRateLimiter(c -> c.setKeyResolver(clientAddressResolver)))
                        .uri(contextSearcherUrl))
                .route(r -> r
                        .path("/feedback")
                        .and()
                        .method(HttpMethod.POST)
                        .filters(f -> f
                                .setPath("/api/v1/feedback")
                                .requestRateLimiter(c -> c
                                        .setRateLimiter(redisStrictRateLimiter())
                                        .setKeyResolver(clientAddressResolver)))
                        .uri(feedbackCollectorUrl))
                // Available only to admins
                // ...
                .build();
    }

    @Bean
    @Primary
    public RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(1, 1, 1);
    }

    @Bean
    public RedisRateLimiter redisStrictRateLimiter() {
        return new RedisRateLimiter(1, 10, 10);
    }
}
