package com.coalesce.bot.music

import com.coalesce.bot.command.Plugin

class MusicPlugin: Plugin() {
    override fun onRegister() {
        addGuiceInjection(MusicBot::class.java, MusicBot())
    }
}