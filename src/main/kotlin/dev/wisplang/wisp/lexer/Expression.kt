package dev.wisplang.wisp.lexer

sealed class Expression

class LiteralExpression( val value: String ) : Expression()

class UnaryExpression( val right: Expression ) : Expression()

class BinaryExpression( val left: Expression, val op: Operator, right: Expression ) : Expression()

class GroupedExpression( val right: Expression ) : Expression()

class CallExpression( val name: String ) : Expression()

class NamedExpression( val name: String ) : Expression()
