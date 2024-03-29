package dev.wisplang.wisp.tokenizer

import dev.wisplang.wisp.Primitive
import dev.wisplang.wisp.TokenizerException

object Tokenizer {
    private val letterRegex = Regex("[a-zA-Z_$]")
    private val symbolRegex = Regex("[{}()<>,.'\"\\[\\]|\\\\+\\-=*/&:!]")

    fun tokenize(value: String): ArrayList<Token> {
        val tokenArray = arrayListOf<Token>()

        var i = 0
        var line = 1
        var col = 1

        fun token(type: Type, value: String, pos: Int = i, lenght: Int = value.length) {
            tokenArray.add(
                Token(
                    type = type,
                    value = value,
                    idx = pos,
                    len = lenght,
                    line = line,
                    col = col
                )
            )
            if (type == Type.NEWLINE) {
                line++
                col = 1
            } else {
                col += lenght
            }
        }

        while (i < value.length) {
            val character = value[i]
            val startPos = i
            when {
                character == ' ' -> col++

                character == '/' && value[i + 1] == '/' -> {
                    i = value.indexOf("\n", i)
                    token(Type.NEWLINE, "\n")
                }

                character == '/' && value[i + 1] == '*' -> {
                    i = value.indexOf("*/", i) + 1
                    if (value.substring(startPos, i).contains("\n"))
                        token(Type.NEWLINE, "\n")
                    col += 1
                }

                letterRegex.matches(character.toString()) -> {
                    val nameString = buildNameString(value, i)
                    token(Type.NAME, nameString)
                    i += nameString.length - 1
                }

                character == '"' -> {
                    value.dropLast(value.length)
                    val string = buildQuotedString(value, ++i)
                    val len = string.replace("\"", "\\\"").replace("\n", "\\n").length
                    token(Type.STRING, string, startPos, len)
                    i += len
                }

                character.isDigit() -> {
                    val integerString = buildNumString(value, i)
                    token(Type.NUMBER, integerString)
                    i += integerString.length - 1
                }

                symbolRegex.matches(character.toString()) -> token(Type.SYMBOL, character.toString())

                character.toString() == "\n" -> token(Type.NEWLINE, "\n")
            }
            i++
        }
        return tokenArray
    }

    private fun buildNumString(value: String, index: Int): String {
        var numberString = ""
        var hadDecimal = false

        for (char in value.substring(index)) {
            if (char.isDigit())
                numberString += char
            else if (char == '.') {
                if (hadDecimal)
                    throw TokenizerException("Wisp: Cannot have multiple decimal points in a number")
                hadDecimal = true
                numberString += char
            } else if (char.isWhitespace())
                break
        }

        return numberString
    }

    private fun buildQuotedString(value: String, index: Int): String {
        var string = ""
        var i = index

        while (i < value.length) {
            when (val char = value[i]) {
                '"' -> break
                '\\' -> string += when (val nextChar = value[++i]) {
                    '"' -> '"'
                    'n' -> "\n"
                    else -> "\\$nextChar"
                }

                else -> string += char
            }
            i++
        }
        return string
    }

    private fun buildNameString(value: String, index: Int): String {
        var string = ""

        for (char in value.substring(index))
            string += if (letterRegex.matches(char.toString()) || char.isDigit())
                char
            else
                break

        return string
    }

    fun matureTokens(tokens: List<Token>): List<MatureToken> {
        val matureTokens = ArrayList<MatureToken>()
        var i = 0

        fun token(type: MatureType, tokn: Token, value: String = tokn.value, idx: Int = tokn.idx) {
            matureTokens.add(
                MatureToken(
                    type = type,
                    value = value,
                    idx = idx,
                    len = value.length,
                    line = tokn.line,
                    col = tokn.col
                )
            )
        }

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
                        in Keywords.STRINGS -> MatureType.KEYWORD
                        in Primitive.STRINGS -> MatureType.PRIMITIVE
                        else -> MatureType.NAME
                    },
                    token
                )

                Type.NEWLINE -> token(MatureType.NEWLINE, token)
            }
        } while (++i < tokens.size)

        matureTokens.add(MatureToken(MatureType.EOF, "", tokens.size, 0, tokens.last().line + 1, 0))

        return matureTokens
    }
}
