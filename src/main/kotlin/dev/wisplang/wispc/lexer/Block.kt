package dev.wisplang.wispc.lexer

data class Block(val statements: List<Statement>) {
    constructor(vararg statements: Statement) : this(statements.asList())
}
