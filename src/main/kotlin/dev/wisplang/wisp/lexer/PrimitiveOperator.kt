package dev.wisplang.wisp.lexer

data class PrimitiveOperator(val value: Operator, val operator: PrimitiveOperator? = null) {
    constructor(value: String, operator: PrimitiveOperator? = null) : this(Operator.of(value), operator)
}
