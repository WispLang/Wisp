package dev.wisplang.wisp.ast

interface BaseType {
    companion object {
        fun findType(name: String): BaseType {
            if (name.isEmpty())
                return VoidType.Void

            for (type in PrimitiveTypes.values())
                if (type.name.lowercase() == name)
                    return type

            return DefinedTypeRef(name)
        }
    }
}

enum class PrimitiveTypes : BaseType {
    U1,
    U8,  I8,
    U16, I16,
    U32, I32, F32,
    U64, I64, F64;
}
enum class VoidType : BaseType { Void }

data class DefinedTypeRef(val name: String) : BaseType
data class DefinedType( val name: String, val variables: List<DefinedVariable> )

