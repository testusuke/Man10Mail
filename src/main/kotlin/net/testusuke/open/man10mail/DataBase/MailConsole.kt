package net.testusuke.open.man10mail.DataBase

import net.testusuke.open.man10mail.MailNoticeSetting
import net.testusuke.open.man10mail.MailUtil
import net.testusuke.open.man10mail.Main.Companion.plugin
import net.testusuke.open.man10mail.Main.Companion.prefix
import org.apache.commons.lang.StringEscapeUtils
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.sql.PreparedStatement
import java.sql.Statement
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Created by testusuke on 2020/07/04
 * @author testusuke
 */
object MailConsole {

    /**
     * function of send mail.
     * @param from[String] 発信元(uuid or name)
     * @param to[String] 送信先(uuid)
     * @param title[String] タイトル
     * @param tag[String] タグ
     * @param message[String] メッセージ [;]で改行
     * @param senderType[MailSenderType] uuid/mcid=PLAYER server=SERVER custom sender=CUSTOM(先頭に[#]が代入される。)
     * @return mailResult[MailResult<V>]
     */
    fun sendMail(from: String, to: String, title: String, tag: String, message: String, senderType: MailSenderType): MailResult {
        //  BlockCheck
        if(checkBlock(from,to)) return MailResult.Error(MailErrorReason.BLOCKED)

        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val formatted = current.format(formatter)
        val formattedFrom = formatFromUser(from,senderType)
        //  Connection
        plugin.dataBase.open()
        val connection = plugin.dataBase.connection
        if (connection == null) {
            plugin.dataBase.sendErrorMessage()
            return MailResult.Error(MailErrorReason.CAN_NOT_ACCESS_DB)
        }

        //val sql = "INSERT INTO mail_list (to_player,to_name,from_player,to_name,title,message,tag,date) VALUES('${to}','${getPlayerName(to)}','$formattedFrom','${getPlayerName(from)}','${title}','${message}','${MailUtil.convertTag(tag)}','${formatted}');"
        val sql = "INSERT INTO mail_list (to_player,to_name,from_player,from_name,title,message,tag,date) VALUES(?, ?, ?, ?, ?, ?, ?, ?);"
        val statement = connection.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)
        statement.setString(1, to)
        statement.setString(2, getPlayerName(to))
        statement.setString(3, formattedFrom)
        statement.setString(4, getPlayerName(from))
        statement.setString(5, title)
        statement.setString(6, message)
        statement.setString(7, MailUtil.convertTag(tag))
        statement.setString(8,formatted)

        statement.executeUpdate()
        val resultSet = statement.generatedKeys
        val id = if(resultSet.next()) {
            resultSet.getInt(1)
        }else -1
        resultSet.close()
        statement.close()

