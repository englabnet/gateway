package net.englab.gateway.configs;

import lombok.RequiredArgsConstructor;
import net.englab.gateway.filters.RecaptchaFilter;
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
    public RouteLocator routeLocator(RouteLocatorBuilder builder, RecaptchaFilter recaptchaFilter) {
        return builder.routes()
                // Available to everyone (with rate limits and captcha)
                .route(r -> r
                        .path("/api/v1/search")
                        .and()
                        .method(HttpMethod.GET)
                        .filters(f -> f
                                .requestRateLimiter(c -> c.setKeyResolver(clientAddressResolver)))
                        .uri(contextSearcherUrl))
                .route(r -> r
                        .path("/api/v1/feedback")
                        .and()
                        .method(HttpMethod.POST, HttpMethod.OPTIONS)
                        .filters(f -> f
                                .filter(recaptchaFilter)
                                .requestRateLimiter(c -> c
                                        .setRateLimiter(strictRedisRateLimiter())
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
    public RedisRateLimiter strictRedisRateLimiter() {
        // 5 requests per 10 minutes
        return new RedisRateLimiter(1, 60 * 10, 60 * 2);
    }
}
