package me.kuku.sa.controller

import me.kuku.pojo.Result
import me.kuku.sa.entity.CallLoggingEntity
import me.kuku.sa.entity.CallLoggingService
import me.kuku.sa.pojo.Page
import me.kuku.sa.utils.convert
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.coRouter

@Component
class CallLoggingController(
    private val callLoggingService: CallLoggingService
) {

    @Bean
    fun callLoggingRouter() = coRouter {
        "callLogging".nest {

            GET("") {
                val queryParams = it.queryParams()
                val callLoggingEntity = queryParams.convert<CallLoggingEntity>()
                val page = queryParams.convert<Page>()
                ok().bodyValueAndAwait(
                    Result.success(callLoggingService.findByAll(callLoggingEntity, page.toPageRequest()))
                )
            }

        }
    }

}