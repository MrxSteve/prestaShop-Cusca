package com.cusca.shopmoney_pg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ShopmoneyPgApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShopmoneyPgApplication.class, args);
	}

}
