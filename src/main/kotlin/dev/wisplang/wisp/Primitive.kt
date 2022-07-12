package dev.wisplang.wisp

enum class Primitive( type: BaseType, size: Short ) {
    U1( BaseType.Unsigned, 1 ),
    U8( BaseType.Unsigned, 8 ),
    I8( BaseType.Integer, 8 ),
    U16( BaseType.Unsigned, 16 ),
    I16( BaseType.Integer, 16 ),
    U32( BaseType.Unsigned, 32 ),
    I32( BaseType.Integer, 32 ),
    F32( BaseType.Float, 32 ),
    U64( BaseType.Unsigned, 64 ),
    I64( BaseType.Integer, 64 ),
    F64( BaseType.Float, 64 );

    override fun toString() = this.name.lowercase()

    companion object {
        private val STRINGS: List<String> = Tokenizer.Keywords.values().map { it.toString() }

        fun strings() = STRINGS
    }

    enum class BaseType( code: String ) {
        Unsigned( "u" ),
        Integer( "i" ),
        Float( "f" )
    }
}