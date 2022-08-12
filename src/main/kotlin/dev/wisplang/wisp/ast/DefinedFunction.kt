package dev.wisplang.wisp.ast

import dev.wisplang.wisp.lexer.Block

data class DefinedFunction(
    val name: String,
    val returnType: BaseType,
    val parameters: List<DefinedVariable>,
    val body: Block
)
