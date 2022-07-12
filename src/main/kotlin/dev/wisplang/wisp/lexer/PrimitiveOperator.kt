package dev.wisplang.wisp.lexer

data class PrimitiveOperator(val value: Statement, val operator: PrimitiveOperator? = null) {
    constructor(value: String, operator: PrimitiveOperator? = null) : this(Statement.of(value), operator)
}
