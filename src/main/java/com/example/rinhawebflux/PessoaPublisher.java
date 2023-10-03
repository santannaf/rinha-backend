package com.example.rinhawebflux;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Component
public class PessoaPublisher {
    private final Sinks.Many<Pessoa> sink;

    public PessoaPublisher(Sinks.Many<Pessoa> sink) {
        this.sink = sink;
    }

    public Mono<String> onReceiveRequestV2(final Pessoa pessoa) {
        this.sink.tryEmitNext(pessoa);
        return Mono.just("sent");
    }

    public void onReceiveRequestV1(final Pessoa pessoa) {
        this.sink.tryEmitNext(pessoa);
    }
}
