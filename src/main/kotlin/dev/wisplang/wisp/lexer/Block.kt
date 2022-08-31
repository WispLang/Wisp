package dev.wisplang.wisp.lexer

data class Block(val statements: List<Statement>) {
    constructor(vararg statements: Statement) : this(statements.asList())
}
