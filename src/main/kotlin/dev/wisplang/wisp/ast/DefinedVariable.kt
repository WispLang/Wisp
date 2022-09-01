package dev.wisplang.wisp.ast

import dev.wisplang.wisp.lexer.Expression

data class DefinedVariable(
    val name: String,
    val type: BaseType,
    val default: Expression? = null
)
