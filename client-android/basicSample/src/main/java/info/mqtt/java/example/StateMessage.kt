package info.mqtt.java.example

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MachineState(var machine_name: String, var work_state: Int)

@JsonClass(generateAdapter=true)
data class StateMessage(
    var msg_sender:String,
    var msg_receiver:String,
    var msg_type:String,
//    var machine_state: ArrayList<MachineState>
//    var machine_state: List<MachineState>
    var machine_state:Int
)