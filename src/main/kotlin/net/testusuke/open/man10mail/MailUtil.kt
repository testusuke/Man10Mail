package net.testusuke.open.man10mail

import net.testusuke.open.man10mail.DataBase.MailBox
import net.testusuke.open.man10mail.DataBase.MailConsole
import net.testusuke.open.man10mail.DataBase.MailSenderType
import net.testusuke.open.man10mail.Main.Companion.plugin
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

/**
 * Created by testusuke on 2020/07/11
 * @author testusuke
 */
object MailUtil {

    /**
     * DBへの保存用に変換
     * @param tag[String] tag
     *
     */
    fun convertTag(tag: String): String {
        return when (tag) {
            "0" -> {    //  normal = 0
                "normal"
            }
            "5" -> {    //  notice=5
                "#5"
            }
            "6" -> {    //  information=6
                "#6"
            }
            else -> {   //  normal=else
                tag
            }
        }
    }

    fun formatTag(tag: String): String {
        return when (tag) {
            "normal" -> {    //  normal = 0
                "§6§lNormal"
            }
            "#5" -> {    //  notice=5
                "§a§lNotice"
            }
            "#6" -> {    //  information=6
                "§d§lInformation"
            }
            else -> {   //  normal=else
                "§6§l${tag}"
            }
        }
    }

    enum class TagType{NORMAL,NOTICE,INFORMATION,CUSTOM}
    fun getTagType(tag: String): TagType {
        return when (tag) {
            "normal" -> {    //  normal = 0
                TagType.NORMAL
            }
            "#5" -> {    //  notice=5
                TagType.NOTICE
            }
            "#6" -> {    //  information=6
                TagType.INFORMATION
            }
            else -> {   //  normal=else
                TagType.CUSTOM
            }
        }
    }
    /**
     * send message
     */
    fun sendMailMessage(player:Player,msg:String){
        val messages = msg.split(";")
        var i = 0
        for (m in messages) {
            if(i == 0){
                player.sendMessage(m.substring(1).replace("&","§"))
                i++
                continue
            }
            player.sendMessage(m.replace("&","§"))
            i++
        }
    }
    fun sendMailMessage(sender:CommandSender,msg:String){
        val messages = msg.split(";")
        var i = 0
        for (m in messages) {
            if(i == 0){
                sender.sendMessage(m.substring(1).replace("&","§"))
                i++
                continue
            }
            sender.sendMessage(m.replace("&","§"))
            i++
        }
    }

    /**
     * Set/Get NBT tag to/from Item
     * @param id[Int] Mail ID
     * @param item[ItemStack] Item
     * @return item[ItemStack] item
     */
    //  Setter
    fun setMailID(id:Int,item:ItemStack): ItemStack{
        val meta = item.itemMeta
        meta.persistentDataContainer.set(NamespacedKey(plugin,"id"), PersistentDataType.INTEGER, id)
        item.itemMeta = meta
        return item
    }
    //  Getter
    fun getMailID(item:ItemStack):Int{
        val meta = item.itemMeta
        return meta.persistentDataContainer[NamespacedKey(plugin,"id"), PersistentDataType.INTEGER] ?: return -0
    }

    /**
     * function of create item that exported mail
     * @param info[MailInformation]
     * @return item[ItemStack]
     */
    fun createExportedMail(info:MailConsole.MailInformation):ItemStack {
        val item = ItemStack(Material.PAPER)
        val meta = item.itemMeta
        //  title
        meta.setDisplayName("§d件名(title): ${info.title.replace("&","§")}")
        //  Lore
        val lore = ArrayList<String>()
        lore.add("§6送信元(from): ${MailBox.formatFromUser(info.from).replace("&","§")}")
        lore.add("§6タグ(tag): ${formatTag(info.tag).replace("&","§")}")
        lore.add("§6メッセージ(msg):")
        //  for
        val messages = info.message.split(";")
        var i = 0
        for (m in messages) {
            if(i == 0){
                lore.add(m.substring(1).replace("&","§"))
                i++
                continue
            }
            lore.add(m.replace("&","§"))
        }
        lore.add("§6日付(date): ${info.date}")
        //  meta
        meta.lore = lore
        item.itemMeta = meta

        return item
    }

}