package me.kuku.sa.config

import com.alibaba.fastjson.JSON
import me.kuku.pojo.Result
import me.kuku.sa.entity.CallLoggingEntity
import me.kuku.sa.entity.CallLoggingService
import me.kuku.sa.entity.ExceptionLogEntity
import me.kuku.sa.entity.ExceptionLogService
import me.kuku.sa.utils.MissingRequestParameterException
import me.kuku.utils.JacksonUtils
import me.kuku.utils.JobManager
import me.kuku.utils.OkHttpUtils
import org.slf4j.LoggerFactory
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import reactor.netty.ByteBufMono
import java.io.PrintWriter
import java.io.StringWriter
import javax.validation.ValidationException

@Component
class WebExceptionHandle(
    private val exceptionLogService: ExceptionLogService
): ErrorWebExceptionHandler {
    override fun handle(exchange: ServerWebExchange, ex: Throwable): Mono<Void> {
        JobManager.now {
            val request = exchange.request
            val method = request.method
            val path = request.path.value()
            val sw = StringWriter()
            val pw = PrintWriter(sw)
            ex.printStackTrace(pw)
            val exceptionStackTrace = sw.toString()
            val url = kotlin.runCatching {
                val jsonObject = OkHttpUtils.postJson("https://api.kukuqaq.com/tool/paste",
                    mapOf("poster" to "kuku", "syntax" to "java", "content" to exceptionStackTrace)
                )
                jsonObject.getJSONObject("data").getString("url")
            }.getOrDefault("Ubuntu paste url 生成失败")
            exceptionLogService.save(ExceptionLogEntity().also {
                it.method = method
                it.path = path
                it.stackTrace = exceptionStackTrace
                it.url = url
            })
        }
        val response = exchange.response
        val headers = response.headers
        response.statusCode = HttpStatus.INTERNAL_SERVER_ERROR
        headers.contentType = MediaType.APPLICATION_JSON
        return response.writeAndFlushWith(
            Mono.just(
                ByteBufMono.just(response.bufferFactory().wrap(
                    JSON.toJSONString(
                        Result.failure<Unit>(ex.message)).toByteArray()))))
    }
}

@Component
@Order(100)
class Filter: WebFilter {
    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        return chain.filter(exchange).onErrorResume(MissingRequestParameterException::class.java) {
            val response = exchange.response
            val headers = response.headers
            response.statusCode = HttpStatus.BAD_REQUEST
            headers.contentType = MediaType.APPLICATION_JSON
            response.writeAndFlushWith(
                Mono.just(
                    ByteBufMono.just(
                        response.bufferFactory().wrap(JSON.toJSONString(Result.failure<Unit>(400, it.message)).toByteArray())
                    )
                )
            )
        }.onErrorResume(ValidationException::class.java) {
            val message = it.message
            val response = exchange.response
            val headers = response.headers
            response.statusCode = HttpStatus.BAD_REQUEST
            headers.contentType = MediaType.APPLICATION_JSON
            response.writeAndFlushWith(
                Mono.just (
                    ByteBufMono.just(
                        response.bufferFactory().wrap(JacksonUtils.toJsonString(Result.failure<Unit>(400, message)).toByteArray())
                    )
                )
            )
        }
    }
}

@Component
@Order(200)
class CallLogging(
    private val callLoggingService: CallLoggingService
): WebFilter {

    private val log = LoggerFactory.getLogger(CallLogging::class.java)

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        return chain.filter(exchange).doFinally {
            // 404 Not Found: GET - /favicon.ico
            val request = exchange.request
            val response = exchange.response
            val statusCode = response.statusCode ?: return@doFinally
            log.info("${statusCode.value()} ${statusCode.name}: ${request.methodValue} - ${request.path}")
            val callLoggingEntity = CallLoggingEntity()
            callLoggingEntity.path = request.path.value()
            callLoggingEntity.httpMethod = request.method
            callLoggingEntity.statusCodeValue = statusCode.value()
            callLoggingEntity.statusCodeName = statusCode.name
            callLoggingEntity.userAgent = request.headers.getFirst("user-agent") ?: ""
            callLoggingService.save(callLoggingEntity)
        }
    }
}