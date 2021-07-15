package com.funck.webflux.service;

import com.funck.webflux.domain.Anime;
import com.funck.webflux.repository.AnimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class AnimeService {

    private final AnimeRepository animeRepository;

    public Flux<Anime> listAll() {
        return animeRepository.findAll();
    }

    public Mono<Anime> findById(Integer id) {
        return animeRepository.findById(id)
                .switchIfEmpty(monoNotFoundError())
                .log();
    }

    public Mono<Anime> save(Anime anime) {
        return animeRepository.save(anime);
    }

    public Mono<Void> update(Anime anime, Integer id) {
        return findById(id)
                .map(animeFound -> anime.withId(animeFound.getId()))
                .flatMap(animeRepository::save)
                .thenEmpty(Mono.empty()); // ou somente then()
    }

    public Mono<Void> delete(Integer id) {
        return findById(id)
                .flatMap(animeRepository::delete);
    }

    private <T> Mono<T> monoNotFoundError() {
        return Mono.error(new ResponseStatusException(NOT_FOUND, "Anime not found"));
    }

}
