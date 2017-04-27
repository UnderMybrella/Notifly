@file:Suppress("ArrayInDataClass")

package org.abimon.notifly.responses

import java.time.LocalDateTime
import java.util.*

data class TumblrDocument(
        val meta: TumblrMeta,
        val response: TumblrResponse
)

data class TumblrMeta(
        val status: Int,
        val msg: String
)

data class TumblrResponse(
        val blog: TumblrBlog,
        val posts: Array<TumblrPost>,
        val total_posts: Int
)

data class TumblrBlog(
        val title: String,
        val posts: Int,
        val name: String,
        val updated: Long,
        val description: String,
        val ask: Boolean,
        val ask_anon: Optional<Boolean>,
        val likes: Optional<Long>,
        val is_blocked_from_primary: Optional<Boolean>
)

data class TumblrPost(
        val blog_name: String,
        val id: Long,
        val post_url: String,
        val type: String,
        val timestamp: Long,
        val date: LocalDateTime,
        val format: String,
        val reblog_key: String,
        val tags: Array<String>,
        val bookmarklet: Boolean = false,
        val mobile: Boolean = false,
        val source_url: Optional<String>,
        val source_title: Optional<String>,
        val liked: Optional<Boolean>,
        val state: String,
        val summary: String
)