package net.testusuke.open.man10mail

import net.testusuke.open.man10mail.Main.Companion.plugin
import org.bukkit.entity.Player
import java.util.*

/**
 * Created on 2020/07/07
 * Author testusuke
 */
object MailNoticeSetting {
    private val noticeList = mutableListOf<String>()

    fun loadList(){
        noticeList.clear()
        val listString = plugin.config.getString("notice").toString()
        val listArgs = listString.split(",")
        for(uuid in listArgs){
            noticeList.add(uuid)
        }
        plugin.logger.info("load notice data ${noticeList.size} players.")
    }
    fun saveList(){
        var listString = "";
        for (uuid in noticeList){
            listString = "${listString}${uuid},"
        }
        plugin.config.set("notice",listString)
        plugin.saveConfig()
        plugin.logger.info("saved notice data.")
    }

    fun enableNotice(player: Player){
        noticeList.add(player.uniqueId.toString())
    }
    fun disableNotice(player:Player){
        noticeList.remove(player.uniqueId.toString())
    }
    fun isEnableNotice(player: Player):Boolean{
        return noticeList.contains(player.uniqueId.toString())
    }
    fun clear(){
        noticeList.clear()
    }
}