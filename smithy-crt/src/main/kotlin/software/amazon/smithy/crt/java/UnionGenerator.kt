package software.amazon.smithy.crt.java

import com.squareup.javapoet.*
import software.amazon.smithy.build.PluginContext
import software.amazon.smithy.crt.java.Types.Utils.toClassName
import software.amazon.smithy.model.shapes.UnionShape
import javax.lang.model.element.Modifier

class UnionGenerator(pluginContext: PluginContext, private val unionShape: UnionShape)
    : ShapeGenerator(pluginContext, toClassName(unionShape)) {

    private val javaUnion = Union(className)
        .addAnnotation(generatedAnnotation(UnionGenerator::class.javaObjectType))

    private val builder = Builder(javaUnion)

    override fun generate(): TypeSpec.Builder {
        log.info("Generating union ${unionShape.id} -> $className")
        builder.build()
        return javaUnion.build().java()
    }
}