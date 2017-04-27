package org.abimon.notifly.inputs

import org.abimon.db4k.DiscordGateway
import org.abimon.db4k.events.DMChannelCreateEvent
import org.abimon.db4k.events.Event
import org.abimon.db4k.events.ReadyEvent
import org.abimon.db4k.objects.Channel
import org.abimon.db4k.objects.DMChannel
import org.abimon.notifly.Notifly
import org.abimon.notifly.orders.DiscordEvent

class DiscordEvents(val notifly: Notifly, val token: String) {
    val privateChannels: MutableSet<DMChannel> = HashSet()
    val channels: MutableMap<String, Channel> = HashMap()

    init {
        DiscordGateway.obtain(token, listeners = arrayListOf(this::event)) {}
    }

    fun event(event: Event) {
        try {
            if (event is ReadyEvent) {
                event.private_channels.forEach { privateChannels.add(it) }
            }

            if (event is DMChannelCreateEvent)
                privateChannels.add(event.dm)

            notifly.imperator.dispatch(DiscordEvent(event, privateChannels, notifly))
        }
        catch(th: Throwable){
            th.printStackTrace()
        }
    }
}