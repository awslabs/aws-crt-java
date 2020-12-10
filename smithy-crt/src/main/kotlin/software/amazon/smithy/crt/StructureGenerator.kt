package software.amazon.smithy.crt

import com.squareup.javapoet.*
import software.amazon.smithy.build.PluginContext
import software.amazon.smithy.crt.TypeMap.Utils.toClassName
import software.amazon.smithy.model.shapes.StructureShape
import software.amazon.smithy.model.traits.DocumentationTrait
import software.amazon.smithy.utils.StringUtils
import javax.lang.model.element.Modifier

open class StructureGeneratorBase(pluginContext: PluginContext, className: ClassName)
    : ShapeGenerator(pluginContext, className) {
    protected val classBuilder: TypeSpec.Builder = TypeSpec.classBuilder(className)
        .addModifiers(Modifier.PUBLIC)
}

open class EmptyStructureGenerator(pluginContext: PluginContext, className: ClassName)
    : StructureGeneratorBase(pluginContext, className) {
    init {
        log.fine("Generating empty structure {}".format(className))
        classBuilder.addAnnotation(AnnotationSpec.builder(GENERATED_ANNOTATION)
            .addMember("value", "${'$'}S", EmptyStructureGenerator::class.qualifiedName)
            .build())
        classBuilder.addMethod(MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC).build())
        buildCommon(classBuilder)
        super.finish(JavaFile.builder(className.packageName(), classBuilder.build()).build())
    }
}

class StructureGenerator(pluginContext: PluginContext, private val structure: StructureShape)
    : StructureGeneratorBase(pluginContext, toClassName(structure)) {

    private val ctorBuilder: MethodSpec.Builder = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC)

    init {
        log.fine("Generating structure {} -> {}".format(structure.id, className))

        classBuilder.addAnnotation(AnnotationSpec.builder(GENERATED_ANNOTATION)
            .addMember("value", "${'$'}S", StructureGenerator::class.qualifiedName)
            .build())

        // Add all data members and accessors
        structure.allMembers.entries.stream().forEach { member ->
            val memberShape = member.value
            val memberTypeShape = model.getShape(memberShape.target.toShapeId()).get()
            val memberName = member.key
            val memberTypeName = toTypeName(memberTypeShape)

            // private T $name
            classBuilder.addField(FieldSpec.builder(
                memberTypeName,
                StringUtils.uncapitalize(memberName),
                Modifier.PRIVATE
            ).build())

            // public T get$Name()
            val getter = MethodSpec.methodBuilder(getterPrefix(memberTypeShape) + StringUtils.capitalize(memberName))
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return ${'$'}L", StringUtils.uncapitalize(memberName))
                .returns(memberTypeName)

            val docTrait = memberTypeShape.getTrait(DocumentationTrait::class.javaObjectType)
            if (docTrait.isPresent) {
                getter.addJavadoc(docTrait.get().value)
            }
            classBuilder.addMethod(getter.build())

            // public void set$Name(T $name)
            val setter = MethodSpec.methodBuilder(setterPrefix(memberTypeShape) + StringUtils.capitalize(memberName))
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ParameterSpec.builder(memberTypeName, StringUtils.uncapitalize(memberName), Modifier.FINAL).build())
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

        super.finish(JavaFile.builder(className.packageName(), classBuilder.build()).build())
    }
}