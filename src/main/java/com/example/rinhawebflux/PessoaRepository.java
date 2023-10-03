package com.example.rinhawebflux;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.UUID;

@Component
public class PessoaRepository implements PessoaDatabase {
    private final DatabaseClient client;
    private static final Logger log = LoggerFactory.getLogger(PessoaRepository.class);

    public PessoaRepository(DatabaseClient client) {
        this.client = client;
    }

    @Override
    public Mono<UUID> salvar(final Pessoa pessoa) {
        return Mono.fromCallable(() -> client.sql("insert into pessoas (id, apelido, nome, nascimento, stack) values (:id, :apelido, :nome, :nascimento, :stack)")
                        .bind("id", pessoa.getId())
                        .bind("apelido", pessoa.getApelido())
                        .bind("nome", pessoa.getNome())
                        .bind("nascimento", pessoa.getNascimento())
                        .bind("stack", pessoa.getStack())
                        .filter(f -> f.returnGeneratedValues("id"))
                        .fetch()
                        .first()
                        .map(row -> (UUID) row.get("id"))
                        .block()
                )
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Boolean> buscarPessoaPorId(UUID id) {
        return Mono.fromCallable(() -> client.sql("select id from pessoas where id = :id")
                        .bind("id", id)
                        .fetch()
                        .first()
                        .map(row -> true)
                        .switchIfEmpty(Mono.empty())
                        .block()
                )
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Boolean> buscarPessoaTermo(String t) {
        return Mono.fromCallable(() -> client.sql("select id from pessoas where busca ilike :t")
                        .bind("t", t)
                        .fetch()
                        .first()
                        .map(row -> true)
                        .switchIfEmpty(Mono.empty())
                        .block()
                )
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Long> contagem() {
        return Mono.fromCallable(() -> client.sql("select count(*) as contagem from pessoas")
                        .fetch()
                        .first()
                        .map(row -> (Long) row.get("contagem"))
                        .block()
                )
                .subscribeOn(Schedulers.boundedElastic());
    }
}
