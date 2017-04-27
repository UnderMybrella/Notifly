package org.abimon.notifly.inputs

import com.mashape.unirest.http.Unirest
import org.abimon.notifly.Notifly
import org.abimon.notifly.orders.NewTumblrPost
import org.abimon.notifly.responses.TumblrDocument
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import kotlin.concurrent.scheduleAtFixedRate

/** Updates every [rssSupport][org.abimon.notifly.ServerConfig.rssRecheck] milliseconds */
class TumblrBlogs(notifly: Notifly) {
    val timer = Timer(false)
    val executor = ThreadPoolExecutor(0, 128,
            60L, TimeUnit.SECONDS,
            SynchronousQueue<Runnable>())
    val blogs = ConcurrentLinkedQueue<Pair<String, Long>>()

    init {
        timer.scheduleAtFixedRate(0, notifly.config.tumblrRecheck) {
            blogs.forEach { (blog, lastUpdated) ->
                executor.submit {
                    try {
                        val url = "https://api.tumblr.com/v2/blog/$blog/posts?api_key=${notifly.config.tumblrConsumerKey.get()}&filter=raw"
                        val response = Unirest.get(url).asString().body
                        val tumblr = notifly.objMapper.readValue(response, TumblrDocument::class.java)
                        if(tumblr.meta.status == 200) {
                            if(tumblr.response.blog.updated > lastUpdated) {
                                tumblr.response.posts.filter { post -> post.timestamp > lastUpdated }.forEach { post -> notifly.imperator.dispatch(NewTumblrPost(tumblr.response.blog.name, post, notifly)) }

                                blogs.remove(Pair(blog, lastUpdated))
                                blogs.add(Pair(blog, tumblr.response.blog.updated))
                            }
                        }
                    }
                    catch(runtime: RuntimeException) {
                        val underlying = runtime.cause ?: return@submit

                        underlying.printStackTrace()
                    }
                    catch(th: Throwable) {
                        th.printStackTrace()
                    }
                }
            }

            Thread.sleep(100)
        }
    }

    fun registerBlog(url: String){
        blogs.add(Pair(url, LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC)))
    }
}