package net.testusuke.open.man10mail

import net.testusuke.open.man10mail.DataBase.DataBase
import org.bukkit.plugin.java.JavaPlugin

/**
 * Created by testusuke on 2020/07/03
 * @author testusuke
 */
class Main:JavaPlugin() {

    companion object{
        lateinit var plugin: Main
        lateinit var dataBase:DataBase
        var prefix = "§e[§dMan10§aMail§e]§f"
    }

    override fun onEnable() {
        //  instance
        plugin = this
        //  Logger

        //  Config
        this.saveDefaultConfig()
        //  DB
        dataBase = DataBase(prefix)
        //  Command
        getCommand("mmail")?.setExecutor(MailCommand)
        //  Event
        server.pluginManager.registerEvents(EventListener,this)
    }

    override fun onDisable() {

    }
}