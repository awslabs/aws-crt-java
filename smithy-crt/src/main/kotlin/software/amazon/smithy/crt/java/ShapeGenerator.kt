package software.amazon.smithy.crt.java

import com.squareup.javapoet.*
import software.amazon.smithy.build.PluginContext
import software.amazon.smithy.crt.java.Types.Utils.toClassName
import software.amazon.smithy.crt.java.Types.Utils.toTypeName
import software.amazon.smithy.model.Model
import software.amazon.smithy.model.shapes.Shape
import software.amazon.smithy.model.shapes.ShapeId
import software.amazon.smithy.model.traits.DocumentationTrait
import software.amazon.smithy.utils.StringUtils
import java.util.*
import java.util.function.Consumer
import java.util.logging.Logger
import java.util.stream.Collectors
import javax.lang.model.element.Modifier

abstract class ShapeGenerator(protected val pluginContext: PluginContext, protected val className: ClassName) : JavaFileGenerator {

    protected val log: Logger = Logger.getLogger(ShapeGenerator::class.simpleName)
    protected val model: Model = pluginContext.model

    companion object {
        val GENERATED_ANNOTATION: ClassName = ClassName.get("software.amazon.awssdk.crt.annotations", "Generated")

        // Standard format/setup for emitting a source file to Java
        fun emit(className: ClassName, builder: TypeSpec.Builder) : JavaFile {
            return JavaFile.builder(className.packageName(), builder.build())
                .addFileComment("""
                    Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
                    SPDX-License-Identifier: Apache-2.0.
                    """.trimIndent())
                .indent("    ")
                .build()
        }
    }

    protected fun getterPrefix(shape: Shape) : String {
        return if (shape.isBooleanShape) "is" else "get"
    }

    protected fun setterPrefix(shape: Shape) : String {
        return "set"
    }

    override fun accept(t: Consumer<JavaFile>) {
        val java = emit(className, generate())
        t.accept(java)
    }

    abstract fun generate() : TypeSpec.Builder

    protected fun objectHashCode(classBuilder: TypeSpec.Builder) : MethodSpec.Builder {
        // @Override
        // public int hashCode()
        return MethodSpec.methodBuilder("hashCode")
            .addAnnotation(Override::class.javaObjectType)
            .addModifiers(Modifier.PUBLIC)
            .addStatement("return ${'$'}T.hash(${'$'}T.class)", ClassName.get(Objects::class.javaObjectType), className)
            .returns(TypeName.INT)
    }

    protected fun objectEquals(classBuilder: TypeSpec.Builder) : MethodSpec.Builder {
        // @Override
        // public boolean equals(Object rhs)
        return MethodSpec.methodBuilder("equals")
            .addAnnotation(Override::class.javaObjectType)
            .addModifiers(Modifier.PUBLIC)
            .addParameter(ParameterSpec.builder(ClassName.get(Object::class.javaObjectType), "rhs").build())
            .addStatement("if (${'$'}L == null) return false", "rhs")
            .addStatement("return (${'$'}L instanceof ${'$'}T)", "rhs", className)
            .returns(TypeName.BOOLEAN)
    }

    protected fun objectOverrides(classBuilder: TypeSpec.Builder, shape: Shape?) : List<MethodSpec.Builder> {
        return listOf(
            objectHashCode(classBuilder),
            objectEquals(classBuilder)
        )
    }

