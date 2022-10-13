package tool.transpiler

import dev.wisplang.wispc.append
import dev.wisplang.wispc.appendLine
import dev.wisplang.wispc.ast.*
import dev.wisplang.wispc.div
import dev.wisplang.wispc.lexer.*
import java.io.File

abstract class Transpiler( protected val root: Root, protected val dir: File ) {
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
    }

    fun java() = root.transpile()

    protected abstract fun Root.transpile()

    private fun DefinedType.transpile(): File {
        this@Transpiler.methodType = this@transpile
        val string = buildString {
            appendLine("public class $name {")
            for (attr in variables)
                appendLine("\tpublic ${attr.transpile()};")
            appendLine()
            appendLine("\tpublic $name(${variables.transpile()}) {")
            appendLine(2, variables.joinToString("\n\t\t") { "this.${it.name} = ${it.name};" })
            appendLine("\t}")
            appendLine()
            for (method in funcs[this@transpile]!!) {
                appendLine("\tpublic ${method.transpile()}")
            }
            appendLine("}")
        }
        this@Transpiler.methodType = null
        val file = dir / "$name.java"
        file.writeText(string)
        return file
    }

    private fun DefinedVariable.transpile(): String {
        return "${type.transpile()} $name${if (default != null) " = ${default!!.transpile()}" else ""}"
    }

    private fun BaseType.transpile(): String {
        return when (this) {
            is PrimitiveTypes -> when (this) {
                PrimitiveTypes.U1 -> "boolean"
                PrimitiveTypes.U8, PrimitiveTypes.I8 -> "byte"
                PrimitiveTypes.U16, PrimitiveTypes.I16 -> "short"
                PrimitiveTypes.U32, PrimitiveTypes.I32 -> "int"
                PrimitiveTypes.U64, PrimitiveTypes.I64 -> "long"
                PrimitiveTypes.F32 -> "float"
                PrimitiveTypes.F64 -> "double"
            }
            is VoidType -> "void"
            is DefinedTypeRef -> name
        }
    }

    private fun Expression.transpile(convertBools: Boolean = false): String = when (this) {
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

    private fun Identifier.transpile(): String = "$name${if (selector != null) ".${selector!!.transpile()}" else ""}"

    private fun List<Any>.transpile() = buildString {
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

    private fun DefinedFunction.transpile(): String {
        var removed: DefinedVariable? = null
        var value = buildString {
            append("${returnType.transpile()} $name(")
            val params = ArrayList(parameters)
            if (params.isNotEmpty()) {
                if (params[0].type is DefinedTypeRef && (params[0].type as DefinedTypeRef).name == methodType?.name)
                    removed = params.removeAt(0)
                append(params.transpile())
            }
            append(") ${body.transpile(2)}\n")
        }

        if (removed != null)
            value = value.replace(parameters[0].name, "this")

        return value
    }

    private fun Block.transpile(indent: Int, inlineBrackets: Boolean = false): String =
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

    private fun Statement.transpile(indent: Int): String = when (this) {
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