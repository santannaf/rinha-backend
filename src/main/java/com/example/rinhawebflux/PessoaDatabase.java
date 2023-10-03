package com.example.rinhawebflux;

import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PessoaDatabase {
    Mono<UUID> salvar(final Pessoa pessoa);
    Mono<Boolean> buscarPessoaPorId(final UUID id);
    Mono<Boolean> buscarPessoaTermo(final String t);
    Mono<Long> contagem();
}
