package net.testusuke.open.man10mail.DataBase

import net.testusuke.open.man10mail.MailUtil
import net.testusuke.open.man10mail.Main.Companion.plugin
import net.testusuke.open.man10mail.Main.Companion.prefix
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

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
        val inventory = Bukkit.createInventory(null, 54, INVENTORY_TITLE)
        //  Message
        player.sendMessage("${prefix}§a§lデータベースに問い合わせています。少々お待ちください。please wait.query a database.")
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
            val to = resultSet.getString("to_player")
            val from = formatFromUser(resultSet.getString("from_player"))
            val title = resultSet.getString("title")
            val tag = MailUtil.formatTag(resultSet.getString("tag"))
            val date = resultSet.getString("date")
            val read = resultSet.getBoolean("read")
            val id = resultSet.getInt("id")
            val item = createMailItem(to, from, title, MailUtil.formatTag(tag), date, read, id)
            inventory.setItem(index, item)
            index++
        }
        resultSet.close()
        statement.close()

    }

    fun showMail(player: Player, mailID: Int) {
        //  Message
        player.sendMessage("${prefix}§a§lデータベースに問い合わせています。少々お待ちください。Please wait.Query a database.")
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
        while (resultSet.next()) {
            val tag = MailUtil.formatTag(resultSet.getString("tag"))
            val messageList = resultSet.getString("message").split(";")
            var message: String = ""
            for (ms in messageList) {
                message += "$ms\n"
            }
            var mailMessage = """
                §6タイトル(title): ${resultSet.getString("title")}
                §6タグ(tag): ${MailUtil.formatTag(tag)}
                §6メッセージ(message):
                $message
                §6送信元(from): ${formatFromUser(resultSet.getString("from_player"))}
                §6日付(date): ${resultSet.getString("date")}
            """.trimIndent()
            player.sendMessage(mailMessage)

            //  既読
            val read = resultSet.getBoolean("read")
            if (!read) {
                statement.executeUpdate("UPDATE mail_list SET `read`=true WHERE to_player='${player.uniqueId}' AND id='${resultSet.getInt("id")}';")
            }
        }
        resultSet.close()
        statement.close()

    }

    private fun formatFromUser(str: String): String {
        return if (str.startsWith("&")) {    //  Server
            str.substring(1)
        } else if (str.startsWith("#")) {  //  Custom
            str.substring(1)
        } else {
            val player = Bukkit.getServer().getPlayer(str)
            if (player != null) {
                return player.uniqueId.toString()
            }
            val offlinePlayer = Bukkit.getServer().getOfflinePlayer(str)
            offlinePlayer.uniqueId.toString()
        }
    }

    private fun createMailItem(to: String, from: String, title: String, tag: String, date: String, read: Boolean, id: Int): ItemStack {
        val itemStack = ItemStack(Material.PAPER)
        val meta = itemStack.itemMeta
        meta.setDisplayName("§d件名(title): $title")
        val lore = ArrayList<String>()
        lore.add("§6送信元(from): $from")
        lore.add("§6送信先(to): $to")
        lore.add("§6タグ(tag): $tag")
        lore.add("§6日付(date): $date")
        if (read) {
            lore.add("§6既読(read): §a済")
        } else {
            lore.add("§6既読(read): §c未")
        }
        //  MailID
        lore.add("$id")
        meta.lore = lore
        itemStack.itemMeta = meta

        return itemStack
    }
}