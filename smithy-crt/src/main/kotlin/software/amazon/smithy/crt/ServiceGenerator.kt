
package software.amazon.smithy.crt.java;

import com.squareup.javapoet.*
import software.amazon.smithy.model.Model;

import software.amazon.smithy.build.PluginContext
import software.amazon.smithy.crt.*
import software.amazon.smithy.model.shapes.*

import java.util.function.Consumer;
import java.util.logging.Logger
import java.util.stream.Collectors

class ServiceGenerator(private val pluginContext: PluginContext) : JavaFileGenerator {
    private val log = Logger.getLogger(ServiceGenerator::class.simpleName)

    private val model: Model = pluginContext.model
    private val typeMap = TypeMap(pluginContext)

    override fun getOutputSubdirectory(): String {
        return "model";
    }

    private fun toShape(id: ShapeId): Shape {
        return model.getShape(id).get();
    }

    private fun emptyStructure(className: ClassName) : JavaFileGenerator {
        return EmptyStructureGenerator(pluginContext, className)
    }

    private fun emptyRequestStructure(operation: OperationShape) : JavaFileGenerator {
        return emptyStructure(ClassName.get(operation.id.namespace, operation.id.name + "Request"))
    }

    private fun emptyResponseStructure(operation: OperationShape) : JavaFileGenerator {
        return emptyStructure(ClassName.get(operation.id.namespace, operation.id.name + "Response"))
    }

    override fun accept(javaFileConsumer: Consumer<JavaFile>) {

        val foundShapes = mutableSetOf<Shape>()
        val generators = mutableListOf<JavaFileGenerator?>()
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
        do {
            val lastCount = foundShapes.count()
            val additionalShapes = mutableSetOf<Shape>()
            foundShapes.forEach { shape ->
                if (shape is StructureShape || shape is UnionShape) {
                    shape.members().stream().forEach { member ->
                        additionalShapes.add(model.getShape(member.target).get())
                    }
                } else if (shape is ListShape) {
                    additionalShapes.add(model.getShape(shape.member.target).get())
                } else if (shape is MapShape) {
                    additionalShapes.addAll(shape.members().stream().map {model.getShape(it.id).get()}.collect(Collectors.toList()))
                }
            }
            foundShapes.addAll(additionalShapes)
        } while (lastCount < foundShapes.count())

        // Make a generator for each shape we found
        foundShapes.mapTo(generators) { shape ->
            log.info("Generating {}".format(shape))
            when {
                shape is StructureShape -> StructureGenerator(pluginContext, shape)
                TypeMap.isEnum(shape) -> EnumGenerator(pluginContext, shape.asStringShape().orElseThrow())
                shape is UnionShape -> UnionGenerator(pluginContext, shape)
                else -> null
            }
        }

        // Run all of the generators
        generators.forEach { generator ->
            generator?.accept(javaFileConsumer)
        }
    }
}
