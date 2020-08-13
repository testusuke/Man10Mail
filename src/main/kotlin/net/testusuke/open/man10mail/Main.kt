package net.testusuke.open.man10mail

import net.testusuke.open.man10mail.DataBase.DataBase
import org.apache.commons.lang.StringEscapeUtils
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

    //  money of send mail
    val MONEY_SEND_MAIL:Int by lazy {
        try {
            config.getInt("money")
        }catch (e:Exception){
            logger.warning("can't get money of send mail.check configuration.")
            0
        }
    }

    override fun onEnable() {
        //  instance
        plugin = this
        //  Config
        this.saveDefaultConfig()
        //  DB
        dataBase = DataBase(prefix)
        //  Command
        getCommand("mmail")?.setExecutor(MailCommand)
        //  Event
        server.pluginManager.registerEvents(EventListener, this)
        //  Vault
        VaultManager.setup()
        //  NoticeData
        MailNoticeSetting.loadList()
        //  CoolTime
        CoolTime.prepare()

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
        //  DB
        dataBase.connection?.close()
    }
}