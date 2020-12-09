package software.amazon.smithy.crt

import com.squareup.javapoet.*
import software.amazon.smithy.build.PluginContext
import software.amazon.smithy.crt.TypeMap.Utils.toClassName
import software.amazon.smithy.crt.TypeMap.Utils.toTypeName
import software.amazon.smithy.crt.TypeMap.Utils.toMemberFieldTypeName
import software.amazon.smithy.crt.java.JavaFileGenerator
import software.amazon.smithy.model.Model
import software.amazon.smithy.model.shapes.Shape
import software.amazon.smithy.model.shapes.StructureShape
import software.amazon.smithy.model.traits.DocumentationTrait
import software.amazon.smithy.utils.StringUtils
import java.lang.reflect.InvocationTargetException
import java.util.*
import java.util.function.Consumer
import java.util.logging.Logger
import javax.annotation.processing.Generated
import javax.lang.model.element.Modifier

open class EmptyStructureGenerator(private val pluginContext: PluginContext, val className: ClassName) : JavaFileGenerator {

    protected val classBuilder: TypeSpec.Builder = TypeSpec.classBuilder(className)
        .addModifiers(Modifier.PUBLIC)
        .addAnnotation(AnnotationSpec.builder(Generated::class.javaObjectType)
            .addMember("value", "${'$'}S", "aws.crt.java.generator")
            .build())
    protected val ctorBuilder: MethodSpec.Builder = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC)
    private lateinit var java: JavaFile

    override fun getOutputSubdirectory(): String {
        TODO("Not yet implemented")
    }

    override fun accept(t: Consumer<JavaFile>) {
        finish()
        t.accept(java)
    }

    init {
        buildCommon()
    }

    fun finish() {
        // add class constructor
        classBuilder.addMethod(ctorBuilder.build())

        java = JavaFile.builder(className.packageName(), classBuilder.build()).build()
    }

    private fun buildHashCode() {
        classBuilder.addMethod(MethodSpec.methodBuilder("hashCode")
            .addAnnotation(Override::class.javaObjectType)
            .addModifiers(Modifier.PUBLIC)
            .addStatement("return ${'$'}T.hash(${'$'}T.class)", ClassName.get(Objects::class.javaObjectType), className)
            .returns(TypeName.INT)
            .build()
        )
    }

    private fun buildEquals() {
        classBuilder.addMethod(MethodSpec.methodBuilder("equals")
            .addAnnotation(Override::class.javaObjectType)
            .addModifiers(Modifier.PUBLIC)
            .addParameter(ParameterSpec.builder(ClassName.get(Object::class.javaObjectType), "rhs").build())
            .addStatement("if (${'$'}L == null) return false", "rhs")
            .addStatement("return (${'$'}L instanceof ${'$'}T)", "rhs", className)
            .returns(TypeName.BOOLEAN)
            .build()
        )
    }

    private fun buildCommon() {
        buildHashCode()
        buildEquals()
    }
}

class StructureGenerator(private val pluginContext: PluginContext, val structure: StructureShape)
    : EmptyStructureGenerator(pluginContext, toClassName(structure)) {

    private val model: Model = pluginContext.model
    private val log: Logger = Logger.getLogger(StructureGenerator::class.simpleName)

    init {
        log.fine("Generating structure {} -> {}".format(structure.id, className))

        // Add all data members and accessors
        structure.allMembers.entries.stream().forEach { member ->
            val memberShape = member.value
            val memberTypeShape = model.getShape(memberShape.target.toShapeId()).get()
            val memberName = member.key
            val memberTypeName = toTypeName(memberTypeShape)
            val memberFieldTypeName = toMemberFieldTypeName(memberTypeShape)

            // Add member field
            classBuilder.addField(FieldSpec.builder(
                memberTypeName,
                StringUtils.uncapitalize(memberName),
                Modifier.PRIVATE
            ).build())

            // Add getter
            val getter = MethodSpec.methodBuilder(getterPrefix(memberTypeShape) + StringUtils.capitalize(memberName))
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return ${'$'}L", StringUtils.uncapitalize(memberName))
                .returns(memberFieldTypeName)

            val docTrait = memberTypeShape.getTrait(DocumentationTrait::class.javaObjectType)
            if (docTrait.isPresent) {
                getter.addJavadoc(docTrait.get().value)
            }
            classBuilder.addMethod(getter.build())

            // Add setter
            val setter = MethodSpec.methodBuilder(setterPrefix(memberTypeShape) + StringUtils.capitalize(memberName))
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ParameterSpec.builder(memberFieldTypeName, StringUtils.uncapitalize(memberName), Modifier.FINAL).build())
                .addStatement(
                    "this.${'$'}L = ${'$'}L",
                    StringUtils.uncapitalize(memberName),
                    StringUtils.uncapitalize(memberName)
                )


            if (docTrait.isPresent) {
                setter.addJavadoc(docTrait.get().value)
            }
            classBuilder.addMethod(setter.build())
            ctorBuilder.addStatement("this.${'$'}L = null", StringUtils.uncapitalize(memberName))
        }
    }

    private fun getterPrefix(shape: Shape) : String {
        return if (shape.isBooleanShape) "is" else "get"
    }

    private fun setterPrefix(shape: Shape) : String {
        return "set"
    }
}