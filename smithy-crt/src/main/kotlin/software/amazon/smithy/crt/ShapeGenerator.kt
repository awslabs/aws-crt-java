package software.amazon.smithy.crt

import com.squareup.javapoet.*
import software.amazon.smithy.build.PluginContext
import software.amazon.smithy.crt.java.JavaFileGenerator
import software.amazon.smithy.model.Model
import software.amazon.smithy.model.shapes.Shape
import software.amazon.smithy.model.shapes.ShapeId
import java.util.*
import java.util.function.Consumer
import java.util.logging.Logger
import javax.lang.model.element.Modifier

open class ShapeGenerator(protected val pluginContext: PluginContext, protected val className: ClassName) : JavaFileGenerator {

    protected val log: Logger = Logger.getLogger(ShapeGenerator::class.simpleName)
    protected val model: Model = pluginContext.model
    private var java: JavaFile? = null

    companion object {
        val GENERATED_ANNOTATION: ClassName = ClassName.get("software.amazon.awssdk.crt.annotations", "Generated")
    }

    protected fun getterPrefix(shape: Shape) : String {
        return if (shape.isBooleanShape) "is" else "get"
    }

    protected fun setterPrefix(shape: Shape) : String {
        return "set"
    }

    override fun getOutputSubdirectory(): String {
        TODO("Not yet implemented")
    }

    override fun accept(t: Consumer<JavaFile>) {
        assert(java != null)
        t.accept(java!!)
    }

    protected fun finish(javaFile: JavaFile) {
        java = javaFile
    }

    protected fun buildHashCode(classBuilder: TypeSpec.Builder) {
        // @Override
        // public int hashCode()
        classBuilder.addMethod(
            MethodSpec.methodBuilder("hashCode")
            .addAnnotation(Override::class.javaObjectType)
            .addModifiers(Modifier.PUBLIC)
            .addStatement("return ${'$'}T.hash(${'$'}T.class)", ClassName.get(Objects::class.javaObjectType), className)
            .returns(TypeName.INT)
            .build()
        )
    }

    protected fun buildEquals(classBuilder: TypeSpec.Builder) {
        // @Override
        // public boolean equals(Object rhs)
        classBuilder.addMethod(
            MethodSpec.methodBuilder("equals")
            .addAnnotation(Override::class.javaObjectType)
            .addModifiers(Modifier.PUBLIC)
            .addParameter(ParameterSpec.builder(ClassName.get(Object::class.javaObjectType), "rhs").build())
            .addStatement("if (${'$'}L == null) return false", "rhs")
            .addStatement("return (${'$'}L instanceof ${'$'}T)", "rhs", className)
            .returns(TypeName.BOOLEAN)
            .build()
        )
    }

    protected fun buildCommon(classBuilder: TypeSpec.Builder) {
        buildHashCode(classBuilder)
        buildEquals(classBuilder)
    }

    protected fun toShape(id: ShapeId) : Shape {
        return model.getShape(id).get()
    }

    protected fun toTypeName(id: ShapeId) : TypeName {
        return TypeMap.toTypeName(toShape(id))
    }

    protected fun toTypeName(shape: Shape) : TypeName {
        return TypeMap.toTypeName(shape)
    }

    protected fun toClassName(shape: Shape) : ClassName {
        return TypeMap.toClassName(shape)
    }
}