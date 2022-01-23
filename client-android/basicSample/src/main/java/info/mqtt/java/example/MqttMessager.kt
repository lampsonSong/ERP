package info.mqtt.java.example

import android.app.Application
import android.content.Context
import android.util.Log
import info.mqtt.android.service.MqttAndroidClient
import org.eclipse.paho.android.service.QoS
import org.eclipse.paho.client.mqttv3.*
import java.util.*

class MqttMessager : Application {
    private lateinit var mqttAndroidClient: MqttAndroidClient
    private lateinit var received_msg: String

    companion object {
        @Volatile
        //        private const val serverUri = "tcp://mqtt.eclipseprojects.io:1883"
        private var serverUri = "tcp://192.168.1.5:1883"
        private const val subscriptionTopic = "stock"
        private const val publishTopic = "stock"
        private var clientId = "BasicSample"
        private var instance: MqttMessager ?= null

        fun getInstance(applicationContext: Context) : MqttMessager
        {
            if(instance == null)
            {
                synchronized(MqttMessager::class)
                {
                    if(instance == null)
                    {
                        instance = MqttMessager(applicationContext)
                    }
                }
            }
            return instance!!
        }
    }


    private constructor(applicationContext: Context) {

        mqttAndroidClient = MqttAndroidClient(applicationContext, MqttMessager.serverUri, MqttMessager.clientId)

        mqttAndroidClient.setCallback(object : MqttCallbackExtended {
            override fun connectComplete(reconnect: Boolean, serverURI: String) {
                if (reconnect) {
                    show_state("Reconnected: $serverURI")

                    // Because Clean Session is true, we need to re-subscribe
                    subscribeToTopic()
                } else {
                    show_state("Connected: $serverURI")
                }
            }

            override fun connectionLost(cause: Throwable?) {
                show_state("The Connection was lost.")
            }

            override fun messageArrived(topic: String, message: MqttMessage) {
                if( String(message.payload).length != received_msg.length ) {
                    received_msg = String(message.payload)
                    show_state("Incoming message: " + String(message.payload))

                    show_state(String(message.payload))
                }
            }

            override fun deliveryComplete(token: IMqttDeliveryToken) {}
        })

        val mqttConnectOptions = MqttConnectOptions()
        mqttConnectOptions.isAutomaticReconnect = true
        mqttConnectOptions.isCleanSession = false

        show_state("Connecting : ${MqttMessager.serverUri}")

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
                show_state("Failed to connect: ${MqttMessager.serverUri}")
            }
        })

    }

    fun subscribeToTopic() {
        mqttAndroidClient.subscribe(MqttMessager.subscriptionTopic, QoS.AtMostOnce.value, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken) {
                show_state("Subscribed! ${MqttMessager.subscriptionTopic}")
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                show_state("Failed to subscribe $exception")
            }
        })

//        // THIS DOES NOT WORK!
//        mqttAndroidClient.subscribe(subscriptionTopic, QoS.AtMostOnce.value) { topic, message ->
//            Timber.d("--Message arrived $topic : ${String(message.payload)}")
//            addToHistory("The Message arrived $message")
//        }
    }

    public fun publishMessage(publishMsg:String) {
        val message = MqttMessage()
        message.payload = publishMsg.toByteArray()
        if (mqttAndroidClient.isConnected) {
            mqttAndroidClient.publish(MqttMessager.publishTopic, message)
            show_state("Message Published >$publishMsg<")
            if (!mqttAndroidClient.isConnected) {
                show_state(mqttAndroidClient.bufferedMessageCount.toString() + " messages in buffer.")
            }
        } else {
            show_state("Not Connected")
        }
    }

    fun show_state(msg:String){
//        val jsonmsg = Gson().fromJson(msg, TaskInfoData.class.java)
//        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
        Log.d("mqtt show_state : ", msg)
    }


}