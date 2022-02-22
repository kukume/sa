package me.kuku.sa.config

import cn.dev33.satoken.reactor.filter.SaReactorFilter
import cn.dev33.satoken.stp.StpInterface
import cn.dev33.satoken.stp.StpUtil
import me.kuku.sa.entity.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Configuration
class SaTokenConfigure {

    @Bean
    fun saReactorFilter(): SaReactorFilter {
        return SaReactorFilter()
            .addInclude("/**")
            .addExclude("/user/login", "/user/register", "/config/configType/2")
            .setAuth {
                StpUtil.checkLogin()
            }
            .setError {
                return@setError """
                    {"code": "50008", "message": "${it.message}"}
                """.trimIndent()
            }
    }
}

@Component
class StpInterfaceImpl: StpInterface {

    @Autowired
    private lateinit var userService: UserService

    @Transactional
    override fun getPermissionList(loginId: Any, loginType: String): MutableList<String> {
        val userEntity = userService.findById(loginId.toString().toInt()) ?: return mutableListOf()
        val list = mutableListOf<String>()
        userEntity.roles.forEach { role ->
            role.permissions.forEach { permission ->
                list.add(permission.name)
            }
        }
        return list
    }

    @Transactional
    override fun getRoleList(loginId: Any, loginType: String): MutableList<String> {
        val userEntity = userService.findById(loginId.toString().toInt()) ?: return mutableListOf()
        val list = mutableListOf<String>()
        userEntity.roles.forEach { role ->
            list.add(role.name)
        }
        return list
    }
}