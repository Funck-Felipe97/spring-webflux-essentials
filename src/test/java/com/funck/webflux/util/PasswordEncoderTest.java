package com.funck.webflux.util;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;

public class PasswordEncoderTest {

    @Test
    void test() {
        System.out.println(PasswordEncoderFactories.createDelegatingPasswordEncoder().encode("1234"));
    }
}
