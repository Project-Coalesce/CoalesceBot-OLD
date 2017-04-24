package com.coalesce.commands.executors

import com.coalesce.commands.Command
import com.coalesce.commands.CommandError
import com.coalesce.commands.CommandExecutor
import com.google.gson.Gson
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageChannel
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import javax.net.ssl.HttpsURLConnection

@Command(name = "yesno", aliases = arrayOf("yesorno"), description = "*Yes or no? Ask the bot!", permission = "commands.yesno")
class YesNo : CommandExecutor() {

    val gson = Gson()

    override fun execute(channel: MessageChannel, message: Message, args: Array<String>) {

        throw CommandError(sendGet("https://tinyrd.ml/api/yesno.php"))
    }

    @Throws(Exception::class)
    fun sendGet(url: String): String {

        val obj = URL(url)
        val con = obj.openConnection() as HttpsURLConnection
        con.requestMethod = "GET"
        con.setRequestProperty("User-Agent", "CodingClubBot/1.0")

        val input = BufferedReader(InputStreamReader(con.inputStream))
        var inputLine: String
        val response = StringBuffer()
        inputLine = input.readLine()

        while (inputLine != null) {
            response.append(inputLine)
            if(input.readLine() == null)
                break
            inputLine = input.readLine()
        }
        input.close()
        return response.toString()

    }

    internal inner class YesNoAPI {
        var answer: String? = null
        var forced: String? = null
        var image: String? = null
    }
}
