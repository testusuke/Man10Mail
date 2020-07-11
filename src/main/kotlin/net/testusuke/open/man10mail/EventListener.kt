package net.testusuke.open.man10mail

import net.testusuke.open.man10mail.DataBase.MailBox
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent

/**
 * Created by testusuke on 2020/07/04
 * @author testusuke
 */
object EventListener:Listener {

    //  InventoryClick
    @EventHandler
    fun onInventoryClick(event:InventoryClickEvent){
        val player = event.whoClicked
        val inv = event.inventory
        val title = event.view.title
        if(title == MailBox.INVENTORY_TITLE){

        }
    }
}