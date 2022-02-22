package me.kuku.sa.logic

import me.kuku.sa.entity.ConfigService
import me.kuku.sa.entity.ConfigType
import me.kuku.sa.pojo.Status
import me.kuku.utils.OkHttpUtils
import org.springframework.stereotype.Service

@Service
class HCaptchaLogic(
    private val configService: ConfigService
) {

    fun verify(token: String): Boolean {
        val configEntity = configService.findByConfigType(ConfigType.H_CAPTCHA) ?: return true
        val hCaptcha = configEntity.content.hCaptcha ?: return true
        if (hCaptcha.status == Status.OFF) return true
        val secret = hCaptcha.secret
        val jsonObject = OkHttpUtils.postJson("https://hcaptcha.com/siteverify", mapOf("response" to token, "secret" to secret))
        val success = jsonObject.getBoolean("success")
        if (success) return true
        else throw VerificationFailedException(jsonObject.getJSONArray("error-codes").getString(0))
    }

}

class VerificationFailedException(message: String): RuntimeException(message)