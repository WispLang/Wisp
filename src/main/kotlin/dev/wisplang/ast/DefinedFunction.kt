package dev.wisplang.ast

data class DefinedFunction(
    val returnType: BasicType?,
    val parameters: Map<String, BasicType>,
    val operators: Unit
)