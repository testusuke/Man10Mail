package net.testusuke.open.man10mail.DataBase

/**
 * Created by testusuke on 2020/07/11
 * @author testusuke
 */
sealed class MailResult {
    data class Success(val id: Int) : MailResult()
    data class Error(val reason: MailErrorReason) : MailResult()
}

enum class MailErrorReason {
    CAN_NOT_ACCESS_DB,
    BLOCKED
}