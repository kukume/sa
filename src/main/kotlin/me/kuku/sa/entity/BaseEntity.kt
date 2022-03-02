package me.kuku.sa.entity

import cn.dev33.satoken.stp.StpUtil
import me.kuku.sa.pojo.Status
import org.springframework.context.annotation.Configuration
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.domain.AuditorAware
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.*
import javax.persistence.Column
import javax.persistence.EntityListeners
import javax.persistence.Enumerated
import javax.persistence.MappedSuperclass

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
open class BaseEntity {
    @CreatedBy
    @Column(nullable = false, updatable = false)
    var createBy: String = ""
    @CreatedDate
    @Column(nullable = false, updatable = false)
    var createDate: LocalDateTime = LocalDateTime.now()
    @LastModifiedBy
    var lastModifiedBy: String = ""
    @LastModifiedDate
    var lastModifiedDate: LocalDateTime = LocalDateTime.now()
    @Enumerated
    var status: Status? = Status.ON
}


@Configuration
class BaseAuditor(
    private val userService: UserService
): AuditorAware<String> {

    override fun getCurrentAuditor(): Optional<String> {
        val loginId = StpUtil.getLoginId() ?: return Optional.of("游客")
        val id = loginId.toString().toInt()
        val userEntity = userService.findById(id) ?: return Optional.of("无此用户")
        return Optional.of(userEntity.username)
    }
}