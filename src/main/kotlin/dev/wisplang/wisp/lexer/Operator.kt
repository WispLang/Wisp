package dev.wisplang.wisp.lexer

/**
 * @param sym the symbol that identifies this operator
 */
enum class Operator(val sym: String) {
    AND("&&"),
    ADD("+"),
    SUB("-"),
    MUL("*"),
    DIV("/"),
    OR("||"),
    NOT("!"),
    EQ("=="),

    MOD("%"),
    GRE(">"),
    GE(">="),
    LOW("<"),
    LE("<="),
    NEQ("!="),
    AAD("++"),
    SSU("--");

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
