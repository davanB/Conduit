package ca.uwaterloo.fydp.conduit.puppets

class PuppetMaster {
    fun startShow(puppetShow: PuppetShow) {
        val script = puppetShow.getScript()
        Thread(Runnable {
            for(lines in script) {
                lines.invoke()
            }
        }).start()
    }
}