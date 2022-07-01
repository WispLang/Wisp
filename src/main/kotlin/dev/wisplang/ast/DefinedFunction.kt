package dev.wisplang.ast

data class DefinedFunction(
    val returnType: List<BasicType>,
    val parameters: Map<String, BasicType>,
    val operators: Unit
)