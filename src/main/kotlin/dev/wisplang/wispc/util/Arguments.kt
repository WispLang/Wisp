package dev.wisplang.wispc.util

import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.ParsingException
import java.io.File

object Arguments {
    private val parser = ArgParser("wispc")

    val file by parser.argument(
        FileArgType( listOf( ".wsp" ), true ),
        "file",
        "Input file, everything imported will be relative to its directory.",
    )

    val dumpTokens by parser.option(
        ArgType.Boolean,
        "dump-tokens",
        description = "Dumps the tokens generated from a file to \$FILENAME.tks"
    )
    val dumpAst by parser.option(
        ArgType.Boolean,
        "dump-ast",
        description = "Dumps the ast generated from a file to \$FILENAME.ast"
    )
    val prettyPrint by parser.option(
        ArgType.Boolean,
        "pretty",
        description = "Make --dump-ast print on multiple lines and indentation levels."
    )

    val transpilationTarget by parser.option(
        ArgType.Choice( TranspilationTarget.values().toList(), TranspilationTarget::valueOf ),
        "transpilation-target",
        description = "Transpile to the given language."
    )

    fun parse( argv: Array<String> ) = parser.parse( argv )
}

enum class TranspilationTarget {
    Java,
    Kotlin,
    Python
}

/**
 * Argument type for file paths.
 */
class FileArgType( folder: kotlin.Boolean, exts: List<kotlin.String> = listOf(), private val mustExist: kotlin.Boolean = false ): ArgType<File>(true) {
    constructor( exts: List<kotlin.String>, mustExist: kotlin.Boolean = false ) : this( false, exts, mustExist )

    private val desc = if ( folder ) "folder" else "file${ if ( exts.isNotEmpty() ) " ending with $exts" else "" }"

    override val description = "{ Path to $desc }"

    override fun convert( value: kotlin.String, name: kotlin.String ) = File( value ).also {
        if ( mustExist && !it.exists() )
            throw ParsingException( "Option $name is expected to be an existing $desc. $value is provided." )
    }
}
