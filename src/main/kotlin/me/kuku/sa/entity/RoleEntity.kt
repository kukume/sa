@file:Suppress("DuplicatedCode")

package me.kuku.sa.entity

import com.alibaba.fastjson.annotation.JSONField
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import javax.persistence.*

@Entity
@Table(name = "role")
@JsonIgnoreProperties("users")
class RoleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null
    @Column(unique = true)
    var name: String = ""
    var description: String = ""
    @ManyToMany(mappedBy = "roles")
    @JSONField(serialize = false)
    var users: MutableSet<UserEntity> = mutableSetOf()
    @ManyToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    @JoinTable(name = "role_permission",
        joinColumns = [JoinColumn(name = "role_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "permission_id", referencedColumnName = "id")])
    var permissions: MutableSet<PermissionEntity> = mutableSetOf()
}

interface RoleRepository: JpaRepository<RoleEntity, Int>

@Service
class RoleService(
    private val roleRepository: RoleRepository,
    private val userRepository: UserRepository
) {
    fun findAll(pageable: Pageable): Page<RoleEntity> = roleRepository.findAll(pageable)

    fun findById(id: Int): RoleEntity? = roleRepository.findById(id).orElse(null)

    fun findAll(): MutableList<RoleEntity> = roleRepository.findAll()

    fun save(roleEntity: RoleEntity): RoleEntity = roleRepository.save(roleEntity)

    fun deleteAllById(id: List<Int>) {
        for (i in id) {
            val roleEntity = roleRepository.findByIdOrNull(i)
            roleEntity?.users?.forEach {
                it.roles.remove(roleEntity)
                userRepository.save(it)
            }
            roleEntity?.let { roleRepository.delete(it) }
        }
    }
}