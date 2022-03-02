package me.kuku.sa.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.querydsl.core.BooleanBuilder
import me.kuku.sa.pojo.Status
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.querydsl.QuerydslPredicateExecutor
import org.springframework.stereotype.Service
import javax.persistence.*

@Entity
@Table(name = "user_")
@JsonIgnoreProperties("password", "salt")
class UserEntity: BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null
    @Column(unique = true)
    var username: String = ""
    var password: String = ""
    var salt: String = ""
    @ManyToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    @JoinTable(name = "user_role",
        joinColumns = [JoinColumn(name = "user_id")], inverseJoinColumns = [JoinColumn(name = "role_id")])
    var roles: MutableSet<RoleEntity> = mutableSetOf()
}

interface UserRepository: JpaRepository<UserEntity, Int>, QuerydslPredicateExecutor<UserEntity> {
    fun findByUsername(username: String): UserEntity?
    fun findByUsernameAndStatus(username: String, status: Status): UserEntity?
}


@Service
class UserService {

    @Autowired
    private lateinit var userRepository: UserRepository

    fun findById(id: Int): UserEntity? = userRepository.findById(id).orElse(null)

    fun findByUsername(username: String): UserEntity? = userRepository.findByUsername(username)

    fun findByUsernameAndStatus(username: String, status: Status) = userRepository.findByUsernameAndStatus(username, status)

    fun save(userEntity: UserEntity): UserEntity = userRepository.save(userEntity)

    fun findAll(pageable: Pageable): Page<UserEntity> = userRepository.findAll(pageable)

    fun findAll(userEntity: UserEntity, pageable: Pageable): Page<UserEntity> {
        val q = QUserEntity.userEntity
        val bl = BooleanBuilder()
        if (userEntity.username.isNotEmpty()) bl.and(q.username.like("%${userEntity.username}%"))
        if (userEntity.status != null) bl.and(q.status.eq(userEntity.status))
        return userRepository.findAll(bl, pageable)
    }

    fun deleteAllById(id: List<Int>) = userRepository.deleteAllById(id)
}