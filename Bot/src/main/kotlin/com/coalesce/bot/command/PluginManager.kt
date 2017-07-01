package com.coalesce.bot.command

import com.coalesce.bot.gson
import com.coalesce.bot.pluginsFolder
import com.coalesce.bot.utilities.readText
import com.coalesce.bot.utilities.truncate
import com.coalesce.bot.utilities.tryLog
import java.io.File
import java.io.IOException
import java.net.URLClassLoader

class PluginManager {
    private val plugins = mutableListOf<Plugin>()
    internal val registeredPlugins: List<Plugin> = plugins
    internal val addedCommands = mutableListOf<Class<*>>()
    internal val addedGuiceInjections = mutableMapOf<Class<*>, Any>()

    init {
        if (pluginsFolder.exists()) {
            println("Registering plugins...")
            pluginsFolder.listFiles { dir, name -> name.endsWith(".jar") }.forEach {
                tryLog("Failed to register plugin at ${it.absolutePath}") {
                    val classLoader = PluginClassLoader(it, javaClass.classLoader)
                    val jsonInfo = gson.fromJson((classLoader.getResourceAsStream("info.json") ?: throw IOException("Plugin doesn't contain info.json!")).readText(), PluginData::class.java)
                    val plugin = classLoader[jsonInfo.main] as? Plugin? ?: Plugin()
                    plugins.add(plugin.apply {
                        pluginClassLoader = classLoader
                        file = it
                        pluginData = jsonInfo
                        pluginManager = this@PluginManager
                    })
                    plugin.onRegister()
                    addedGuiceInjections[plugin.javaClass] = plugin
                }
            }
            println("Registered ${plugins.size} plugins: ${plugins.joinToString(separator = ", ") { it.pluginData.name }.truncate(0, 1000)}")
        } else pluginsFolder.mkdirs()
    }

    data class PluginData(
         val main: String?,
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
    protected fun addGuiceInjection(clazz: Class<*>, value: Any) {

        pluginManager.addedGuiceInjections[clazz] = value
    }

    open fun onRegister() {}
}

class PluginClassLoader(val file: File, parent: ClassLoader): URLClassLoader(arrayOf(file.toURI().toURL()), parent) {
    operator fun get(name: String?): Any? = if (name == null) null else Class.forName(name, true, this).newInstance()
}
