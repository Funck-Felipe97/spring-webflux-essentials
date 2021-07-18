package com.funck.webflux;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.blockhound.BlockHound;

@SpringBootApplication
public class SpringWebfluxEssentialsApplication {

	static {
		BlockHound.install(buider -> {
			buider
					.allowBlockingCallsInside("java.io.InputStream", "readNBytes")
					.allowBlockingCallsInside("java.io.FilterInputStream", "read");
		});
	}

	public static void main(String[] args) {
		SpringApplication.run(SpringWebfluxEssentialsApplication.class, args);
	}

}
