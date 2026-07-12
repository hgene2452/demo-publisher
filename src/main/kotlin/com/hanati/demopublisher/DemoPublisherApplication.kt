package com.hanati.demopublisher

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class DemoPublisherApplication

fun main(args: Array<String>) {
    runApplication<DemoPublisherApplication>(*args)
}
