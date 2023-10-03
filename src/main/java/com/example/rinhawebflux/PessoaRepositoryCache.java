package com.example.rinhawebflux;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.UUID;

@Component
public class PessoaRepositoryCache implements PessoaDatabaseCache {
    private final ReactiveRedisOperations<String, String> opsRedis;
    private final ObjectMapper mapper;
    private static final Logger log = LoggerFactory.getLogger(PessoaRepositoryCache.class);

    public PessoaRepositoryCache(ReactiveRedisOperations<String, String> opsRedis, ObjectMapper mapper) {
        this.opsRedis = opsRedis;
        this.mapper = mapper;
        this.mapper.registerModule(new JavaTimeModule());
    }

    @Override
    public Mono<UUID> salvarCacheV2(Pessoa pessoa) throws JsonProcessingException {
        var json = this.mapper.writeValueAsString(pessoa);
        return Mono.fromCallable(() -> opsRedis.opsForHash().put("byId", pessoa.getId().toString(), json)
                        .map(s -> pessoa.getId())
                        .block()
                )
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Pessoa> buscarCachePorIdV2(String id) {
        return Mono.fromCallable(() -> opsRedis.opsForHash().get("byId", id)
                        .<Pessoa>handle((s, sink) -> {
                            Pessoa p;
                            try {
                                p = this.mapper.readValue((String) s, Pessoa.class);
                            } catch (JsonProcessingException e) {
                                sink.error(new RuntimeException(e));
                                return;
                            }
                            sink.next(p);
                        })
                        .block()
                )
                .subscribeOn(Schedulers.boundedElastic());
    }


    @Override
    public Mono<UUID> salvarCacheV1(Pessoa pessoa) {
        return Mono.fromCallable(() -> opsRedis.opsForHash().put("byId", pessoa.getId().toString(), pessoa.getApelido())
                        .map(s -> pessoa.getId())
                        .block()
                )
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Boolean> buscarCachePorIdV1(String id) {
        return Mono.fromCallable(() -> opsRedis.opsForHash().hasKey("byId", id)
                        .flatMap(exists -> {
                            if (exists) return Mono.just(true);
                            return Mono.empty();
                        })
                        .block()
                )
                .subscribeOn(Schedulers.boundedElastic());
    }
}
