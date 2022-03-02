package me.kuku.sa

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication
@EnableJpaAuditing
class SaApplication

fun main(args: Array<String>) {
    runApplication<SaApplication>(*args)
}