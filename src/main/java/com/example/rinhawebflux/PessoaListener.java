package com.example.rinhawebflux;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

@Component
public class PessoaListener implements InitializingBean {
    private final Sinks.Many<Pessoa> sink;
    private final PessoaDatabase pessoaDatabase;

    public PessoaListener(Sinks.Many<Pessoa> sink, PessoaDatabase pessoaDatabase) {
        this.sink = sink;
        this.pessoaDatabase = pessoaDatabase;
    }

    @Override
    public void afterPropertiesSet() {
        this.sink
                .asFlux()
                .subscribe(next -> this.pessoaDatabase.salvar(next)
                        .subscribeOn(Schedulers.boundedElastic())
                        .subscribe()
                );
    }
}
