package software.amazon.smithy.crt.java

import com.squareup.javapoet.TypeSpec
import software.amazon.smithy.build.PluginContext
import software.amazon.smithy.crt.codegen.ClassName
import software.amazon.smithy.crt.codegen.TypeName
import software.amazon.smithy.crt.java.Types.Utils.serviceException
import java.lang.RuntimeException

class ServiceExceptionGenerator(
    pluginContext: PluginContext,
    className: ClassName = serviceException())
    : ShapeGenerator(pluginContext, className) {

    private val javaException = Exception(className)
        .superclass(ClassName.get(RuntimeException::class.javaObjectType))
        .addModifier(Modifier.PUBLIC)
        .addMembers(
            Field("message", TypeName.get(String::class.javaObjectType)),
            Field("requestId", TypeName.get(String::class.javaObjectType)),
            Field("statusCode", TypeName.INT),
            Field("cause", TypeName.get(Throwable::class.javaObjectType))
        )
        .addAnnotation(generatedAnnotation(ServiceExceptionGenerator::class.javaObjectType))

    private val builder = Builder(javaException)

    override fun generate(): TypeSpec.Builder {
        builder.build()
        return javaException.build().java()
    }
}