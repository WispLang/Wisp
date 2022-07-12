package dev.wisplang.wisp.ast

data class DefinedFunction(
    val name: String,
    val returnType: BaseType,
    val parameters: List<DefinedVariable>,
    val operators: Unit
)
