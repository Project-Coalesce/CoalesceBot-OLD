package com.coalesce.commands.executors

import com.coalesce.commands.Command
import com.coalesce.commands.CommandError
import com.coalesce.commands.CommandExecutor
import com.coalesce.utils.Http
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageChannel

@Command(name = "yesno", aliases = arrayOf("yesorno"), description = "*Yes or no? Ask the bot!", permission = "commands.yesno")
class YesNo : CommandExecutor() {

    override fun execute(channel: MessageChannel, message: Message, args: Array<String>) {
        throw CommandError(Http.sendGet("https://tinyrd.ml/api/yesno.php"))

    }
}
