package com.coalesce.commands.executors

import com.coalesce.commands.Command
import com.coalesce.commands.CommandExecutor
import com.coalesce.commands.CommandType
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageChannel

@Command(name = "ProjectCoalesce", aliases = arrayOf("whatisprojectcoalesce", "coalesce"), description = "Shows a brief description of what is project coalesce for those wondering",
        permission = "commands.coalesceexplanation", globalCooldown = 60, type = CommandType.INFORMATION)
class ProjectCoalesce : CommandExecutor() {
    override fun execute(channel: MessageChannel, message: Message, args: Array<String>) {
        channel.sendMessage("Project-Coalesce is a diverse group of developers. We specialize in plugins using the Spigot-API, however we are open to all kinds of development.").queue()
    }
}
