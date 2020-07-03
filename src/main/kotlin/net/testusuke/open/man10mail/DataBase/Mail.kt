package net.testusuke.open.man10mail.DataBase

/**
 * Created by testusuke on 2020/07/04
 * @author testusuke
 */
data class Mail(
    val from:String,
    val title:String,
    val to:String,
    val tag:String,
    val message:String,
    val date:String,
    var read:Boolean
    )