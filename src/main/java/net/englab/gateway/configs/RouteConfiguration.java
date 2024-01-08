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
    @Value("${gateway.spelling-trainer.url}")
    private String spellingTrainerUrl;
    @Value("${gateway.feedback-collector.url}")
    private String feedbackCollectorUrl;
    @Value("${gateway.admin-console.url}")
    private String adminConsoleUrl;

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder, RecaptchaFilter recaptchaFilter) {
        return builder.routes()
                // Available to everyone (with rate limits and captcha)
                .route(r -> r
                        .path("/public/api/v1/search")
                        .and()
                        .method(HttpMethod.GET)
                        .filters(f -> f
                                .requestRateLimiter(c -> c.setKeyResolver(clientAddressResolver))
                                .rewritePath("/public", "/"))
                        .uri(contextSearcherUrl))
                .route(r -> r
                        .path("/public/api/v1/tests/**")
                        .and()
                        .method(HttpMethod.GET)
                        .or()
                        .path("/public/api/v1/words/**")
                        .and()
                        .method(HttpMethod.GET)
                        .filters(f -> f
                                .requestRateLimiter(c -> c.setKeyResolver(clientAddressResolver))
                                .rewritePath("/public", "/"))
                        .uri(spellingTrainerUrl))
                // we don't want to have any rate limiters when we access media
                .route(r -> r
                        .path("/public/api/v1/media/**")
                        .and()
                        .method(HttpMethod.GET)
                        .filters(f -> f.rewritePath("/public", "/"))
                        .uri(spellingTrainerUrl))
                .route(r -> r
                        .path("/public/api/v1/tests")
                        .and()
                        .method(HttpMethod.POST, HttpMethod.OPTIONS)
                        .filters(f -> f
                                .filter(recaptchaFilter)
                                .requestRateLimiter(c -> c
                                        .setRateLimiter(strictRedisRateLimiter())
                                        .setKeyResolver(clientAddressResolver))
                                .rewritePath("/public", "/"))
                        .uri(spellingTrainerUrl))
                .route(r -> r
                        .path("/public/api/v1/feedback")
                        .and()
                        .method(HttpMethod.POST, HttpMethod.OPTIONS)
                        .filters(f -> f
                                .filter(recaptchaFilter)
                                .requestRateLimiter(c -> c
                                        .setRateLimiter(strictRedisRateLimiter())
                                        .setKeyResolver(clientAddressResolver))
                                .rewritePath("/public", "/"))
                        .uri(feedbackCollectorUrl))
                // Available only to admins
                .route(r -> r
                        .path("/admin/admin-console/**")
                        .and()
                        .method(HttpMethod.GET)
                        .filters(f -> f.rewritePath("/admin/admin-console", "/"))
                        .uri(adminConsoleUrl))
                .route(r -> r
                        .path("/admin/api/v1/indexer/**")
                        .or()
                        .path("/admin/api/v1/videos/**")
                        .filters(f -> f.rewritePath("/admin", "/"))
                        .uri(contextSearcherUrl))
                .route(r -> r
                        .path("/admin/api/v1/feedback/**")
                        .filters(f -> f.rewritePath("/admin", "/"))
                        .uri(feedbackCollectorUrl))
                .build();
    }

    @Bean
    @Primary
    public RedisRateLimiter redisRateLimiter() {
        // 3 requests per second
        return new RedisRateLimiter(3, 3, 1);
    }

    @Bean
    public RedisRateLimiter strictRedisRateLimiter() {
        // 5 requests per 10 minutes
        return new RedisRateLimiter(1, 60 * 10, 60 * 2);
    }
}
