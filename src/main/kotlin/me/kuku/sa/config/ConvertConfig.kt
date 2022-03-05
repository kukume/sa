@file:Suppress("UNCHECKED_CAST")

package me.kuku.sa.config

import org.springframework.core.convert.converter.Converter
import org.springframework.core.convert.converter.ConverterFactory
import org.springframework.format.FormatterRegistry
import org.springframework.stereotype.Component
import org.springframework.util.Assert
import org.springframework.web.reactive.config.WebFluxConfigurer

class StringToEnumConverterFactory: ConverterFactory<String, Enum<*>> {
    override fun <T : Enum<*>?> getConverter(targetType: Class<T>): Converter<String, T> {
        val ss = StringToEnum()
        ss.enumType = getEnumType(targetType) as? Class<Enum<*>>
        return ss as Converter<String, T>
    }

    private fun getEnumType(targetType: Class<*>?): Class<*> {
        var enumType = targetType
        while (enumType != null && !enumType.isEnum) {
            enumType = enumType.superclass
        }
        Assert.notNull(enumType, "The target type ${targetType?.name} does not refer to an enum")
        return enumType!!
    }

    private class StringToEnum<T: Enum<*>>: Converter<String, T> {

        var enumType: Class<T>? = null

        override fun convert(source: String): T? {
            val ordinal = source.toIntOrNull()
            return if (ordinal == null) {
                if (source.isEmpty()) null
                else {
                    enumType?.enumConstants?.find { it.name == source } as T
                }
            } else
                enumType?.enumConstants?.get(ordinal)
        }
    }
}

// 如果为SpringMvc，应该实现WebMvcConfigurer接口
@Component
class MyConfig: WebFluxConfigurer {
    override fun addFormatters(registry: FormatterRegistry) {
        super.addFormatters(registry)
        registry.addConverterFactory(StringToEnumConverterFactory())
    }
}