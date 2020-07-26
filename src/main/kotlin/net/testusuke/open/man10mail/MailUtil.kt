package net.testusuke.open.man10mail

/**
 * Created by testusuke on 2020/07/11
 * @author testusuke
 */
object MailUtil {

    /**
     * DBへの保存用に変換
     * @param tag[String] tag
     *
     */
    fun convertTag(tag: String): String {
        return when (tag) {
            "0" -> {    //  normal = 0
                "normal"
            }
            "5" -> {    //  notice=5
                "#5"
            }
            "6" -> {    //  information=6
                "#6"
            }
            else -> {   //  normal=else
                tag
            }
        }
    }

    fun formatTag(tag: String): String {
        return when (tag) {
            "normal" -> {    //  normal = 0
                "§6§lNormal"
            }
            "#5" -> {    //  notice=5
                "§a§lNotice"
            }
            "#6" -> {    //  information=6
                "§d§lInformation"
            }
            else -> {   //  normal=else
                "§6§l${tag}"
            }
        }
    }
}