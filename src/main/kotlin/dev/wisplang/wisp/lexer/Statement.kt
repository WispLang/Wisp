package dev.wisplang.wisp.lexer

enum class Statement(val sym: String, val precedence: Int, val rightAssociative: Boolean = false) {
    And("&&", 0),
    Add("+", 1),
    Sub("-", 1),
    Mul("*", 2),
    Div("/", 2),
    Or("||", 3),
    Not("!", 4, true);

    companion object {
        private val STRINGS: List<String> = Statement.values().map { it.sym }

        fun of(value: String): Statement {
            for (op in Statement.values())
                if (op.sym == value)
                    return op
            throw IllegalStateException("Invalid operator: '$value'!")
        }

        operator fun contains(value: String) = value in STRINGS
    }
}
