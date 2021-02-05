package software.amazon.smithy.crt.java

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.TypeSpec
import software.amazon.smithy.build.PluginContext
import software.amazon.smithy.crt.java.Types.Utils.serviceException
import software.amazon.smithy.crt.java.Types.Utils.toClassName
import software.amazon.smithy.model.shapes.StructureShape
import java.lang.RuntimeException

class ExceptionGenerator(pluginContext: PluginContext, structure: StructureShape)
    : ShapeGenerator(pluginContext, toClassName(structure)) {

    private val javaException : Exception = Exception(className)
        .superclass(serviceException())
        // Attach @Generated annotation
        .addAnnotation(generatedAnnotation(ExceptionGenerator::class.javaObjectType))

    private val builder = Builder(javaException)

    override fun generate(): TypeSpec.Builder {
        builder.build()
        return javaException.build().java()
    }
}