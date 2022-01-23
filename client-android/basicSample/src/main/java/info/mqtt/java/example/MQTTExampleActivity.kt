package info.mqtt.java.example

import info.mqtt.android.service.MqttAndroidClient
import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions
import timber.log.Timber
import android.annotation.SuppressLint
import android.graphics.Color
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.snackbar.Snackbar
import org.eclipse.paho.android.service.QoS
import java.text.SimpleDateFormat
import java.util.*

class MQTTExampleActivity : AppCompatActivity() {

    private lateinit var mqttAndroidClient: MqttAndroidClient
    private lateinit var adapter: HistoryAdapter
    private var mName:String=""
    private var received_msg:String=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scrolling)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.title = "ToolBar Demo"
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_ios_24)

        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener { publishMessage("hello") }

        val state_btn = findViewById<Button>(R.id.state_btn)
        state_btn.setOnClickListener { state_btn_clicked() }

        // 接收参数
        val bundle:Bundle ?= intent.extras
        var userName:String?="t"
        var ipText:String?="192.168.1.5"
        var machineName:String?="A1B2"

        if(bundle!=null){
            userName = bundle.getString("userName")
            ipText = bundle.getString("ipText")
            machineName = bundle.getString("machineName")
        }
        mName = machineName.toString()

        serverUri = "tcp://" + ipText.toString() + ":1883"


        val recyclerView = findViewById<RecyclerView>(R.id.history_recycler_view)
        val mLayoutManager: LayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = mLayoutManager
        adapter = HistoryAdapter()
        recyclerView.adapter = adapter
        clientId += System.currentTimeMillis()
        mqttAndroidClient = MqttAndroidClient(applicationContext, serverUri, clientId)
        mqttAndroidClient.setCallback(object : MqttCallbackExtended {
            override fun connectComplete(reconnect: Boolean, serverURI: String) {
                if (reconnect) {
                    addToHistory("Reconnected: $serverURI")
                    // Because Clean Session is true, we need to re-subscribe
                    subscribeToTopic()
                } else {
                    addToHistory("Connected: $serverURI")
                }
            }

            override fun connectionLost(cause: Throwable?) {
                addToHistory("The Connection was lost.")
            }

            override fun messageArrived(topic: String, message: MqttMessage) {
                if( String(message.payload).length != received_msg.length ) {
                    received_msg = String(message.payload)
                    addToHistory("Incoming message: " + String(message.payload))

                    analyze_message(String(message.payload))
                }
            }

            override fun deliveryComplete(token: IMqttDeliveryToken) {}
        })
        val mqttConnectOptions = MqttConnectOptions()
        mqttConnectOptions.isAutomaticReconnect = true
        mqttConnectOptions.isCleanSession = false
        addToHistory("Connecting: $serverUri")
        mqttAndroidClient.connect(mqttConnectOptions, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken) {
                val disconnectedBufferOptions = DisconnectedBufferOptions()
                disconnectedBufferOptions.isBufferEnabled = true
                disconnectedBufferOptions.bufferSize = 100
                disconnectedBufferOptions.isPersistBuffer = false
                disconnectedBufferOptions.isDeleteOldestMessages = false
                mqttAndroidClient.setBufferOpts(disconnectedBufferOptions)
                subscribeToTopic()
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                addToHistory("Failed to connect: $serverUri")
            }
        })


    }

    private fun addToHistory(mainText: String) {
        Timber.d(mainText)
        @SuppressLint("SimpleDateFormat")
        val timestamp = SimpleDateFormat("HH:mm.ss.SSS").format(Date(System.currentTimeMillis()))
        adapter.add("$timestamp $mainText")
//        Snackbar.make(findViewById(android.R.id.content), mainText, Snackbar.LENGTH_LONG).setAction("Action", null).show()
    }

    fun subscribeToTopic() {
        mqttAndroidClient.subscribe(subscriptionTopic, QoS.AtMostOnce.value, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken) {
                addToHistory("Subscribed! $subscriptionTopic")
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                addToHistory("Failed to subscribe $exception")
            }
        })

//        // THIS DOES NOT WORK!
//        mqttAndroidClient.subscribe(subscriptionTopic, QoS.AtMostOnce.value) { topic, message ->
//            Timber.d("--Message arrived $topic : ${String(message.payload)}")
//            addToHistory("The Message arrived $message")
//        }
    }

    private fun publishMessage(publishMsg:String) {
        val message = MqttMessage()
        message.payload = publishMsg.toByteArray()
        if (mqttAndroidClient.isConnected) {
            mqttAndroidClient.publish(publishTopic, message)
            addToHistory("Message Published >$publishMsg<")
            if (!mqttAndroidClient.isConnected) {
                addToHistory(mqttAndroidClient.bufferedMessageCount.toString() + " messages in buffer.")
            }
        } else {
            Snackbar.make(findViewById(android.R.id.content), "Not connected", Snackbar.LENGTH_SHORT).setAction("Action", null).show()
        }
    }

    private fun analyze_message(msg:String){
//        val jsonmsg = Gson().fromJson(msg, TaskInfoData.class.java)
    }

    private fun state_btn_clicked() {
//        Toast.makeText(this, "test", Toast.LENGTH_LONG).show()
        val state_btn = findViewById<Button>(R.id.state_btn)
//        Toast.makeText(this, "收到 : uriserver=", Toast.LENGTH_LONG).show()
        if( state_btn.text == "Click" )
        {
            state_btn.setText("GO")
//            state_btn.setBackgroundColor(Color.rgb(0, 255, 0))
            publishMessage(mName)
        }
        else
        {
            state_btn.setText("Click")
        }
    }

    companion object {
//        private const val serverUri = "tcp://mqtt.eclipseprojects.io:1883"
        private var serverUri = "tcp://192.168.1.5:1883"
        private const val subscriptionTopic = "stock"
        private const val publishTopic = "stock"
        private var clientId = "BasicSample"
    }

}