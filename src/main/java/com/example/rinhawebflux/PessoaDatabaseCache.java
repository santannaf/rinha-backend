package com.example.rinhawebflux;

import com.fasterxml.jackson.core.JsonProcessingException;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PessoaDatabaseCache {
    Mono<UUID> salvarCacheV1(final Pessoa pessoa);

    Mono<Boolean> buscarCachePorIdV1(final String id);

    Mono<UUID> salvarCacheV2(Pessoa pessoa) throws JsonProcessingException;

    Mono<Pessoa> buscarCachePorIdV2(String id);
}
