package org.abimon.notifly

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.mashape.unirest.http.JsonNode
import com.mashape.unirest.http.Unirest
import com.mashape.unirest.request.HttpRequestWithBody
import org.abimon.db4k.objMapper
import org.abimon.imperator.handle.Imperator
import org.abimon.imperator.handle.Order
import org.abimon.imperator.handle.Scout
import org.abimon.imperator.impl.BasicImperator
import org.abimon.notifly.inputs.DiscordEvents
import org.abimon.notifly.inputs.RSSFeeds
import org.abimon.notifly.inputs.TumblrBlogs
import org.abimon.notifly.responses.AtomText
import org.abimon.notifly.responses.AtomTextDeserialiser
import org.abimon.visi.collections.Pool
import org.abimon.visi.collections.PoolableObject
import org.abimon.visi.lang.asOptional
import org.abimon.visi.lang.invoke
import org.json.JSONObject
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.Statement
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.TimeUnit


fun main(args: Array<String>) {
    val notifly = Notifly()
}

class Notifly(val config: ServerConfig = run {
    val configFile = File("config.json")
    if (!configFile.exists())
        configFile.writeText("{}")
    return@run objMapper.readValue(configFile, ServerConfig::class.java)
}): Scout {
    override fun addAnnouncements(order: Order) {}

    override fun getName(): String = "Notifly"

    override fun setImperator(imperator: Imperator) {}

    val objMapper: ObjectMapper = ObjectMapper()
            .findAndRegisterModules()
            .registerModule(SimpleModule("LDT").addDeserializer(LocalDateTime::class.java, LDTDeserializer()).addDeserializer(AtomText::class.java, AtomTextDeserialiser()))
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            .setSerializationInclusion(JsonInclude.Include.NON_ABSENT)

    val mysqlConnectionPool = Pool<Connection>(128)

    val imperator = BasicImperator()

    val rss: Optional<RSSFeeds>
    val tumblr: Optional<TumblrBlogs>
    val discord: Optional<DiscordEvents>

    fun makeConnection(): PoolableObject<Connection> = PoolableObject(DriverManager.getConnection("jdbc:mysql://${config.mysqlServer()}/${config.mysqlDatabase()}?user=${config.mysqlUsername()}&password=${config.mysqlPassword()}&serverTimezone=GMT"))
    fun getConnection(): PoolableObject<Connection> = mysqlConnectionPool.getOrAddOrWait(60, TimeUnit.SECONDS, { makeConnection() }) as PoolableObject<Connection>

    init {
        println(config)

        if(!config.userConfigDirectory.exists())
            config.userConfigDirectory.mkdirs()

        if(config.rssSupport) {
            rss = RSSFeeds(this).asOptional()
        }
        else
            rss = Optional.empty()

        if(config.tumblrSupport && config.tumblrConsumerKey.isPresent) {
            tumblr = TumblrBlogs(this).asOptional()
        }
        else
            tumblr = Optional.empty()

        if(config.discordToken.isPresent)
            discord = DiscordEvents(this, config.discordToken()).asOptional()
        else
            discord = Optional.empty()
    }

    fun usingMysql(): Boolean = config.mysqlServer.isPresent && config.mysqlDatabase.isPresent && config.mysqlUsername.isPresent && config.mysqlPassword.isPresent

    fun ensureAccountsTable() = getConnection().use { connection -> connection.createStatement().executeAndClose("CREATE TABLE IF NOT EXISTS accounts (id VARCHAR(63) PRIMARY KEY, google_token VARCHAR(255), refresh_token VARCHAR(255), token VARCHAR(255));") }

    fun sendFirebaseMessage(device: String, notificationPayload: Optional<NotificationPayload> = Optional.empty(), dataPayload: Optional<JSONObject> = Optional.empty()) {
        config.firebaseServerToken.ifPresent { token ->
            val payload = JSONObject()
            payload.put("to", device)
            notificationPayload.ifPresent { notification -> payload.put("notification", JSONObject(objMapper.writeValueAsString(notification))) }
            dataPayload.ifPresent { data -> payload.put("data", data) }
            println("Sending $payload")
            val response = Unirest.post(config.firebaseServerAddress).header("Authorization", "key=$token").jsonBody(payload).asJson().body.toJsonObject()
            println(response)
        }
    }
}

fun Statement.executeAndClose(sql: String) {
    execute(sql)
    close()
}
fun Statement.executeQueryAndClose(sql: String): ResultSet {
    closeOnCompletion()
    return executeQuery(sql)
}
fun <T: HttpRequestWithBody> T.jsonBody(json: JSONObject): T {
    header("Content-Type", "application/json")
    body(json.toString())
    return this
}
fun JsonNode.toJsonObject(): JSONObject = JSONObject(`object`.toString())