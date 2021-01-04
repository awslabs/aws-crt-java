package software.amazon.smithy.crt.java

import com.squareup.javapoet.*
import software.amazon.smithy.crt.codegen.*
import software.amazon.smithy.crt.codegen.ClassName
import software.amazon.smithy.crt.codegen.TypeName
import software.amazon.smithy.model.shapes.Shape
import software.amazon.smithy.utils.CaseUtils
import software.amazon.smithy.utils.StringUtils
import java.nio.file.Path
import java.util.*
import java.util.function.Consumer
import java.util.stream.Collectors
import java.util.stream.Stream

// These are specializations of the basic language elements that are tailored to the expected output from
// our Java SDK-style APIs.

typealias Field = software.amazon.smithy.crt.codegen.Field
typealias Fields = software.amazon.smithy.crt.codegen.Fields
typealias Modifier = software.amazon.smithy.crt.codegen.Modifier

fun software.amazon.smithy.crt.codegen.Field.Companion.defaultValue(typeName: TypeName) : String {
    val numericTypes = setOf(
        TypeName.BYTE,
        TypeName.CHAR,
        TypeName.DOUBLE,
        TypeName.FLOAT,
        TypeName.INT,
        TypeName.LONG,
        TypeName.SHORT,
    )
    return when (typeName) {
        in numericTypes -> 0
        TypeName.BOOLEAN -> false
        TypeName.get(String::class.javaObjectType) -> "\"\""
        else -> null
    }.toString()
}

interface JavaElement {
    fun java() : Any

    companion object {
        fun addModifiers(builder: TypeSpec.Builder, element: AbstractElement<*>) {
            element.modifiers.forEach { mod ->
                builder.addModifiers(mod)
            }
        }

        fun addModifiers(builder: MethodSpec.Builder, element: AbstractElement<*>) {
            element.modifiers.forEach { mod ->
                builder.addModifiers(mod)
            }
        }

        fun addModifiers(builder: FieldSpec.Builder, element: AbstractElement<*>) {
            element.modifiers.forEach { mod ->
                builder.addModifiers(mod)
            }
        }

        fun addAnnotations(builder: TypeSpec.Builder, element: AbstractElement<*>) {
            element.annotations.map{ it as Annotation }.forEach { annotation ->
                builder.addAnnotation(annotation.java().build())
            }
        }

        fun addAnnotations(builder: MethodSpec.Builder, element: AbstractElement<*>) {
            element.annotations.map{ it as Annotation }.forEach { annotation ->
                builder.addAnnotation(annotation.java().build())
            }
        }

        fun addAnnotations(builder: FieldSpec.Builder, element: AbstractElement<*>) {
            element.annotations.map{ it as Annotation }.forEach { annotation ->
                builder.addAnnotation(annotation.java().build())
            }
        }
    }
}

// Represents any class that has a Builder/BuilderImpl as an inner class and uses
// the Builder pattern for construction
interface BuilderClient<T: AbstractStructure<T>>
    : AbstractStructure<T>
    , JavaElement

interface MethodElement : JavaElement {
    open val javaBuilder: MethodSpec.Builder
    override fun java() : MethodSpec.Builder {
        return javaBuilder
    }
    open val args: Array<out Field>
}

open class Method(name: String, returnType: TypeName = TypeName.VOID, vararg args: Field)
    : software.amazon.smithy.crt.codegen.Method<Method>(name, returnType, *args)
    , MethodElement {
    override val javaBuilder: MethodSpec.Builder = MethodSpec.methodBuilder(name)
        .returns(returnType)
    fun java(mod: (MethodSpec.Builder) -> Unit) : Method {
        mod(javaBuilder)
        return this
    }

    override fun java() : MethodSpec.Builder {
        return javaBuilder
    }

    override fun build(): Method {
        return this
    }

    companion object {
        fun <M: MethodElement> build(builder: TypeSpec.Builder, src: M): M {
            val spec = src.java()
            JavaElement.addModifiers(spec, src as AbstractElement<M>)
            JavaElement.addAnnotations(spec, src as AbstractElement<M>)
            src.args.forEach { arg ->
                spec.addParameter(
                    ParameterSpec.builder(arg.typeName, arg.name, *arg.modifiers.toTypedArray()).build()
                )
            }

            builder.addMethod(spec.build())
            return src
        }
    }
}

