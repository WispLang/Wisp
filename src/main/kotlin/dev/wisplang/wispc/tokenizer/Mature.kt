package dev.wisplang.wispc.tokenizer

data class MatureToken(val type: MatureType, val value: String, val idx: Int, val len: Int, val line: Int, val col: Int) {
    override fun toString() = when ( type ) {
        MatureType.FLOAT, MatureType.INTEGER -> "${type.name.lowercase()} value $value"
        MatureType.STRING -> "$type value '$value'"
        else -> "$type $value"
    }
}

enum class MatureType {
    SYMBOL,
    KEYWORD,
    INTEGER,
    FLOAT,
    STRING,
    NAME,
    NEWLINE,
    PRIMITIVE,
    EOF
}
