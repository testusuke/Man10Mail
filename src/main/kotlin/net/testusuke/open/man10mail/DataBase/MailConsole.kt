package net.testusuke.open.man10mail.DataBase

import net.testusuke.open.man10mail.*
import net.testusuke.open.man10mail.Main.Companion.plugin
import java.sql.Statement
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Created by testusuke on 2020/07/04
 * @author testusuke
 */
object MailConsole {

    /**
     * function of send mail.
     * @param from[String] 発信元(uuid or name)
     * @param to[String] 送信先(uuid or mcid)
     * @param title[String] タイトル
     * @param tag[String] タグ
     * @param message[String] メッセージ [;]で改行
     * @param senderType[MailSenderType] uuid/mcid=PLAYER server=SERVER custom sender=CUSTOM(先頭に[#]が代入される。)
     * @return mailResult[MailResult<V>]
     */
    fun sendMail(from:String,to:String,title:String,tag:String,message:String,senderType:MailSenderType):MailResult{
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val formatted = current.format(formatter)
        val sql = "INSERT INTO mail_list (to_player,from_player,title,message,tag,date) VALUES('${to}','%from%','${title}','${message}','${tag}','${formatted}');"
        plugin.dataBase.open()
        val connection = plugin.dataBase.connection
        if(connection == null){
            plugin.dataBase.sendErrorMessage()
            return MailResult.Error(MailErrorReason.CAN_NOT_ACCESS_DB)
        }
        val statement = connection.createStatement()
        statement.executeUpdate(replaceSQL(sql, from, senderType), Statement.RETURN_GENERATED_KEYS)
        val resultSet = statement.generatedKeys
        var id = 0
        if(resultSet.next()){
            id = resultSet.getInt("id")
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
    fun sendEveryoneMail(from:String,title:String,tag:String,message:String,senderType: MailSenderType):MailResult{
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val formatted = current.format(formatter)
        val sql = "INSERT INTO mail_all (from_player,title,message,tag,`date`) VALUES('%from%','${title}','${message}','${tag}','${formatted}');"
        plugin.dataBase.open()
        val connection = plugin.dataBase.connection
        if(connection == null){
            plugin.dataBase.sendErrorMessage()
            return MailResult.Error(MailErrorReason.CAN_NOT_ACCESS_DB)
        }
        val statement = connection.createStatement()
        statement.executeUpdate(replaceSQL(sql,from,senderType), Statement.RETURN_GENERATED_KEYS)
        val resultSet = statement.generatedKeys
        var id = 0
        if(resultSet.next()){
            id = resultSet.getInt("id")
        }
        return MailResult.Success(id)
    }

    private fun replaceSQL(sql:String,from: String,senderType: MailSenderType):String{
        when(senderType){
            MailSenderType.PLAYER -> {
                sql.replace("%from%",from)
            }
            MailSenderType.SERVER -> {
                sql.replace("%from%","&SERVER")
            }
            MailSenderType.CUSTOM -> {
                sql.replace("%from%","#${from}")
            }
        }
        return sql
    }

    /*
    オートインクリースIDの取得方法
    https://stackoverflow.com/questions/1376218/is-there-a-way-to-retrieve-the-autoincrement-id-from-a-prepared-statement
     */

}