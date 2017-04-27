package org.abimon.notifly.orders

import org.abimon.db4k.events.Event
import org.abimon.db4k.objects.DMChannel
import org.abimon.imperator.impl.BaseOrder
import org.abimon.notifly.Notifly

data class DiscordEvent(val event: Event, val privateChannels: Set<DMChannel>, val notifly: Notifly): BaseOrder("New event of type ${event::class.simpleName}", notifly)