open class Constructor(className: ClassName, vararg args: Field)
    : software.amazon.smithy.crt.codegen.Constructor<Constructor>(className, *args)
    , MethodElement {
    override val javaBuilder: MethodSpec.Builder = MethodSpec.constructorBuilder()
    fun java(mod: (MethodSpec.Builder) -> Unit) : Constructor {
        mod(javaBuilder)
        return this
    }

    override fun java() : MethodSpec.Builder {
        return javaBuilder
    }

    override fun build(): Constructor {
        return this
    }
}

open class Interface(className: ClassName)
    : software.amazon.smithy.crt.codegen.Interface<Interface>(className)
    , JavaElement {

    open val javaBuilder: TypeSpec.Builder = TypeSpec.interfaceBuilder(className)
    open fun java(mod: (TypeSpec.Builder) -> Unit) : Interface {
        mod(javaBuilder)
        return this
    }

    override fun java() : TypeSpec.Builder {
        return javaBuilder
    }

    constructor(packageName: String, name: String)
        : this(ClassName.get(packageName, name))

    override fun build(): Interface {
        return build(javaBuilder, this)
    }

    override fun generate(dir: Path, consumer: Consumer<Path>) {
        consumer.accept(JavaFile.builder(className.packageName(), javaBuilder.build()).build().writeToPath(dir))
    }

    companion object  {
        fun addInterfaces(builder: TypeSpec.Builder, src: AbstractInterface<*>) {
            src.interfaces.forEach {
                builder.addSuperinterface(it.className)
            }
        }
        fun addDataMembers(builder: TypeSpec.Builder, src: AbstractInterface<*>) {
            src.members.values.forEach { member ->
                val field = FieldSpec.builder(member.typeName, StringUtils.uncapitalize(member.name))
                    .addJavadoc(member.documentation ?: "")
                builder.addField(field.build())
            }
        }
        fun addMethods(builder: TypeSpec.Builder, src: AbstractInterface<*>) {
            src.methods.values.forEach { method ->
                Method.build(builder, method as MethodElement)
            }
        }
        fun <T : AbstractInterface<*>> build(builder: TypeSpec.Builder, src: T) : T {
            JavaElement.addModifiers(builder, src)
            JavaElement.addAnnotations(builder, src)
            addInterfaces(builder, src)
            addDataMembers(builder, src)
            addMethods(builder, src)
            return src
        }
    }
}

