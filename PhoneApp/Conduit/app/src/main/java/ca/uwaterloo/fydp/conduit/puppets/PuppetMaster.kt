package ca.uwaterloo.fydp.conduit.puppets

import ca.uwaterloo.fydp.conduit.AppConstants
import java.util.ArrayList

class PuppetMaster {
    var nextShowTime: Long = 0

    // Chains shows in list one after the other
    fun chainShows(puppetShows: List<PuppetShow>) {
        if(!AppConstants.PUPPET_MASTER_ENABLED) {
            return
        }
        for (puppetShow in puppetShows) {
            puppetShow.writeScript()
            val startTime = nextShowTime
            puppetShow.script.add(0, { Thread.sleep(startTime) } )
            Thread(Runnable {
                for(lines in puppetShow.script) {
                    lines.invoke()
                }
            }).start()
            nextShowTime += puppetShow.showEndTime
        }
    }

    fun startShow(puppetShow: PuppetShow) {
        if(!AppConstants.PUPPET_MASTER_ENABLED) {
            return
        }
        val shows: ArrayList<PuppetShow> = ArrayList()
        shows.add(puppetShow)
        chainShows(shows)
    }
}