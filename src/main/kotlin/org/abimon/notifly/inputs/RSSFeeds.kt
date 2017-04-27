package org.abimon.notifly.inputs

import com.mashape.unirest.http.Unirest
import org.abimon.notifly.Notifly
import org.abimon.notifly.orders.NewAtomEntry
import org.abimon.notifly.orders.NewRSSItem
import org.abimon.notifly.responses.AtomDocument
import org.abimon.notifly.responses.RSSDocument
import org.json.XML
import java.net.MalformedURLException
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import kotlin.concurrent.scheduleAtFixedRate

@Suppress("UNUSED_DESTRUCTURED_PARAMETER_ENTRY")
/** Updates every [tumblrSupport][org.abimon.notifly.ServerConfig.tumblrRecheck] milliseconds */
class RSSFeeds(notifly: Notifly) {
    val timer = Timer(false)
    val executor = ThreadPoolExecutor(0, 128,
            60L, TimeUnit.SECONDS,
            SynchronousQueue<Runnable>())
    val feeds = ConcurrentLinkedQueue<Pair<String, Long>>()

    init {
        timer.scheduleAtFixedRate(0, notifly.config.rssRecheck) {
            feeds.forEach { (feedSource, lastUpdated) ->
                executor.submit {
                    try {
                        println("Checking $feedSource")
                        val xml = Unirest.get(feedSource).header("User-Agent", "${System.getProperty("os.name")}:org.abimon.notifly:v0.1 (by /u/UnderMybrella_)").asString().body
                        val jsonObj = XML.toJSONObject(xml)
                        val json = jsonObj.toString()
                        println(json)

                        if(jsonObj.has("rss")) {
                            val rss = notifly.objMapper.readValue(json, RSSDocument::class.java).rss
                            var newestItem = -1L
                            rss.channel.item.filter { item -> item.pubDate.isPresent }.filter { item -> item.pubDate.get().toEpochSecond(ZoneOffset.UTC) > lastUpdated }.forEach { item ->
                                notifly.imperator.dispatch(NewRSSItem(feedSource, item, notifly))

                                val postTime = item.pubDate.get().toEpochSecond(ZoneOffset.UTC)
                                if (postTime > newestItem)
                                    newestItem = postTime
                            }

                            if (newestItem > lastUpdated) {
                                feeds.remove(Pair(feedSource, lastUpdated))
                                feeds.add(Pair(feedSource, newestItem))
                            }
                        }
                        else if(jsonObj.has("feed")) {
                            val feed = notifly.objMapper.readValue(json, AtomDocument::class.java).feed
                            if(feed.updated.toEpochSecond(ZoneOffset.UTC) > lastUpdated) {
                                feed.entry.filter { (id, title, updated) -> updated.toEpochSecond(ZoneOffset.UTC) > lastUpdated }.forEach { item -> notifly.imperator.dispatch(NewAtomEntry(feedSource, item, notifly)) }

                                feeds.remove(Pair(feedSource, lastUpdated))
                                feeds.add(Pair(feedSource, feed.updated.toEpochSecond(ZoneOffset.UTC)))
                            }
                        }
                    }
                    catch(runtime: RuntimeException) {
                        val underlying = runtime.cause ?: return@submit
                        if(underlying is MalformedURLException)
                            feeds.remove(Pair(feedSource, lastUpdated))
                        else
                            underlying.printStackTrace()
                    }
                    catch(th: Throwable) {
                        th.printStackTrace()
                    }
                }
            }
        }
    }

    fun registerFeed(url: String) {
        feeds.add(Pair(url, LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC)))
    }
}