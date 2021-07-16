package com.funck.webflux.resource;

import com.funck.webflux.domain.Anime;
import com.funck.webflux.service.AnimeService;
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
import reactor.blockhound.BlockHound;
import reactor.blockhound.BlockingOperationError;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.anyInt;

@ExtendWith(SpringExtension.class)
class AnimeResourceTest {

    @InjectMocks
    private AnimeResource animeResource;

    @Mock
    private AnimeService animeService;

    private Anime anime = AnimeCreator.createValidAnime();

    @BeforeAll
    static void beforeAll() {
        BlockHound.install();
    }

    @BeforeEach
    void setUp() {
        BDDMockito.when(animeService.listAll())
                .thenReturn(Flux.just(anime));

        BDDMockito.when(animeService.findById(anyInt()))
                .thenReturn(Mono.just(anime));

        BDDMockito.when(animeService.save(AnimeCreator.createAnimeToBeSaved()))
                .thenReturn(Mono.just(anime));
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
    @DisplayName("findAll returns a mono of Anime")
    void findAll_ReturnFluxOfAnime_whenSuccessful() {
        StepVerifier.create(animeResource.listAll())
                .expectSubscription()
                .expectNext(anime)
                .verifyComplete();
    }

    @Test
    @DisplayName("findById returns a Mono of Anime")
    void findById_ReturnMonoOfAnime_whenSuccessful() {
        StepVerifier.create(animeResource.findById(1))
                .expectSubscription()
                .expectNext(anime)
                .verifyComplete();
    }

    @Test
    @DisplayName("Save create an anime when successful")
    void save_CreateAnime_whenSuccessful() {
        Anime animeToBeSaved = AnimeCreator.createAnimeToBeSaved();

        StepVerifier.create(animeResource.save(animeToBeSaved))
                .expectSubscription()
                .expectNext(anime)
                .verifyComplete();
    }

    @Test
    @DisplayName("Delete removes anime when successful")
    void delete_Remove_whenSuccessful() {
        BDDMockito.when(animeService.delete(1)).thenReturn(Mono.empty());

        StepVerifier.create(animeResource.delete(1))
                .expectSubscription()
                .verifyComplete();
    }

    @Test
    @DisplayName("Update save updated anime when successful and returns empty mono")
    void update_SaveUpdateAndReturnEmptyMono_whenSuccessful() {
        BDDMockito.when(animeService.update(anime, 1)).thenReturn(Mono.empty());

        StepVerifier.create(animeResource.update(anime, 1))
                .expectSubscription()
                .verifyComplete();
    }

}