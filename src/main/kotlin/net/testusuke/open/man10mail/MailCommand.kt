package net.testusuke.open.man10mail

import net.testusuke.open.man10mail.DataBase.*
import net.testusuke.open.man10mail.Main.Companion.enable
import net.testusuke.open.man10mail.Main.Companion.plugin
import net.testusuke.open.man10mail.Main.Companion.prefix
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import java.util.*

/**
 * Created by testusuke on 2020/07/04
 * @author testusuke
 */
object MailCommand : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission(Permission.GENERAL)) {
            sendPermissionError(sender)
            return false
        }

        if (args.isEmpty()) {
            if (sender is Player) {
                if (!enable) {
                    sendDisable(sender)
                    return false
                }
                if (!sender.hasPermission(Permission.OPEN_MAIL_BOX)) {
                    sendPermissionError(sender)
                    return false
                }
                sender.sendMessage("${prefix}§aメールボックスを開きます。")
                object : BukkitRunnable(){
                    override fun run() {
                        MailConsole.sendEveryoneMail(sender.uniqueId.toString())
                        MailBox.openMailBox(sender)
                    }
                }.runTask(plugin)

                return true
            } else {
                sendNotPlayerError(sender)
                return false
            }
        }
        //  args[0]
        when (args[0]) {
            "help" -> {
                sendHelp(sender)
            }
            "notice" -> {
                if (sender !is Player) return false
                if (!enable) {
                    sendDisable(sender)
                    return false
                }
                MailNoticeSetting.smartModeChange(sender)
            }
            "send" -> {
                if (sender !is Player) return false
                if (!sender.hasPermission(Permission.SEND_MAIL)) {
                    sendPermissionError(sender)
                    return false
                }
                if (!enable) {
                    sendDisable(sender)
                    return false
                }
                //  args size
                if (args.size < 2) {
                    sender.sendMessage("${prefix}§c送信先を指定してください。please enter player name or uuid. /mmail send <player> <title> <message> [tag]")
                    return false
                }
                val id = args[1]
                val targetPlayer = Bukkit.getOfflinePlayer(id)
                val uuid = targetPlayer.uniqueId.toString()
                if (uuid.isEmpty()) {
                    sender.sendMessage("${prefix}§cプレイヤー情報を取得できませんでした。could not get player info.")
                    return false
                }
                //  Title
                if (args.size < 3) {
                    sender.sendMessage("${prefix}§cタイトルを入力してください。please enter mail title.")
                    return false
                }
                val title = args[2]
                //  Message
                if (args.size < 4) {
                    sender.sendMessage("${prefix}§cメッセージを入力してください。please enter message.")
                    return false
                }
                object : BukkitRunnable() {
                    override fun run() {
                        val message = formatMessage(args, 3)
                        //  Send Mail
                        val result = MailConsole.sendMail(sender.uniqueId.toString(), uuid, title, "0", message, MailSenderType.PLAYER)
                        if (result is MailResult.Success) {
                            val mailID = result.id

                            sender.sendMessage("${prefix}§aメールを送信します。")
                            sender.sendMessage("§6メール番号: $mailID")
                            sender.sendMessage("§6タイトル(title): ${title.replace("&","§")}")
                            /*
                            sender.sendMessage("§6メール本文:")
                            MailUtil.sendMailMessage(sender,message)
                             */

                        } else if (result is MailResult.Error) {
                            when (result.reason) {
                                MailErrorReason.CAN_NOT_ACCESS_DB -> {
                                    sender.sendMessage("${prefix}§c失敗しました。")
                                    cancel()
                                }
                            }
                        }
                    }
                }.runTask(plugin)
            }
            "send-tag" -> {
                if (sender is Player) {
                    if (!sender.hasPermission(Permission.ADMIN)) {
                        sender.sendMessage("${prefix}§c権限がありません。You don't have permission.")
                        return false
                    }
                    //  args size
                    if (args.size < 2) {
                        sender.sendMessage("${prefix}§c送信先を指定してください。please enter player name or uuid. /mmail send <player> <title> <message> [tag]")
                        return false
                    }
                    val id = args[1]
                    val targetPlayer = Bukkit.getOfflinePlayer(id)
                    val uuid = targetPlayer.uniqueId.toString()
                    if (uuid.isEmpty()) {
                        sender.sendMessage("${prefix}§cプレイヤー情報を取得できませんでした。could not get player info.")
                        return false
                    }
                    //  Title
                    if (args.size < 3) {
                        sender.sendMessage("${prefix}§cタイトルを入力してください。please enter mail title.")
                        return false
                    }
                    val title = args[2]
                    //  tag
                    if (args.size < 4) {
                        sender.sendMessage("${prefix}§cタグを設定してください。please enter tag name.")
                        return false
                    }
                    val tag = args[3]
                    //  Message
                    if (args.size < 5) {
                        sender.sendMessage("${prefix}§cメッセージを入力してください。please enter message.")
                        return false
                    }
                    object : BukkitRunnable() {
                        override fun run() {
                            val message = formatMessage(args, 4)
                            //  Send Mail
                            val result = MailConsole.sendMail(sender.uniqueId.toString(), uuid, title, tag, message, MailSenderType.PLAYER)
                            //  Send Mail
                            if (result is MailResult.Success) {
                                val mailID = result.id

                                sender.sendMessage("${prefix}§aメールを送信します。")
                                sender.sendMessage("§6メール番号: $mailID")
                                sender.sendMessage("§6タイトル(title): ${title.replace("&","§")}")
                                /*
                                sender.sendMessage("§6メール本文:")
                                MailUtil.sendMailMessage(sender,message)
                                 */

                            } else if (result is MailResult.Error) {
                                when (result.reason) {
                                    MailErrorReason.CAN_NOT_ACCESS_DB -> {
                                        sender.sendMessage("${prefix}§c失敗しました。")
                                        cancel()
                                    }
                                }
                            }
                        }
                    }.runTask(plugin)
                } else {
                    //  from
                    if (args.size < 2) return false
                    val from = args[1]
                    //  args size
                    if (args.size < 3) return false
                    val id = args[2]
                    val targetPlayer = Bukkit.getOfflinePlayer(id)
                    val uuid = targetPlayer.uniqueId.toString()
                    if (uuid.isEmpty()) return false
                    //  Title
                    if (args.size < 4) return false
                    val title = args[3]
                    //  tag
                    if (args.size < 5) return false
                    val tag = args[4]
                    //  Message
                    if (args.size < 6) return false

                    object : BukkitRunnable() {
                        override fun run() {
                            val message = formatMessage(args, 5)
                            //  Send Mail
                            val result = MailConsole.sendMail(from, uuid, title, tag, message, MailSenderType.CUSTOM)
                            //  Send Mail
                            if (result is MailResult.Success) {
                                val mailID = result.id

                                sender.sendMessage("${prefix}§aメールを送信します。")
                                sender.sendMessage("§6メール番号: $mailID")
                                sender.sendMessage("§6タイトル(title): ${title.replace("&","§")}")
                                /*
                                sender.sendMessage("§6メール本文:")
                                MailUtil.sendMailMessage(sender,message)
                                 */

                            } else if (result is MailResult.Error) {
                                when (result.reason) {
                                    MailErrorReason.CAN_NOT_ACCESS_DB -> {
                                        sender.sendMessage("${prefix}§c失敗しました。")
                                        cancel()
                                    }
                                }
                            }
                        }
                    }.runTask(plugin)
                }
            }

            "send-all" -> {
                if (!sender.hasPermission(Permission.ADMIN)) {
                    sender.sendMessage("${prefix}§c権限がありません。You don't have permission.")
                    return false
                }
                //  Title
                if (args.size < 2) {
                    sender.sendMessage("${prefix}§cタイトルを入力してください。please enter mail title.")
                    return false
                }
                val title = args[1]
                //  tag
                if (args.size < 3) return false
                val tag = args[2]
                //  Message
                if (args.size < 4) {
                    sender.sendMessage("${prefix}§cメッセージを入力してください。please enter message.")
                    return false
                }
                object : BukkitRunnable() {
                    override fun run() {
                        val message = formatMessage(args, 3)
                        //  Send Mail
                        val result = MailConsole.issueEveryoneMail("SERVER", title, tag, message, MailSenderType.SERVER)
                        if (result is MailResult.Success) {
                            val mailID = result.id

                            sender.sendMessage("${prefix}§aメールを送信します。")
                            sender.sendMessage("§6メール番号: $mailID")
                            sender.sendMessage("§6タイトル(title): ${title.replace("&","§")}")
                            /*
                            sender.sendMessage("§6メール本文:")
                            MailUtil.sendMailMessage(sender,message)
                             */

                        } else if (result is MailResult.Error) {
                            when (result.reason) {
                                MailErrorReason.CAN_NOT_ACCESS_DB -> {
                                    sender.sendMessage("${prefix}§c失敗しました。")
                                    cancel()
                                }
                            }
                        }
                    }
                }.runTask(plugin)
            }

            "on" -> {
                changeEnable(sender, true)
            }
            "off" -> {
                changeEnable(sender, false)
            }

            /* §c/mmail remove <mail-id> <- 指定したIDのメールを削除します。
            "remove" -> {
                if(sender is Player){
                    //  int check
                    val id = args[1].toIntOrNull()
                    if (id == null) {
                        sender.sendMessage("${prefix}§c半角数字で入力してください。")
                        return false
                    }
                    //  op
                    if(sender.hasPermission(Permission.ADMIN)){
                        if(MailConsole.removeMail(id)){
                            sender.sendMessage("${prefix}§aメールを削除しました。id: $id")
                        }else{
                            sender.sendMessage("${prefix}§c削除できませんでした。")
                        }
                        return true
                    }
                    //  所有権チェック
                    val mailInfo = MailConsole.getInformation(id)
                    if(mailInfo == null){
                        sender.sendMessage("${prefix}§cIDが誤っています。")
                        return false
                    }
                    val toPlayer = mailInfo.to
                }
            }
            */

            else -> sendHelp(sender)
        }
        return false
    }

    private fun changeEnable(sender: CommandSender, mode: Boolean) {
        if (!sender.hasPermission(Permission.ADMIN)) {
            sendPermissionError(sender)
            return
        }
        if (enable == mode) {
            sender.sendMessage("${prefix}§cすでに§e${mode}§cになっています。")
        } else {
            enable = mode
            sender.sendMessage("${prefix}§aプラグインが§e${mode}§aになりました。")
        }
    }

    private fun sendPermissionError(sender: CommandSender) {
        sender.sendMessage("${ChatColor.RED}You do not have permission.")
    }

    private fun sendNotPlayerError(sender: CommandSender) {
        sender.sendMessage("${ChatColor.RED}can not use console.")
    }

    private fun sendDisable(player: Player) {
        player.sendMessage("${prefix}§c§l現在利用できません。")
    }

    private fun sendHelp(sender: CommandSender) {
        val msg = """
            §e§l====================================
            §6§l/mmail -> メールボックスを開きます。
            §6§l/mmail help -> ヘルプを表示します。
            §6§l/mmail send <player> <title> <message> <- メールを送信します。メッセージの改行は[;]を入力してください。
            §6§l/mmail notice <- 通知を有効/無効にします。
            §c§lAdmin Commands
            §c§l/mmail send-tag/send-all/remove these command can use on the console.
            §c§l/mmail send-tag <player> <title> <tag> <message> <- タグ付きでメッセージを送信します。tag 0<-normal 5<-notice 6<-information etc...
            §c§l/mmail send-tag <from> <player> <title> <tag> <message> <- 発信元を指定 §c§l※プレイヤーからの実行はできません。
            §c§l/mmail send-all <title> <tag> <message> <- 全体メッセージを送信します。
            §c§l/mmail on/off <- プラグインを有効化/無効化します。
            §d§lCreated by testusuke
            §e§l====================================
        """.trimIndent()
        sender.sendMessage(msg)
    }

    /**
     * 改行などをするformatの関数
     * @param args[Array] message
     * @param index[Int] miss format index
     *
     * @return [String] formatted message
     */
    private fun formatMessage(args: Array<out String>, index: Int): String {
        var formatted = ""
        for ((i, str) in args.withIndex()) {
            if (i >= index) {
                formatted += " $str"
            }
        }
        return formatted
    }

}