package org.abimon.notifly.orders

import org.abimon.imperator.impl.BaseOrder
import org.abimon.notifly.Notifly
import org.abimon.notifly.responses.RSSItem

data class NewRSSItem(val feed: String, val item: RSSItem, val notifly: Notifly): BaseOrder("New item from $feed", notifly)