package dev.wisplang.wisp.tokenizer;

enum class Keywords {
    DO, ELSE,
    ENUM, EXT,
    FOR, FUNC,
    IF, IMP,
    IN, IS,
    MATCH, PRIM,
    TYPE, VAR,
    WHILE;

    companion object {
        private val STRINGS: List<String> = Keywords.values().map { it.name.lowercase() }

        fun strings() = STRINGS
    }
}
