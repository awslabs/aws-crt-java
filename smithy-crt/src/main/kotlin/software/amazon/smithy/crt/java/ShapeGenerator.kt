package software.amazon.smithy.crt.java

import com.squareup.javapoet.*
import software.amazon.smithy.build.PluginContext
import software.amazon.smithy.crt.codegen.SourceGenerator
import software.amazon.smithy.crt.java.Types.Utils.toClassName
import software.amazon.smithy.crt.java.Types.Utils.toTypeName
import software.amazon.smithy.model.Model
import software.amazon.smithy.model.shapes.Shape
import software.amazon.smithy.model.shapes.ShapeId
import software.amazon.smithy.model.traits.DocumentationTrait
import software.amazon.smithy.utils.StringUtils
import java.nio.file.Path
import java.util.*
import java.util.function.Consumer
import java.util.logging.Logger
import java.util.stream.Collectors

// ShapeGenerators map from Smithy Shape -> Java source file
// Each shape generator creates 1 or more Java Elements internally, and
// turns them into Java code via JavaPoet
abstract class ShapeGenerator(protected val pluginContext: PluginContext, protected val className: ClassName) : SourceGenerator {

    protected val log: Logger = Logger.getLogger(ShapeGenerator::class.simpleName)
    protected val model: Model = pluginContext.model

    companion object {
        // Standard format/setup for emitting a source file to Java
        fun emit(className: ClassName, builder: TypeSpec.Builder) : JavaFile {
            return JavaFile.builder(className.packageName(), builder.build())
                .addFileComment(
                    """
                    Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
                    SPDX-License-Identifier: Apache-2.0.
                    """.trimIndent()
                )
                .indent("    ")
                .build()
        }
    }

    protected fun fieldsFromShape(shape: Shape) : Fields {
        return shape.members().stream().map { member ->
            val field = Field(member.memberName, toTypeName(toShape(member.target)))
            if (member.hasTrait(DocumentationTrait::class.javaObjectType)) {
                field.addDocumentation(member.getTrait(DocumentationTrait::class.javaObjectType).get().value)
            }
            return@map field
        }.collect(Collectors.toList())
    }

    fun toShape(id: ShapeId) : Shape {
        return model.getShape(id).get()
    }

    protected fun generatedAnnotation(generator: Class<*>): Annotation {
        return Annotation(Generated.CLASS_NAME)
            .java { builder ->
                builder.addMember("value", "${'$'}S", ClassName.bestGuess(generator.name))
            }
    }

    override fun accept(dir: Path, manifest: Consumer<Path>) {
        try {
            val javaSource = emit(className, generate())
            manifest.accept(javaSource.writeToPath(dir))
        } catch (ex : Throwable) {
            log.severe(ex.message)
        }
    }

    abstract fun generate() : TypeSpec.Builder
}