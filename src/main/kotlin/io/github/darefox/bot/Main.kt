package io.github.darefox.bot

import io.github.darefox.bot.extensions.PingPongExtension
import io.github.darefox.bot.extensions.TempRoomCreatorExtension
import com.kotlindiscord.kord.extensions.ExtensibleBot
import dev.kord.gateway.Intents
import dev.kord.gateway.PrivilegedIntent
import org.litote.kmongo.KMongo

@OptIn(PrivilegedIntent::class)
suspend fun main(args: Array<String>) {
    val bot = ExtensibleBot(Config.botToken) {
        // To add extensions, you need to pass reference to extension class via :: symbol
        extensions {
            add(::TempRoomCreatorExtension)
        }
        intents {
            +Intents.all
        }
    }
    bot.start()
}