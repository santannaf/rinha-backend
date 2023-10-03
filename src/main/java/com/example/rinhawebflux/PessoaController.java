package com.example.rinhawebflux;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.UUID;

@RestController
@RequestMapping(path = {"/pessoas"})
public class PessoaController {
    private final PessoaDatabase database;
    private final PessoaDatabaseCache cache;
    private final PessoaPublisher publisher;

    public PessoaController(PessoaDatabase database, PessoaDatabaseCache cache, PessoaPublisher publisher) {
        this.database = database;
        this.cache = cache;
        this.publisher = publisher;
    }

    /*
    *
    * V1 endpoint CREATED People
    * - flow
    * 1 - validate payload
    * 2 - save to database
    * 3 - save to cache redis
    *
    * */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Deprecated
    public Mono<ResponseEntity<Object>> createPessoaV1(@RequestBody final Pessoa request, UriComponentsBuilder componentsBuilder) {
        if (!request.isValid()) return Mono.just(ResponseEntity.unprocessableEntity().build());
        if (!request.isTypeValid()) return Mono.just(ResponseEntity.badRequest().build());

        final UUID id = request.getId();

        return database.salvar(request)
                .flatMap(next -> this.cache.salvarCacheV1(request))
                .subscribeOn(Schedulers.boundedElastic())
                .thenReturn(ResponseEntity.created(
                                        componentsBuilder
                                                .path("/pessoas/{id}")
                                                .buildAndExpand(id)
                                                .toUri()
                                )
                                .build()
                )
                .onErrorReturn(ResponseEntity.badRequest().build());
    }

    /*
     *
     * V2 endpoint CREATED People
     * - flow
     * 1 - validate payload
     * 2 - save to cache
     * 3 - publisher event
     *
     * */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Deprecated
    public Mono<ResponseEntity<Object>> createPessoaV2(@RequestBody final Pessoa request, UriComponentsBuilder componentsBuilder) throws JsonProcessingException {
        if (!request.isValid()) return Mono.just(ResponseEntity.unprocessableEntity().build());
        if (!request.isTypeValid()) return Mono.just(ResponseEntity.badRequest().build());

        final UUID id = request.getId();

        return this.cache.salvarCacheV2(request)
                .doOnNext(next -> this.publisher.onReceiveRequestV1(request))
                .subscribeOn(Schedulers.boundedElastic())
                .thenReturn(ResponseEntity.created(
                                        componentsBuilder
                                                .path("/pessoas/{id}")
                                                .buildAndExpand(id)
                                                .toUri()
                                )
                                .build()
                )
                .onErrorReturn(ResponseEntity.badRequest().build());
    }

    /*
     *
     * V3 endpoint CREATED People
     * - flow
     * 1 - validate payload
     * 2 - save to cache and publisher event (concurrently)
     *
     * */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ResponseEntity<Object>> createPessoaV3(@RequestBody final Pessoa request, UriComponentsBuilder componentsBuilder) throws JsonProcessingException {
        if (!request.isValid()) return Mono.just(ResponseEntity.unprocessableEntity().build());
        if (!request.isTypeValid()) return Mono.just(ResponseEntity.badRequest().build());

        final UUID id = request.getId();

        var atCache = this.cache.salvarCacheV2(request).subscribeOn(Schedulers.boundedElastic());
        var atPublisher = this.publisher.onReceiveRequestV2(request).subscribeOn(Schedulers.boundedElastic());

        return Mono.zip(atCache, atPublisher)
                .thenReturn(ResponseEntity.created(
                                        componentsBuilder
                                                .path("/pessoas/{id}")
                                                .buildAndExpand(id)
                                                .toUri()
                                )
                                .build()
                )
                .onErrorReturn(ResponseEntity.badRequest().build());
    }

    @GetMapping(path = {"/{id}"})
    @ResponseStatus(HttpStatus.OK)
    public Mono<ResponseEntity<Object>> buscaPessoaPorIdV1(@PathVariable UUID id) {
        return cache.buscarCachePorIdV1(id.toString())
                .flatMap(r -> Mono.just(ResponseEntity.ok().build()))
                .switchIfEmpty(
                        Mono.defer(() -> database.buscarPessoaPorId(id)
                                .map(result -> ResponseEntity.ok().build())
                                .defaultIfEmpty(ResponseEntity.notFound().build())
                        )
                )
                .subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping(path = {"/{id}"})
    @ResponseStatus(HttpStatus.OK)
    @Deprecated
    public Mono<ResponseEntity<Object>> buscaPessoaPorIdV2(@PathVariable UUID id) {
        return cache.buscarCachePorIdV2(id.toString())
                .flatMap(r -> Mono.just(ResponseEntity.ok().build()))
                .switchIfEmpty(
                        Mono.defer(() -> database.buscarPessoaPorId(id)
                                .map(result -> ResponseEntity.ok().build())
                                .defaultIfEmpty(ResponseEntity.notFound().build())
                        )
                )
                .subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Mono<ResponseEntity<Object>> buscaPessoaPorTermo(@RequestParam String t) {
        return this.database.buscarPessoaTermo("%" + t + "%")
                .flatMap(r -> Mono.just(ResponseEntity.ok().build()))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping(path = {"/contagem-pessoas"})
    @ResponseStatus(HttpStatus.OK)
    public Mono<Long> contagemPessoas() {
        return this.database.contagem();
    }
}
