package dev.wisplang.wisp.lexer

sealed class Expression

data class LiteralExpression( val value: String ) : Expression()

data class UnaryExpression( val op: Operator, val right: Expression ) : Expression()
data class InverseUnaryExpression( val left: Expression, val op: Operator ) : Expression()

data class BinaryExpression( val left: Expression, val op: Operator, val right: Expression ) : Expression()

data class GroupedExpression( val right: Expression ) : Expression()

data class AccessExpression( val name: Identifier ) : Expression()

data class CallExpression( val func: Expression, val params: ArrayList<Expression> ) : Expression()
data class ConstructExpression( val name: Identifier, val params: ArrayList<Expression> ) : Expression()

data class NamedExpression( val name: Identifier ) : Expression()
