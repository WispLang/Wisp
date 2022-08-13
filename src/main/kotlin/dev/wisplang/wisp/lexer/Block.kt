package dev.wisplang.wisp.lexer

class Block( val statements: List<Statement> ) {
    constructor( vararg statements: Statement ) : this( statements.asList() )
}
