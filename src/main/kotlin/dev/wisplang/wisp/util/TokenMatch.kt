package dev.wisplang.wisp.util

import dev.wisplang.wisp.lexer.Lexer
import dev.wisplang.wisp.tokenizer.MatureToken
import dev.wisplang.wisp.tokenizer.MatureType

object TokenMatch {
    fun Lexer.match(token: MatureToken = consume(), function: MatchBody.() -> Unit) {
        val body = MatchBody()
        function(body)
        for ((key, value) in body.cases)
            if (key.first == token.type && (key.second == null || key.second == token.value))
                return value(token)
        body.defaultBody(token)
    }

    class MatchBody {
        internal val cases: MutableMap<Pair<MatureType, String?>, (MatureToken) -> Unit> = HashMap()
        internal var defaultBody: MatureToken.() -> Unit = { }

        fun on(type: MatureType, value: String? = null, function: (MatureToken) -> Unit) {
            cases[Pair(type, value)] = function
        }

        fun oni(type: MatureType, value: String? = null, function: MatureToken.() -> Unit) {
            cases[Pair(type, value)] = function
        }

        fun on(vararg types: MatureType, function: MatureToken.() -> Unit) {
            for (type in types)
                cases[Pair(type, null)] = function
        }

        fun default(function: MatureToken.() -> Unit) {
            defaultBody = function
        }
    }
}
