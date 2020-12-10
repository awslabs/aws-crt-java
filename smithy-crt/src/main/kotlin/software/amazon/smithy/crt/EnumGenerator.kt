package software.amazon.smithy.crt

import com.squareup.javapoet.*
import software.amazon.smithy.build.PluginContext
import software.amazon.smithy.crt.TypeMap.Utils.toClassName
import software.amazon.smithy.model.shapes.StringShape
import software.amazon.smithy.model.traits.EnumTrait
import software.amazon.smithy.utils.CaseUtils
import java.util.stream.Collectors
import java.util.stream.Stream
import javax.lang.model.element.Modifier

class EnumGenerator(pluginContext: PluginContext, private val enumShape: StringShape)
    : ShapeGenerator(pluginContext, toClassName(enumShape)) {

    companion object {
        private const val UNKNOWN_VALUE = "UNKNOWN_TO_SDK_VERSION"
    }

    private val enumTrait = enumShape.getTrait(EnumTrait::class.javaObjectType).get()
    private val enumName = ClassName.get(enumShape.id.namespace, enumShape.id.name)

    private fun toEnumName(s: String) : String {
        var ident = s.replace(":*", "")
        ident = ident.replace(':', '_')
        return CaseUtils.toSnakeCase(ident).toUpperCase()
    }

    init {
        log.info("Generating enum {} -> {}".format(enumShape.id, enumName))

        val enumBuilder = TypeSpec.enumBuilder(enumName)
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(AnnotationSpec.builder(ClassName.get("software.amazon.awssdk.crt.annotations", "Generated"))
                .addMember("value", "${'$'}S", EnumGenerator::class.qualifiedName)
                .build())

        // private T(String value)
        val ctorBuilder = MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PRIVATE)
            .addParameter(ParameterSpec.builder(ClassName.get(String::class.javaObjectType), "value").build())
            .addStatement("this.${'$'}L = ${'$'}L", "value", "value")
        enumBuilder.addMethod(ctorBuilder.build())

        // private String value
        enumBuilder.addField(
            FieldSpec.builder(ClassName.get(String::class.javaObjectType), "value", Modifier.PRIVATE, Modifier.FINAL).build())

        // public static T fromValue(String s)
        val fromValue = MethodSpec.methodBuilder("fromValue")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addParameter(ClassName.get(String::class.javaObjectType), "value")
            .beginControlFlow("if (${'$'}L == null)", "value")
            .addStatement("return null")
            .endControlFlow()
            .addStatement("return ${'$'}T.of(${'$'}L.values()).filter(e -> e.toString().equals(${'$'}L)).findFirst().orElse(${'$'}L)",
                Stream::class.javaObjectType, enumName, "value", UNKNOWN_VALUE)
            .returns(enumName)
        enumBuilder.addMethod(fromValue.build())

        // public static Set<T> knownValues()
        val knownValues = MethodSpec.methodBuilder("knownValues")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addStatement("return ${'$'}T.of(values()).filter(v -> v != ${'$'}L).collect(toSet())",
                Stream::class.javaObjectType, UNKNOWN_VALUE)
            .returns(ParameterizedTypeName.get(ClassName.get(Set::class.javaObjectType), enumName))
        enumBuilder.addMethod(knownValues.build())

        // enum values
        enumTrait.values.stream().forEach { enumValue ->
            enumBuilder.addEnumConstant(
                toEnumName(enumValue.name.orElse(enumValue.value)),
                TypeSpec.anonymousClassBuilder("${'$'}S", enumValue.value).build()
            )
        }
        enumBuilder.addEnumConstant(
            UNKNOWN_VALUE,
            TypeSpec.anonymousClassBuilder("${'$'}S", UNKNOWN_VALUE).build()
        )

        finish(
            JavaFile.builder(enumName.packageName(), enumBuilder.build())
            .addStaticImport(ClassName.get(Collectors::class.javaObjectType), "toSet")
            .build()
        )
    }
}