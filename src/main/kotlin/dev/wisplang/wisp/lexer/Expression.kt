package dev.wisplang.wisp.lexer

sealed class Expression

class LiteralExpression( val value: String ) : Expression()

class UnaryExpression( val op: Operator, val right: Expression ) : Expression()
class InverseUnaryExpression( val left: Expression, val op: Operator ) : Expression()

class BinaryExpression( val left: Expression, val op: Operator, val right: Expression ) : Expression()

class GroupedExpression( val right: Expression ) : Expression()

class CallExpression( val name: Identifier, val params: ArrayList<String> ) : Expression()
class ConstructExpression( val name: Identifier, val params: ArrayList<String> ) : Expression()

class NamedExpression( val name: Identifier ) : Expression()
