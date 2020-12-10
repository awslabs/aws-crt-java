package software.amazon.smithy.crt

import com.squareup.javapoet.*
import software.amazon.smithy.build.PluginContext
import software.amazon.smithy.crt.TypeMap.Utils.toClassName
import software.amazon.smithy.model.shapes.UnionShape
import software.amazon.smithy.utils.StringUtils
import javax.lang.model.element.Modifier

class UnionGenerator(pluginContext: PluginContext, unionShape: UnionShape)
    : ShapeGenerator(pluginContext, toClassName(unionShape)) {

    private val unionTypeName = ClassName.get(unionShape.id.namespace, unionShape.id.name)
    private val builderTypeName = unionTypeName.nestedClass("Builder")

    init {
        log.info("Generating union {} -> {}".format(unionShape.id, unionTypeName))

        val unionBuilder = TypeSpec.classBuilder(unionTypeName)
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(AnnotationSpec.builder(GENERATED_ANNOTATION)
                .addMember("value", "${'$'}S", UnionGenerator::class.qualifiedName)
                .build())

        buildCommon(unionBuilder)

        // private T(Builder builder)
        val ctorBuilder = MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PRIVATE)
            .addParameter(ParameterSpec.builder(builderTypeName, "builder").build())
        // this.$member = builder.$member
        unionShape.members().stream().forEach { member ->
            ctorBuilder.addStatement("this.${'$'}L = builder.${'$'}L",
                StringUtils.uncapitalize(member.memberName),
                StringUtils.uncapitalize(member.memberName))
        }
        unionBuilder.addMethod(ctorBuilder.build())

        // value and function per member
        unionShape.members().stream().forEach { member ->
            unionBuilder.addField(
                FieldSpec.builder(
                    TypeMap.toClassName(model.getShape(member.target).get()), StringUtils.uncapitalize(member.memberName), Modifier.PRIVATE)
                .build()
            )
            val getter = MethodSpec.methodBuilder(StringUtils.uncapitalize(member.memberName))
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return ${'$'}L", StringUtils.uncapitalize(member.memberName))
                .returns(TypeMap.toClassName(model.getShape(member.target).get()))
            unionBuilder.addMethod(getter.build())
        }

        // static final class Builder
        val builderBuilder = TypeSpec.classBuilder("Builder")
            .addModifiers(Modifier.STATIC, Modifier.FINAL)
            // private Builder()
            .addMethod(MethodSpec.constructorBuilder().addModifiers(Modifier.PRIVATE).build())

        // private Builder($union model)
        val builderCtor = MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PRIVATE)
            .addParameter(ParameterSpec.builder(unionTypeName, "model").build())
        unionShape.members().stream().forEach { member ->
            builderCtor.addStatement("${'$'}L(model.${'$'}L)",
                StringUtils.uncapitalize(member.memberName),
                StringUtils.uncapitalize(member.memberName))
        }
        builderBuilder.addMethod(builderCtor.build())

        // public $union build()
        val build = MethodSpec.methodBuilder("build")
            .addModifiers(Modifier.PUBLIC)
            .addStatement("return new ${'$'}L(this)", unionTypeName)
            .returns(unionTypeName)
        builderBuilder.addMethod(build.build())

        unionShape.members().stream().forEach { member ->
            val target = toShape(member.target)
            // private T $name
            builderBuilder.addField(FieldSpec.builder(TypeMap.toClassName(target), StringUtils.uncapitalize(member.memberName), Modifier.PRIVATE).build())
            // public T get$Name()
            val getter = MethodSpec.methodBuilder(getterPrefix(target) + StringUtils.capitalize(member.memberName))
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return ${'$'}L", StringUtils.uncapitalize(member.memberName))
                .returns(toTypeName(target))
            builderBuilder.addMethod(getter.build())
            // public void set$Name(T $name)
            val setter = MethodSpec.methodBuilder(setterPrefix(target) + StringUtils.capitalize(member.memberName))
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ParameterSpec.builder(toTypeName(target), StringUtils.uncapitalize(member.memberName), Modifier.FINAL).build())
                .addStatement(
                    "this.${'$'}L = ${'$'}L",
                    StringUtils.uncapitalize(member.memberName),
                    StringUtils.uncapitalize(member.memberName)
                )
            builderBuilder.addMethod(setter.build())
            // public final Builder $name(T $name)
            val chain = MethodSpec.methodBuilder(StringUtils.uncapitalize(member.memberName))
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addParameter(ParameterSpec.builder(toTypeName(target), StringUtils.uncapitalize(member.memberName)).build())
                .addStatement("this.${'$'}L = ${'$'}L",
                    StringUtils.uncapitalize(member.memberName),
                    StringUtils.uncapitalize(member.memberName))
                .addStatement("return this")
                .returns(builderTypeName)
            builderBuilder.addMethod(chain.build())
        }
        unionBuilder.addType(builderBuilder.build())

        // public static Builder builder()
        val builder = MethodSpec.methodBuilder("builder")
            .addModifiers(Modifier.PUBLIC)
            .addStatement("return new Builder()")
            .returns(builderTypeName)
        unionBuilder.addMethod(builder.build())

        val toBuilder = MethodSpec.methodBuilder("toBuilder")
            .addModifiers(Modifier.PUBLIC)
            .addStatement("return new Builder(this)")
            .returns(builderTypeName)
        unionBuilder.addMethod(toBuilder.build())

        finish(
            JavaFile.builder(unionTypeName.packageName(), unionBuilder.build())
            .build()
        )
    }
}