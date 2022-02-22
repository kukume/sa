package me.kuku.sa.entity

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.vladmihalcea.hibernate.type.json.JsonType
import me.kuku.sa.pojo.Status
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "config")
@TypeDef(name = "json", typeClass = JsonType::class)
class ConfigEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null
    @Column(unique = true)
    var configType: ConfigType? = null
    @Type(type = "json")
    @Column(columnDefinition = "json")
    var content: ConfigContent = ConfigContent()
    var localDateTime: LocalDateTime = LocalDateTime.now()
}

@JsonFormat(shape = JsonFormat.Shape.NUMBER)
enum class ConfigType(val value: String) {
    REGISTER("是否开启注册"),
    DEFAULT_ROLE("注册用户默认分配的角色"),
    H_CAPTCHA("hCaptcha验证码")
    ;

    companion object {
        fun byOrdinal(num: Int): ConfigType? {
            val array = ConfigType.values()
            for (configType in array) {
                if (num == configType.ordinal) return configType
            }
            return null
        }
    }
}

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ConfigContent(
    var register: Boolean? = null,
    var defaultRole: String? = null,
    var hCaptcha: HCaptcha? = null
)

interface ConfigRepository: JpaRepository<ConfigEntity, Int> {
    fun findByConfigType(configType: ConfigType): ConfigEntity?
}

@Service
class ConfigService(
    private val configRepository: ConfigRepository
) {
    fun findByConfigType(configType: ConfigType) = configRepository.findByConfigType(configType)

    fun save(configEntity: ConfigEntity): ConfigEntity = configRepository.save(configEntity)

    fun findAll(): List<ConfigEntity> = configRepository.findAll()
}

data class HCaptcha(
    var status: Status = Status.OFF,
    var siteKey: String = "",
    @JsonIgnore
    var secret: String = ""
)