package ca.uwaterloo.fydp.conduit

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader


class LogDumpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_dump)

        try {
            val process = Runtime.getRuntime().exec("logcat -d")
            val bufferedReader = BufferedReader(
                    InputStreamReader(process.inputStream))

            val log = StringBuilder()
            var line:String? = ""
            while (line != null) {
                log.append(line).append('\n')
                line = bufferedReader.readLine()
            }
            val tv = findViewById<TextView>(R.id.logdumpview)
            tv.text = log.toString()
        } catch (e: IOException) {
            // Handle Exception
        }

    }
}
