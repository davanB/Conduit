package ca.uwaterloo.fydp.conduit.flow.slave

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import ca.uwaterloo.fydp.conduit.AppConstants
import ca.uwaterloo.fydp.conduit.R

class GroupJoinActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_join)

        findViewById<Button>(R.id.join_group).setOnClickListener{
            val userName = findViewById<EditText>(R.id.user_name).text.toString()
            val intent = Intent(GroupJoinActivity@this, QrCodeScanner::class.java)
            intent.putExtra(AppConstants.USER_NAME_KEY, userName)
            startActivity(intent)
        }
    }
}
