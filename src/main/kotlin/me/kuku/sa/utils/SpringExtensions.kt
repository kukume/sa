package me.kuku.sa.utils

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.alibaba.fastjson.TypeReference
import kotlinx.coroutines.reactive.awaitSingle
import me.kuku.utils.toJSONString
import org.springframework.cache.Cache
import org.springframework.http.MediaType
import org.springframework.http.codec.multipart.FilePart
import org.springframework.http.codec.multipart.FormFieldPart
import org.springframework.http.codec.multipart.Part
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.awaitFormData
import org.springframework.web.reactive.function.server.bodyToMono

suspend inline fun <reified T: Any> ServerRequest.awaitReceive(): T {
    return when (this.headers().contentType().get().subtype) {
        MediaType.APPLICATION_JSON.subtype -> {
            this.bodyToMono<T>().awaitSingle()
        }
        MediaType.APPLICATION_FORM_URLENCODED.subtype -> {
            val formData = this.awaitFormData()
            val jsonObject = JSONObject()
            formData.forEach { (k, _) ->
                jsonObject[k] = formData.getFirst(k)
            }
            jsonObject.toJavaObject(T::class.java)
        }
        else -> {
            val contentType = this.headers().contentType()
            throw ContentTypeNotConfiguredException(contentType.get().type)
        }
    }
}

inline fun <reified T: Any> ServerRequest.receive(): T {
    return when (this.headers().contentType().get().subtype) {
        MediaType.APPLICATION_JSON.subtype -> {
            this.bodyToMono<T>().block()!!
        }
        MediaType.APPLICATION_FORM_URLENCODED.subtype -> {
            val formData = this.formData().block()!!
            val jsonObject = JSONObject()
            formData.forEach { (k, _) ->
                jsonObject[k] = formData.getFirst(k)
            }
            jsonObject.toJavaObject(T::class.java)
        }
        else -> {
            val contentType = this.headers().contentType()
            throw ContentTypeNotConfiguredException(contentType.get().type)
        }
    }
}

inline fun <reified T: Any> MultiValueMap<String, String>.receive(): T {
    val map = this.toSingleValueMap()
    val clazz = T::class.java
    val jsonStr = JSON.toJSONString(map)
    return JSON.parseObject(jsonStr, clazz)
}

fun MultiValueMap<String, Part>.formPart(name: String): FormFieldPart? {
    return this.getFirst(name) as? FormFieldPart
}

fun MultiValueMap<String, Part>.filePart(name: String): FilePart {
    return this.getFirst(name) as FilePart
}

fun MultiValueMap<String, String>.getFirstOrFail(name: String): String {
    return this.getFirst(name) ?: throw MissingRequestParameterException(name)
}

inline fun <reified T: Any> MultiValueMap<String, String>.convert(): T {
    val map = this.toSingleValueMap()
    return JSON.parseObject(map.toJSONString(), object: TypeReference<T>() {})
}

fun ServerRequest.queryParamOrFail(name: String): String {
    return this.queryParam(name).orElseThrow {
        MissingRequestParameterException(name)
    }
}

inline fun <reified T: Any> ServerRequest.attr(name: String): T {
    return this.attribute(name).get() as T
}

inline fun <reified T: Any> Cache.getRf(name: String): T? {
    return this.get(name, T::class.java)
}

class ContentTypeNotConfiguredException(contentType: String): RuntimeException(contentType)

class BeforeException(message: String): RuntimeException(message)

class MissingRequestParameterException(parameterName: String): BadRequestException("Request parameter $parameterName is missing")

open class BadRequestException(override val message: String): RuntimeException(message)