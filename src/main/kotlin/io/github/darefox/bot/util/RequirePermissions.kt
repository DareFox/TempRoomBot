package io.github.darefox.bot.util

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommandContext
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.common.entity.Permission
import dev.kord.rest.builder.message.create.embed

/**
 * Require these permissions to run slash command
 *
 * Returns **NULL** if requirements are not met.
 * Useful for fast exit via `?: return`
 */
suspend fun <T : Arguments> EphemeralSlashCommandContext<T>.requirePermissions(list: Set<Permission>): Unit? {
    val sufficientPermissions = member?.asMember()?.getPermissions()?.values?.containsAll(list) ?: false

    if (!sufficientPermissions) {
        respond {
            embed {
                title = "You don't have permissions to run this command."
            }
        }
    }

    return if (sufficientPermissions) Unit else null
}