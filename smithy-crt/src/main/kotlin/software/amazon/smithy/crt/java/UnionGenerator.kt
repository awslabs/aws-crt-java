package software.amazon.smithy.crt.java

import com.squareup.javapoet.*
import software.amazon.smithy.build.PluginContext
import software.amazon.smithy.crt.java.Types.Utils.toClassName
import software.amazon.smithy.model.shapes.UnionShape
import javax.lang.model.element.Modifier

class UnionGenerator(pluginContext: PluginContext, private val unionShape: UnionShape)
    : ShapeGenerator(pluginContext, toClassName(unionShape)) {

    private val unionTypeName = ClassName.get(unionShape.id.namespace, unionShape.id.name)
    private val builderTypeName = unionTypeName.nestedClass("Builder")

    override fun generate(): TypeSpec.Builder {
        log.info("Generating union ${unionShape.id} -> $unionTypeName")

        val unionBuilder = TypeSpec.classBuilder(unionTypeName)
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(AnnotationSpec.builder(GENERATED_ANNOTATION)
                .addMember("value", "${'$'}S", UnionGenerator::class.qualifiedName)
                .build())

        val objectOverrides = objectOverrides(unionBuilder, unionShape)
        val builderBuilder = builder(unionBuilder, unionShape)
        val builderCtor = builderCtor(unionBuilder, unionShape)
        val dataMembers = dataMembers(unionBuilder, unionShape)
        val getters = getters(unionBuilder, unionShape)

        unionBuilder.addMethod(builderCtor.build())
        unionBuilder.addMethods(objectOverrides.map { it.build() })
        unionBuilder.addFields(dataMembers.map { it.build() })
        unionBuilder.addMethods(getters.map { it.build() })
        unionBuilder.addType(builderBuilder.build())

        return unionBuilder
    }
}