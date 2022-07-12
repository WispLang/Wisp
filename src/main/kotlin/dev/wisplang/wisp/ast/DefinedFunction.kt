package dev.wisplang.wisp.ast

data class DefinedFunction(
    val name: String,
    val returnType: BaseType,
    val parameters: Map<String, BaseType>,
    val operators: Unit
)
