package dev.wisplang.wispc.tokenizer

import dev.wisplang.wispc.util.TokenizerError
import dev.wisplang.wispc.util.contains

object Tokenizer {
    private val keywords = listOf( "do", "else", "enum", "ext", "for", "func", "if", "imp", "in", "is", "match", "prim", "type", "var", "while" )
    private val primitives = listOf( "u1", "u8", "i8", "u16", "i16", "u32", "i32", "f32", "u64", "i64", "f64" )
    private val symbolRegex = Regex("[{}()<>,.'\"\\[\\]|\\\\+\\-=*/&:!]")
    private val letterRegex = Regex("[a-zA-Z_$]")

    fun tokenize(code: String): Pair<List<Token>, List<TokenizerError>> {
        var i = 0
        var line = 1
        var col = 1
        var startPos = 0

        val tokens = mutableListOf<Token>()
        val errors = mutableListOf<TokenizerError>()

        fun token(type: Type, value: String, pos: Int = i, length: Int = value.length ) {
            tokens.add( Token( type, value, pos, length, line, col ) )
            if ( type == Type.NEWLINE ) {
                line++
                col = 1
            } else
                col += length
        }
        fun error( message: String ) = errors.add( TokenizerError( message, startPos, line, col ) )

        while (i < code.length) {
            val char = code[i]
            startPos = i

            when {
                char == ' ' -> col++

                char == '/' && code[i + 1] == '/' -> i = code.indexOf( '\n', i ) - 1

                char == '/' && code[i + 1] == '*' -> {
                    i = code.indexOf( "*/", i ) + 1
                    if ( i == -1 ) {
                        error("Reached end of file while looking for end of multiline comment!")
                        continue
                    }
                    val lines = code.substring( startPos, i ).lines()
                    line += lines.size
                    col = lines.last().length + 2
                }

                char in letterRegex -> {
                    var string = ""

                    while ( i < code.length && ( code[i] in letterRegex || code[i].isDigit() ) )
                        string += code[i++]
                    i--

                    token( Type.NAME, string )
                    col += string.length - 1
                }

                char == '"' -> {
                    var string = ""
                    i++

                    while ( i < code.length && code[i] != '"' ) {
                        when ( val character = code[i] ) {
                            '\\' -> string += when ( val nextChar = code[++i] ) {
                                '"' -> '"'
                                'n' -> "\n"
                                else -> "\\$nextChar"
                            }
                            else -> string += character
                        }
                        i++
                    }

                    token( Type.STRING, string )
                    col += i - startPos
                }

                char.isDigit() -> {
                    var numberString = ""
                    var decimal = false
                    var errored = false

                    while ( code[i].isDigit() || code[i] in "._" ) {
                        if ( errored || code[i] == '_' ) {
                            i++
                            continue
                        }

                        if ( code[i] == '.' ) {
                            if (decimal) {
                                error("Cannot have multiple decimal points in a number!")
                                errored = true
                            }
                            decimal = true
                        }

                        numberString += code[ i++ ]
                    }
                    i--

                    token( Type.NUMBER, numberString )
                    col += i - startPos
                }

                char in symbolRegex -> token( Type.SYMBOL, char.toString() )
                char == '\n' -> token( Type.NEWLINE, "\n" )
            }
            i++
        }

        return tokens to errors
    }

    fun matureTokens(tokens: List<Token>): List<MatureToken> {
        val matureTokens = ArrayList<MatureToken>()
        var i = 0

        fun token(type: MatureType, tokn: Token, value: String = tokn.value, idx: Int = tokn.idx) =
            matureTokens.add( MatureToken( type, value, idx, value.length, tokn.line, tokn.col ) )

        do {
            val token = tokens[i]
            when (token.type) {
                Type.SYMBOL -> {
                    when (token.value) {
                        "-" -> {
                            when (tokens[++i].value) {
                                "-" -> token(MatureType.SYMBOL, token, "--")
                                ">" -> token(MatureType.SYMBOL, token, "->")
                                "=" -> token(MatureType.SYMBOL, token, "-=")
                                else -> {
                                    i--
                                    token(MatureType.SYMBOL, token)
                                }
                            }
                        }

                        "+" -> {
                            when (tokens[++i].value) {
                                "+" -> token(MatureType.SYMBOL, token, "++")
                                "=" -> token(MatureType.SYMBOL, token, "+=")
                                else -> {
                                    i--
                                    token(MatureType.SYMBOL, token)
                                }
                            }
                        }

                        "=" -> {
                            when (tokens[++i].value) {
                                "=" -> token(MatureType.SYMBOL, token, "==")
                                else -> {
                                    i--
                                    token(MatureType.SYMBOL, token)
                                }
                            }
                        }

                        "<" -> {
                            when (tokens[++i].value) {
                                "<" -> token(MatureType.SYMBOL, token, "<<")
                                "=" -> token(MatureType.SYMBOL, token, "<=")
                                else -> {
                                    i--
                                    token(MatureType.SYMBOL, token)
                                }
                            }
                        }

                        ">" -> {
                            when (tokens[++i].value) {
                                ">" -> token(MatureType.SYMBOL, token, ">>")
                                "=" -> token(MatureType.SYMBOL, token, ">=")
                                else -> {
                                    i--
                                    token(MatureType.SYMBOL, token)
                                }
                            }
                        }

                        "!" -> {
                            when (tokens[++i].value) {
                                "=" -> token(MatureType.SYMBOL, token, "!=")
                                else -> {
                                    i--
                                    token(MatureType.SYMBOL, token)
                                }
                            }
                        }

                        "&" -> {
                            when (tokens[++i].value) {
                                "&" -> token(MatureType.SYMBOL, token, "&&")
                                else -> {
                                    i--
                                    token(MatureType.SYMBOL, token)
                                }
                            }
                        }

                        "|" -> {
                            when (tokens[++i].value) {
                                "|" -> token(MatureType.SYMBOL, token, "||")
                                else -> {
                                    i--
                                    token(MatureType.SYMBOL, token)
                                }
                            }
                        }

                        else -> token(MatureType.SYMBOL, token)
                    }
                }

                Type.NEWLINE -> token(MatureType.NEWLINE, token)
                Type.STRING -> token(MatureType.STRING, token)
                Type.NUMBER -> token(
                    if (token.value.contains("."))
                        MatureType.FLOAT
                    else
                        MatureType.INTEGER,
                    token
                )

                Type.NAME -> token(
                    when (token.value) {
                        in keywords -> MatureType.KEYWORD
                        in primitives -> MatureType.PRIMITIVE
                        else -> MatureType.NAME
                    },
                    token
                )
            }
        } while (++i < tokens.size)

        matureTokens.add(MatureToken(MatureType.EOF, "", tokens.size, 0, tokens.last().line + 1, 0))

        return matureTokens
    }
}
