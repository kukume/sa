package me.kuku.sa

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SaApplication

fun main(args: Array<String>) {
    runApplication<SaApplication>(*args)
}