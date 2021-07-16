package com.funck.webflux.util;

import com.funck.webflux.domain.Anime;

public class AnimeCreator {

    public static Anime createAnimeToBeSaved() {
        return Anime.builder()
                .name("Naruto")
                .build();
    }

    public static Anime createValidAnime() {
        return Anime.builder()
                .id(1)
                .name("Naruto")
                .build();
    }

    public static Anime createValidAnimeUpdated() {
        return Anime.builder()
                .id(1)
                .name("Naruto 2")
                .build();
    }

}
