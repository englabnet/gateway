package net.englab.gateway.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity.CsrfSpec;
import org.springframework.security.web.server.SecurityWebFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class SecurityConfiguration {
    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .csrf(CsrfSpec::disable)
                .cors(withDefaults())
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(HttpMethod.GET, "/public/api/v1/search")
                        .permitAll()
                        .pathMatchers(HttpMethod.GET, "/public/api/v1/words/**")
                        .permitAll()
                        .pathMatchers(HttpMethod.GET, "/public/api/v1/tests/**")
                        .permitAll()
                        .pathMatchers(HttpMethod.POST, "/public/api/v1/tests")
                        .permitAll()
                        .pathMatchers(HttpMethod.GET, "/public/api/v1/media/**")
                        .permitAll()
                        .pathMatchers(HttpMethod.POST, "/public/api/v1/feedback")
                        .permitAll()
                        .pathMatchers(HttpMethod.OPTIONS, "/public/api/v1/feedback")
                        .permitAll()
                        .anyExchange()
                        .authenticated()
                )
                .httpBasic(withDefaults());
        return http.build();
    }
}
