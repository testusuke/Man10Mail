package net.testusuke.open.man10mail

import net.testusuke.open.man10mail.Main.Companion.plugin
import net.testusuke.open.man10mail.Main.Companion.prefix
import org.bukkit.Bukkit
import org.bukkit.configuration.Configuration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File
import java.io.IOException
import java.util.*

/**
 * Created on 2020/07/07
 * Author testusuke
 */
object MailNoticeSetting {
    private val noticeList = mutableListOf<String>()

    /*
    //  config
    private lateinit var file: File

    private val config:YamlConfiguration by lazy {
        var c = YamlConfiguration()
        try {
            val directory: File = Main.plugin.dataFolder
            if (!directory.exists()) directory.mkdir()
            file = File(directory, "notice.yml")
            if (!file.exists()) {
                file.createNewFile()
            }
            c = YamlConfiguration.loadConfiguration(file)
        } catch (e: IOException) {
            e.printStackTrace()
            Main.plugin.logger.warning("コンフィグのロードに失敗しました。")
        }
        c
    }
     */
    fun loadList() {
        noticeList.clear()
        /*
        val listString = this.config.getString("notice").toString()
        val listArgs = listString.split(",")
        for (uuid in listArgs) {
            if(uuid == "")continue
            noticeList.add(uuid)
        }
        plugin.logger.info("load notice data ${noticeList.size} players.")
         */
        for(player in Bukkit.getOnlinePlayers()){
            noticeList.add(player.uniqueId.toString())
        }
    }

    /*
    fun saveList() {
        var listString = ""
        for (uuid in noticeList) {
            if(uuid == "") continue
            if(listString == ""){
                listString += uuid
                continue
            }
            listString += ",$uuid"
        }
        this.config.set("notice", listString)
        this.config.save(file)
        plugin.logger.info("saved notice data.")
    }
     */

    fun enableNotice(player: Player) {
        noticeList.add(player.uniqueId.toString())
        player.sendMessage("${prefix}§a通知を有効にしました。")
    }

    fun disableNotice(player: Player) {
        noticeList.remove(player.uniqueId.toString())
        player.sendMessage("${prefix}§c通知を無効にしました。")
    }

    fun isEnableNotice(player: Player): Boolean {
        return noticeList.contains(player.uniqueId.toString())
    }

    fun smartModeChange(player: Player) {
        if (isEnableNotice(player)) {
            disableNotice(player)
        } else {
            enableNotice(player)
        }
    }
}