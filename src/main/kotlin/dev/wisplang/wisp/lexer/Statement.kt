package dev.wisplang.wisp.lexer

import dev.wisplang.wisp.ast.DefinedFunction
import dev.wisplang.wisp.ast.DefinedVariable
import dev.wisplang.wisp.ast.PrimitiveTypes

sealed class Statement

class ExpressionStatement(val expr: Expression) : Statement()

class VarDefStatement(val variable: DefinedVariable) : Statement()

class ReturnStatement(val expr: Expression) : Statement()

class WhileStatement(val condition: Expression, val body: Block) : Statement()
class DoWhileStatement(val condition: Expression, val body: Block) : Statement()

class AssignStatement(val id: Identifier, val parseExpression: Expression) : Statement()

class ForStatement(
    val variable: DefinedVariable,
    val condition: Expression,
    val operation: Expression,
    val body: Block,
) : Statement()

data class IfStatement(val condition: Expression, val body: Block, val next: Statement?) : Statement()
data class ElseStatement(val body: Block) : Statement()

@Suppress("unused")
private fun exampleAstUsage() {
    DefinedFunction(
        "testConditions",
        PrimitiveTypes.U1,
        listOf(DefinedVariable("number", PrimitiveTypes.I32)),
        Block(
            IfStatement(
                BinaryExpression(
                    NamedExpression(Identifier("number")),
                    Operator.EQ,
                    LiteralExpression("0")
                ),
                Block(ReturnStatement(LiteralExpression("1"))),
                IfStatement(
                    BinaryExpression(
                        NamedExpression(Identifier("number")),
                        Operator.EQ,
                        LiteralExpression("1")
                    ),
                    Block(ReturnStatement(LiteralExpression("1"))),
                    ElseStatement(Block(ReturnStatement(LiteralExpression("0"))))
                )
            )
        )
    )
}
