package dev.wisplang.wispc.lexer

data class Identifier(val name: String, val selector: Identifier? = null)
