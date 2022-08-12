package dev.wisplang.wisp.lexer

/**
 * @param sym the symbol that identifies this operator
 * @param precedence the precedence of this operator on others
 * @param rightAssociative whether this operator only accepts a single value on the right (unary operator)
 */
enum class Operator(val sym: String, val precedence: Int, val rightAssociative: Boolean = false) {
    AND("&&", 0),
    ADD("+", 1),
    SUB("-", 1),
    MUL("*", 2),
    DIV("/", 2),
    OR("||", 3),
    NOT("!", 4, true),
    EQ("==", 0); // FIXME: precedence is a random number rn

    companion object {
        private val STRINGS: List<String> = Operator.values().map { it.sym }

        fun of(value: String): Operator {
            for (op in Operator.values())
                if (op.sym == value)
                    return op
            throw IllegalStateException("Invalid operator: '$value'!")
        }

        operator fun contains(value: String) = value in STRINGS
    }
}
