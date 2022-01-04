package me.kuku.sa.entity

import com.alibaba.fastjson.annotation.JSONField
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Service
import javax.persistence.*

@Entity
@Table(name = "menu")
@JsonIgnoreProperties("parent")
class MenuEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null
    var name: String = ""
    @Column(unique = true)
    var enName: String = ""
    var path: String = ""
    var icon: String = ""

    @Column(name = "order_")
    var order: Int = 0

    @ManyToOne(cascade = [CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST])
    @JoinColumn(name = "parent_id")
    @JSONField(serialize = false)
    var parent: MenuEntity? = null

    @OneToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER, mappedBy = "parent")
    var menus: MutableSet<MenuEntity> = mutableSetOf()

    @OneToOne(mappedBy = "menuEntity", cascade = [CascadeType.DETACH])
    @JSONField(serialize = false)
    var permission: PermissionEntity? = null

}

interface MenuRepository: JpaRepository<MenuEntity, Int> {
    fun findByParentIsNull(): List<MenuEntity>
    fun findByPermissionIsNull(): List<MenuEntity>
}

@Service
class MenuService(
    private val menuRepository: MenuRepository
) {
    fun save(menuEntity: MenuEntity): MenuEntity = menuRepository.save(menuEntity)

    fun deleteAllById(id: List<Int>) = menuRepository.deleteAllById(id)

    fun findAll(): List<MenuEntity> {
        return menuRepository.findByParentIsNull()
    }

    fun findById(id: Int): MenuEntity? = menuRepository.findById(id).orElse(null)

    fun findByPermissionIsNull(): List<MenuEntity> = menuRepository.findByPermissionIsNull()
}