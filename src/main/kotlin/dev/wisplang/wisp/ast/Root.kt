package dev.wisplang.wisp.ast

data class Root(
    val filename: String,
    val types: Map<String, DefinedType>,
    val globals: Map<String, DefinedVariable>,
    val functions: Map<String, DefinedFunction>
)
