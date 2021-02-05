
package software.amazon.smithy.crt.java;

import com.squareup.javapoet.*
import software.amazon.smithy.model.Model;

import software.amazon.smithy.build.PluginContext
import software.amazon.smithy.crt.*
import software.amazon.smithy.crt.codegen.SourceGenerator
import software.amazon.smithy.model.shapes.*
import java.nio.file.Path

import java.util.function.Consumer;
import java.util.logging.Logger
import java.util.stream.Collectors

class ServiceGenerator(private val pluginContext: PluginContext) : SourceGenerator {
    private val log = Logger.getLogger(ServiceGenerator::class.simpleName)

    private val model: Model = pluginContext.model
    private val typeMap = Types(pluginContext)

    private fun toShape(id: ShapeId): Shape {
        return model.getShape(id).get();
    }

    private fun emptyStructure(className: ClassName) : SourceGenerator {
        return EmptyStructureGenerator(pluginContext, className)
    }

    private fun emptyRequestStructure(operation: OperationShape) : SourceGenerator {
        return emptyStructure(ClassName.get(operation.id.namespace + ".model", operation.id.name + "Request"))
    }

    private fun emptyResponseStructure(operation: OperationShape) : SourceGenerator {
        return emptyStructure(ClassName.get(operation.id.namespace + ".model", operation.id.name + "Response"))
    }
    override fun accept(dir: Path, manifest: Consumer<Path>) {
        val foundShapes = mutableSetOf<Shape>()
        val generators = mutableListOf<SourceGenerator>(
            ServiceExceptionGenerator(pluginContext)
        )
        val operations = model.shapes(OperationShape::class.javaObjectType)
        operations.forEach { operation ->
            if (operation.input.isPresent) {
                foundShapes.add(toShape(operation.input.get()))
            } else {
                generators.add(emptyRequestStructure(operation))
            }

            if (operation.output.isPresent) {
                foundShapes.add(toShape(operation.output.get()))
            } else {
                generators.add(emptyResponseStructure(operation))
            }
                            
            foundShapes.addAll(
                operation.errors.stream()
                    .map { it -> toShape(it) }
                    .collect(Collectors.toList())
            )
        }

        // Recursively search through all found shapes until no new shapes are found
        var shapesToEvaluate = foundShapes.toList()
        do {
            val lastCount = foundShapes.count()
            val shapesFoundThisPass = mutableSetOf<Shape>()
            shapesToEvaluate.forEach { shape ->
                if (shape is StructureShape || shape is UnionShape) {
                    shapesFoundThisPass.addAll(
                        shape.members().stream()
                            .map { model.getShape(it.target).get() }
                            .collect(Collectors.toSet())
                    )
                } else if (shape is ListShape) {
                    shapesFoundThisPass.add(model.getShape(shape.member.target).get())
                } else if (shape is MapShape) {
                    shapesFoundThisPass.add(model.getShape(shape.value.target).get())
                } else if (Types.isEnum(shape)) {
                    shapesFoundThisPass.add(shape)
                }
            }
            shapesToEvaluate = shapesFoundThisPass.toList()
            foundShapes.addAll(shapesFoundThisPass)
        } while (lastCount < foundShapes.count())

        // Make a generator for each shape we found
        val shapeGenerators = foundShapes.mapNotNull { shape ->
            log.info("Processing ${shape.id}")
            when {
                Types.isException(shape) -> ExceptionGenerator(pluginContext, shape.asStructureShape().get())
                shape is StructureShape -> StructureGenerator(pluginContext, shape)
                Types.isEnum(shape) -> EnumGenerator(pluginContext, shape.asStringShape().get())
                shape is UnionShape -> UnionGenerator(pluginContext, shape)
                else -> null
            }
        }
        generators.addAll(shapeGenerators)

        // Run all of the generators
        generators.forEach { generator ->
            generator.accept(dir, manifest)
        }
    }
}
