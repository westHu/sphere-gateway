package com.sphere.infrastructure.filter;

import com.sphere.common.exception.GatewayException;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

import java.util.function.Supplier;

/**
 * @author west
 */
public class TradeCachedBodyOutputMessage implements ReactiveHttpOutputMessage {

    private final DataBufferFactory bufferFactory;
    private final HttpHeaders httpHeaders;
    private boolean cached = false;
    private Flux<DataBuffer> body = Flux.error(new GatewayException("The body is not set. Did handling complete " +
            "with success?"));

    public TradeCachedBodyOutputMessage(ServerWebExchange exchange, HttpHeaders httpHeaders) {
        this.bufferFactory = exchange.getResponse().bufferFactory();
        this.httpHeaders = httpHeaders;
    }

    public void beforeCommit(@NonNull Supplier<? extends Mono<Void>> action) {
        //
    }

    public boolean isCommitted() {
        return false;
    }

    public boolean isCached() {
        return this.cached;
    }

    @NonNull
    public HttpHeaders getHeaders() {
        return this.httpHeaders;
    }

    @NonNull
    public DataBufferFactory bufferFactory() {
        return this.bufferFactory;
    }

    public Flux<DataBuffer> getBody() {
        return this.body;
    }

    @NonNull
    public Mono<Void> writeWith(@NonNull Publisher<? extends DataBuffer> body) {
        this.body = Flux.from(body);
        this.cached = true;
        return Mono.empty();
    }

    @NonNull
    public Mono<Void> writeAndFlushWith(@NonNull Publisher<? extends Publisher<? extends DataBuffer>> body) {
        return this.writeWith(Flux.from(body).flatMap(p -> p));
    }

    @NonNull
    public Mono<Void> setComplete() {
        return this.writeWith(Flux.empty());
    }
}
