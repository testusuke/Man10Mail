package net.testusuke.open.man10mail

import net.testusuke.open.man10mail.DataBase.DataBase
import org.bukkit.plugin.java.JavaPlugin
import java.lang.NullPointerException

/**
 * Created by testusuke on 2020/07/03
 * @author testusuke
 */
class Main : JavaPlugin() {

    companion object {
        lateinit var plugin: Main

        //  Enable
        var enable = false
        var prefix = "§e[§dMan10§aMail§e]§f"
    }

    //  DB
    lateinit var dataBase: DataBase

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
        server.pluginManager.registerEvents(EventListener, this)
        //  NoticeData
        MailNoticeSetting.loadList()

        //  Prefix
        try {
            enable = config.getBoolean("enable")
            prefix = config.getString("prefix").toString().replace("&","§")
        } catch (e: NullPointerException) {
            logger.info("can not load config.")
        }
    }

    override fun onDisable() {

        //  save
        MailNoticeSetting.saveList()

        //  enable
        config.set("enable", enable)
        this.saveConfig()
    }
}