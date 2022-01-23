package info.mqtt.java.example

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.squareup.moshi.Moshi

class PersonActivity : AppCompatActivity() {
    private lateinit var mqtt_messager : MqttMessager
    private var machine_name = "machine_A"
    private val moshi = Moshi.Builder().build()
    private val adapter = moshi.adapter(StateMessage::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.personal_page)

        val toolbar = findViewById<Toolbar>(R.id.person_toolbar)
        setSupportActionBar(toolbar)

        toolbar.setNavigationOnClickListener {
            val intent = Intent()
            intent.setClass(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        // end navigation onclick

        mqtt_messager = MqttMessager.getInstance(applicationContext)
        Log.d("show_state", " -- init mqtt")


    }

    fun state_btn_clicked(view: View)
    {
        val toolbar = findViewById<Toolbar>(R.id.person_toolbar)
        if ((view as Button).text == "未开始任务")
        {
            view.setBackgroundResource(R.drawable.start_state_btn)
            toolbar.setBackgroundResource(R.drawable.start_state_btn)
            (view as Button).setText("正在进行任务")

//            val msg = StateMessage(machine_name, "server", "work",listOf(MachineState(machine_name, 1)))
            val msg = StateMessage(machine_name, "server", "work",1)
            mqtt_messager.publishMessage(adapter.toJson(msg).toString())
        }
        else
        {
            view.setBackgroundResource(R.drawable.end_state_btn)
            toolbar.setBackgroundResource(R.drawable.end_state_btn)
            (view as Button).setText("未开始任务")
//            val msg = StateMessage(machine_name, "server", "work",listOf(MachineState(machine_name, 0)))
            val msg = StateMessage(machine_name, "server", "work",0)
            mqtt_messager.publishMessage(adapter.toJson(msg).toString())
        }
    }

}