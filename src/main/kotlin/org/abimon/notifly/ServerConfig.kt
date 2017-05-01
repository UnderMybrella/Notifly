package org.abimon.notifly

import org.abimon.visi.lang.asOptional
import java.io.File
import java.util.*

data class ServerConfig(
        val mysqlServer: Optional<String> = "localhost".asOptional(),
        val mysqlDatabase: Optional<String> = Optional.empty(),
        val mysqlUsername: Optional<String> = Optional.empty(),
        val mysqlPassword: Optional<String> = Optional.empty(),

        val firebaseServerToken: Optional<String> = Optional.empty(),
        val firebaseServerAddress: String = "https://fcm.googleapis.com/fcm/send",

        val userConfigDirectory: File = File("users"),

        val rssSupport: Boolean = false,
        val rssRecheck: Long = 10 * 1000,

        val tumblrSupport: Boolean = false,
        val tumblrRecheck: Long = 10 * 1000,
        val tumblrConsumerKey: Optional<String> = Optional.empty(),
        val tumblrSecretKey: Optional<String> = Optional.empty(),

        val discordToken: Optional<String> = Optional.empty(),

        val websocketAuthorisation: Optional<String> = Optional.empty(),
        val websocketPort: Int = 8080
)