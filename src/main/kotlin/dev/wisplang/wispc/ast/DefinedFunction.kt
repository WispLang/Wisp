package dev.wisplang.wispc.ast

import dev.wisplang.wispc.lexer.Block

data class DefinedFunction(
    val name: String,
    val returnType: BaseType,
    val parameters: List<DefinedVariable>,
    val body: Block
)
