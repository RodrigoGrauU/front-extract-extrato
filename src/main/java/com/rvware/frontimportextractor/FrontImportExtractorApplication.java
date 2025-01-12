package com.rvware.frontimportextractor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.rvsoftware.extractextrato", "com.rvware"})
public class FrontImportExtractorApplication {

	public static void main(String[] args) {
		SpringApplication.run(FrontImportExtractorApplication.class, args);
	}

}
