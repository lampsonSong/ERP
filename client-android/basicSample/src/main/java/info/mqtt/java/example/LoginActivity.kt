package info.mqtt.java.example

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.squareup.moshi.Moshi
import java.lang.Thread.sleep
import kotlin.concurrent.thread

class LoginActivity: AppCompatActivity() {
    private lateinit var mqtt_messager : MqttMessager
    private var machine_name = "machine_A"
    private val msg = StateMessage(machine_name, "server", "heartbeat", 0)
    private val moshi = Moshi.Builder().build()
    private val adapter = moshi.adapter(StateMessage::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

//        val msg = StateMessage("test_A", "server", listOf(MachineState("test_A", 2)))
//        val json_str = "{\"msg_sender\":\"server\", \"msg_receiver\":\"test_B\", \"machine_state\":[ {\"machine_name\": \"testB\"," +
//                "\"work_state\":10},{\"machine_name\":\"test_C\", \"work_state\":40}] }"
//
//        val moshi = Moshi.Builder().build()
//        val adapter = moshi.adapter(StateMessage::class.java)
//        val mess1 = adapter.fromJson(json_str)
//
//        Log.d("ttout : ", mess1.toString())
//
//        Log.d("ttout tojson : ", adapter.toJson(msg).toString())

        mqtt_messager = MqttMessager.getInstance(applicationContext)
        heart_thread()
    }

    fun login_btn_clicked(view: View)
    {
        val userName = findViewById<EditText>(R.id.et_userName)

        val intent = Intent()
//        intent.setClass(this, MQTTExampleActivity::class.java)
        intent.setClass(this, PersonActivity::class.java)
//        Log.i("TAG","clicked login btn")
        intent.putExtra("userName",userName.text.toString())

        startActivity(intent)
    }

    private fun heart_thread()
    {
        thread {
            try {
                while (true) {
                    mqtt_messager.publishMessage(adapter.toJson(msg).toString())
                    Thread.sleep(3000)
                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }

    }
}