        //  サーバー内にユーザーがいる場合は通知
        val player = Bukkit.getPlayer(UUID.fromString(to))
        if(player != null) {
            if (MailNoticeSetting.isEnableNotice(player)) player.sendMessage("${prefix}§6新しいメールが届いています。/mmailでメールボックスを開く")
        }
        return MailResult.Success(id)
    }

    /**
     * function of send mail to everyone.
     * @param from[String] 発信元
     * @param title[String] タイトル
     * @param tag[String] タグ
     * @param message[String] メッセージ [;]で改行
     */
    fun issueEveryoneMail(from: String, title: String, tag: String, message: String, senderType: MailSenderType): MailResult {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val formatted = current.format(formatter)
        val formattedFrom = formatFromUser(from,senderType)
        //  Connection
        plugin.dataBase.open()
        val connection = plugin.dataBase.connection
        if (connection == null) {
            plugin.dataBase.sendErrorMessage()
            return MailResult.Error(MailErrorReason.CAN_NOT_ACCESS_DB)
        }
        //val sql = "INSERT INTO mail_all (from_name,title,message,tag,`date`) VALUES('$formattedFrom','${title}','${message}','${MailUtil.convertTag(tag)}','${formatted}');"
        val sql = "INSERT INTO mail_all (from_name,title,message,tag,`date`) VALUES(?, ?, ?, ?, ?);"
        val statement = connection.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)
        statement.setString(1,formattedFrom)
        statement.setString(2,title)
        statement.setString(3,message)
        statement.setString(4,MailUtil.convertTag(tag))
        statement.setString(5,formatted)

        statement.executeUpdate()
        val resultSet = statement.generatedKeys
        val id = if(resultSet.next()) {
            resultSet.getInt(1)
        }else -1

        //  close
        resultSet.close()
        statement.close()

        //  サバ内のプレイヤーに通知
        for (player in Bukkit.getOnlinePlayers()) {
            val uuid = player.uniqueId.toString()
            sendEveryoneMail(uuid)
            if(MailNoticeSetting.isEnableNotice(player)) player.sendMessage("${prefix}§6新しいメールが届いています。/mmailでメールボックスを開く")
        }

        return MailResult.Success(id)
    }

    /**
     * function of send mail in 'mail_all' table to 'mail_list'
     * @param uuid[String] ターゲット uuid
     * @return amount[Int] 読み込んだカラム数
     */
    fun sendEveryoneMail(uuid: String): MailResult {
        plugin.dataBase.open()
        val connection = plugin.dataBase.connection
        if (connection == null) {
            plugin.dataBase.sendErrorMessage()
            return MailResult.Error(MailErrorReason.CAN_NOT_ACCESS_DB)
        }
        val selectReadSQL = "SELECT from_mail_id FROM mail_read WHERE to_player=?;"
        val selectReadStatement = connection.prepareStatement(selectReadSQL)
        selectReadStatement.setString(1,uuid)

        val selectReadResult = selectReadStatement.executeQuery()
        //  送信済みのMail ID
        val readMailList = mutableListOf<Int>()
        while (selectReadResult.next()) {
            readMailList.add(selectReadResult.getInt("from_mail_id"))
        }
        //  close
        selectReadResult.close()
        selectReadStatement.close()

        //  Mail All Table
        val selectAllSQL = "SELECT * FROM mail_all;"
        val selectAllStatement = connection.createStatement()
        val selectAllResult = selectAllStatement.executeQuery(selectAllSQL)

        var amount = 0
        while (selectAllResult.next()) {
            if (!readMailList.contains(selectAllResult.getInt("id"))) {
                val from = selectAllResult.getString("from_name")
                val title = selectAllResult.getString("title")
                val tag = selectAllResult.getString("tag")
                val message = selectAllResult.getString("message")
                val date = selectAllResult.getString("date")
                //val insertMailSQL = "INSERT INTO mail_list (to_player,to_name,from_player,from_name,title,message,tag,date) VALUES('${uuid}','${getPlayerName(uuid)}','${from}','${getPlayerName(from)}','${title}','${message}','${MailUtil.convertTag(tag)}','${date}');"
                val insertMailSQL = "INSERT INTO mail_list (to_player,to_name,from_player,from_name,title,message,tag,date) VALUES(?, ?, ?, ?, ?, ?, ?, ?);"
                val insertMailStatement = connection.prepareStatement(insertMailSQL)
                insertMailStatement.setString(1,uuid)
                insertMailStatement.setString(2,getPlayerName(uuid))
                insertMailStatement.setString(3,from)
                insertMailStatement.setString(4, getPlayerName(from))
                insertMailStatement.setString(5,title)
                insertMailStatement.setString(6,message)
                insertMailStatement.setString(7,MailUtil.convertTag(tag))
                insertMailStatement.setString(8,date)
                insertMailStatement.executeUpdate()
                //  close
                insertMailStatement.close()

                //val insertMailReadSQL = "INSERT INTO `mail_read` (`to_player`,`from_mail_id`) VALUES ('${uuid}','${selectAllResult.getInt("id")}');"
                val insertMailReadSQL = "INSERT INTO mail_read (to_player,from_mail_id) VALUES (?, ?);"
                val insertMailReadStatement = connection.prepareStatement(insertMailReadSQL)
                insertMailReadStatement.setString(1,uuid)
                insertMailReadStatement.setInt(2,selectAllResult.getInt("id"))
                insertMailReadStatement.executeUpdate()
                // close
                insertMailReadStatement.close()

                amount++
            }
        }
        selectAllResult.close()
        selectAllStatement.close()

        return MailResult.Success(amount)
    }

    /**
     * function of remove mail with id
     * @param id[Int] id
     * @return success[Boolean]
     */
    fun removeMail(id: Int): Boolean {
        plugin.dataBase.open()
        val connection = plugin.dataBase.connection
        if (connection == null) {
            plugin.dataBase.sendErrorMessage()
            return false
        }
        //val sql = "DELETE FROM mail_all WHERE id='$id';"
        val sql = "DELETE FROM mail_all WHERE id=?;"
        val statement = connection.prepareStatement(sql)
        statement.setInt(1,id)
        statement.executeUpdate()
        statement.close()
        return true
    }

    data class MailInformation(val id: Int, val from: String, val to: String, val title: String, val message: String, val tag: String,val date:String)

    /**
     * function of get mail information
     * @param id[Int]
     * @return info[MailInformation]
     */
    fun getInformation(id: Int): MailInformation? {
        plugin.dataBase.open()
        val connection = plugin.dataBase.connection
        if (connection == null) {
            plugin.dataBase.sendErrorMessage()
            return null
        }
        //val sql = "SELECT * FROM mail_list WHERE id='$id' LIMIT 1;"
        val sql = "SELECT * FROM mail_list WHERE id=? LIMIT 1;"
        val statement = connection.prepareStatement(sql)
        statement.setInt(1,id)
        val result = statement.executeQuery()
        val info = if(result.next()) {
            val from = result.getString("from_player")
            val to = result.getString("to_player")
            val title = result.getString("title")
            val message = result.getString("message")
            val tag = result.getString("tag")
            val date = result.getString("date")
            MailInformation(id, from, to, title, message, tag,date)
        }else null
        result.close()
        statement.close()
        return info
    }

    /*
    /**
     * function of remove old mail
     * @param uuid[String] uuid
     */
    fun removeOldMail(uuid: String){
        plugin.dataBase.open()
        val connection = plugin.dataBase.connection
        if (connection == null) {
            plugin.dataBase.sendErrorMessage()
            return
        }
        val sql = "SELECT id FROM mail_list where to_player='${uuid}' ORDER BY id desc LIMIT 54,30;"
        val statement = connection.createStatement()
        val result = statement.executeQuery(escapeWildcardsForMySQL(sql))
        val removeStatement = connection.createStatement()
        var amount = 0
        while (result.next()){
            val id = result.getInt("id")
            val removeSql = "DELETE FROM mail_list WHERE id='${id}';"
            removeStatement.executeUpdate(escapeWildcardsForMySQL(removeSql))
            amount ++
        }
        result.close()
        statement.close()
        removeStatement.close()
        //  Logger
        plugin.logger.info("delete $amount mails. uuid:$uuid")
    }*/
    /**
     * function of remove old mail
     * @param uuid[String] uuid
     */
    fun removeOldMail(uuid: String){
        plugin.dataBase.open()
        val connection = plugin.dataBase.connection
        if (connection == null) {
            plugin.dataBase.sendErrorMessage()
            return
        }
        //val countSql = "SELECT id FROM mail_list where to_player='${uuid}';"
        val countSql = "SELECT id FROM mail_list where to_player=?;"
        val statement = connection.prepareStatement(countSql)
        statement.setString(1,uuid)
        val countResult = statement.executeQuery()
        //  count
        val c = plugin.dataBase.countColumn(countResult)
        if(c <= 54){
            countResult.close()
            statement.close()
            return
        }
        var mustRemoveValue = c - 54
        //val deleteReadSQL = "DELETE FROM mail_list WHERE to_player='$uuid' AND read='true' ORDER BY id asc LIMIT $mustRemoveValue;"
        val deleteReadSQL = "DELETE FROM mail_list WHERE to_player=? AND read=? ORDER BY id asc LIMIT ?;"
        val removeReadStatement = connection.prepareStatement(deleteReadSQL)
        removeReadStatement.setString(1,uuid)
        removeReadStatement.setBoolean(2,true)
        removeReadStatement.setInt(3,mustRemoveValue)
        val removedCount = removeReadStatement.executeUpdate()

        mustRemoveValue - removedCount
        if(mustRemoveValue == 0){
            removeReadStatement.close()
            return
        }
        //  val sql = "SELECT id FROM mail_list where to_player='${uuid}' ORDER BY id asc LIMIT 54;"
        val sql = "SELECT id FROM mail_list where to_player=? ORDER BY id asc LIMIT 54;"
        val removeStatement = connection.prepareStatement(sql)
        removeStatement.setString(1,uuid)
        val result = removeStatement.executeQuery()
        val deleteStatement = connection.createStatement()
        while (result.next()){
            val id = result.getInt("id")
            val removeSql = "DELETE FROM mail_list WHERE id='${id}';"
            deleteStatement.executeUpdate(removeSql)
        }
        result.close()
        removeStatement.close()
        deleteStatement.close()
        removeReadStatement.close()
        countResult.close()
        statement.close()

        //  Logger
        plugin.logger.info("delete ${c-54} mails. uuid:$uuid")
    }

    /**
     * function of send no-read mail's amount
     * @param player[Player] player
     */
    fun sendNotReadMail(player: Player){
        plugin.dataBase.open()
        val connection = plugin.dataBase.connection
        if (connection == null) {
            plugin.dataBase.sendErrorMessage()
            return
        }
        //  val sql = "SELECT id,`read` FROM mail_list where to_player='${player.uniqueId.toString()}';"
        val sql = "SELECT id,`read` FROM mail_list where to_player=?;"
        val statement = connection.prepareStatement(sql)
        statement.setString(1,player.uniqueId.toString())
        val result = statement.executeQuery()
        var amount = 0
        while (result.next()){
            if(!result.getBoolean("read")){
                amount++
            }
        }
        //  0
        if(amount == 0){
            player.sendMessage("${prefix}§6未読メールはありません。")
        }else{
            player.sendMessage("${prefix}§d${amount}件§6の未読メールがあります。/mmailでメールボックスを開く")
        }

        result.close()
        statement.close()
    }

    /**
     * function of Check block.
     * @param from[String]
     * @param to[String]
     * @return [Boolean] if blocked,return true. else return false
     */
    fun checkBlock(from:String,to: String):Boolean{
        plugin.dataBase.open()
        val connection = plugin.dataBase.connection
        if (connection == null) {
            plugin.dataBase.sendErrorMessage()
            return false
        }
        //  val sql = "SELECT * FROM mail_block_list WHERE from_player='$from' AND to_player='$to' LIMIT 1;"
        val sql = "SELECT * FROM mail_block_list WHERE from_player=? AND to_player=? LIMIT 1;"
        val statement = connection.prepareStatement(sql)
        statement.setString(1,from)
        statement.setString(2,to)
        val result = statement.executeQuery()
        val b = result.next()
        result.close()
        statement.close()
        return b
    }

    /**
     * function of block player
     * @param from[String]
     * @param to[String]
     */
    fun blockUser(from: String,to: String){
        if(checkBlock(from,to))return
        plugin.dataBase.open()
        val connection = plugin.dataBase.connection
        if (connection == null) {
            plugin.dataBase.sendErrorMessage()
            return
        }
        //  val sql = "INSERT INTO mail_block_list (from_player,to_player) VALUES ('$from','$to');"
        val sql = "INSERT INTO mail_block_list (from_player,to_player) VALUES (?, ?);"
        val statement = connection.prepareStatement(sql)
        statement.setString(1,from)
        statement.setString(2,to)
        statement.executeUpdate()
        statement.close()
    }

    /**
     * function of unblock player
     * @param from[String]
     * @param to[String]
     */
    fun unblockUser(from: String,to: String){
        if(!checkBlock(from,to))return
        plugin.dataBase.open()
        val connection = plugin.dataBase.connection
        if (connection == null) {
            plugin.dataBase.sendErrorMessage()
            return
        }
        //  val sql = "DELETE FROM mail_block_list WHERE from_player='$from' AND to_player='$to';"
        val sql = "DELETE FROM mail_block_list WHERE from_player=? AND to_player=?;"
        val statement = connection.prepareStatement(sql)
        statement.setString(1,from)
        statement.setString(2,to)
        statement.executeUpdate()
        statement.close()
    }

    private fun formatFromUser(from: String, senderType: MailSenderType): String {
        return when (senderType) {
            MailSenderType.PLAYER -> {
                from
            }
            MailSenderType.SERVER -> {
                "&SERVER"
            }
            MailSenderType.CUSTOM -> {
                "#${from}"
            }
        }
    }

    /*
    /**
     * MySQL String エスケープ
     */
    private fun escapeStringForMySQL(s: String): String {
        return s.replace("\\", "\\\\")
            .replace("\b", "\\b")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
            .replace("\\x1A", "\\Z")
            .replace("\\x00", "\\0")
            .replace("'", "\\'")
            .replace("\"", "\\\"")
    }

    private fun escapeWildcardsForMySQL(s: String): String {
        return escapeStringForMySQL(s)
            .replace("%", "\\%")
            .replace("_", "\\_")
    }
     */

    //  Player Exist
    private fun existPlayer(player: OfflinePlayer):Boolean {
        return player.hasPlayedBefore()
    }

    private fun getPlayerName(str:String):String {
        try {
            val uuid = UUID.fromString(str)
            val player = Bukkit.getServer().getOfflinePlayer(uuid)
            if(!existPlayer(player)) return "none"
            return player.name!! // <- if player has login before,mcid must not be null.
        }catch (e:Exception) {
            return "none"
        }
    }

}