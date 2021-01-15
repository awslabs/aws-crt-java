package software.amazon.smithy.crt.java

import com.squareup.javapoet.*
import software.amazon.smithy.build.PluginContext
import software.amazon.smithy.crt.codegen.SourceGenerator
import java.util.function.Consumer

import java.lang.annotation.ElementType
import java.lang.annotation.RetentionPolicy

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.nio.file.Path

// Annotation for marking classes as generated
class Generated : Structure(CLASS_NAME) {
    companion object {
        val CLASS_NAME : ClassName = ClassName.get("software.amazon.aws.sdk.crt.annotations", "Generated")
    }

    override val javaBuilder : TypeSpec.Builder = TypeSpec.annotationBuilder(className)

    init {
        modifiers.add(Modifier.PUBLIC)
        documentation = """
                Marker interface for generated source code. Generated source code should not be edited directly.
                """.trimIndent()
        annotations.add(Annotation(ClassName.get(Documented::class.javaObjectType)))
        annotations.add(Annotation(ClassName.get(Retention::class.javaObjectType)).java { builder ->
            builder.addMember("value", "${'$'}T.${'$'}L", RetentionPolicy::class.javaObjectType, "SOURCE")
        })
        annotations.add(Annotation(ClassName.get(Target::class.javaObjectType)).java { builder ->
            builder.addMember("value",
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
        })
        addMethod(Method("value", ArrayTypeName.get(String::class.javaObjectType))
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addDocumentation("""
                The value element MUST have the name of the code generator.
                The recommended convention is to use the fully qualified name of the
                code generator. For example: com.acme.generator.CodeGen.
                """.trimIndent())
        )
        addMethod(Method("date", TypeName.get(String::class.javaObjectType))
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addDocumentation("""
                Date when the source was generated.
                """.trimIndent())
            .java { builder ->
                builder.defaultValue("${'$'}S", "")
            })
        addMethod(Method("comments", TypeName.get(String::class.javaObjectType))
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addDocumentation("""
                 A place holder for any comments that the code generator may want to
                 include in the generated code.
                 """.trimIndent())
            .java { builder ->
                builder.defaultValue("${'$'}S", "")
            }
        )
    }

    override fun build() : Generated {
        Interface.build(javaBuilder, this)
        return this
    }
}

class AnnotationsGenerator(@Suppress("UNUSED_PARAMETER") pluginContext: PluginContext) : SourceGenerator {
    override fun accept(dir: Path, manifest: Consumer<Path>) {
        manifest.accept(generated().writeToPath(dir))
    }

    // Generated annotation
    private fun generated() : JavaFile {
        val gen = Generated()
        return ShapeGenerator.emit(gen.className, gen.build().java())
    }
}