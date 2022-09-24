package dev.wisplang.transpiler

import dev.wisplang.wisp.ast.*
import dev.wisplang.wisp.div
import dev.wisplang.wisp.lexer.Expression
import java.io.File

class Transpiler( private val root: Root, private val dir: File ) {
    private val files: MutableMap<String, File> = HashMap()

    fun java() {
        root.java()
    }

    private fun Root.java() {
        var name = File( filename ).nameWithoutExtension
        name = name[0].uppercase() + name.substring( 1 .. name.length - 1 ) + "Wsp"

        // contained classes go to own files
        for ( type in types )
            files[ type.key ] = type.value.java()

        // globals and functions go into "${Filename}Wsp.java"
        files[ filename ] = dir / "$name.java"
        val string = buildString {
            appendLine( "public class $name {" )
            for ( global in globals )
                appendLine( "\t" + global.value.java( true ) )
            appendLine( "}" )
        }
        files[ filename ]!!.writeText( string )
    }

    private fun DefinedType.java(): File {
        val string = buildString {
            appendLine( "public class $name {" )
            for ( attr in variables )
                appendLine( "\t${attr.java( true )}" )
            appendLine("}")
        }
        val file = dir / "$name.java"
        file.writeText( string )
        return file
    }

    private fun DefinedVariable.java( clazzDef: Boolean ): String {
        return "${ if( clazzDef ) "public " else "" }${type.java()} $name${ if( default != null ) " = ${default!!.java()}" else "" };"
    }

    private fun BaseType.java(): String {
        return when ( this ) {
            is PrimitiveTypes -> when ( this ) {
                PrimitiveTypes.U1 -> "boolean"
                PrimitiveTypes.U8,  PrimitiveTypes.I8 -> "byte"
                PrimitiveTypes.U16, PrimitiveTypes.I16 -> "short"
                PrimitiveTypes.U32, PrimitiveTypes.I32 -> "int"
                PrimitiveTypes.U64, PrimitiveTypes.I64 -> "long"
                PrimitiveTypes.F32 -> "float"
                PrimitiveTypes.F64 -> "double"
            }
            is VoidType -> "void"
            is DefinedTypeRef -> name
            else -> throw IllegalStateException("Invalid type $this!")
        }
    }

    private fun Expression.java(): String = "default"
}
