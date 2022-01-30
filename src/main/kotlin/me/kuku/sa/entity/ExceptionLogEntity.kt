package me.kuku.sa.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.querydsl.core.BooleanBuilder
import me.kuku.sa.utils.plus
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.domain.Example
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.querydsl.QuerydslPredicateExecutor
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "exception_log")
//@JsonIgnoreProperties("stackTrace")
class ExceptionLogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null
    var path: String = ""
    var method: HttpMethod? = null
    @Column(columnDefinition = "TEXT")
    var stackTrace: String = ""
    var url: String = ""
    @CreatedDate
    var localDateTime: LocalDateTime = LocalDateTime.now()
}

interface ExceptionLogRepository: JpaRepository<ExceptionLogEntity, Int>, QuerydslPredicateExecutor<ExceptionLogEntity> {
    fun findByPath(path: String): List<ExceptionLogEntity>
}


@Service
class ExceptionLogService(
    private val exceptionLogRepository: ExceptionLogRepository
) {
    fun save(exceptionLogEntity: ExceptionLogEntity): ExceptionLogEntity = exceptionLogRepository.save(exceptionLogEntity)

    fun findByAll(exceptionLogEntity: ExceptionLogEntity, pageable: Pageable): Page<ExceptionLogEntity> {
        with(QExceptionLogEntity.exceptionLogEntity) {
            val bb = BooleanBuilder()
            if (exceptionLogEntity.path.isNotEmpty()) bb + path.eq(exceptionLogEntity.path)
            return exceptionLogRepository.findAll(bb, pageable)
        }
    }

    fun findByPath(path: String) =  exceptionLogRepository.findByPath(path)

    fun findAll(pageable: Pageable): Page<ExceptionLogEntity> {
        return exceptionLogRepository.findAll(pageable)
    }

}