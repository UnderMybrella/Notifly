package org.abimon.notifly.responses

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import org.json.JSONObject
import org.jsoup.Jsoup
import java.time.LocalDateTime
import java.util.*

data class AtomDocument(val feed: AtomFeed)

data class AtomFeed(
        val id: String,
        val title: String,
        val updated: LocalDateTime,
        val author: Optional<AtomPerson>,
        val link: Optional<Array<AtomLink>>,
        val category: Optional<AtomCategory>,
        val contributor: Optional<AtomPerson>,
        val generator: Optional<AtomGenerator>,
        val icon: Optional<String>,
        val logo: Optional<String>,
        val rights: Optional<AtomText>,
        val subtitle: Optional<AtomText>,

        val entry: Array<AtomEntry>
)

data class AtomPerson(
        val name: String,
        val uri: Optional<String>,
        val email: Optional<String>
)

data class AtomLink(
        val href: String,
        val rel: Optional<String>,
        val type: Optional<String>,
        val hreflang: Optional<String>,
        val title: Optional<String>,
        val length: Optional<Long>
)

data class AtomCategory(
        val term: String,
        val scheme: Optional<String>,
        val label: Optional<String>
)

data class AtomGenerator(
        val uri: Optional<String>,
        val version: Optional<String>
)

data class AtomText(
        val type: String = "text",
        val content: String
)

data class TmpAtomText(val type: String = "text", val content: String) {
    constructor(content: String): this("text", content)
}

data class AtomEntry(
        val id: String,
        val title: String,
        val updated: LocalDateTime,

        val author: Optional<AtomPerson>,
        val content: Optional<AtomText>,
        val link: Optional<Array<AtomLink>>,
        val summary: Optional<AtomText>,

        val category: Optional<AtomCategory>,
        val contributor: Optional<AtomPerson>,
        val published: Optional<LocalDateTime>,
        val rights: Optional<AtomText>,
        val source: Optional<JSONObject>
)

class AtomTextDeserialiser: JsonDeserializer<AtomText>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): AtomText {
        val tmp = p.readValueAs(TmpAtomText::class.java)
        if(tmp.type == "html")
            return AtomText(tmp.type, Jsoup.parseBodyFragment(tmp.content).text())
        return AtomText(tmp.type, tmp.content)
    }

}