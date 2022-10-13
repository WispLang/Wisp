package dev.wisplang.tool

import dev.wisplang.wispc.appendLine
import dev.wisplang.wispc.ast.DefinedFunction
import dev.wisplang.wispc.ast.DefinedType
import dev.wisplang.wispc.ast.DefinedVariable
import dev.wisplang.wispc.ast.Root
import dev.wisplang.wispc.div
import java.io.File

class AstPrinter(private val root: Root, val pretty: Boolean = false) {

    fun save( dir: File ) = ( dir / "${root.file.name.removeSuffix("wsp")}ast" ).writeText( root.prettyString() )

    fun print() = print( root.prettyString() )

    private fun Root.prettyString(): String = buildString {
        appendLine("Root(")

        appendLine( 1, "types={" )
        for ( ( name, type ) in types )
            appendLine( 2, "$name=${type.prettyPrint()}" )
        appendLine( 1, "}," )

        appendLine( 1, "globals={" )
        for ( ( name, global ) in globals )
            appendLine( 2, "$name=${global.prettyPrint()}" )
        appendLine( 1, "}," )

        appendLine( 1, "functions={" )
        for ( ( name, function ) in functions )
            appendLine( 2, "$name=${function.prettyPrint()}" )
        appendLine( 1, "}" )
        append( "}" )
    }

    private fun DefinedType.prettyPrint(): String  = ""

    private fun DefinedVariable.prettyPrint(): String  = ""

    private fun DefinedFunction.prettyPrint(): String = ""
}
