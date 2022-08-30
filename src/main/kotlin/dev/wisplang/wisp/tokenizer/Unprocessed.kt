package dev.wisplang.wisp.tokenizer

data class Token( val type: Type, val value: String, val idx: Int, val len: Int, val line: Int, val col: Int )

enum class Type {
    SYMBOL,
    NUMBER,
    STRING,
    NAME,
    NEWLINE
}
