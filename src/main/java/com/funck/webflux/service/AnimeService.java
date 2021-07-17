package com.funck.webflux.service;

import com.funck.webflux.domain.Anime;
import com.funck.webflux.repository.AnimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

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

    @Transactional
    public Flux<Anime> saveAll(List<Anime> animes) {
        return animeRepository.saveAll(animes)
                .doOnNext(this::throwResponseStatusExceptionWhenEmptyname);
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

    private void throwResponseStatusExceptionWhenEmptyname(Anime anime) {
        if (StringUtils.isEmpty(anime.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid name");
        }
    }

}
