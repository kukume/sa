@file:Suppress("DuplicatedCode")

package me.kuku.sa.entity

import com.alibaba.fastjson.annotation.JSONField
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.querydsl.core.BooleanBuilder
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.querydsl.QuerydslPredicateExecutor
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.persistence.*

@Entity
@Table(name = "permission")
@JsonIgnoreProperties("roles", "menuEntity")
class PermissionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null
    @Column(unique = true)
    var name: String = ""
    var description: String = ""
    @OneToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "menu")
    var menuEntity: MenuEntity? = null

    @ManyToMany(mappedBy = "permissions")
    @JSONField(serialize = false)
    var roles: MutableSet<RoleEntity> = mutableSetOf()
}


interface PermissionRepository: JpaRepository<PermissionEntity, Int>, QuerydslPredicateExecutor<PermissionEntity> {
    fun findByName(name: String): PermissionEntity?
}

@Service
class PermissionService(
    private val permissionRepository: PermissionRepository,
    private val roleRepository: RoleRepository
) {
    fun save(permissionEntity: PermissionEntity): PermissionEntity = permissionRepository.save(permissionEntity)

    @Transactional
    fun deleteAllById(id: List<Int>) {
        for (i in id) {
            deleteById(i)
        }
    }

    @Transactional
    fun deleteById(id: Int) {
        val permissionEntity = permissionRepository.findByIdOrNull(id)
        permissionEntity?.roles?.forEach {
            it.permissions.remove(permissionEntity)
            roleRepository.save(it)
        }
        permissionEntity?.let { permissionRepository.delete(it) }
    }

    fun findAll(pageable: Pageable): Page<PermissionEntity> = permissionRepository.findAll(pageable)

    fun findAll(permissionEntity: PermissionEntity, pageable: Pageable): Page<PermissionEntity> {
        val q = QPermissionEntity.permissionEntity
        val bb = BooleanBuilder()
        if (permissionEntity.name.isNotEmpty()) bb.and(q.name.like("%${permissionEntity.name}%"))
        if (permissionEntity.description.isNotEmpty()) bb.and(q.description.like("%${permissionEntity.description}%"))
        return permissionRepository.findAll(bb, pageable)
    }

    fun findAll(): List<PermissionEntity> = permissionRepository.findAll()

    fun findById(id: Int): PermissionEntity? = permissionRepository.findById(id).orElse(null)

    fun findByName(name: String) = permissionRepository.findByName(name)

}