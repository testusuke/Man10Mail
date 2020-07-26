package net.testusuke.open.man10mail.DataBase

/**
 * Created by testusuke on 2020/07/04
 * @author testusuke
 */
data class Mail(
        val from: String, // UUID
        val title: String,   //  Title
        val to: String,  //  UUID
        val tag: String, //  Tag
        val message: String, //  [ ; ]で改行
        val date: String,    //  日付
        var read: Boolean    //  既読
)