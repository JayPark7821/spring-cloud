package com.example.apigatewayservice.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class LoggingFilter extends AbstractGatewayFilterFactory<LoggingFilter.Config> {

	public LoggingFilter() {
		super(Config.class);
	}

	@Getter
	@Setter
	public static class Config {
		// put the configuration properties
		private String baseMessage;
		private boolean preLogger;
		private boolean postLogger;
	}

	@Override
	public GatewayFilter apply(Config config) {
		// Custom Pre filter
		// return ((exchange, chain) -> {
		// 	ServerHttpRequest request = exchange.getRequest();
		// 	ServerHttpResponse response = exchange.getResponse();
		//
		// 	log.info("Global filter baseMessage: {} ", config.getBaseMessage());
		//
		// 	if (config.isPreLogger()) {
		// 		log.info("Global filter Start: request id -> {} ", request.getId());
		// 	}
		// 	//Custom Post Filter
		// 	return chain.filter(exchange).then(Mono.fromRunnable(() ->{
		// 		if (config.isPostLogger()) {
		// 			log.info("Global Filter End: response code -> {} ", response.getStatusCode());
		// 		}
		// 	}));
		// });

		GatewayFilter filter = new OrderedGatewayFilter((exchange, chain) -> {
				ServerHttpRequest request = exchange.getRequest();
				ServerHttpResponse response = exchange.getResponse();

				log.info("Logging filter baseMessage: {} ", config.getBaseMessage());

				if (config.isPreLogger()) {
					log.info("Logging PRE filter: request id -> {} ", request.getId());
				}
				//Custom Post Filter
				return chain.filter(exchange).then(Mono.fromRunnable(() ->{
					if (config.isPostLogger()) {
						log.info("Logging POST Filter: response code -> {} ", response.getStatusCode());
					}
				}));
		}, Ordered.HIGHEST_PRECEDENCE);

		return filter;
	}
}
