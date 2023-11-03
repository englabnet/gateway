package net.englab.gateway.filters;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.englab.gateway.models.RecaptchaResponse;
import net.englab.gateway.services.RecaptchaService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecaptchaFilter implements GatewayFilter {
    private final RecaptchaService recaptchaService;
    @Value("${gateway.recaptcha.enabled}")
    private boolean enabled;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!enabled) {
            return chain.filter(exchange);
        }
        var request = exchange.getRequest();
        String recaptchaToken = request.getHeaders().getFirst("recaptcha");
        if (!StringUtils.hasText(recaptchaToken)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "There is no recaptcha token");
        }
        RecaptchaResponse response = recaptchaService.validateToken(recaptchaToken);
        if (response.success() && response.score() >= 0.6) {
            return chain.filter(exchange);
        }
        throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "reCAPTCHA validation failed");
    }
}
