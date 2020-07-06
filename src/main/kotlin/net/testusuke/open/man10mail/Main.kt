package net.testusuke.open.man10mail

import net.testusuke.open.man10mail.DataBase.DataBase
import org.bukkit.plugin.java.JavaPlugin
import java.lang.NullPointerException

/**
 * Created by testusuke on 2020/07/03
 * @author testusuke
 */
class Main:JavaPlugin() {

    companion object{
        lateinit var plugin: Main
        var prefix = "§e[§dMan10§aMail§e]§f"
    }
    //  Enable
    var enable = false

    //  DB
    lateinit var dataBase:DataBase

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

        //  Prefix
        try {
            enable = config.getBoolean("enable")
            prefix = config.getString("prefix").toString()
        }catch (e:NullPointerException){
            logger.info("can not load config.")
        }
    }

    override fun onDisable() {

        //  enable
        config.set("enable",enable)
        this.saveConfig()
    }
}