package net.testusuke.open.man10mail

import org.bukkit.plugin.java.JavaPlugin

/**
 * Created by testusuke on 2020/07/03
 * @author testusuke
 */
class Main:JavaPlugin() {

    companion object{
        lateinit var plugin: Main
    }

    override fun onEnable() {
        //  instance
        plugin = this
        //  Logger

        //  Command
        getCommand("mmail")?.setExecutor(MailCommand)
        //  Event
        server.pluginManager.registerEvents(EventListener,this)
    }

    override fun onDisable() {

    }
}