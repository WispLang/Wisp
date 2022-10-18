package dev.wisplang.tool.transpiler

import dev.wisplang.wispc.ast.*
import dev.wisplang.wispc.util.appendLine
import dev.wisplang.wispc.util.div
import java.io.File

class JavaTranspiler( root: Root, dir: File ) : Transpiler(root, dir.apply { mkdirs() }, ".java") {
    override fun Root.transpile() {
        var name = file.nameWithoutExtension
        name = name[0].uppercase() + name.substring(1 until name.length) + "Wsp"

        // contained classes go to own files
        for (type in types)
            files[type.key] = type.value.transpile()

        // globals and functions go into "${Filename}Wsp${ext}"
        files[file.path] = dir / "$name$fileExt"
        val string = buildString {
            appendLine("public class $name {")
            for (global in globals)
                appendLine("\tpublic static ${global.value.transpile()};")
            appendLine()
            for (method in funcs[null]!!) {
                this@JavaTranspiler.type = method.returnType
                appendLine("\tpublic static ${method.transpile()}")
            }
            appendLine("}")
        }
        files[file.path]!!.writeText(string)
    }

    override fun DefinedType.transpile(): File {
        super.methodType = this@transpile
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
        super.methodType = null
        val file = dir / "$name$fileExt"
        file.writeText(string)
        return file
    }

    override fun DefinedVariable.transpile() =
        "${type.transpile()} $name${if (default != null) " = ${default.transpile()}" else ""}"

    override fun BaseType.transpile(): String {
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

    override fun DefinedFunction.transpile(): String {
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
}
