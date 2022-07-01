package io.github.wisplang

object Tokenizer {
    enum class Type {
        SYMBOL,
        NUMBER,
        STRING,
        NAME
    }
    data class Token(val type: Type, val value: String, val idx: Int, val len: Int)

    val letterRegex = Regex("[a-zA-Z_$]")
    val symbolRegex = Regex("[{}()<>,.'\"\\[\\]|\\\\+\\-=*/&:]")

    fun tokenize(value: String): ArrayList<Token> {
        val tokenArray = arrayListOf<Token>()

        var i = 0
        while(i < value.length) {
            val character = value[i]
            val startPos = i
            when {
                character == '/' && value[i+1] == '/' ->
                    while (i < value.length) if (value[++i].toString() == "\n") break
                character == '/' && value[i+1] == '*' ->
                    i = value.indexOf("*/", i) + 1
                letterRegex.matches(character.toString()) -> {
                    val nameString = buildNameString(value, i)
                    i += nameString.length-1
                    tokenArray.add(Token(Type.NAME, nameString, i, nameString.length))
                }
                character == '"' -> {
                    value.dropLast(value.length)
                    val string = buildQuotedString(value,++i)
                    val len = string.replace("\"","\\\"").replace("\n","\\n").length
                    i += len
                    tokenArray.add(Token(Type.STRING, string, i, len))
                }
                character.isDigit() -> {
                    val integerString = buildNumString(value, i)
                    i+=integerString.length-1
                    tokenArray.add(Token(Type.NUMBER, integerString, i, integerString.length))
                }
                symbolRegex.matches(character.toString()) -> {
                    tokenArray.add(Token(Type.SYMBOL, character.toString(), i, 1))
                }
            }
            i++
        }
        return tokenArray
    }

    fun buildNumString(value: String, index: Int): String {
        var numberString = ""
        var hadDecimal = false
        for(i in index..value.length-1) {
            val char = value[i]
            if(char.isDigit() ) {
                numberString += char
            } else if (!hadDecimal && char == '.') {
                hadDecimal = true
                numberString += char
            } else if (char == '.') {
                //TODO: Replace with dedicated exception
                throw Exception("Wisp: Cannot have multiple decimal points in a number")
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
                        else -> {"\\$nextChar"}
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


    enum class MatureType {
        SYMBOL,
        KEYWORD,
        INTEGER,
        FLOAT,
        STRING,
        RETURN,
        NAME,
        PRIMITIVE
    }

    data class MatureToken(val type: MatureType, val value: String, val idx: Int, val len: Int)

    val keywords = arrayOf("ext", "imp", "match", "in", "is", "prim", "func", "var", "type", "if", "else", "for", "while")
    val primitives = arrayOf("u1", "u8", "i8", "u16", "i16", "u32", "i32", "f32", "u64", "i64", "f64")

    fun matureTokens(tokens: List<Token>): List<MatureToken> {
        val matureTokens = ArrayList<MatureToken>()
        var i = 0;
        do {
            val token = tokens[i]
            when (token.type) {
                Type.SYMBOL -> {
                    when (token.value) {
                        "-" -> {
                            when (tokens[++i].value) {
                                ">" ->
                                    matureTokens.add(MatureToken(MatureType.RETURN, "->", token.idx, 2))
                                "=" ->
                                    matureTokens.add(MatureToken(MatureType.SYMBOL, "-=", token.idx, 2))
                                else -> {
                                    i--
                                    matureTokens.add(MatureToken(MatureType.SYMBOL, token.value, token.idx, token.len))
                                }
                            }
                        }
                        "+" -> {
                            when (tokens[++i].value) {
                                "+" ->
                                    matureTokens.add(MatureToken(MatureType.SYMBOL, "++", token.idx, 2))
                                "=" ->
                                    matureTokens.add(MatureToken(MatureType.SYMBOL, "+=", token.idx, 2))
                                else -> {
                                    i--
                                    matureTokens.add(MatureToken(MatureType.SYMBOL, token.value, token.idx, token.len))
                                }
                            }
                        }
                        "=" -> {
                            when (tokens[++i].value) {
                                "=" ->
                                    matureTokens.add(MatureToken(MatureType.SYMBOL, "==", token.idx, 2))
                                else -> {
                                    i--
                                    matureTokens.add(MatureToken(MatureType.SYMBOL, token.value, token.idx, token.len))
                                }
                            }
                        }
                        "<" -> {
                            when (tokens[++i].value) {
                                "<" ->
                                    matureTokens.add(MatureToken(MatureType.SYMBOL, "<<", token.idx, 2))
                                "=" ->
                                    matureTokens.add(MatureToken(MatureType.SYMBOL, "<=", token.idx, 2))
                                else ->{
                                    i--
                                    matureTokens.add(MatureToken(MatureType.SYMBOL, token.value, token.idx, token.len))
                                }
                            }
                        }
                        ">" -> {
                            when (tokens[++i].value) {
                                ">" ->
                                    matureTokens.add(MatureToken(MatureType.SYMBOL, ">>", token.idx, 2))
                                "=" ->
                                    matureTokens.add(MatureToken(MatureType.SYMBOL, ">=", token.idx, 2))
                                else ->{
                                    i--
                                    matureTokens.add(MatureToken(MatureType.SYMBOL, token.value, token.idx, token.len))
                                }
                            }
                        }
                        "!" -> {
                            when (tokens[++i].value) {
                                "=" ->
                                    matureTokens.add(MatureToken(MatureType.SYMBOL, "!=", token.idx, 2))
                                else -> {
                                    i--
                                    matureTokens.add(MatureToken(MatureType.SYMBOL, token.value, token.idx, token.len))
                                }
                            }
                        }
                        "&" -> {
                            when (tokens[++i].value) {
                                "&" ->
                                    matureTokens.add(MatureToken(MatureType.SYMBOL, "&&", token.idx, 2))
                                else -> {
                                    i--
                                    matureTokens.add(MatureToken(MatureType.SYMBOL, token.value, token.idx, token.len))
                                }
                            }
                        }
                        "|" -> {
                            when (tokens[++i].value) {
                                "|" ->
                                    matureTokens.add(MatureToken(MatureType.SYMBOL, "||", token.idx, 2))
                                else ->{
                                    i--
                                    matureTokens.add(MatureToken(MatureType.SYMBOL, token.value, token.idx, token.len))
                                }
                            }
                        }
                        else ->
                            matureTokens.add(MatureToken(MatureType.SYMBOL, token.value, token.idx, token.len))
                    }
                }
                Type.STRING ->
                    matureTokens.add(MatureToken(MatureType.STRING, token.value, token.idx, token.len))
                Type.NUMBER -> {
                    val type: MatureType = if (token.value.contains("."))
                        MatureType.FLOAT
                    else MatureType.INTEGER
                    matureTokens.add(MatureToken(type, token.value, token.idx, token.len))
                }
                Type.NAME -> {
                    val type = when (token.value) {
                        in keywords -> MatureType.KEYWORD
                        in primitives -> MatureType.PRIMITIVE
                        else -> MatureType.NAME
                    }
                    matureTokens.add(MatureToken(type, token.value, token.idx, token.len))
                }
            }
        } while (++i < tokens.size)

        return matureTokens
    }
}