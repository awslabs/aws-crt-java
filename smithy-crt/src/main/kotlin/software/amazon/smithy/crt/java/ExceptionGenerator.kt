package software.amazon.smithy.crt.java

import com.squareup.javapoet.TypeSpec
import software.amazon.smithy.build.PluginContext
import software.amazon.smithy.crt.java.Types.Utils.toClassName
import software.amazon.smithy.model.shapes.StructureShape
import java.lang.RuntimeException

class ExceptionGenerator(pluginContext: PluginContext, structure: StructureShape)
    : StructureGenerator(pluginContext, structure, toClassName(structure)){

    override fun generate(): TypeSpec.Builder {
        log.info("Generating exception ${structure.id} -> $className")
        val exceptionBuilder = super.generate()
        exceptionBuilder.superclass(RuntimeException::class.javaObjectType)

        return exceptionBuilder
    }
}