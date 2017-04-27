package org.abimon.notifly.orders

import org.abimon.imperator.impl.BaseOrder
import org.abimon.notifly.Notifly
import org.abimon.notifly.responses.AtomEntry

data class NewAtomEntry(val feed: String, val item: AtomEntry, val notifly: Notifly): BaseOrder("New item from $feed", notifly)