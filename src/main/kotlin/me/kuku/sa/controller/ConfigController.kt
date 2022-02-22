package me.kuku.sa.controller

import me.kuku.pojo.Result
import me.kuku.pojo.ResultStatus
import me.kuku.sa.entity.ConfigContent
import me.kuku.sa.entity.ConfigEntity
import me.kuku.sa.entity.ConfigService
import me.kuku.sa.entity.ConfigType
import me.kuku.sa.utils.awaitReceive
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.coRouter

@Component
class ConfigController(
    private val configService: ConfigService
) {

    @Bean
    fun config() = coRouter {

        "config".nest {

            GET("type") {
                val array = ConfigType.values()
                val list = mutableListOf<Map<String, Any>>()
                array.forEach { list.add(mapOf("id" to it.ordinal, "value" to it.value)) }
                ok().bodyValueAndAwait(Result.success(list))
            }

            GET("") {
                val list = configService.findAll()
                ok().bodyValueAndAwait(Result.success(list))
            }

            GET("configType/{type}") {
                val type = it.pathVariable("type").toIntOrNull() ?: return@GET ok().bodyValueAndAwait(Result.failure("类型不正确", null))
                val configType = ConfigType.byOrdinal(type) ?: return@GET ok().bodyValueAndAwait(Result.failure<Unit>("类型不正确"))
                val configEntity = configService.findByConfigType(configType)
                if (configEntity == null)
                 ok().bodyValueAndAwait(Result.failure<Unit>(ResultStatus.DATA_NOT_EXISTS))
                else ok().bodyValueAndAwait(Result.success(configEntity))
            }

            POST("{configType}") {
                val configContent = it.awaitReceive<ConfigContent>()
                val type = it.pathVariable("configType").toInt()
                val configType = ConfigType.byOrdinal(type)
                    ?: return@POST ok().bodyValueAndAwait(Result.failure(ResultStatus.PARAM_ERROR, null))
                val configEntity = configService.findByConfigType(configType)
                    ?: ConfigEntity().also { entity -> entity.configType = configType }
                configEntity.content = configContent
                configService.save(configEntity)
                ok().bodyValueAndAwait(Result.success(configEntity))
            }

        }

    }

}