package me.kuku.sa.controller

import me.kuku.pojo.Result
import me.kuku.sa.entity.ExceptionLogEntity
import me.kuku.sa.entity.ExceptionLogService
import me.kuku.sa.pojo.Page
import me.kuku.sa.utils.convert
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.coRouter

@Component
class ExceptionLogController(
    private val exceptionLogService: ExceptionLogService
) {

    @Bean
    fun exceptionLog() = coRouter {

        "exceptionLog".nest {

            GET("") {
                val queryParams = it.queryParams()
                val exceptionLogEntity = queryParams.convert<ExceptionLogEntity>()
                val page = queryParams.convert<Page>()
                return@GET ok().bodyValueAndAwait(
                    Result.success(exceptionLogService.findByAll(exceptionLogEntity, page.toPageRequest()))
                )
            }

        }

    }

}