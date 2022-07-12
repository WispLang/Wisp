package dev.wisplang.wisp.ast

import dev.wisplang.wisp.lexer.PrimitiveOperator

data class DefinedVariable(
    val name: String,
    val type: BaseType,
    val default: ArrayList<PrimitiveOperator>?
)
