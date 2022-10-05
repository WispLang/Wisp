package dev.wisplang.tool

import dev.wisplang.wisp.append
import dev.wisplang.wisp.appendLine
import dev.wisplang.wisp.ast.*
import dev.wisplang.wisp.div
import dev.wisplang.wisp.lexer.*
import java.io.File

class Transpiler(private val root: Root, private val dir: File) {
    private val files: MutableMap<String, File> = HashMap()
    private val funcs: MutableMap<DefinedType?, MutableList<DefinedFunction>> = HashMap()
    private var type: BaseType = VoidType.Void
    private var methodType: DefinedType? = null

    init {
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

    fun java() = root.java()

    private fun Root.java() {
        val name = File(filename).nameWithoutExtension.replaceFirstChar(Char::uppercase) + "Wsp"

        // contained classes go to own files
        for (type in types)
            files[type.key] = type.value.java()

        // globals and functions go into "${Filename}Wsp.java"
        files[filename] = dir / "$name.java"
        val string = buildString {
            appendLine("public class $name {")
            for (global in globals)
                appendLine("\tpublic static ${global.value.java()};")
            appendLine()
            for (method in funcs[null]!!) {
                this@Transpiler.type = method.returnType
                appendLine("\tpublic static ${method.java()}")
            }
            appendLine("}")
        }
        files[filename]!!.writeText(string)
    }

    private fun DefinedType.java(): File {
        this@Transpiler.methodType = this@java
        val string = buildString {
            appendLine("public class $name {")
            for (attr in variables)
                appendLine("\tpublic ${attr.java()};")
            appendLine()
            appendLine("\tpublic $name(${variables.java()}) {")
            appendLine(2, variables.joinToString("\n\t\t") { "this.${it.name} = ${it.name};" })
            appendLine("\t}")
            appendLine()
            for (method in funcs[this@java]!!) {
                appendLine("\tpublic ${method.java()}")
            }
            appendLine("}")
        }
        this@Transpiler.methodType = null
        val file = dir / "$name.java"
        file.writeText(string)
        return file
    }

    private fun DefinedVariable.java(): String {
        return "${type.java()} $name${if (default != null) " = ${default!!.java()}" else ""}"
    }

    private fun BaseType.java(): String {
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

    private fun Expression.java(convertBools: Boolean = false): String = when (this) {
        is BinaryExpression -> "${left.java()}${if (op != Operator.ACC) " ${op.sym} " else op.sym}${right.java()}"
        is CallExpression -> "${func.java()}(${params.java()})"
        is ConstructExpression -> "new ${name.java()}(${params.java()})"
        is GroupedExpression -> "( ${right.java()} )"
        is InverseUnaryExpression -> "${left.java()}${op.sym}"
        is LiteralExpression -> when {
            type == LiteralType.String -> "\"$value\""
            convertBools && value.toInt() in listOf(0, 1) -> "${value == "1"}" // handle booleans
            else -> value
        }
        is NamedExpression -> name.java()
        is UnaryExpression -> "${op.sym}${right.java()}"
    }

    private fun Identifier.java(): String = "$name${if (selector != null) ".${selector!!.java()}" else ""}"

    private fun List<Any>.java() = buildString {
        if (this@java.isNotEmpty()) {
            append(
                joinToString(", ", prefix = " ") {
                    when (it) {
                        is Expression -> it.java()
                        is DefinedVariable -> it.java()
                        else -> throw IllegalStateException()
                    }
                }
            )
            append(" ")
        }
    }

    private fun DefinedFunction.java(): String {
        var removed: DefinedVariable? = null
        var value = buildString {
            append("${returnType.java()} $name(")
            val params = ArrayList(parameters)
            if (params.isNotEmpty()) {
                if (params[0].type is DefinedTypeRef && (params[0].type as DefinedTypeRef).name == methodType?.name)
                    removed = params.removeAt(0)
                append(params.java())
            }
            append(") ${body.java(2)}\n")
        }

        if (removed != null)
            value = value.replace(parameters[0].name, "this")

        return value
    }

    private fun Block.java(indent: Int, inlineBrackets: Boolean = false): String =
        if (statements.isEmpty())
            "{  } "
        else if (inlineBrackets && statements.size == 1)
            "\n${"\t".repeat(indent)}${statements[0].java(indent)}\n${"\t".repeat(indent - 1)}"
        else buildString {
            appendLine("{")
            for (statement in statements)
                appendLine(indent, statement.java(indent))
            append(indent - 1, "} ")
        }

    private fun Statement.java(indent: Int): String = when (this) {
        is AssignStatement -> "${name.java()} = ${expr.java()};"
        is DoWhileStatement -> "do ${body.java(indent + 1, true)}while ( ${condition.java(true)} );"
        is ElseStatement -> body.java(indent + 1, true)
        is ExpressionStatement -> "${expr.java()};"
        is ForStatement -> "for ( ${variable.java()}; ${condition.java()}; ${operation.java()} ) ${body.java(indent + 1,true)}"
        is IfStatement -> "if ( ${condition.java()} ) ${body.java(indent + 1,true)}${if (next != null) "else ${next!!.java(indent)}" else ""}"
        is ReturnStatement -> "return ${expr.java(type == PrimitiveTypes.U1)};"
        is VarDefStatement -> "${variable.java()};"
        is WhileStatement -> "while ( ${condition.java(true)} ) ${body.java(indent + 1, true)}"
    }
}
