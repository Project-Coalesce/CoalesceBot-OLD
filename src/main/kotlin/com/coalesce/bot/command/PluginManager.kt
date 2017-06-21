package com.coalesce.bot.command

import com.coalesce.bot.gson
import com.coalesce.bot.pluginsFolder
import com.coalesce.bot.utilities.readText
import com.coalesce.bot.utilities.tryLog
import java.io.File
import java.net.URLClassLoader

class PluginManager {
    private val plugins = mutableListOf<Plugin>()
    val registeredPlugins: List<Plugin> = plugins
    val addedCommands = mutableListOf<Class<*>>()

    init {
        if (pluginsFolder.exists()) {
            println("Registering plugins...")
            pluginsFolder.listFiles { dir, name -> name.endsWith(".jar") }.forEach {
                tryLog("Failed to register plugin at ${it.absolutePath}") {
                    val classLoader = PluginClassLoader(it, javaClass.classLoader)
                    val jsonInfo = gson.fromJson(classLoader.getResourceAsStream("info.json").readText(), PluginData::class.java)
                    println("Loading [${jsonInfo.name}]")
                    val clazz = classLoader[jsonInfo.main]

                    val plugin = clazz.newInstance() as Plugin
                    plugins.add(plugin.apply {
                        pluginClassLoader = classLoader
                        file = it
                        pluginData = jsonInfo
                        pluginManager = this@PluginManager
                    })
                    plugin.onRegister()
                }
            }
        } else pluginsFolder.mkdirs()
    }

    data class PluginData(
         val main: String,
         val name: String,
         val packagesScan: List<String>
    )
}

open class Plugin {
    lateinit var file: File
    lateinit var pluginClassLoader: PluginClassLoader
    lateinit var pluginData: PluginManager.PluginData
    lateinit var pluginManager: PluginManager

    fun registerCommand(vararg commandHandler: Class<*>) = pluginManager.addedCommands.addAll(commandHandler)

    open fun onRegister() {}
}

class PluginClassLoader(val file: File, parent: ClassLoader): URLClassLoader(arrayOf(file.toURI().toURL()), parent) {
    operator fun get(name: String): Class<*> = Class.forName(name, true, this)
}
