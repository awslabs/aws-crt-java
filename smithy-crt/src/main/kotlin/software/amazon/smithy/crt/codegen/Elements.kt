package software.amazon.smithy.crt.codegen

import java.nio.file.Path
import java.util.function.Consumer

typealias ClassName = com.squareup.javapoet.ClassName
typealias TypeName = com.squareup.javapoet.TypeName
typealias Modifier = javax.lang.model.element.Modifier
typealias GenericType = com.squareup.javapoet.ParameterizedTypeName

interface AbstractElement<T> {
    val packageName: String
    val name: String
    val modifiers : MutableSet<Modifier>
    fun addModifier(mod: Modifier) : T
    fun addModifiers(vararg mods: Modifier) : T
    val annotations : MutableList<Annotation<*>>
    fun addAnnotation(a: Annotation<*>) : T
    fun addAnnotations(vararg annotations: Annotation<*>) : T
    var documentation : String?
    fun addDocumentation(doc: String) : T

    abstract fun build() : T
}

abstract class Element<T>(override val packageName: String, override val name: String)
    : AbstractElement<T>{
    override val modifiers = mutableSetOf<Modifier>()
    override fun addModifier(mod: Modifier) : T {
        modifiers.add(mod)
        @Suppress("UNCHECKED_CAST")
        return this as T
    }
    override fun addModifiers(vararg mods: Modifier) : T {
        modifiers.addAll(mods)
        @Suppress("UNCHECKED_CAST")
        return this as T
    }
    override val annotations = mutableListOf<Annotation<*>>()
    override fun addAnnotation(a : Annotation<*>) : T {
        annotations.add(a)
        @Suppress("UNCHECKED_CAST")
        return this as T
    }
    override fun addAnnotations(vararg annotations: Annotation<*>) : T {
        this.annotations.addAll(annotations)
        @Suppress("UNCHECKED_CAST")
        return this as T
    }
    override var documentation : String? = null
    override fun addDocumentation(doc: String) : T {
        documentation = doc
        @Suppress("UNCHECKED_CAST")
        return this as T
    }

    open fun generate(dir: Path, consumer: Consumer<Path>) {

    }
}

interface AbstractField<F> : AbstractElement<F>

open class Field(name: String, val typeName: TypeName)
    : Element<Field>("", name)
    , AbstractField<Field>{

    companion object {}

    override fun build() : Field {
        return this
    }
}

typealias Fields = Collection<Field>

interface AbstractMethod<M> : AbstractElement<M> {
    val returnType: TypeName
    val args: Array<out Field>
}

abstract class Method<M>(name: String, override val returnType: TypeName, override vararg val args: Field)
    : Element<M>("", name)
    , AbstractMethod<M>

abstract class Constructor<C>(val className : ClassName, vararg args: Field)
    : Method<C>(className.simpleName(), TypeName.VOID, *args)

interface AbstractEnum<E> {
    val values : MutableMap<String, String?>
    fun addValue(name: String, value: String?=null) : E
    fun addValues(vararg values: Pair<String, String?>) : E
}

interface Enum<E> : AbstractEnum<E> {
    override val values : MutableMap<String, String?>

    override fun addValue(name: String, value: String?) : E {
        values[name] = value
        @Suppress("UNCHECKED_CAST")
        return this as E
    }

    override fun addValues(vararg values: Pair<String, String?>) : E {
        values.forEach {
            this.values[it.first] = it.second
        }
        @Suppress("UNCHECKED_CAST")
        return this as E
    }
}

interface AbstractInterface<I> : AbstractElement<I> {
    val className : ClassName
    val interfaces : MutableSet<Interface<*>>
    fun addInterface(i: Interface<*>) : I
    val members : MutableMap<String, Field>
    fun addMember(field: Field) : I
    fun addMembers(vararg members: Field) : I {
        members.forEach { field ->
            addMember(field)
        }
        @Suppress("UNCHECKED_CAST")
        return this as I
    }
    val methods : MutableMap<String, Method<*>>
    fun addMethod(method: Method<*>) : I
}

abstract class Interface<I>(override val className: ClassName)
    : Element<I>(className.packageName(), className.simpleName())
    , AbstractInterface<I> {
    override val interfaces = mutableSetOf<Interface<*>>()
    override fun addInterface(i: Interface<*>) : I {
        interfaces.add(i)
        @Suppress("UNCHECKED_CAST")
        return this as I
    }
    override val members = mutableMapOf<String, Field>()
    override fun addMember(field: Field) : I {
        members[field.name] = field
        @Suppress("UNCHECKED_CAST")
        return this as I
    }
    override val methods = mutableMapOf<String, Method<*>>()
    override fun addMethod(method: Method<*>) : I {
        methods[method.name] = method
        @Suppress("UNCHECKED_CAST")
        return this as I
    }
}

interface AbstractStructure<S> : AbstractInterface<S> {
    var superclass : ClassName?
    fun superclass(sc: Structure<*>) : S { return superclass(sc.className) }
    fun superclass(sc: ClassName) : S
    val constructors : MutableList<Constructor<*>>
    fun addConstructor(ctor: Constructor<*>) : S
}

abstract class Structure<S>(className: ClassName)
    : Interface<S>(className)
    , AbstractStructure<S> {
    override var superclass : ClassName? = null
    override fun superclass(sc: ClassName) : S {
        superclass = sc;
        @Suppress("UNCHECKED_CAST")
        return this as S
    }
    override val constructors = mutableListOf<Constructor<*>>()
    override fun addConstructor(ctor: Constructor<*>) : S {
        constructors.add(ctor)
        @Suppress("UNCHECKED_CAST")
        return this as S
    }
}

interface AbstractException<E> : AbstractStructure<E>

abstract class Exception<E>(className: ClassName)
    : Structure<E>(className)
    , AbstractException<E> {

    init {
        modifiers.addAll(listOf(Modifier.PUBLIC))
    }
}

interface AbstractAnnotation<A> : AbstractStructure<A>

abstract class Annotation<A>(className: ClassName)
    : Structure<A>(className) {

    init {
        modifiers.addAll(listOf(Modifier.PUBLIC))
    }
}

open class List<T> : Element<List<T>>("", "List"){
    override fun build() : List<T> {
        return this
    }
}

open class Map<K, V> : Element<Map<K, V>>("", "Map") {
    override fun build() : Map<K, V> {
        return this
    }
}