open class Structure(className: ClassName)
    : software.amazon.smithy.crt.codegen.Structure<Structure>(className)
    , BuilderClient<Structure>
    , JavaElement {
    open val javaBuilder: TypeSpec.Builder = TypeSpec.classBuilder(className)
    fun java(mod: (TypeSpec.Builder) -> Unit) : Structure {
        mod(javaBuilder)
        return this
    }

    override fun java() : TypeSpec.Builder {
        return javaBuilder
    }

    constructor(packageName: String, name: String)
        : this(ClassName.get(packageName, name))

    override fun build(): Structure {
        return build(javaBuilder, this)
    }

    override fun generate(dir: Path, consumer: Consumer<Path>) {
        consumer.accept(JavaFile.builder(className.packageName(), javaBuilder.build()).build().writeToPath(dir))
    }

    fun addDefaultConstructor() : Constructor {
        val ctor = Constructor(className)
            // this.$member = null|0|""
            .java { builder ->
                members.values.forEach {
                    builder.addStatement("this.${'$'}L = ${'$'}L", StringUtils.uncapitalize(it.name), Field.defaultValue(it.typeName))
                }
            }
        constructors.add(ctor)
        return ctor
    }

    companion object {
        fun addConstructors(builder: TypeSpec.Builder, src: AbstractStructure<*>) {
            src.constructors
                .map { (it as Constructor).build() }
                .forEach { ctor ->
                    Method.build(builder, ctor)
                }
        }

        fun addSetters(builder: TypeSpec.Builder, src: AbstractStructure<*>) {
            builder.addMethods(
                src.members.values.map {
                    // public void set$Name(T $name)
                    MethodSpec.methodBuilder("set" + StringUtils.capitalize(it.name))
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(ParameterSpec.builder(it.typeName, StringUtils.uncapitalize(it.name), Modifier.FINAL).build())
                        .addStatement(
                            "this.${'$'}L = ${'$'}L",
                            StringUtils.uncapitalize(it.name),
                            StringUtils.uncapitalize(it.name))
                        .build()
                })
        }

        fun addGetters(builder: TypeSpec.Builder, src: AbstractStructure<*>) {
            builder.addMethods(
                src.members.values.map {
                    // public T $name()
                    MethodSpec.methodBuilder(StringUtils.uncapitalize(it.name))
                        .addModifiers(Modifier.PUBLIC)
                        .addStatement("return ${'$'}L", StringUtils.uncapitalize(it.name))
                        .returns(it.typeName)
                        .build()
                })
        }

        fun objectHashCode(classBuilder: TypeSpec.Builder, src: AbstractStructure<*>) : MethodSpec.Builder {
            // @Override
            // public int hashCode()
            return MethodSpec.methodBuilder("hashCode")
                .addAnnotation(Override::class.javaObjectType)
                .addModifiers(Modifier.PUBLIC)
                    // TODO: add memberwise hash digest here
                .addStatement("return ${'$'}T.hash(${'$'}T.class)", ClassName.get(Objects::class.javaObjectType), src.className)
                .returns(TypeName.INT)
        }

        fun objectEquals(classBuilder: TypeSpec.Builder, src: AbstractStructure<*>) : MethodSpec.Builder {
            // @Override
            // public boolean equals(Object rhs)
            return MethodSpec.methodBuilder("equals")
                .addAnnotation(Override::class.javaObjectType)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ParameterSpec.builder(ClassName.get(Object::class.javaObjectType), "rhs").build())
                .addStatement("if (${'$'}L == null) return false", "rhs")
                    // TODO: add memberwise comparison here
                .addStatement("return (${'$'}L instanceof ${'$'}T)", "rhs", src.className)
                .returns(TypeName.BOOLEAN)
        }

        fun addObjectOverrides(classBuilder: TypeSpec.Builder, src: AbstractStructure<*>) {
            classBuilder.addMethods(listOf(
                objectHashCode(classBuilder, src),
                objectEquals(classBuilder, src)
            ).map { it.build() })
        }


        fun <T : AbstractStructure<*>> build(builder: TypeSpec.Builder, src: T) : T {
            Interface.build(builder, src)
            addConstructors(builder, src)
            if (src.superclass != null) {
                builder.superclass(src.superclass)
            }
            addObjectOverrides(builder, src)
            addGetters(builder, src)
            addSetters(builder, src)
            return src
        }
    }
}

