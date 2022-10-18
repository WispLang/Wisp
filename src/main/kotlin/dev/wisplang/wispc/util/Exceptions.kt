package dev.wisplang.wispc.util

open class Error(open val message: String, open val idx: Int, open val line: Int, open val col: Int)
data class TokenizerError(override val message: String, override val idx: Int, override val line: Int, override val col: Int) : Error( message, idx, line, col )
data class LexerError(override val message: String, override val idx: Int, override val line: Int, override val col: Int) : Error( message, idx, line, col )
