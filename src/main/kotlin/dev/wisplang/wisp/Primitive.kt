package dev.wisplang.wisp

import dev.wisplang.wisp.tokenizer.Tokenizer

enum class Primitive( type: BaseType, size: Short ) {
    U1( BaseType.UNSIGNED, 1 ),
    U8( BaseType.UNSIGNED, 8 ),
    I8( BaseType.INTEGER, 8 ),
    U16( BaseType.UNSIGNED, 16 ),
    I16( BaseType.INTEGER, 16 ),
    U32( BaseType.UNSIGNED, 32 ),
    I32( BaseType.INTEGER, 32 ),
    F32( BaseType.FLOAT, 32 ),
    U64( BaseType.UNSIGNED, 64 ),
    I64( BaseType.INTEGER, 64 ),
    F64( BaseType.FLOAT, 64 );

    override fun toString() = this.name.lowercase()

    companion object {
        private val STRINGS: List<String> = Tokenizer.Keywords.values().map { it.toString() }

        fun strings() = STRINGS
    }

    enum class BaseType( code: String ) {
        UNSIGNED( "u" ),
        INTEGER( "i" ),
        FLOAT( "f" )
    }
}