open class Enum(className: ClassName, override var values: MutableMap<String, String?> = mutableMapOf())
    : Structure(className)
    , software.amazon.smithy.crt.codegen.Enum<Enum> {

    companion object {
        private const val UNKNOWN_VALUE = "UNKNOWN_TO_SDK_VERSION"
        fun toEnumName(s: String) : String {
            var ident = s.replace(":*", "")
            ident = ident.replace(':', '_')
            return CaseUtils.toSnakeCase(ident).toUpperCase()
        }
    }

    override val javaBuilder: TypeSpec.Builder = TypeSpec.enumBuilder(className)

    override fun build(): Enum {
        // private final String value
        val value = Field("value", TypeName.get(String::class.javaObjectType))
            .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
        addMember(value)

        val valueParam = Field("value", TypeName.get(String::class.javaObjectType))
        // private $enum(String value)
        addConstructor(
            Constructor(className, valueParam)
                .addModifier(Modifier.PRIVATE)
                .java { builder ->
                    builder.addStatement("this.${'$'}L = ${'$'}L", "value", "value")
                }
        )
        // enum constants + unknown value
        values.forEach{ (name, value) ->
            if (value != null) {
                javaBuilder.addEnumConstant(name, TypeSpec.anonymousClassBuilder("${'$'}S", valueParam).build())
            } else {
                javaBuilder.addEnumConstant(name)
            }
        }
        javaBuilder.addEnumConstant(UNKNOWN_VALUE, TypeSpec.anonymousClassBuilder("null").build())

        // public static T fromValue(String s)
        val fromValue = Method("fromValue", className, valueParam)
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .java { builder ->
                builder
                    .beginControlFlow("if (${'$'}L == null)", "value")
                    .addStatement("return null")
                    .endControlFlow()
                    .addStatement("return ${'$'}T.of(${'$'}L.values()).filter(e -> e.toString().equals(${'$'}L)).findFirst().orElse(${'$'}L)",
                        Stream::class.javaObjectType, className, "value", UNKNOWN_VALUE
                    )
            }
        addMethod(fromValue)

        // public static Set<T> knownValues()
        val knownValues = Method("knownValues", GenericType.get(ClassName.get(Set::class.javaObjectType), className))
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .java { builder ->
                builder.addStatement("return ${'$'}T.of(values()).filter(v -> v != ${'$'}L).collect(${'$'}T.toSet())",
                    Stream::class.javaObjectType, UNKNOWN_VALUE, Collectors::class.javaObjectType)
            }
        addMethod(knownValues)

        Interface.build(javaBuilder, this)
        addConstructors(javaBuilder, this)
        if (superclass != null) {
            javaBuilder.superclass(superclass)
        }
        addGetters(javaBuilder, this)
        addSetters(javaBuilder, this)

        return this
    }

    override fun generate(dir: Path, consumer: Consumer<Path>) {
        consumer.accept(JavaFile.builder(className.packageName(), javaBuilder.build()).build().writeToPath(dir))
    }
}

open class Exception(className: ClassName)
    : software.amazon.smithy.crt.codegen.Exception<Exception>(className)
    , BuilderClient<Exception>
    , JavaElement {
    private val javaBuilder: TypeSpec.Builder = TypeSpec.classBuilder(className)
    fun java(mod: (TypeSpec.Builder) -> Unit) : Exception {
        mod(javaBuilder)
        return this
    }

    override fun java() : TypeSpec.Builder {
        return javaBuilder
    }

    override fun build(): Exception {
        return Structure.build(javaBuilder, this)
    }

    override fun generate(dir: Path, consumer: Consumer<Path>) {
        consumer.accept(JavaFile.builder(className.packageName(), javaBuilder.build()).build().writeToPath(dir))
    }
}

open class Annotation(className: ClassName)
    : software.amazon.smithy.crt.codegen.Annotation<Annotation>(className)
    , JavaElement {
    private val javaBuilder: AnnotationSpec.Builder = AnnotationSpec.builder(className)
    fun java(mod: (AnnotationSpec.Builder) -> Unit) : Annotation {
        mod(javaBuilder)
        return this
    }

    override fun java() : AnnotationSpec.Builder {
        return javaBuilder
    }

    override fun build() : Annotation {
        modifiers.addAll(listOf(Modifier.PUBLIC, Modifier.ABSTRACT))
        return this
    }

    companion object {
        fun build(builder: TypeSpec.Builder, src: Annotation) : Annotation {
            builder.addAnnotation(src.java().build())
            return src
        }
    }
}

open class Union(className: ClassName)
    : Structure(className) {

    override fun build() : Union {
        JavaElement.addModifiers(javaBuilder, this)
        Interface.addDataMembers(javaBuilder, this)
        addConstructors(javaBuilder, this)
        addGetters(javaBuilder, this)
        addObjectOverrides(javaBuilder, this)
        // TODO Builder

        return this
    }
}

