package net.testusuke.open.man10mail

import org.bukkit.Bukkit
import org.bukkit.entity.Player

/**
 * Created on 2020/07/07
 * Author testusuke
 */
object MailBox {
    const val INVENTORY_TITLE = ""

    /**
     * function of open mail box.
     * @param player[Player] Player
     * @return
     */
    fun openMailBox(player:Player){
        val inventory = Bukkit.createInventory(null,54, INVENTORY_TITLE)

    }
}