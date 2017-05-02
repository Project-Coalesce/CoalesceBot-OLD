package com.coalesce.commands.executors

import com.coalesce.commands.Command
import com.coalesce.commands.CommandError
import com.coalesce.commands.CommandExecutor
import com.coalesce.commands.CommandType
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageChannel

@Command(name = "xD", aliases = arrayOf("bramsblessing", "eggsdee", "eggsd"), description = "Meme command (Eggs dee, boi.)", permission = "commands.eggsdee",
        type = CommandType.FUN)
class EggsDee : CommandExecutor() {
    override fun execute(channel: MessageChannel, message: Message, args: Array<String>) {
        throw CommandError(":egg: :egg: :regional_indicator_d: :regional_indicator_e: :regional_indicator_e:")
    }
}
