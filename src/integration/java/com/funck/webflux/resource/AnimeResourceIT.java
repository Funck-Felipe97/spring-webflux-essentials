package com.funck.webflux.resource;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.blockhound.BlockHound;
import reactor.blockhound.BlockingOperationError;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyIterable;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureWebTestClient
public class AnimeResourceIT {

    private static final String ADMIN = "admin";
    private static final String REGULAR = "felipe";

    @MockBean
    private AnimeRepository animeRepository;

    @Autowired
    private WebTestClient client;

    private final Anime anime = AnimeCreator.createValidAnime();

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

        BDDMockito.when(animeRepository.delete(any(Anime.class)))
                .thenReturn(Mono.empty());

        BDDMockito.when(animeRepository.saveAll(List.of(AnimeCreator.createAnimeToBeSaved(), AnimeCreator.createAnimeToBeSaved())))
                .thenReturn(Flux.just(anime, anime));
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
    @WithUserDetails(REGULAR)
    void findAll_ReturnFluxOfAnime_whenSuccessful() {
        client
                .get()
                .uri("/animes")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.[0].id").isEqualTo(anime.getId())
                .jsonPath("$.[0].name").isEqualTo(anime.getName());
    }

    @Test
    @DisplayName("findById returns a Mono of Anime")
    @WithUserDetails(REGULAR)
    void findById_ReturnMonoOfAnime_whenSuccessful() {
        client
                .get()
                .uri("/animes/{id}", 1)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(anime.getId())
                .jsonPath("$.name").isEqualTo(anime.getName());
    }

    @Test
    @DisplayName("findById returns Mono error when anime does not exists")
    @WithUserDetails(REGULAR)
    void findById_ReturnMonoError_whenEmptyMonoReturns() {
        BDDMockito.when(animeRepository.findById(anyInt()))
                .thenReturn(Mono.empty());

        client
                .get()
                .uri("/animes/{id}", 1)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("Save create an anime when successful and user has role ADMIN")
    @WithUserDetails(ADMIN)
    void save_CreateAnime_whenSuccessful() {
        client
                .post()
                .uri("/animes")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(AnimeCreator.createAnimeToBeSaved()))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo(anime.getId())
                .jsonPath("$.name").isEqualTo(anime.getName());
    }

    @Test
    @DisplayName("Saveall create a list of animes when successful and user has role ADMIN")
    @WithUserDetails(ADMIN)
    void saveAll_CreateAnimes_whenSuccessful() {
        client
                .post()
                .uri("/animes/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(List.of(AnimeCreator.createAnimeToBeSaved(), AnimeCreator.createAnimeToBeSaved())))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.[0].id").isEqualTo(anime.getId())
                .jsonPath("$.[0].name").isEqualTo(anime.getName())
                .jsonPath("$.[1].id").isEqualTo(anime.getId())
                .jsonPath("$.[1].name").isEqualTo(anime.getName());
    }

    @Test
    @DisplayName("SaveAll returns bad request when has any invalid anime and user has role ADMIN")
    @WithUserDetails(ADMIN)
    void saveAll_ReturnsError_whenHasEmptyName() {
        BDDMockito.when(animeRepository.saveAll(anyIterable()))
                .thenReturn(Flux.just(anime, anime.withName("")));

        client
                .post()
                .uri("/animes/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(List.of(AnimeCreator.createAnimeToBeSaved(), AnimeCreator.createAnimeToBeSaved().withName(""))))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("Save return bad request when name is empty and user has role ADMIN")
    @WithUserDetails(ADMIN)
    void save_ReturnError_whenNameIsEmpty() {
        client
                .post()
                .uri("/animes")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(AnimeCreator.createAnimeToBeSaved().withName("")))
                .exchange()
                .expectStatus().isBadRequest();
    }

    /*@Test
    @DisplayName("Delete removes anime when successful")
    void delete_Remove_whenSuccessful() {
        BDDMockito.when(animeRepository.delete(anime)).thenReturn(Mono.empty());

        webTestClient
                .delete()
                .uri("animes/{id}", 1)
                .exchange()
                .expectStatus().isNoContent();
    }*/

    @Test
    @DisplayName("Delete returns error when anime does not exists")
    @WithUserDetails(REGULAR)
    void delete_returnError_whenNotFound() {
        BDDMockito.when(animeRepository.findById(anyInt()))
                .thenReturn(Mono.empty());

        client
                .delete()
                .uri("animes/{id}", 1)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("Update save updated anime when successful and user has role admin and returns empty mono")
    @WithUserDetails(ADMIN)
    void update_SaveUpdateAndReturnEmptyMono_whenSuccessful() {
        BDDMockito.when(animeRepository.save(anime)).thenReturn(Mono.just(anime));

        client
                .put()
                .uri("/animes/{id}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(anime))
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @DisplayName("Update return mono error when anime not exists")
    @WithUserDetails(REGULAR)
    void update_returnMonoError_whenNotExists() {
        BDDMockito.when(animeRepository.findById(anyInt())).thenReturn(Mono.empty());

        client
                .put()
                .uri("animes/{id}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(AnimeCreator.createValidAnime()))
                .exchange()
                .expectStatus().isNotFound();
    }

}
