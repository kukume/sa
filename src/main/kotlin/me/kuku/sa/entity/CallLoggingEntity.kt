package me.kuku.sa.entity

import com.querydsl.core.BooleanBuilder
import me.kuku.sa.utils.plus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.querydsl.QuerydslPredicateExecutor
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import javax.persistence.*

@Entity
@Table(name = "call_logging")
class CallLoggingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null
    var path: String = ""
    var statusCodeValue: Int = 0
    var statusCodeName: String = ""
    var httpMethod: HttpMethod? = null
    var userAgent: String = ""
}

interface CallLoggingRepository: JpaRepository<CallLoggingEntity, Int>, QuerydslPredicateExecutor<CallLoggingEntity> {
}

@Service
class CallLoggingService(
    private val callLoggingRepository: CallLoggingRepository
) {
    fun save(callLoggingEntity: CallLoggingEntity): CallLoggingEntity = callLoggingRepository.save(callLoggingEntity)

    fun findByAll(callLoggingEntity: CallLoggingEntity, pageable: Pageable): Page<CallLoggingEntity> {
        with(QCallLoggingEntity.callLoggingEntity) {
            val bb = BooleanBuilder()
            if (callLoggingEntity.path.isNotEmpty()) bb + path.like("%${callLoggingEntity.path}%")
            return callLoggingRepository.findAll(bb, pageable)
        }
    }
}