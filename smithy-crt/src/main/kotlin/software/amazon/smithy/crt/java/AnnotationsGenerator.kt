package software.amazon.smithy.crt.java

import com.squareup.javapoet.*
import software.amazon.smithy.build.PluginContext
import java.util.function.Consumer
import javax.lang.model.element.Modifier

import java.lang.annotation.ElementType
import java.lang.annotation.RetentionPolicy

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

class AnnotationsGenerator(pluginContext: PluginContext) : JavaFileGenerator {
    override fun accept(t: Consumer<JavaFile>) {
        t.accept(generated())
    }

    // Generated annotation
    fun generated() : JavaFile {
        val className = ClassName.get("software.amazon.awssdk.crt.annotations", "Generated")
        val classBuilder = TypeSpec.annotationBuilder(className)
            .addModifiers(Modifier.PUBLIC)
            .addJavadoc("""
                Marker interface for generated source code. Generated source code should not be edited directly.
                """.trimIndent())
            .addAnnotation(Documented::class.javaObjectType)
            .addAnnotation(
                AnnotationSpec.builder(Retention::class.javaObjectType)
                    .addMember("value", "${'$'}T.${'$'}L", RetentionPolicy::class.javaObjectType,"SOURCE")
                    .build()
            )
            .addAnnotation(
                AnnotationSpec.builder(Target::class.javaObjectType)
                    .addMember("value",
                        "{${'$'}T.${'$'}L, ${'$'}T.${'$'}L, ${'$'}T.${'$'}L, ${'$'}T.${'$'}L, ${'$'}T.${'$'}L, ${'$'}T.${'$'}L, ${'$'}T.${'$'}L, ${'$'}T.${'$'}L,}",
                        ElementType::class.javaObjectType, "PACKAGE",
                        ElementType::class.javaObjectType, "TYPE",
                        ElementType::class.javaObjectType, "ANNOTATION_TYPE",
                        ElementType::class.javaObjectType, "METHOD",
                        ElementType::class.javaObjectType, "CONSTRUCTOR",
                        ElementType::class.javaObjectType, "FIELD",
                        ElementType::class.javaObjectType, "LOCAL_VARIABLE",
                        ElementType::class.javaObjectType, "PARAMETER",
                        )
                    .build()
            )
            .addMethod(MethodSpec.methodBuilder("value")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addJavadoc("""
                    The value element MUST have the name of the code generator.
                    The recommended convention is to use the fully qualified name of the
                    code generator. For example: com.acme.generator.CodeGen.
                    """.trimIndent())
                .returns(ArrayTypeName.get(String::class.javaObjectType))
                .build()
            )
            .addMethod(MethodSpec.methodBuilder("date")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addJavadoc("""
                    Date when the source was generated.
                    """.trimIndent())
                .defaultValue("${'$'}S", "")
                .returns(TypeName.get(String::class.javaObjectType))
                .build()
            )
            .addMethod(MethodSpec.methodBuilder("comments")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addJavadoc("""
                     A place holder for any comments that the code generator may want to
                     include in the generated code.
                     """.trimIndent())
                .defaultValue("${'$'}S", "")
                .returns(TypeName.get(String::class.javaObjectType))
                .build()
            )
        return ShapeGenerator.emit(className, classBuilder)
    }
}