package dev.wisplang.wisp.util

import dev.wisplang.wisp.lexer.Lexer
import dev.wisplang.wisp.tokenizer.MatureToken
import dev.wisplang.wisp.tokenizer.MatureType

object TokenMatch {
    fun Lexer.match(token: MatureToken = consume(), function: MatchBody.() -> Unit ) {
        val body = MatchBody()
        function(body)
        for (case in body.cases)
            if (case.key.first == token.type && (case.key.second == null || case.key.second == token.value)) {
                case.value(token)
                return
            }
        body.defaultBody(token)
    }

    class MatchBody {
        internal val cases: MutableMap<Pair<MatureType, String?>, (MatureToken) -> Unit> = HashMap()
        internal var defaultBody: MatureToken.() -> Unit = { }

        fun on(type: MatureType, value: String? = null, function: MatureToken.() -> Unit) {
            cases[Pair(type, value)] = function
        }

        fun default(function: MatureToken.() -> Unit) {
            defaultBody = function
        }
    }
}
