package io.android.serial.api

/** Protocol-neutral request/response correlation strategy. */
public fun interface ResponseMatcher {
    public fun matches(request: ByteArray, response: ByteArray): Boolean
}

public object ResponseMatchers {
    public val Any: ResponseMatcher = ResponseMatcher { _, _ -> true }

    public fun exact(expected: ByteArray): ResponseMatcher = ResponseMatcher { _, response -> response.contentEquals(expected) }

    public fun prefix(prefix: ByteArray): ResponseMatcher = ResponseMatcher { _, response ->
        response.size >= prefix.size && response.copyOf(prefix.size).contentEquals(prefix)
    }
}
