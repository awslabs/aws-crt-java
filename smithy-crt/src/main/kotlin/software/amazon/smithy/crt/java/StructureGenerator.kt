package software.amazon.smithy.crt.java

import com.squareup.javapoet.*
import software.amazon.smithy.build.PluginContext
import software.amazon.smithy.crt.java.Types.Utils.toClassName
import software.amazon.smithy.model.shapes.StructureShape
import javax.lang.model.element.Modifier

abstract class StructureGeneratorBase(pluginContext: PluginContext, className: ClassName)
    : ShapeGenerator(pluginContext, className) {
    override fun generate(): TypeSpec.Builder {
        return TypeSpec.classBuilder(className)
            .addModifiers(Modifier.PUBLIC)
    }
}

open class EmptyStructureGenerator(pluginContext: PluginContext, className: ClassName)
    : StructureGeneratorBase(pluginContext, className) {
    override fun generate(): TypeSpec.Builder {
        log.fine("Generating empty structure $className")
        val classBuilder = super.generate()
        classBuilder.addAnnotation(AnnotationSpec.builder(GENERATED_ANNOTATION)
            .addMember("value", "${'$'}S", EmptyStructureGenerator::class.qualifiedName)
            .build())
        classBuilder.addMethod(MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC).build())
        classBuilder.addMethods(objectOverrides(classBuilder, null).map { it.build() })
        return classBuilder
    }
}

open class StructureGenerator(pluginContext: PluginContext, protected val structure: StructureShape, name: ClassName = toClassName(structure))
    : StructureGeneratorBase(pluginContext, name) {

    private val ctorBuilder: MethodSpec.Builder = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC)

    override fun generate(): TypeSpec.Builder {
        log.fine("Generating structure ${structure.id} -> $className")
        val classBuilder = super.generate()
        try {
            classBuilder.addAnnotation(
                AnnotationSpec.builder(GENERATED_ANNOTATION)
                    .addMember("value", "${'$'}S", StructureGenerator::class.qualifiedName)
                    .build()
            )

            val builderBuilder = builder(classBuilder, structure)
            val defaultCtor = defaultCtor(classBuilder, structure)
                .addModifiers(Modifier.PRIVATE)
            val builderCtor = builderCtor(classBuilder, structure)
                .addModifiers(Modifier.PRIVATE)
            val dataMembers = dataMembers(classBuilder, structure)
            val getters = getters(classBuilder, structure)
            val setters = setters(classBuilder, structure)
            val objectOverrides = objectOverrides(classBuilder, structure)

            classBuilder.addFields(dataMembers.map { it.build() })
            classBuilder.addMethod(defaultCtor.build())
            classBuilder.addMethod(builderCtor.build())
            classBuilder.addMethods(objectOverrides.map { it.build() })
            getters.zip(setters) { getter, setter ->
                classBuilder.addMethod(getter.build())
                classBuilder.addMethod(setter.build())
            }
            classBuilder.addType(builderBuilder.build())
        } catch (ex : Exception) {
            log.severe(ex.message)
        }
        return classBuilder
    }
}