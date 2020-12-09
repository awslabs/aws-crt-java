
package software.amazon.smithy.crt.java;

import com.squareup.javapoet.*
import software.amazon.smithy.model.Model;

import software.amazon.smithy.build.PluginContext
import software.amazon.smithy.crt.EmptyStructureGenerator
import software.amazon.smithy.crt.StructureGenerator
import software.amazon.smithy.crt.TypeMap
import software.amazon.smithy.model.shapes.*
import software.amazon.smithy.model.traits.DocumentationTrait
import software.amazon.smithy.model.traits.EnumTrait
import software.amazon.smithy.utils.StringUtils
import java.util.*

import java.util.function.Consumer;
import java.util.logging.Logger
import java.util.stream.Collectors
import javax.lang.model.element.Modifier

class DataModelGenerator(private val pluginContext: PluginContext) : JavaFileGenerator {
    private val log = Logger.getLogger(DataModelGenerator::class.simpleName)

    private val model: Model = pluginContext.model
    private val typeMap = TypeMap(pluginContext)

    override fun getOutputSubdirectory(): String {
        return "model";
    }

    private fun toTypeName(shape: Shape): TypeName {
        return typeMap.toTypeName(shape)
    }

    private fun toClassName(shape: Shape): ClassName {
        return typeMap.toClassName(shape)
    }

    private fun toMemberFieldTypeName(shape: Shape): TypeName {
        return typeMap.toMemberFieldTypeName(shape)
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
        val shapesToGenerate = mutableListOf<Shape>()
        val generators = mutableListOf<JavaFileGenerator?>()
        val operations = model.shapes(OperationShape::class.javaObjectType)
        operations.forEach { operation ->
            if (operation.input.isPresent) {
                shapesToGenerate.add(toShape(operation.input.get()))
            } else {
                generators.add(emptyRequestStructure(operation))
            }

            if (operation.output.isPresent) {
                shapesToGenerate.add(toShape(operation.output.get()))
            } else {
                generators.add(emptyResponseStructure(operation))
            }

            shapesToGenerate.addAll(
                operation.errors.stream()
                    .map { it -> toShape(it) }
                    .collect(Collectors.toList())
            )
        }

        shapesToGenerate.mapTo(generators) { shape ->
            log.info("Generating {}".format(shape))
            when (shape) {
                is StructureShape -> StructureGenerator(pluginContext, shape)
                else -> null
            }
        }

        generators.forEach { generator ->
            generator?.accept(javaFileConsumer)
        }
    }
}
