package org.abimon.notifly.orders

import org.abimon.imperator.impl.BaseOrder
import org.abimon.notifly.Notifly
import org.abimon.notifly.responses.TumblrPost

data class NewTumblrPost(val author: String, val post: TumblrPost, val notifly: Notifly): BaseOrder("Post by $author", notifly)