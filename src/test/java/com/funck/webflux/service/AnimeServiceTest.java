package com.funck.webflux.service;

import com.funck.webflux.domain.Anime;
import com.funck.webflux.repository.AnimeRepository;
import com.funck.webflux.util.AnimeCreator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ResponseStatusException;
import reactor.blockhound.BlockHound;
import reactor.blockhound.BlockingOperationError;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyIterable;

@ExtendWith(SpringExtension.class)
class AnimeServiceTest {

    @InjectMocks
    private AnimeService animeService;

    @Mock
    private AnimeRepository animeRepository;

    private Anime anime = AnimeCreator.createValidAnime();

    @BeforeAll
    static void beforeAll() {
        BlockHound.install();
    }

    @BeforeEach
    void setUp() {
        BDDMockito.when(animeRepository.findAll())
                .thenReturn(Flux.just(anime));

        BDDMockito.when(animeRepository.findById(anyInt()))
                .thenReturn(Mono.just(anime));

        BDDMockito.when(animeRepository.save(AnimeCreator.createAnimeToBeSaved()))
                .thenReturn(Mono.just(anime));

        BDDMockito.when(animeRepository.saveAll(List.of(AnimeCreator.createAnimeToBeSaved(), AnimeCreator.createAnimeToBeSaved())))
                .thenReturn(Flux.just(anime, anime));

        BDDMockito.when(animeRepository.delete(any(Anime.class)))
                .thenReturn(Mono.empty());
    }

    @Test
    @DisplayName("Verifica se o blockhound esta funcionando")
    void blockHoundWorks() {
        try {
            FutureTask<?> task = new FutureTask<>(() -> {
                Thread.sleep(0);
                return "";
            });

            Schedulers.parallel().schedule(task);

            task.get(10, TimeUnit.SECONDS);
            Assertions.fail("should fail");
        } catch (Exception e) {
            Assertions.assertTrue(e.getCause() instanceof BlockingOperationError);
        }
    }

    @Test
    @DisplayName("findAll returns a flux of Anime")
    void findAll_ReturnFluxOfAnime_whenSuccessful() {
        StepVerifier.create(animeService.listAll())
                .expectSubscription()
                .expectNext(anime)
                .verifyComplete();
    }

    @Test
    @DisplayName("findById returns a Mono of Anime")
    void findById_ReturnMonoOfAnime_whenSuccessful() {
        StepVerifier.create(animeService.findById(1))
                .expectSubscription()
                .expectNext(anime)
                .verifyComplete();
    }

    @Test
    @DisplayName("findById returns Mono error when anime does not exists")
    void findById_ReturnMonoError_whenEmptyMonoReturns() {
        BDDMockito.when(animeRepository.findById(anyInt()))
                .thenReturn(Mono.empty());

        StepVerifier.create(animeService.findById(1))
                .expectSubscription()
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    @DisplayName("Save create an anime when successful")
    void save_CreateAnime_whenSuccessful() {
        Anime animeToBeSaved = AnimeCreator.createAnimeToBeSaved();

        StepVerifier.create(animeService.save(animeToBeSaved))
                .expectSubscription()
                .expectNext(anime)
                .verifyComplete();
    }

    @Test
    @DisplayName("SaveAll create some animes list when successful")
    void saveAll_CreateAnimes_whenSuccessful() {
        Anime animeToBeSaved = AnimeCreator.createAnimeToBeSaved();

        StepVerifier.create(animeService.saveAll(List.of(animeToBeSaved, animeToBeSaved)))
                .expectSubscription()
                .expectNext(anime, anime)
                .verifyComplete();
    }

    @Test
    @DisplayName("saveAll returns mono error when has any anime with invalid name")
    void saveAll_ReturnsMonoError_whenContaingInvalidName() {
        Anime animeToBeSaved = AnimeCreator.createAnimeToBeSaved();

        BDDMockito.when(animeRepository.saveAll(anyIterable()))
                .thenReturn(Flux.just(anime, anime.withName("")));

        StepVerifier.create(animeService.saveAll(List.of(animeToBeSaved, animeToBeSaved.withName(""))))
                .expectSubscription()
                .expectNext(anime)
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    @DisplayName("Delete removes anime when successful")
    void delete_Remove_whenSuccessful() {
        StepVerifier.create(animeService.delete(1))
                .expectSubscription()
                .verifyComplete();
    }

    @Test
    @DisplayName("Delete returns error when anime does not exists")
    void delete_returnError_whenNotFound() {
        BDDMockito.when(animeRepository.findById(anyInt()))
                .thenReturn(Mono.empty());

        StepVerifier.create(animeService.delete(1))
                .expectSubscription()
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    @DisplayName("Update save updated anime when successful and returns empty mono")
    void update_SaveUpdateAndReturnEmptyMono_whenSuccessful() {
        BDDMockito.when(animeRepository.save(anime)).thenReturn(Mono.just(anime));

        StepVerifier.create(animeService.update(anime, 1))
                .expectSubscription()
                .verifyComplete();
    }

    @Test
    @DisplayName("Update return mono error when anime not exists")
    void update_returnMonoError_whenNotExists() {
        BDDMockito.when(animeRepository.findById(anyInt())).thenReturn(Mono.empty());

        StepVerifier.create(animeService.update(anime, 1))
                .expectSubscription()
                .expectError(ResponseStatusException.class)
                .verify();
    }

}