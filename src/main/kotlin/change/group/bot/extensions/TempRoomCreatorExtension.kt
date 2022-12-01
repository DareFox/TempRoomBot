package change.group.bot.extensions

import change.group.bot.Config
import change.group.bot.database.guild.tempRoomCollection
import change.group.bot.database.guild.tempRoomSettings
import change.group.bot.models.TempRoomEntry
import change.group.bot.models.TempRoomSettingsEntry
import change.group.bot.util.toSnow
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.extensions.event
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.entity.*
import dev.kord.core.behavior.createVoiceChannel
import dev.kord.core.behavior.edit
import dev.kord.core.entity.Guild
import dev.kord.core.entity.Member
import dev.kord.core.entity.channel.VoiceChannel
import dev.kord.core.event.user.VoiceStateUpdateEvent
import dev.kord.rest.builder.channel.PermissionOverwriteBuilder
import dev.kord.rest.builder.message.create.embed
import kotlinx.coroutines.flow.singleOrNull
import mu.KotlinLogging

private val klogger = KotlinLogging.logger { }

/**
 * Example of how you can use Extensions in Kord Extensions
 *
 * For more information, [check official Kord Extensions page](https://kordex.kotlindiscord.com)
 */

// If you in class file, here's your clickable link -> https://kordex.kotlindiscord.com
class TempRoomCreatorExtension : Extension() {
    override val name: String = "PingPongExtension"

    override suspend fun setup() {
        event<VoiceStateUpdateEvent> {
            action {
                val settings = event.state.getGuild().tempRoomSettings.getSettings() ?: return@action;
                val guild = event.state.getGuild()

                // On join creator room
                if (event.state.channelId.toString() == settings.creatorRoomId) {
                    val room = createRoomForUser(event.state.getGuild(), event.state.userId)
                    moveUserToRoom(event.state.getMember(), room)
                }

                val previousChannel = event.old?.getChannelOrNull() ?: return@action
                val noOneInChannel = previousChannel.voiceStates.singleOrNull() == null

                // On exit of created channel
                if (event.state.channelId == null && noOneInChannel) {
                    val roomEntry = guild.tempRoomCollection.getRoomByRoomId(previousChannel.id.toString()) ?: return@action
                    val newPermissions = previousChannel.asChannel().permissionOverwrites.map {
                        val data = it.data
                        Overwrite(data.id, data.type, data.allowed, data.denied)
                    }.toSet()

                    val newEntry = roomEntry.copy(overwrites = newPermissions)
                    guild.tempRoomCollection.updateRoom(newEntry);
                    previousChannel.delete("Room is empty")
                }
            }
        }

        // Personal slash command. Only author of message can see it.
        ephemeralSlashCommand(::TempRoomCreatorArguments) {
            name = "setcreator" // Name should be without whitespaces
            description = "Set creator room"

            guild(Config.testGuildID.toSnow())

            action {
                respond {
                    klogger.info { "executed private/ephemeral creator command" }

                    embed {
                        title = "Setting id creator"
                    }

                    // Update counter
                    guild?.tempRoomSettings?.setSettings(TempRoomSettingsEntry(arguments.idCreator));
                }
            }
        }
    }

    private suspend fun createRoomForUser(guild: Guild, memberId: Snowflake): VoiceChannel {
        val roomEntry =
            guild.tempRoomCollection.getRoomByAuthor(memberId.toString())
                ?: createDefaultRoomEntry(memberId).also { guild.tempRoomCollection.updateRoom(it) }

        val channel =  guild.createVoiceChannel(roomEntry.name) {
            permissionOverwrites = roomEntry.overwrites.toMutableSet()
        }

        guild.tempRoomCollection.updateRoom(roomEntry.copy(previousRoomId = channel.id.toString()))

        return channel
    }

    private suspend fun moveUserToRoom(guildMember: Member, voiceChannel: VoiceChannel) {
        guildMember.edit {
            voiceChannelId = voiceChannel.id;
        }
    }

    private fun createDefaultRoomEntry(memberId: Snowflake): TempRoomEntry {
        val permissions = PermissionOverwriteBuilder(OverwriteType.Member, memberId).apply {
            allowed = Permissions(Permission.Connect)
        }.toOverwrite()

        return TempRoomEntry(
            name = "Room",
            overwrites = setOf(permissions),
            authorId = memberId.toString(),
            previousRoomId = null
        )
    }
}

private class TempRoomCreatorArguments: Arguments() {
    val idCreator by string {
        name = "id"
        description = "creator room id"
    }
}