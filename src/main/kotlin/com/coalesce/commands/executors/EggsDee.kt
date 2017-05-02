package com.coalesce.commands.executors

import com.coalesce.commands.Command
import com.coalesce.commands.CommandExecutor
import com.coalesce.commands.CommandType
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageChannel
import java.util.concurrent.TimeUnit

@Command(name = "xD", aliases = arrayOf("bramsblessing", "eggsdee", "eggsd"), description = "Meme command (Eggs dee, boi.)", permission = "commands.eggsdee",
        globalCooldown = 30, type = CommandType.FUN)
class EggsDee : CommandExecutor() {
    override fun execute(channel: MessageChannel, message: Message, args: Array<String>) {
        channel.sendMessage(":egg: :egg: :regional_indicator_d: :regional_indicator_e: :regional_indicator_e:").queue { it.delete().queueAfter(20, TimeUnit.SECONDS) }
    }
}
