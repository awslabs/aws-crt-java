package software.amazon.smithy.crt

import com.squareup.javapoet.ArrayTypeName
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import software.amazon.smithy.build.PluginContext
import software.amazon.smithy.model.shapes.*
import software.amazon.smithy.model.traits.EnumTrait
import software.amazon.smithy.utils.StringUtils

class TypeMap(val pluginContext: PluginContext) {

    companion object Utils {
        fun isEnum(shape: Shape): Boolean {
            return shape.isStringShape && shape.hasTrait(EnumTrait::class.javaObjectType)
        }

        var staticInstance: TypeMap? = null;
        fun instance() : TypeMap {
            return staticInstance!!;
        }

        fun toClassName(shape: Shape) : ClassName {
            return instance().toClassName(shape)
        }

        fun toTypeName(shape: Shape) : TypeName {
            return instance().toTypeName(shape)
        }
    }

    class TypeNameVisitor(private val typeMap: TypeMap) : ShapeVisitor<TypeName> {
        private fun javaNamespace(shape: Shape): String {
            return shape.id.namespace
        }

        override fun blobShape(shape: BlobShape): TypeName {
            return ArrayTypeName.of(TypeName.BYTE)
        }

        override fun booleanShape(shape: BooleanShape): TypeName {
            return ClassName.get(Boolean::class.javaObjectType)
        }

        override fun listShape(shape: ListShape): TypeName {
            return ParameterizedTypeName.get(
                ClassName.get(java.util.List::class.javaObjectType),
                shape.member.accept(this)
            )
        }

        override fun setShape(shape: SetShape): TypeName {
            return ParameterizedTypeName.get(
                ClassName.get(java.util.Set::class.javaObjectType),
                shape.member.accept(this)
            )
        }

        override fun mapShape(shape: MapShape): TypeName {
            return ParameterizedTypeName.get(
                ClassName.get(java.util.Map::class.javaObjectType),
                shape.key.accept(this),
                shape.value.accept(this)
            )
        }

        override fun byteShape(shape: ByteShape): TypeName {
            return ClassName.get(Byte::class.javaObjectType)
        }

        override fun shortShape(shape: ShortShape): TypeName {
            return ClassName.get(Short::class.javaObjectType)
        }

        override fun integerShape(shape: IntegerShape): TypeName {
            return ClassName.get(Integer::class.javaObjectType)
        }

        override fun longShape(shape: LongShape): TypeName {
            return ClassName.get(Long::class.javaObjectType)
        }

        override fun floatShape(shape: FloatShape): TypeName {
            return ClassName.get(Float::class.javaObjectType)
        }

        override fun documentShape(shape: DocumentShape): TypeName {
            return ParameterizedTypeName.get(
                ClassName.get(java.util.Map::class.javaObjectType),
                ClassName.get(String::class.javaObjectType),
                ClassName.get(Object::class.javaObjectType)
            )
        }

        override fun doubleShape(shape: DoubleShape): TypeName {
            return ClassName.get(Double::class.javaObjectType)
        }

        override fun bigIntegerShape(shape: BigIntegerShape): TypeName {
            return ClassName.get(java.math.BigInteger::class.javaObjectType)
        }

        override fun bigDecimalShape(shape: BigDecimalShape): TypeName {
            return ClassName.get(java.math.BigDecimal::class.javaObjectType)
        }

        override fun operationShape(shape: OperationShape): TypeName {
            TODO("Not yet implemented")
        }

        override fun resourceShape(shape: ResourceShape): TypeName {
            TODO("Not yet implemented")
        }

        override fun serviceShape(shape: ServiceShape): TypeName {
            TODO("Not yet implemented")
        }

        override fun stringShape(shape: StringShape): TypeName {
            if (shape.hasTrait(EnumTrait::class.javaObjectType)) {
                return ClassName.get(javaNamespace(shape), StringUtils.capitalize(shape.id.name))
            }
            return ClassName.get(java.lang.String::class.javaObjectType)
        }

