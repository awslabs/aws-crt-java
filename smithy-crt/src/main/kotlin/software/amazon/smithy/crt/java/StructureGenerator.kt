package software.amazon.smithy.crt.java

import com.squareup.javapoet.*
import software.amazon.smithy.build.PluginContext
import software.amazon.smithy.crt.codegen.AbstractStructure
import software.amazon.smithy.crt.java.Types.Utils.toClassName
import software.amazon.smithy.crt.java.Types.Utils.toTypeName
import software.amazon.smithy.model.shapes.StructureShape

abstract class StructureGeneratorBase (pluginContext: PluginContext, className: ClassName)
    : ShapeGenerator(pluginContext, className) {

    protected val javaStructure : Structure = Structure(className)
        // Attach @Generated annotation
        .addAnnotation(generatedAnnotation(StructureGenerator::class.javaObjectType))

    protected val builder = Builder(javaStructure)

    override fun generate(): TypeSpec.Builder {
        javaStructure.addDefaultConstructor()
        builder.build()
        return javaStructure.build().java()
    }
}

open class EmptyStructureGenerator(pluginContext: PluginContext, className: ClassName)
    : StructureGeneratorBase(pluginContext, className) {

    init {
        javaStructure.addModifier(Modifier.PUBLIC)
    }

    override fun generate(): TypeSpec.Builder {
        log.fine("Generating empty structure $className")
        return super.generate()
    }
}

open class StructureGenerator(pluginContext: PluginContext, protected val structure: StructureShape, name: ClassName = toClassName(structure))
    : StructureGeneratorBase(pluginContext, name) {

    init {
        javaStructure.addModifier(Modifier.PUBLIC)
        fieldsFromShape(structure).forEach{ field ->
            javaStructure.addMember(field)
        }
    }

    override fun generate(): TypeSpec.Builder {
        log.fine("Generating structure ${structure.id} -> $className")

        if(javaStructure.className.toString().endsWith("Request")) {

            val requestHeaderTypeName = ClassName.get("software.amazon.awssdk.crt.http", "HttpHeader");
            val arrayRequestHeaderTypeName = ArrayTypeName.of( requestHeaderTypeName );
            javaStructure.addMember(Field("customHeaders", arrayRequestHeaderTypeName));

            val customQueryParametersMapTypeName = ParameterizedTypeName.get( 
                ClassName.get("java.util", "Map"),
                TypeName.get(String::class.javaObjectType),
                TypeName.get(String::class.javaObjectType));

            javaStructure.addMember(Field("customQueryParameters", customQueryParametersMapTypeName));
        }

        return super.generate()
    }
}
