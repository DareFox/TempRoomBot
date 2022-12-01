package change.group.bot.extensions

import change.group.bot.Config
import change.group.bot.database.global.CounterCollection
import change.group.bot.models.PongCounterEntry
import change.group.bot.util.toSnow
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.rest.builder.message.create.embed
import mu.KotlinLogging

private val klogger = KotlinLogging.logger { }

/**
 * Example of how you can use Extensions in Kord Extensions
 *
 * For more information, [check official Kord Extensions page](https://kordex.kotlindiscord.com)
 */

// If you in class file, here's your clickable link -> https://kordex.kotlindiscord.com
class PingPongExtension : Extension() {
    override val name: String = "PingPongExtension"

    override suspend fun setup() {
        // Public slash command anyone can see
        publicSlashCommand {
            name = "ping"
            description = "If you ping me, I will pong you"

            // For testing, use guild specific commands
            // Because it takes long time to create/update global commands
            // https://kordex.kotlindiscord.com/en/concepts/commands/slash#slash-commands
            guild(Config.testGuildID.toSnow())

            action {
                klogger.info { "executed public ping command" }
                respond {
                    content = "pong"
                }
            }
        }

        // Personal slash command. Only author of message can see it.
        ephemeralSlashCommand {
            name = "personalping" // Name should be without whitespaces
            description = "Personal ping for your persona"

            // For testing, use guild specific commands
            // Because it takes long time to create/update global commands
            // https://kordex.kotlindiscord.com/en/concepts/commands/slash#slash-commands
            guild(Config.testGuildID.toSnow())

            action {
                respond {
                    klogger.info { "executed private/ephemeral ping command" }

                    // If no entry about this person, create a new one
                    val countEntry = CounterCollection.getCounter(user.id) ?: PongCounterEntry(user.id.toString(), 0)

                    // Increment counter
                    val newCounter = countEntry.copy(counter = countEntry.counter + 1)

                    embed {
                        title = "Pong! You pinged me ${newCounter.counter} times."
                        description = "[Now I want you to leave me alone](https://www.youtube.com/watch?v=85Z3iwpFQeg)"

                        footer {
                            // Create hyperlink markdown for Discord embed message
                            // https://support.discord.com/hc/en-us/community/posts/360061776032
                            text = "They say the devil that you know is better than the devil that you don't"
                        }
                    }

                    // Update counter
                    CounterCollection.setCounter(newCounter)
                }
            }
        }
    }
}