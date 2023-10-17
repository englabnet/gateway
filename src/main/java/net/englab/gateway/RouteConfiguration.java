package net.englab.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

@Configuration
public class RouteConfiguration {
    @Value("${gateway.context-searcher.url}")
    private String contextSearcherUrl;
    @Value("${gateway.feedback-collector.url}")
    private String feedbackCollectorUrl;

    @Bean
    public RouteLocator myRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                // Available to everyone (with captcha)
                .route(r -> r
                        .path("/search")
                        .filters(f -> f.setPath("/api/v1/searcher/search"))
                        .uri(contextSearcherUrl))
                .route(r -> r
                        .path("/feedback")
                        .and()
                        .method(HttpMethod.POST)
                        .filters(f -> f.setPath("/api/v1/feedback"))
                        .uri(feedbackCollectorUrl))
                // Available only to admins
                // ...
                .build();
    }
}
