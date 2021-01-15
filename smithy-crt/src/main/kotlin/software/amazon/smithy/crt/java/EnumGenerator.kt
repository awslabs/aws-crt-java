package software.amazon.smithy.crt.java

import com.squareup.javapoet.*
import software.amazon.smithy.build.PluginContext
import software.amazon.smithy.crt.java.Types.Utils.toClassName
import software.amazon.smithy.model.shapes.StringShape
import software.amazon.smithy.model.traits.EnumTrait
import software.amazon.smithy.utils.CaseUtils

class EnumGenerator(pluginContext: PluginContext, private val enumShape: StringShape)
    : ShapeGenerator(pluginContext, toClassName(enumShape)) {

    companion object {
        private const val UNKNOWN_VALUE = "UNKNOWN_TO_SDK_VERSION"
        private fun enumValuesFromTrait(enumTrait: EnumTrait): MutableMap<String, String?> {
            val values = mutableMapOf<String, String?>()
            enumTrait.values.stream().forEach { enumValue ->
                val name = toEnumName(enumValue.name.orElse(enumValue.value))
                val value = if (enumValue.value != "") enumValue.value else null
                values[name] = value
            }
            return values
        }

        private fun toEnumName(s: String) : String {
            var ident = s.replace(":*", "")
            ident = ident.replace(':', '_')
            return CaseUtils.toSnakeCase(ident).toUpperCase()
        }
    }

    private val enumTrait = enumShape.getTrait(EnumTrait::class.javaObjectType).get()
    private val enumName = toClassName(enumShape)
    private val javaEnum = Enum(className, enumValuesFromTrait(enumTrait))
        .addAnnotation(generatedAnnotation(EnumGenerator::class.javaObjectType))

    override fun generate(): TypeSpec.Builder {
        log.info("Generating enum ${enumShape.id} -> $enumName")
        return javaEnum.build().java()
    }
}