class List<T> : software.amazon.smithy.crt.codegen.List<T>() {
    override fun build(): List<T> {
        super.build()
        return this
    }
}

class Map<K, V> : software.amazon.smithy.crt.codegen.Map<K, V>() {
    override fun build() : Map<K, V> {
        super.build()
        return this
    }
}

open class Builder<T: BuilderClient<*>>(private val client: T) {
    // public interface Builder
    private val builderInterface = Interface(client.className.nestedClass("Builder"))
        .addModifier(Modifier.PUBLIC)

    // static final class BuilderImpl implements Builder
    private val builderImpl = Structure(client.className.nestedClass("BuilderImpl"))
        .addInterface(builderInterface)
        .addModifiers(Modifier.PROTECTED, Modifier.STATIC)

    fun build() : Builder<T> {
        if (client.superclass != null && client.superclass!!.packageName() != "java.lang") {
            builderInterface.addInterface(Interface(client.superclass!!.nestedClass("Builder")))
            builderImpl.superclass(client.superclass!!.nestedClass("BuilderImpl"))
        }

        builderImpl.addDefaultConstructor().addModifier(Modifier.PROTECTED)

        // private $Client(BuilderImpl builder)
        client.addConstructor(Constructor(client.className, Field("builder", builderImpl.className))
            .addModifier(Modifier.PROTECTED)
            .java { builder ->
                if (client.superclass != null && client.superclass!!.packageName() != "java.lang") {
                    builder.addStatement("super(builder)")
                }
                client.members.values.forEach { member ->
                    builder.addStatement("this.${'$'}L = builder.${'$'}L",
                        StringUtils.uncapitalize(member.name), StringUtils.uncapitalize(member.name))
                }
            }
        )

        // public Builder toBuilder()
        val toBuilder = Method("toBuilder", builderInterface.className)
            .addModifier(Modifier.PUBLIC)
            .java { builder ->
                builder.addStatement("return new ${'$'}T(this)", builderImpl.className)
            }
        if (client.superclass != null && client.superclass!!.packageName() != "java.lang") {
            toBuilder.addAnnotation(Annotation(ClassName.get(Override::class.javaObjectType)))
        }
        client.addMethod(toBuilder)

        // public static Builder builder()
        client.addMethod(Method("builder", builderInterface.className)
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .java { builder ->
                builder.addStatement("return new ${'$'}T()", builderImpl.className)
            }
        )

        // public Builder $member(T $member)
        client.members.values.forEach { member ->
            builderInterface.addMethod(
                Method(
                    StringUtils.uncapitalize(member.name),
                    builderInterface.className,
                    Field(StringUtils.uncapitalize(member.name), member.typeName)
                ).addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            )
        }

        builderImpl
            .addConstructor(Constructor(client.className, Field("model", client.className))
                .addModifier(Modifier.PRIVATE)
                .java { builder ->
                    client.members.values.forEach { member ->
                        builder.addStatement("${'$'}L(model.${'$'}L)",
                            StringUtils.uncapitalize(member.name), StringUtils.uncapitalize(member.name))
                    }
                }
            )
            .addMethod(Method("build", client.className)
                .addModifier(Modifier.PUBLIC)
                .java { builder ->
                    builder.addStatement("return new ${'$'}T(this)", client.className)
                }
            )
        client.members.values.forEach { member ->
            builderImpl.addMethod(
                Method(
                    StringUtils.uncapitalize(member.name),
                    builderInterface.className,
                    Field(StringUtils.uncapitalize(member.name), member.typeName)
                ).addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .java {
                        it.addStatement("this.${'$'}L = ${'$'}L",
                            StringUtils.uncapitalize(member.name), StringUtils.uncapitalize(member.name))
                        it.addStatement("return this")
                    }
            )
            builderImpl.addMember(member)
        }

        val builder = client.java() as TypeSpec.Builder
        builder.addType(builderInterface.build().java().build())
        builder.addType(builderImpl.build().java().build())

        return this
    }
}