    protected fun builderCtor(classBuilder: TypeSpec.Builder, shape: Shape) : MethodSpec.Builder {
        val shapeTypeName = toClassName(shape)
        val builderTypeName = shapeTypeName.nestedClass("Builder")
        // private T(Builder builder)
        val ctorBuilder = MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PRIVATE)
            .addParameter(ParameterSpec.builder(ClassName.bestGuess("Builder"), "builder").build())
        // this.$member = builder.$member
        shape.members().stream().forEach { member ->
            ctorBuilder.addStatement("this.${'$'}L = builder.${'$'}L",
                StringUtils.uncapitalize(member.memberName),
                StringUtils.uncapitalize(member.memberName))
        }
        return ctorBuilder
    }

    protected fun builder(classBuilder: TypeSpec.Builder, shape: Shape) : TypeSpec.Builder {
        val shapeTypeName = toClassName(shape)
        val builderClassName = ClassName.bestGuess("Builder")

        // static final class Builder
        val builderBuilder = TypeSpec.classBuilder("Builder")
            .addModifiers(Modifier.STATIC, Modifier.FINAL)
            // private Builder()
            .addMethod(MethodSpec.constructorBuilder().addModifiers(Modifier.PRIVATE).build())

        // private Builder($shape model)
        val builderCtor = MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PRIVATE)
            .addParameter(ParameterSpec.builder(shapeTypeName, "model").build())
        shape.members().stream().forEach { member ->
            builderCtor.addStatement("${'$'}L(model.${'$'}L)",
                StringUtils.uncapitalize(member.memberName),
                StringUtils.uncapitalize(member.memberName))
        }
        builderBuilder.addMethod(builderCtor.build())

        // public $union build()
        val build = MethodSpec.methodBuilder("build")
            .addModifiers(Modifier.PUBLIC)
            .addStatement("return new ${'$'}L(this)", shapeTypeName)
            .returns(shapeTypeName)
        builderBuilder.addMethod(build.build())

        shape.members().stream().forEach { member ->
            val target = toShape(member.target)
            val docTrait = member.getTrait(DocumentationTrait::class.javaObjectType)
            // private T $name
            builderBuilder.addField(FieldSpec.builder(toTypeName(target), StringUtils.uncapitalize(member.memberName), Modifier.PRIVATE).build())

            // public final Builder $name(T $name)
            val chain = MethodSpec.methodBuilder(StringUtils.uncapitalize(member.memberName))
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addParameter(ParameterSpec.builder(toTypeName(target), StringUtils.uncapitalize(member.memberName)).build())
                .addStatement("this.${'$'}L = ${'$'}L",
                    StringUtils.uncapitalize(member.memberName),
                    StringUtils.uncapitalize(member.memberName))
                .addStatement("return this")
                .returns(builderClassName)
            if (docTrait.isPresent) {
                chain.addJavadoc(docTrait.get().value)
            }
            builderBuilder.addMethod(chain.build())
        }

        // public static Builder builder()
        val builder = MethodSpec.methodBuilder("builder")
            .addModifiers(Modifier.PUBLIC)
            .addStatement("return new Builder()")
            .returns(builderClassName)
        classBuilder.addMethod(builder.build())

        val toBuilder = MethodSpec.methodBuilder("toBuilder")
            .addModifiers(Modifier.PUBLIC)
            .addStatement("return new Builder(this)")
            .returns(builderClassName)
        classBuilder.addMethod(toBuilder.build())

        return builderBuilder
    }

    fun getters(classBuilder: TypeSpec.Builder, shape: Shape) : List<MethodSpec.Builder> {
        return shape.members().stream().map { member ->
            val memberTypeShape = model.getShape(member.target.toShapeId()).get()
            val memberName = member.memberName
            val memberTypeName = toTypeName(memberTypeShape)

            // public T $name()
            MethodSpec.methodBuilder(StringUtils.uncapitalize(memberName))
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return ${'$'}L", StringUtils.uncapitalize(memberName))
                .returns(memberTypeName)
        }.collect(Collectors.toList())
    }

    fun setters(classBuilder: TypeSpec.Builder, shape: Shape) : List<MethodSpec.Builder> {
        return shape.members().stream().map { member ->
            val memberTypeShape = model.getShape(member.target.toShapeId()).get()
            val memberName = member.memberName
            val memberTypeName = toTypeName(memberTypeShape)

            // public void set$Name(T $name)
            MethodSpec.methodBuilder(setterPrefix(memberTypeShape) + StringUtils.capitalize(memberName))
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ParameterSpec.builder(memberTypeName, StringUtils.uncapitalize(memberName), Modifier.FINAL).build())
                .addStatement(
                    "this.${'$'}L = ${'$'}L",
                    StringUtils.uncapitalize(memberName),
                    StringUtils.uncapitalize(memberName))
        }.collect(Collectors.toList())
    }

    fun dataMembers(classBuilder: TypeSpec.Builder, shape: Shape) : List<FieldSpec.Builder> {
        // Add all data members and accessors
        return shape.members().stream().map { member ->
            val memberTypeShape = model.getShape(member.target.toShapeId()).get()
            val memberName = member.memberName
            val memberTypeName = toTypeName(memberTypeShape)

            // private T $name
            FieldSpec.builder(
                memberTypeName,
                StringUtils.uncapitalize(memberName),
                Modifier.PRIVATE
            )
        }.collect(Collectors.toList())
    }

    fun defaultCtor(classBuilder: TypeSpec.Builder, shape: Shape) : MethodSpec.Builder {
        val ctorBuilder = MethodSpec.constructorBuilder()
        // this.$member = null
        shape.members().stream().forEach { member ->
            ctorBuilder.addStatement("this.${'$'}L = null", StringUtils.uncapitalize(member.memberName))
        }
        return ctorBuilder
    }

    protected fun toShape(id: ShapeId) : Shape {
        return model.getShape(id).get()
    }
}