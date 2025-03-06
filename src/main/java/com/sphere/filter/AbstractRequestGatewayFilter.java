package com.sphere.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.support.BodyInserterContext;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

import java.util.function.BiFunction;

@Slf4j
public abstract class AbstractRequestGatewayFilter {

    /**
     * filter
     */
    protected Mono<Void> buildVoidMono(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerRequest serverRequest = ServerRequest.create(exchange, HandlerStrategies.withDefaults().messageReaders());
        Mono<String> modifiedBody = serverRequest.bodyToMono(String.class)
                .flatMap(originalBody -> modifyBody().apply(exchange, originalBody));

        BodyInserter<Mono<String>, ReactiveHttpOutputMessage> bodyInserter =
                BodyInserters.fromPublisher(modifiedBody, String.class);

        HttpHeaders headers = new HttpHeaders();
        headers.putAll(exchange.getRequest().getHeaders());
        headers.remove("Content-Length");

        TradeCachedBodyOutputMessage outputMessage = new TradeCachedBodyOutputMessage(exchange, headers);

        return bodyInserter.insert(outputMessage, new BodyInserterContext()).then(Mono.defer(() -> {
            ServerHttpRequest decorator = decorate(exchange, headers, outputMessage);
            return chain.filter(exchange.mutate().request(decorator).build());
        })).onErrorResume(throwable -> release(outputMessage, throwable));
    }

    /**
     * request release
     */
    protected Mono<Void> release(TradeCachedBodyOutputMessage outputMessage,
                                 Throwable throwable) {
        return outputMessage.isCached() ?
                outputMessage.getBody().map(DataBufferUtils::release).then(Mono.error(throwable)) :
                Mono.error(throwable);
    }

    /**
     * decorate
     */
    protected ServerHttpRequestDecorator decorate(ServerWebExchange exchange, HttpHeaders headers,
                                                  TradeCachedBodyOutputMessage outputMessage) {
        return new ServerHttpRequestDecorator(exchange.getRequest()) {

            @NonNull
            @Override
            public HttpHeaders getHeaders() {
                long contentLength = headers.getContentLength();
                HttpHeaders httpHeaders = new HttpHeaders();
                httpHeaders.putAll(headers);
                if (contentLength > 0L) {
                    httpHeaders.setContentLength(contentLength);
                } else {
                    httpHeaders.set("Transfer-Encoding", "chunked");
                }

                return httpHeaders;
            }

            @NonNull
            @Override
            public Flux<DataBuffer> getBody() {
                return outputMessage.getBody();
            }
        };
    }

    /**
     * modifyBody
     */
    protected abstract BiFunction<ServerWebExchange, String, Mono<String>> modifyBody();

}
