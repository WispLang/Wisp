package dev.wisplang.wisp.ast

import dev.wisplang.wisp.lexer.PrimitiveOperator

object Ast {
    data class Root(
        val types: Map<String, DefinedType>,
        val globals: Map<String, DefinedVariable>,
        val functions: Map<String, DefinedFunction>
    )

    data class DefinedFunction(
        val name: String,
        val returnType: BaseType,
        val parameters: Map<String, BaseType>,
        val operators: Unit
    )

    data class DefinedVariable(
        val name: String,
        val type: BaseType,
        val default: ArrayList<PrimitiveOperator>?
    )
}
