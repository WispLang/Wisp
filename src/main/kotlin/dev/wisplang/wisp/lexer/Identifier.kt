package dev.wisplang.wisp.lexer

data class Identifier(val name: String, val selector: Identifier? = null)
