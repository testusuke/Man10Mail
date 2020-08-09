package net.testusuke.open.man10mail

import net.testusuke.open.man10mail.Main.Companion.plugin
import net.testusuke.open.man10mail.Main.Companion.prefix
import org.bukkit.entity.Player
import java.util.*

/**
 * Created on 2020/07/07
 * Author testusuke
 */
object MailNoticeSetting {
    private val noticeList = mutableListOf<String>()

    fun loadList() {
        noticeList.clear()
        val listString = plugin.config.getString("notice").toString()
        val listArgs = listString.split(",")
        for (uuid in listArgs) {
            if(uuid == "")continue
            noticeList.add(uuid)
        }
        plugin.logger.info("load notice data ${noticeList.size} players.")
    }

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
        plugin.config.set("notice", listString)
        plugin.saveConfig()
        plugin.logger.info("saved notice data.")
    }

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