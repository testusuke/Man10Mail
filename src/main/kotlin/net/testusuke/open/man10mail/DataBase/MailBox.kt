package net.testusuke.open.man10mail.DataBase

import net.testusuke.open.man10mail.MailUtil
import net.testusuke.open.man10mail.Main.Companion.plugin
import net.testusuke.open.man10mail.Main.Companion.prefix
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created on 2020/07/07
 * Author testusuke
 */
object MailBox {
    const val INVENTORY_TITLE = "§d§lMan10§a§lMail §f§lBox"

    /**
     * function of open mail box.
     * @param player[Player] Player
     * @return
     */
    fun openMailBox(player: Player) {
        //  REMOVE
        MailConsole.removeOldMail(player.uniqueId.toString())

        val inventory = Bukkit.createInventory(null, 54, INVENTORY_TITLE)
        //  DB問い合わせ
        val sql = "SELECT * FROM mail_list WHERE to_player='${player.uniqueId}' ORDER BY `date` desc LIMIT 54;"
        plugin.dataBase.open()
        val connection = plugin.dataBase.connection
        if (connection == null) {
            player.sendMessage("${prefix}§c§lエラーが発生しました。")
            plugin.dataBase.sendErrorMessage()
            return
        }
        val statement = connection.createStatement()
        val resultSet = statement.executeQuery(sql)
        var index = 0
        while (resultSet.next()) {
            //val to = resultSet.getString("to_player")
            val from = formatFromUser(resultSet.getString("from_player"))
            val title = resultSet.getString("title")
            val tag = MailUtil.formatTag(resultSet.getString("tag"))
            val message = resultSet.getString("message")
            val date = resultSet.getString("date")
            val read = resultSet.getBoolean("read")
            val id = resultSet.getInt("id")
            val tagType = MailUtil.getTagType(resultSet.getString("tag"))
            val item = createMailItem(from, title, MailUtil.formatTag(tag), date,message, read, id, tagType)
            inventory.setItem(index, item)
            index++
        }
        resultSet.close()
        statement.close()

        Bukkit.getScheduler().runTask(plugin, Runnable {
            //  OpenInventory
            player.openInventory(inventory)
        })
    }

    fun showMail(player: Player, mailID: Int) {
        //  DB
        val sql = "SELECT * FROM mail_list WHERE to_player='${player.uniqueId}' AND id='${mailID}' LIMIT 1;"
        plugin.dataBase.open()
        val connection = plugin.dataBase.connection
        if (connection == null) {
            player.sendMessage("${prefix}§c§lエラーが発生しました。")
            plugin.dataBase.sendErrorMessage()
            return
        }
        val statement = connection.createStatement()
        val resultSet = statement.executeQuery(sql)

        resultSet.next()
        val tag = MailUtil.formatTag(resultSet.getString("tag"))

        player.sendMessage("§6タイトル(title): ${resultSet.getString("title").replace("&","§")}")
        player.sendMessage("§6タグ(tag): ${MailUtil.formatTag(tag).replace("&","§")}")
        player.sendMessage("§6メッセージ(message):")
        MailUtil.sendMailMessage(player,resultSet.getString("message"))
        player.sendMessage("§6送信元(from): ${formatFromUser(resultSet.getString("from_player"))}")
        player.sendMessage("§6日付(date): ${resultSet.getString("date")}")

        //  既読
        val read = resultSet.getBoolean("read")
        if (!read) {
            statement.executeUpdate(
                "UPDATE mail_list SET `read`=true WHERE to_player='${player.uniqueId}' AND id='${resultSet.getInt(
                    "id"
                )}';"
            )
        }

        resultSet.close()
        statement.close()

    }

    fun formatFromUser(str: String): String {
        return if (str.startsWith("&")) {    //  Server
            str.substring(1)
        } else if (str.startsWith("#")) {  //  Custom
            str.substring(1)
        } else {
            try {
                val uuid = UUID.fromString(str)
                val player = Bukkit.getServer().getPlayer(uuid)
                if (player != null) {
                    return player.name
                }
                val offlinePlayer = Bukkit.getServer().getOfflinePlayer(uuid)
                offlinePlayer.name ?: "none"
            }catch (e:Exception){
                return str
            }
        }
    }

    private fun createMailItem(from: String, title: String, tag: String, date: String,message:String, read: Boolean, id: Int,tagType:MailUtil.TagType): ItemStack {
        val itemStack = ItemStack(Material.PAPER)
        val meta = itemStack.itemMeta
        meta.setDisplayName("§d件名(title): ${title.replace("&","§")}")
        val lore = ArrayList<String>()
        lore.add("§6送信元(from): $from")
        lore.add("§6タグ(tag): ${tag.replace("&","§")}")
        lore.add("§6メッセージ(msg): ${getFirstLine(message)}")
        lore.add("§6日付(date): $date")
        if (read) {
            lore.add("§6既読(read): §a○")
        } else {
            lore.add("§6既読(read): §c×")
        }
        meta.lore = lore
        //  Enchantment
        when(tagType){
            MailUtil.TagType.INFORMATION -> {
                meta.addEnchant(Enchantment.DURABILITY,1,false)
            }
        }
        itemStack.itemMeta = meta
        //  Mail ID
        return MailUtil.setMailID(id,itemStack)
    }

    private fun getFirstLine(message: String):String{
        //  replace
        val split = message.split(";")
        return split[0].replace("&","§")
    }
}