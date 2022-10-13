package dev.wisplang.wispc.ast

import dev.wisplang.wispc.lexer.Expression

data class DefinedVariable(
    val name: String,
    val type: BaseType,
    val default: Expression? = null
)
