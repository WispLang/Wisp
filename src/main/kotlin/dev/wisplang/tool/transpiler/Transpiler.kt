package dev.wisplang.tool.transpiler

import dev.wisplang.wispc.ast.*
import dev.wisplang.wispc.lexer.*
import dev.wisplang.wispc.util.append
import dev.wisplang.wispc.util.appendLine
import java.io.File


/*
SO:
 - 1: remove all possible duplicate code.
 - 2: transpilers SHOULD be able to work with multiple translation units.
 - 3: generify the base implementation as much as possible.
 */

abstract class Transpiler(protected val root: Root, protected val dir: File, protected val fileExt: String ) {
    /** source file -> destination file map */
    protected val files: MutableMap<String, File> = HashMap()
    protected val funcs: MutableMap<DefinedType?, MutableList<DefinedFunction>> = HashMap()
    protected var type: BaseType = VoidType.Void
    protected var methodType: DefinedType? = null

    fun transpile() {
        funcs[null] = ArrayList()

        for ((_, func) in root.functions) {
            if (func.parameters.isNotEmpty() && func.parameters[0].type is DefinedTypeRef && (func.parameters[0].type as DefinedTypeRef).name in root.types) {
                val type = root.types[(func.parameters[0].type as DefinedTypeRef).name]
                funcs.computeIfAbsent(type) { ArrayList() }
                funcs[type]!!.add(func)
            } else
                funcs[null]!!.add(func)
        }

        root.transpile()
    }

    protected abstract fun Root.transpile()

    protected abstract fun DefinedType.transpile(): File

    protected abstract fun DefinedVariable.transpile(): String

    protected abstract fun BaseType.transpile(): String

    protected fun Expression.transpile(convertBools: Boolean = false): String = when (this) {
        is BinaryExpression -> "${left.transpile()}${if (op != Operator.ACC) " ${op.sym} " else op.sym}${right.transpile()}"
        is CallExpression -> "${func.transpile()}(${params.transpile()})"
        is ConstructExpression -> "new ${name.transpile()}(${params.transpile()})"
        is GroupedExpression -> "( ${right.transpile()} )"
        is InverseUnaryExpression -> "${left.transpile()}${op.sym}"
        is LiteralExpression -> when {
            type == LiteralType.String -> "\"$value\""
            convertBools && value.toInt() in listOf(0, 1) -> "${value == "1"}" // handle booleans
            else -> value
        }
        is NamedExpression -> name.transpile()
        is UnaryExpression -> "${op.sym}${right.transpile()}"
    }

    protected fun Identifier.transpile(): String = "$name${if (selector != null) ".${selector.transpile()}" else ""}"

    protected fun List<Any>.transpile() = buildString {
        if (this@transpile.isNotEmpty()) {
            append(
                joinToString(", ", prefix = " ") {
                    when (it) {
                        is Expression -> it.transpile()
                        is DefinedVariable -> it.transpile()
                        else -> throw IllegalStateException()
                    }
                }
            )
            append(" ")
        }
    }

    protected abstract fun DefinedFunction.transpile(): String

    protected fun Block.transpile(indent: Int, inlineBrackets: Boolean = false): String =
        if (statements.isEmpty())
            "{  } "
        else if (inlineBrackets && statements.size == 1)
            "\n${"\t".repeat(indent)}${statements[0].transpile(indent)}\n${"\t".repeat(indent - 1)}"
        else buildString {
            appendLine("{")
            for (statement in statements)
                appendLine(indent, statement.transpile(indent))
            append(indent - 1, "} ")
        }

    protected fun Statement.transpile(indent: Int): String = when (this) {
        is AssignStatement -> "${name.transpile()} = ${expr.transpile()};"
        is DoWhileStatement -> "do ${body.transpile(indent + 1, true)}while ( ${condition.transpile(true)} );"
        is ElseStatement -> body.transpile(indent + 1, true)
        is ExpressionStatement -> "${expr.transpile()};"
        is ForStatement -> "for ( ${variable.transpile()}; ${condition.transpile()}; ${operation.transpile()} ) ${body.transpile(indent + 1,true)}"
        is IfStatement -> "if ( ${condition.transpile()} ) ${body.transpile(indent + 1,true)}${if (next != null) "else ${next!!.transpile(indent)}" else ""}"
        is ReturnStatement -> "return ${expr.transpile(type == PrimitiveTypes.U1)};"
        is VarDefStatement -> "${variable.transpile()};"
        is WhileStatement -> "while ( ${condition.transpile(true)} ) ${body.transpile(indent + 1, true)}"
    }
}
