package dev.wisplang.wisp.tokenizer

import dev.wisplang.wisp.Primitive
import dev.wisplang.wisp.TokenizerException

object Tokenizer {
    private val letterRegex = Regex("[a-zA-Z_$]")
    private val symbolRegex = Regex("[{}()<>,.'\"\\[\\]|\\\\+\\-=*/&:]")

    fun tokenize(value: String): ArrayList<Token> {
        val tokenArray = arrayListOf<Token>()

        var i = 0
        while (i < value.length) {
            val character = value[i]
            val startPos = i
            when {
                character == '/' && value[i + 1] == '/' -> {
                    i = value.indexOf("\n", i)
                    tokenArray.add(Token(Type.NEWLINE, "\n", i, 1))
                }
                character == '/' && value[i + 1] == '*' -> {
                    i = value.indexOf("*/", i) + 1
                    if (value.substring(startPos, i).contains("\n"))
                        tokenArray.add(Token(Type.NEWLINE, "\n", i, 1))
                }
                letterRegex.matches(character.toString()) -> {
                    val nameString = buildNameString(value, i)
                    i += nameString.length - 1
                    tokenArray.add(Token(Type.NAME, nameString, startPos, nameString.length))
                }
                character == '"' -> {
                    value.dropLast(value.length)
                    val string = buildQuotedString(value, ++i)
                    val len = string.replace("\"", "\\\"").replace("\n", "\\n").length
                    i += len
                    tokenArray.add(Token(Type.STRING, string, startPos, len))
                }
                character.isDigit() -> {
                    val integerString = buildNumString(value, i)
                    i += integerString.length - 1
                    tokenArray.add(Token(Type.NUMBER, integerString, startPos, integerString.length))
                }
                symbolRegex.matches(character.toString()) -> tokenArray.add(Token(Type.SYMBOL, character.toString(), startPos, 1))
                character.toString() == "\n" -> tokenArray.add(Token(Type.NEWLINE, "\n", i, 1))
            }
            i++
        }
        return tokenArray
    }

    fun buildNumString(value: String, index: Int): String {
        var numberString = ""
        var hadDecimal = false
        for (i in index until value.length) {
            val char = value[i]
            if (char.isDigit()) {
                numberString += char
            } else if (char == '.') {
                if (hadDecimal)
                    throw TokenizerException("Wisp: Cannot have multiple decimal points in a number")
                hadDecimal = true
                numberString += char
            } else if (char.isWhitespace()) {
                break
            }
        }
        return numberString
    }

    fun buildQuotedString(value: String, index: Int): String {
        var string = ""
        var i = index
        while (i < value.length) {
            val char = value[i]

            when (char) {
                '"' -> break
                '\\' -> {
                    val nextChar = value[++i]
                    string += when (nextChar) {
                        '"' -> '"'
                        'n' -> "\n"
                        else -> {
                            "\\$nextChar"
                        }
                    }
                }
                else -> string += char
            }
            i++
        }
        return string
    }

    fun buildNameString(value: String, index: Int): String {
        var string = ""
        var i = index
        while (i < value.length) {
            val char = value[i]

            if (letterRegex.matches(char.toString()) || char.isDigit()) {
                string += char
            } else break
            i++
        }
        return string
    }

    fun matureTokens(tokens: List<Token>): List<MatureToken> {
        val matureTokens = ArrayList<MatureToken>()
        var i = 0
        do {
            val token = tokens[i]
            matureTokens.add(when (token.type) {
                Type.SYMBOL -> {
                    when (token.value) {
                        "-" -> {
                            when (tokens[++i].value) {
                                ">" -> MatureToken(MatureType.SYMBOL, "->", token.idx, 2)
                                "=" -> MatureToken(MatureType.SYMBOL, "-=", token.idx, 2)
                                else -> {
                                    i--
                                    MatureToken(MatureType.SYMBOL, token.value, token.idx, token.len)
                                }
                            }
                        }
                        "+" -> {
                            when (tokens[++i].value) {
                                "+" -> MatureToken(MatureType.SYMBOL, "++", token.idx, 2)
                                "=" -> MatureToken(MatureType.SYMBOL, "+=", token.idx, 2)
                                else -> {
                                    i--
                                    MatureToken(MatureType.SYMBOL, token.value, token.idx, token.len)
                                }
                            }
                        }
                        "=" -> {
                            when (tokens[++i].value) {
                                "=" -> MatureToken(MatureType.SYMBOL, "==", token.idx, 2)
                                else -> {
                                    i--
                                    MatureToken(MatureType.SYMBOL, token.value, token.idx, token.len)
                                }
                            }
                        }
                        "<" -> {
                            when (tokens[++i].value) {
                                "<" -> MatureToken(MatureType.SYMBOL, "<<", token.idx, 2)
                                "=" -> MatureToken(MatureType.SYMBOL, "<=", token.idx, 2)
                                else -> {
                                    i--
                                    MatureToken(MatureType.SYMBOL, token.value, token.idx, token.len)
                                }
                            }
                        }
                        ">" -> {
                            when (tokens[++i].value) {
                                ">" -> MatureToken(MatureType.SYMBOL, ">>", token.idx, 2)
                                "=" -> MatureToken(MatureType.SYMBOL, ">=", token.idx, 2)
                                else -> {
                                    i--
                                    MatureToken(MatureType.SYMBOL, token.value, token.idx, token.len)
                                }
                            }
                        }
                        "!" -> {
                            when (tokens[++i].value) {
                                "=" -> MatureToken(MatureType.SYMBOL, "!=", token.idx, 2)
                                else -> {
                                    i--
                                    MatureToken(MatureType.SYMBOL, token.value, token.idx, token.len)
                                }
                            }
                        }
                        "&" -> {
                            when (tokens[++i].value) {
                                "&" -> MatureToken(MatureType.SYMBOL, "&&", token.idx, 2)
                                else -> {
                                    i--
                                    MatureToken(MatureType.SYMBOL, token.value, token.idx, token.len)
                                }
                            }
                        }
                        "|" -> {
                            when (tokens[++i].value) {
                                "|" -> MatureToken(MatureType.SYMBOL, "||", token.idx, 2)
                                else -> {
                                    i--
                                    MatureToken(MatureType.SYMBOL, token.value, token.idx, token.len)
                                }
                            }
                        }
                        else -> MatureToken(MatureType.SYMBOL, token.value, token.idx, token.len)
                    }
                }
                Type.STRING -> MatureToken(MatureType.STRING, token.value, token.idx, token.len)
                Type.NUMBER -> MatureToken(
                    if (token.value.contains(".")) MatureType.FLOAT else MatureType.INTEGER,
                    token.value,
                    token.idx,
                    token.len
                )
                Type.NAME -> MatureToken(
                    when (token.value) {
                        in Keywords.strings() -> MatureType.KEYWORD
                        in Primitive.strings() -> MatureType.PRIMITIVE
                        else -> MatureType.NAME
                    },
                    token.value,
                    token.idx,
                    token.len
                )
                Type.NEWLINE -> MatureToken(MatureType.NEWLINE, token.value, token.idx, 1)
            }
            )
        } while (++i < tokens.size)

        matureTokens.add( MatureToken( MatureType.EOF, "", tokens.size, 0 ) )

        return matureTokens
    }
}
