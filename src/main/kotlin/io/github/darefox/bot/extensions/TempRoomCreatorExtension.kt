package io.github.darefox.bot.extensions

import io.github.darefox.bot.Config
import io.github.darefox.bot.database.guild.tempRoomCollection
import io.github.darefox.bot.database.guild.tempRoomSettings
import io.github.darefox.bot.models.TempRoomEntry
import io.github.darefox.bot.models.TempRoomSettingsEntry
import io.github.darefox.bot.util.requirePermissions
import io.github.darefox.bot.util.toSnow
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
    override val name: String = "TempRoomCreatorExtension"

    override suspend fun setup() {
        event<VoiceStateUpdateEvent> {
            action {
                val settings = event.state.getGuild().tempRoomSettings.getSettings() ?: return@action;
                val guild = event.state.getGuild()

                val previousChannel = event.old?.getChannelOrNull()
                val roomEntry =  guild.tempRoomCollection.getRoomByAuthor(event.state.userId.toString())

                val noOneInOldChannel = previousChannel != null && previousChannel.voiceStates.singleOrNull() == null
                val joinedCreatorRoom = event.state.channelId.toString() == settings.creatorRoomId
                val authorLeft = roomEntry?.authorId != null && roomEntry.authorId == event.old?.userId?.toString()

                // On join creator room
                if (joinedCreatorRoom) {
                    // If last user exists channel, and it's author, and he's moved to creator room
                    // Then don't re-create channel, just move to already existing one
                    if (noOneInOldChannel && authorLeft) {
                        previousChannel ?: return@action

                        moveUserToRoom(event.state.getMember(), previousChannel.asChannelOf())
                        return@action
                    } else { // Create room and move user
                        val room = createRoomForUser(event.state.getGuild(), event.state.userId)
                        moveUserToRoom(event.state.getMember(), room)
                        return@action
                    }
                }

                // On exit of created channel and room is empty
                if (noOneInOldChannel) {
                    previousChannel ?: return@action
                    roomEntry ?: return@action

                    val newPermissions = previousChannel.asChannel().permissionOverwrites.map {
                        val data = it.data
                        Overwrite(data.id, data.type, data.allowed, data.denied)
                    }.toSet()

                    // TODO: Save bitrate and limit count
                    val newEntry = roomEntry.copy(
                        name = previousChannel.asChannel().name,
                        overwrites = newPermissions,
                    )
                    guild.tempRoomCollection.updateRoom(newEntry);
                    previousChannel.delete("Room is empty")
                    return@action
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