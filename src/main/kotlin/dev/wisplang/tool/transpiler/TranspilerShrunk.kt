//@formatter:off

@file:Suppress("DuplicatedCode")

package dev.wisplang.tool.transpiler

import dev.wisplang.wispc.ast.*
import dev.wisplang.wispc.lexer.*
import dev.wisplang.wispc.util.append
import dev.wisplang.wispc.util.appendLine
import dev.wisplang.wispc.util.div
import java.io.File

class TranspilerShrunk(private val root: Root, private val dir: File) {
    private val files: MutableMap<String, File> = HashMap()
    private val funcs: MutableMap<DefinedType?, MutableList<DefinedFunction>> = object : HashMap<DefinedType?, MutableList<DefinedFunction>>() { init { this[null] = ArrayList() } }
    private var type: BaseType = VoidType.Void
    private var methodType: DefinedType? = null

    fun java() = root.java()
    private fun Identifier.java(): String = "$name${if (selector != null) ".${selector.java()}" else ""}"
    private fun DefinedVariable.java() = "${type.java()} $name${if (default != null) " = ${default.java()}" else ""}"
    init { for ((_, func) in root.functions) if (func.parameters.isNotEmpty() && func.parameters[0].type is DefinedTypeRef && (func.parameters[0].type as DefinedTypeRef).name in root.types) funcs.computeIfAbsent(root.types[(func.parameters[0].type as DefinedTypeRef).name]) { ArrayList() }.add(func) else funcs[null]!!.add(func) }
    private fun Root.java() = ( file.nameWithoutExtension.replaceFirstChar(Char::uppercase) + "Wsp" ).let { print(it);files.apply { for (type in types) files[type.key] = type.value.java() }.computeIfAbsent( file.path ) { _ -> dir / "${it}.java" }.writeText( buildString { appendLine("public class $it {").apply { for (global in globals) appendLine("\tpublic static ${global.value.java()};") }.appendLine().apply { for (method in funcs[null]!!) this.apply { this@TranspilerShrunk.type = method.returnType }.appendLine("\tpublic static ${method.java()}") }.appendLine("}") }) }
    private fun DefinedType.java() = ( dir / "$name.java" ).apply { this@TranspilerShrunk.methodType = this@java }.apply { writeText( buildString { appendLine("public class ${this@java.name} {").apply { for (attr in variables) appendLine("\tpublic ${attr.java()};") }.appendLine().appendLine("\tpublic ${this@java.name}(${variables.java()}) {").appendLine(2, variables.joinToString("\n\t\t") { "this.${it.name} = ${it.name};" }).appendLine("\t}").appendLine().apply { for (method in funcs[this@java]!!) appendLine("\tpublic ${method.java()}") }.appendLine("}") } ) }.apply { this@TranspilerShrunk.methodType = null }
    private fun List<Any>.java() = buildString { if (this@java.isNotEmpty()) append(joinToString(", ", prefix = " ") { when (it) { is Expression -> it.java(); is DefinedVariable -> it.java() else -> throw IllegalStateException()}}).append(" ") }
    private fun DefinedFunction.java() = parameters.find { it.type is DefinedTypeRef && it.type.name == methodType?.name }.let { buildString { append("${returnType.java()} $name(").apply { if (parameters.isNotEmpty()) append( ArrayList(parameters).apply { remove( it ) }.java() ) }.append(") ${body.java(2)}\n") }.run { if (it != null) this.replace(it.name, "this") else this } }
    private fun Block.java(indent: Int, inlineBrackets: Boolean = false): String = if (statements.isEmpty()) "{  } " else if (inlineBrackets && statements.size == 1) "\n${"\t".repeat(indent)}${statements[0].java(indent)}\n${"\t".repeat(indent - 1)}" else buildString { appendLine("{").let { statements.map { appendLine( indent, it.java( indent ) ) }[0].append(indent - 1, "} ") } }
    private fun BaseType.java() = when (this) { is PrimitiveTypes -> { when (this) { PrimitiveTypes.U1 -> { "boolean" } PrimitiveTypes.U8, PrimitiveTypes.I8 -> { "byte" } PrimitiveTypes.U16, PrimitiveTypes.I16 -> { "short" } PrimitiveTypes.U32, PrimitiveTypes.I32 -> { "int" } PrimitiveTypes.U64, PrimitiveTypes.I64 -> { "long" } PrimitiveTypes.F32 -> { "float" } PrimitiveTypes.F64 -> { "double" } } } is VoidType -> { "void" } is DefinedTypeRef -> name }
    private fun Expression.java(convertBools: Boolean = false): String = when (this) { is BinaryExpression -> { "${left.java()}${if (op != Operator.ACC) " ${op.sym} " else op.sym}${right.java()}" } is CallExpression -> { "${func.java()}(${params.java()})" } is ConstructExpression -> { "new ${name.java()}(${params.java()})" } is GroupedExpression -> { "( ${right.java()} )" } is NamedExpression -> { name.java() } is UnaryExpression -> { "${op.sym}${right.java()}" } is InverseUnaryExpression -> { "${left.java()}${op.sym}" } is LiteralExpression -> { when { type == LiteralType.String -> "\"$value\"";convertBools && value.toInt() in listOf(0, 1) -> "${value == "1"}";else -> value } } }
    private fun Statement.java(indent: Int): String = when (this) { is AssignStatement -> { "${name.java()} = ${expr.java()};" } is ElseStatement -> { body.java(indent + 1, true) } is DoWhileStatement -> { "do ${body.java(indent + 1, true)}while ( ${condition.java(true)} );" } is ExpressionStatement -> { "${expr.java()};" } is VarDefStatement -> { "${variable.java()};" } is ForStatement -> { "for ( ${variable.java()}; ${condition.java()}; ${operation.java()} ) ${body.java(indent + 1,true)}" } is IfStatement -> { "if ( ${condition.java()} ) ${body.java(indent + 1,true)}${if (next != null) "else ${next.java(indent)}" else ""}" } is ReturnStatement -> { "return ${expr.java(type == PrimitiveTypes.U1)};" } is WhileStatement -> { "while ( ${condition.java(true)} ) ${body.java(indent + 1, true)}" } }
}
