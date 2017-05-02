package org.abimon.notifly

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatter.*
import java.time.format.DateTimeFormatterBuilder

class LDTSerializer: JsonSerializer<LocalDateTime>() {
    override fun serialize(value: LocalDateTime, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeString(value.format(ISO_LOCAL_DATE_TIME))
    }
}

class LDTDeserializer: JsonDeserializer<LocalDateTime>() {
    val tumblrFormatter: DateTimeFormatter = DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(ISO_LOCAL_DATE)
            .appendLiteral(' ')
            .append(ISO_LOCAL_TIME)
            .appendLiteral(' ')
            .appendZoneId()
            .toFormatter()

    val redditFormatter: DateTimeFormatter = DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(ISO_LOCAL_DATE_TIME)
            .appendZoneId()
            .toFormatter()

    val formatter: DateTimeFormatter = DateTimeFormatterBuilder()
            .appendOptional(redditFormatter)
            .appendOptional(tumblrFormatter)
            .appendOptional(RFC_1123_DATE_TIME)
            .appendOptional(ISO_LOCAL_DATE_TIME)
            .appendOptional(ISO_OFFSET_DATE_TIME)
            .toFormatter()

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): LocalDateTime = LocalDateTime.parse(p.valueAsString, formatter)
}