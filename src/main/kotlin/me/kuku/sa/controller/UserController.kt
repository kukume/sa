@file:Suppress("DuplicatedCode", "NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package me.kuku.sa.controller

import cn.dev33.satoken.secure.SaSecureUtil
import cn.dev33.satoken.stp.StpUtil
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import kotlinx.coroutines.reactive.awaitFirst
import me.kuku.pojo.Result
import me.kuku.pojo.ResultStatus
import me.kuku.sa.entity.*
import me.kuku.sa.pojo.Page
import me.kuku.sa.pojo.Status
import me.kuku.utils.MyUtils
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.PageRequest
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.web.bind.annotation.*
import org.springframework.web.reactive.function.server.bodyToMono
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.coRouter

@RestController
@RequestMapping("/user")
class UserController(
    private val userService: UserService,
    private val roleService: RoleService
) {

    @PostMapping("login")
    fun login(@RequestBody params: UserEntity): Result<Any> {
        val userEntity = userService.findByUsername(params.username) ?: return Result.failure("用户名或密码错误")
        val enPass = SaSecureUtil.md5BySalt(params.password, userEntity.salt)
        return if (enPass != userEntity.password) Result.failure("用户名或密码错误")
        else {
            StpUtil.login(userEntity.id)
            Result.success("登录成功", StpUtil.getTokenInfo())
        }
    }

    @GetMapping("info")
    @Transactional
    fun info(): Result<Any> {
        val id = StpUtil.getLoginIdAsInt()
        val userEntity = userService.findById(id)
        return Result.success(userEntity)
    }

    @PostMapping("register")
    suspend fun register(@RequestBody params: UserEntity): Result<Any> {
        val query = userService.findByUsername(params.username)
        if (query != null) return Result.failure("该用户名已存在")
        val salt = MyUtils.randomStr(6)
        val enPass = SaSecureUtil.md5BySalt(params.username, salt)
        val userEntity = UserEntity()
        userEntity.username = params.username
        userEntity.password = enPass
        userEntity.salt = salt
        userService.save(userEntity)
        StpUtil.login(userEntity.id)
        return Result.success(StpUtil.getTokenInfo())
    }

    @GetMapping
    @Transactional
    fun list(page: Page):  Result<*>{
        return Result.success(userService.findAll(page.toPageRequest()))
    }

    @GetMapping("{id}")
    @Transactional
    fun queryDetail(@PathVariable id: Int): Result<*> {
        val userEntity = userService.findById(id) ?: return ResultStatus.DATA_NOT_EXISTS.toResult()
        return Result.success(userEntity)
    }

    @PostMapping
    @Transactional
    fun save(@RequestBody userSaveParams: UserSaveParams): Result<Void> {
        return kotlin.runCatching {
            val userEntity = if (userSaveParams.id == null) UserEntity()
            else userService.findById(userSaveParams.id!!) ?: return ResultStatus.DATA_NOT_EXISTS.toResult()
            userEntity.username = userSaveParams.username
            if ("******" != userSaveParams.password) {
                val salt = MyUtils.randomStr(6)
                val enPass = SaSecureUtil.md5BySalt(userSaveParams.password, salt)
                userEntity.password = enPass
                userEntity.salt = salt
            }
            userEntity.id = userSaveParams.id
            userSaveParams.role.also { userEntity.roles.clear() }.forEach {
                roleService.findById(it)?.let { role ->
                    userEntity.roles.add(role)
                }
            }
            userEntity.status = if (userSaveParams.status) Status.ON else Status.OFF
            userService.save(userEntity)
            Result.success()
        }.onFailure {
            Result.failure("保存失败，请检查参数", null)
        }.getOrThrow()
    }

    @DeleteMapping
    fun delete(@RequestBody list: List<Int>): Result<*> {
        userService.deleteAllById(list)
        return Result.success()
    }


    @GetMapping("logout")
    fun logout(): Result<*> {
        StpUtil.logout()
        return Result.success()
    }
}


@Configuration
class SystemController(
    private val roleService: RoleService,
    private val permissionService: PermissionService,
    private val menuService: MenuService,
    private val userService: UserService,
    private val transactionTemplate: TransactionTemplate
) {
    @Bean
    fun role() = coRouter {

        "role".nest {
            GET("") {
                val page = it.queryParam("page").orElse("1").toInt()
                val size = it.queryParam("size").orElse("20").toInt()
                val re = roleService.findAll(PageRequest.of(page - 1, size))
                ok().bodyValueAndAwait(Result.success(re))
            }
            GET("n/all") {
                val list = roleService.findAll()
                ok().bodyValueAndAwait(Result.success(list))
            }
            GET("{id}") {
                val id = it.pathVariable("id").toInt()
                val roleEntity = roleService.findById(id)
                    ?: return@GET ok().bodyValueAndAwait(ResultStatus.DATA_NOT_EXISTS.toResult())
                ok().bodyValueAndAwait(Result.success(roleEntity))
            }
            POST("") {
                val roleSaveParams = it.bodyToMono<RoleSaveParams>().awaitFirst()
                val result = transactionTemplate.execute {
                    val roleEntity = if (roleSaveParams.id != null)
                        roleService.findById(roleSaveParams.id!!) ?: return@execute ResultStatus.DATA_NOT_EXISTS.toResult()
                    else RoleEntity()
                    roleEntity.name = roleSaveParams.name
                    roleEntity.description = roleSaveParams.description
                    roleEntity.permissions.clear()
                    roleSaveParams.permission.forEach { id ->
                        val permissionEntity = permissionService.findById(id) ?: return@forEach
                        roleEntity.permissions.add(permissionEntity)
                    }
                    Result.success(roleService.save(roleEntity))
                }
                ok().bodyValueAndAwait(result)

            }
            DELETE("") {
                val ids = it.bodyToMono<List<Int>>().awaitFirst()
                roleService.deleteAllById(ids)
                ok().bodyValueAndAwait(Result.success())
            }
        }

        "permission".nest {
            GET("") {
                val page = it.queryParam("page").orElse("1").toInt()
                val size = it.queryParam("size").orElse("20").toInt()
                val re = permissionService.findAll(PageRequest.of(page - 1, size))
                ok().bodyValueAndAwait(Result.success(re))
            }
            GET("{id}") {
                val id = it.pathVariable("id").toInt()
                val permissionEntity = permissionService.findById(id)
                ok().bodyValueAndAwait(Result.success(permissionEntity))
            }
            GET("n/all") {
                val list = permissionService.findAll()
                ok().bodyValueAndAwait(Result.success(list))
            }
            POST("") {
                val permissionEntity = it.bodyToMono<PermissionEntity>().awaitFirst()
                ok().bodyValueAndAwait(Result.success(permissionService.save(permissionEntity)))
            }
            DELETE("") {
                val ids = it.bodyToMono<List<Int>>().awaitFirst()
                permissionService.deleteAllById(ids)
                ok().bodyValueAndAwait(Result.success())
            }
        }

        "menu".nest {
            GET("") {
                val re = menuService.findAll()
                ok().bodyValueAndAwait(Result.success(re))
            }
            GET("tree") {
                val list = menuService.findAll()
                val jsonArray = JSON.parseArray(JSON.toJSONString(list))
                jsonArray.map { it as JSONObject }.forEach {
                    updateKey(it)
                }
                ok().bodyValueAndAwait(Result.success(jsonArray))
            }
            GET("id/{id}") {
                val id = it.pathVariable("id").toInt()
                val menuEntity = menuService.findById(id)
                    ?: return@GET ok().bodyValueAndAwait(ResultStatus.DATA_NOT_EXISTS.toResult())
                val jsonObject = JSON.parseObject(JSON.toJSONString(menuEntity))
                jsonObject["parentId"] = menuEntity.parent?.id
                jsonObject["permissionName"] = menuEntity.permission?.name
                ok().bodyValueAndAwait(Result.success(jsonObject))
            }
            GET("my") {
                val id = StpUtil.getLoginIdAsInt()
                val map = mutableMapOf<Int, MenuEntity>()
                val menus = mutableListOf<MenuEntity>()
                transactionTemplate.execute {
                    val userEntity = userService.findById(id)!!
                    userEntity.roles.forEach {
                        it.permissions.forEach { p ->
                            p.menuEntity?.let{ menuEntity ->
                                    menuEntity.menus = mutableSetOf()
                                    map[menuEntity.id!!] = menuEntity
                                }
                        }
                    }
                    menuService.findByPermissionIsNull().forEach {
                        map[it.id!!] = it
                    }
                }
                map.forEach { (_, v) ->
                    val parentEntity = v.parent
                    if (map.containsKey(parentEntity?.id)) parentEntity?.menus?.add(v)
                }
                map.forEach { (_, v) ->
                    val parentEntity = v.parent
                    if (!map.containsKey(parentEntity?.id)) menus.add(v)
                }
                ok().bodyValueAndAwait(Result.success(menus))
            }
            POST("") {
                val menuSaveParams = it.bodyToMono<MenuSaveParams>().awaitFirst()
                if (menuSaveParams.path.contains('/')) return@POST ok().bodyValueAndAwait(Result.failure<Unit>("路径中不允许含有/"))
                val result = transactionTemplate.execute {
                    val menuEntity = if (menuSaveParams.id != null)
                        menuService.findById(menuSaveParams.id!!) ?: return@execute ResultStatus.DATA_NOT_EXISTS.toResult()
                    else MenuEntity()
                    menuEntity.name = menuSaveParams.name
                    menuEntity.enName = menuSaveParams.enName
                    menuEntity.path = menuSaveParams.path
                    menuEntity.icon = menuSaveParams.icon
                    menuEntity.order = menuSaveParams.order
                    if (menuSaveParams.parentId != null) {
                        val parentEntity = menuService.findById(menuSaveParams.parentId!!)
                            ?: return@execute ResultStatus.DATA_NOT_EXISTS.toResult()
                        menuEntity.parent = parentEntity
                    }
                    var permissionParams = menuSaveParams.permission
                    if (permissionParams.name.isNotEmpty()) {
                        val alreadyPermission = permissionService.findByName(permissionParams.name)
                        if (menuSaveParams.id == null && alreadyPermission != null) return@execute Result.failure<Unit>(
                            "该权限名称已存在"
                        )
                        if (alreadyPermission == null) {
                            permissionParams.menuEntity = menuEntity
                            permissionParams.description = "菜单：${menuEntity.name}"
                        } else permissionParams = alreadyPermission
                        menuEntity.permission = permissionParams
                    }
                    Result.success(menuService.save(menuEntity))
                }
                ok().bodyValueAndAwait(result)
            }
            DELETE("") {
                val ids = it.bodyToMono<List<Int>>().awaitFirst()
                menuService.deleteAllById(ids)
                ok().bodyValueAndAwait(Result.success())
            }
        }
    }


    private fun updateKey(jsonObject: JSONObject) {
        val name = jsonObject.getString("name")
        jsonObject.remove("name")
        jsonObject["label"] = name
        val jsonArray = jsonObject.getJSONArray("menus")
        jsonObject.remove("menus")
        jsonObject["children"] = jsonArray
        jsonArray.map { it as JSONObject }.forEach {
            updateKey(it)
        }
    }
}

class UserSaveParams {
    var id: Int? = null
    var username: String = ""
    var password: String = "******"
    var role: MutableList<Int> = mutableListOf()
    var status: Boolean = true
}

class RoleSaveParams {
    var id: Int? = null
    var name: String = ""
    var description: String = ""
    var permission: MutableList<Int> = mutableListOf()
}

class MenuSaveParams {
    var id: Int? = null
    var name: String = ""
    var enName: String = ""
    var path: String = ""
    var icon: String = ""
    var parentId: Int? = null
    var order: Int = 0
    var permission: PermissionEntity = PermissionEntity()
}