        override fun structureShape(shape: StructureShape): TypeName {
            return ClassName.get(javaNamespace(shape), StringUtils.capitalize(shape.id.name))
        }

        override fun unionShape(shape: UnionShape): TypeName {
            return ClassName.get(javaNamespace(shape), StringUtils.capitalize(shape.id.name))
        }

        override fun memberShape(shape: MemberShape): TypeName {
            return typeMap.pluginContext.model.getShape(shape.target).get().accept(this)
        }

        override fun timestampShape(shape: TimestampShape): TypeName {
            return ClassName.get(java.time.Instant::class.javaObjectType)
        }

    }

    class ClassNameVisitor(private val typeNameVisitor: TypeNameVisitor): ShapeVisitor<ClassName> {
        override fun blobShape(shape: BlobShape): ClassName {
            return shape.accept(typeNameVisitor) as ClassName
        }

        override fun booleanShape(shape: BooleanShape): ClassName {
            return shape.accept(typeNameVisitor) as ClassName
        }

        override fun listShape(shape: ListShape): ClassName {
            return shape.accept(typeNameVisitor) as ClassName
        }

        override fun setShape(shape: SetShape): ClassName {
            return shape.accept(typeNameVisitor) as ClassName
        }

        override fun mapShape(shape: MapShape): ClassName {
            return shape.accept(typeNameVisitor) as ClassName
        }

        override fun byteShape(shape: ByteShape): ClassName {
            return shape.accept(typeNameVisitor) as ClassName
        }

        override fun shortShape(shape: ShortShape): ClassName {
            return shape.accept(typeNameVisitor) as ClassName
        }

        override fun integerShape(shape: IntegerShape): ClassName {
            return shape.accept(typeNameVisitor) as ClassName
        }

        override fun longShape(shape: LongShape): ClassName {
            return shape.accept(typeNameVisitor) as ClassName
        }

        override fun floatShape(shape: FloatShape): ClassName {
            return shape.accept(typeNameVisitor) as ClassName
        }

        override fun documentShape(shape: DocumentShape): ClassName {
            return shape.accept(typeNameVisitor) as ClassName
        }

        override fun doubleShape(shape: DoubleShape): ClassName {
            return shape.accept(typeNameVisitor) as ClassName
        }

        override fun bigIntegerShape(shape: BigIntegerShape): ClassName {
            return shape.accept(typeNameVisitor) as ClassName
        }

        override fun bigDecimalShape(shape: BigDecimalShape): ClassName {
            return shape.accept(typeNameVisitor) as ClassName
        }

        override fun operationShape(shape: OperationShape): ClassName {
            return shape.accept(typeNameVisitor) as ClassName
        }

        override fun resourceShape(shape: ResourceShape): ClassName {
            return shape.accept(typeNameVisitor) as ClassName
        }

        override fun serviceShape(shape: ServiceShape): ClassName {
            return shape.accept(typeNameVisitor) as ClassName
        }

        override fun stringShape(shape: StringShape): ClassName {
            return shape.accept(typeNameVisitor) as ClassName
        }

        override fun structureShape(shape: StructureShape): ClassName {
            return shape.accept(typeNameVisitor) as ClassName
        }

        override fun unionShape(shape: UnionShape): ClassName {
            return shape.accept(typeNameVisitor) as ClassName
        }

        override fun memberShape(shape: MemberShape): ClassName {
            return shape.accept(typeNameVisitor) as ClassName
        }

        override fun timestampShape(shape: TimestampShape): ClassName {
            return shape.accept(typeNameVisitor) as ClassName
        }

    }

    private val typeNameVisitor = TypeNameVisitor(this)
    private val classNameVisitor = ClassNameVisitor(typeNameVisitor)

    init {
        staticInstance = this
    }

    fun toClassName(shape: Shape): ClassName {
        return shape.accept(classNameVisitor)
    }

    fun toTypeName(shape: Shape): TypeName {
        return shape.accept(typeNameVisitor)
    }
}
