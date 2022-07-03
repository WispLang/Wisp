package dev.wisplang.ast

interface BasicType

enum class PrimitiveTypes : BasicType {
    U1,
    U8, I8,
    U16, I16,
    U32, I32, F32,
    U64, I64, F64
}

data class DefinedTypeRef(
    val name: String
): BasicType

data class DefinedType(
    val variables: DefinedVariable
)