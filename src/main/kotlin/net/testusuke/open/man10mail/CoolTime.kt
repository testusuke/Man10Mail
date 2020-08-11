package net.testusuke.open.man10mail

import net.testusuke.open.man10mail.Main.Companion.plugin
import java.util.*


/**
 * Created by testusuke on 2020/08/11
 * @author testusuke
 */
object CoolTime {

    fun prepare(){
        coolTime = (1000 * plugin.config.getInt("cooltime")).toLong()

    }
    private val coolTimeMap = mutableMapOf<String,Long>()
    //  1s=1000ms   クールタイム
    private var coolTime:Long = 1000

    fun clear(){
        coolTimeMap.clear()
    }
    /**
     * クールダウン中か
     * @param uuid [String]
     * @return result true=クールダウン中　false=クールダウン終了
     */
    fun isCoolDown(uuid: String): Boolean {
        val now = System.currentTimeMillis()
        val before = coolTimeMap[uuid] ?: return false
        return now - before < coolTime
    }

    /**
     * クールダウンのスタート
     * @param uuid [String]
     */
    fun start(uuid: String) {
        coolTimeMap[uuid] = System.currentTimeMillis()
    }
}