package net.testusuke.open.man10mail

import net.testusuke.open.man10mail.DataBase.MailConsole
import net.testusuke.open.man10mail.Main.Companion.enable
import net.testusuke.open.man10mail.Main.Companion.prefix
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * Created by testusuke on 2020/07/04
 * @author testusuke
 */
object MailCommand: CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if(!sender.hasPermission(Permission.GENERAL)){
            sendPermissionError(sender)
            return false
        }

        if(args.isEmpty()){
            if(sender is Player){
                if(!enable){
                    sendDisable(sender)
                    return false
                }
                sender.sendMessage("${prefix}§aメールボックスを開きます。")
                MailBox.openMailBox(sender)
                return true
            }else{
                sendNotPlayerError(sender)
                return false
            }
        }
        //  args[0]
        when(args[0]){
            "help" -> {
                sendHelp(sender)
            }
            "" -> {
            }

        }
        return false
    }

    private fun sendPermissionError(sender:CommandSender){
        sender.sendMessage("${ChatColor.RED}You do not have permission.")
    }
    private fun sendNotPlayerError(sender:CommandSender){
        sender.sendMessage("${ChatColor.RED}can not use console.")
    }
    private fun sendDisable(player: Player){
        player.sendMessage("${prefix}§c§l現在利用できません。")
    }

    private fun sendHelp(sender: CommandSender){
        val msg = """
            
        """.trimIndent()
        sender.sendMessage(msg)
    }
}