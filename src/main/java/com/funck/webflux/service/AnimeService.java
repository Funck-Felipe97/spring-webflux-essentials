package com.funck.webflux.service;

import com.funck.webflux.domain.Anime;
import com.funck.webflux.repository.AnimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

    private <T> Mono<T> monoNotFoundError() {
        return Mono.error(new ResponseStatusException(NOT_FOUND, "Anime not found"));
    }

}
