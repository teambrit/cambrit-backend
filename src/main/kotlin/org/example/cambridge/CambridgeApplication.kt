package org.example.cambridge

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients

@SpringBootApplication
@EnableFeignClients
@OpenAPIDefinition(
	info = Info(title = "Cambridge API", version = "v1")
)
class CambridgeApplication

fun main(args: Array<String>) {
	runApplication<CambridgeApplication>(*args)
}
