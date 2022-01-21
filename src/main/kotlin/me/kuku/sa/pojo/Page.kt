package me.kuku.sa.pojo

import org.springframework.data.domain.PageRequest

data class Page(var page: Int = 1, var size: Int = 20) {
    fun toPageRequest(): PageRequest = PageRequest.of(page - 1, size)
}

data class PageData<T>(var page: Page = Page(), var data: T)