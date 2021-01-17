package io.github.zbytes.examples.orderservice.infrastructure.config;

import io.github.zbytes.examples.orderservice.infrastructure.jpa.JpaRoot;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan(basePackageClasses = {JpaRoot.class})
@EnableJpaRepositories(considerNestedRepositories = true, basePackageClasses = {JpaRoot.class})
public class JpaConfig {

}
