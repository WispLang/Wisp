package dev.wisplang

import dev.wisplang.ast.DefinedFunction
import dev.wisplang.ast.DefinedType
import dev.wisplang.ast.DefinedVariable

object Lexer {
    fun lex(tokens: List<Tokenizer.MatureToken>) {
        var i = 0
        do {
            val token = tokens[i]
            when {

            }
        } while (++i < tokens.size)
    }

    fun parseFunction(idx: Int, tokens: List<Tokenizer.MatureToken>): Triple<DefinedFunction?, Int, String?> {
        var i = idx
        do {

        } while (++i < tokens.size)
        return Triple(null, i, null)
    }
    fun parseVariable(idx: Int, tokens: List<Tokenizer.MatureToken>): Triple<DefinedVariable?, Int, String?> {
        var i = idx
        do {

        } while (++i < tokens.size)
        return Triple(null, i, null)
    }
    fun parseType(idx: Int, tokens: List<Tokenizer.MatureToken>): Triple<DefinedType? ,Int, String?> {
        var i = idx
        do {

        } while (++i < tokens.size)
        return Triple(null, i, null)
    }
}