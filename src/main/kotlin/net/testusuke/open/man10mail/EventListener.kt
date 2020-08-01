package net.testusuke.open.man10mail

import net.testusuke.open.man10mail.DataBase.MailBox
import net.testusuke.open.man10mail.DataBase.MailConsole
import net.testusuke.open.man10mail.Main.Companion.plugin
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable

/**
 * Created by testusuke on 2020/07/04
 * @author testusuke
 */
object EventListener : Listener {

    //  InventoryClick
    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked
        if (player !is Player) return
        val title = event.view.title
        if (title == MailBox.INVENTORY_TITLE) {
            //  cancel
            event.isCancelled = true
            //  ItemStack if null -> return
            val item = event.currentItem ?: return
            if (item.type != Material.PAPER) return
            val id = checkMailID(item)
            if (id == -1) return
            //  show
            object : BukkitRunnable(){
                override fun run() {
                    MailBox.showMail(player, id)
                }
            }.runTask(plugin)
            player.closeInventory()
        }
    }

    //  PlayerJoinEvent
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        //  Check
        object : BukkitRunnable(){
            override fun run() {
                MailConsole.sendEveryoneMail(player.uniqueId.toString())
            }
        }.runTask(plugin)

    }

    /**
     * function of check mail id
     * @param item[ItemStack]
     * @return id[Int]
     */
    private fun checkMailID(item: ItemStack): Int {
        var id: Int?
        val meta = item.itemMeta ?: return -1
        val lore = meta.lore ?: return -1
        val lastIndex = lore.size - 1
        id = lore[lastIndex].toIntOrNull()
        if (id == null) return -1
        return id
    }
}