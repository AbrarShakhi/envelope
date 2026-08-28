package com.abrarshakhi.envelope.engine

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class EnvelopeEngineApplication

fun main(args: Array<String>) {
    runApplication<EnvelopeEngineApplication>(*args)
}
