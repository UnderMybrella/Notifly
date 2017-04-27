package org.abimon.notifly.responses

import java.time.LocalDateTime
import java.util.*

data class RSSDocument(val rss: RSS)

data class RSS(val channel: RSSChannel)

@Suppress("ArrayInDataClass")
data class RSSChannel(
        val title: String,
        val link: String,
        val description: String,
        val language: Optional<String>,
        val copyright: Optional<String>,
        val managingEditor: Optional<String>,
        val webMaster: Optional<String>,
        val pubDate: Optional<String>,
        val lastBuildDate: Optional<String>,
        val category: Optional<String>,
        val generator: Optional<String>,
        val docs: Optional<String>,
        val ttl: Optional<String>,
        val item: Array<RSSItem>
)

data class RSSItem(
    val title: Optional<String>,
    val link: Optional<String>,
    val description: Optional<String>,
    val author: Optional<String>,
    val category: Optional<String>,
    val comments: Optional<String>,
    val enclosure: Optional<String>,
    val guid: Optional<RSSGuid>,
    val pubDate: Optional<LocalDateTime>,
    val source: Optional<String>
)

data class RSSGuid(
        val content: String,
        val isPermalink: Optional<Boolean>
)