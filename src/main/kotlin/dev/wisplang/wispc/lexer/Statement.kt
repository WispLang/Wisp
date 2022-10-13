package dev.wisplang.wispc.lexer

import dev.wisplang.wispc.ast.DefinedFunction
import dev.wisplang.wispc.ast.DefinedVariable
import dev.wisplang.wispc.ast.PrimitiveTypes

sealed class Statement

data class ExpressionStatement(val expr: Expression) : Statement()

data class VarDefStatement(val variable: DefinedVariable) : Statement()

data class ReturnStatement(val expr: Expression) : Statement()

data class WhileStatement(val condition: Expression, val body: Block) : Statement()
data class DoWhileStatement(val condition: Expression, val body: Block) : Statement()

data class AssignStatement(val name: Identifier, val expr: Expression) : Statement()

data class ForStatement(
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
                    LiteralExpression(LiteralType.Number, "0")
                ),
                Block(ReturnStatement(LiteralExpression(LiteralType.Number, "1"))),
                IfStatement(
                    BinaryExpression(
                        NamedExpression(Identifier("number")),
                        Operator.EQ,
                        LiteralExpression(LiteralType.Number, "1")
                    ),
                    Block(ReturnStatement(LiteralExpression(LiteralType.Number, "1"))),
                    ElseStatement(Block(ReturnStatement(LiteralExpression(LiteralType.Number, "0"))))
                )
            )
        )
    )
}
