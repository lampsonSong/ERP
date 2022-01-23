# created by lampson @ 20211212
# test flask-socketio

from flask import Flask, render_template
from flask_socketio import SocketIO,emit
import threading
from threading import Lock
import random
import paho.mqtt.client as mqtt
from threading import Thread
import time
import json
import ast
import demjson
import os

async_mode = None
app = Flask(__name__)
app.config['SECRET_KEY'] = 'secret!'
socketio = SocketIO(app)
thread = None
thread_lock = Lock()

mqtt_thread = None
mqtt_send_thread = None
sender_thread = None

out_message = ""
machine_name_lst = ""
machine_state_lst = 0
send_msg_flag = 0
pre_state_lst = machine_state_lst

lock = threading.Lock()

class MQTT_Thread(Thread):
    def __init__(self):
        Thread.__init__(self)
        self.stop = False
    
    def run(self):
        while not self.stop and mqtt_client.loop_forever() == 0:
            pass
        print("MQTT Thread terminado")

## mqtt
def on_connect(client, userdata, flags, rc):
    if rc == 0:
        client.subscribe("stock", 0)
        #print("Connected with result code : " + str(rc))
    else:
        print("Connected with result code : " + str(rc))

def on_message(client, userdata, receive_msg):
    global machine_state_lst
    global machine_name_lst
    global send_msg_flag
    receive_topic = receive_msg.topic
    receive_message = receive_msg.payload.decode('utf-8')
    
    msg_dict = {}
    try:
        msg_dict = demjson.decode(receive_message)
    except:
        print("received message is not json format")
        return

    msg_type = msg_dict.get("msg_type", "")
    msg_sender = msg_dict.get("msg_sender", "")
    msg_receiver = msg_dict.get("msg_receiver", "")


    if msg_type == "heartbeat" and msg_receiver == "server":
        #send_msg("test")
        print("heart")
        return
    
    if msg_type == "work":
        lock.acquire()
        machine_name_lst = msg_dict["msg_sender"]
        machine_state_lst = msg_dict["machine_state"]
        lock.release()
        write_state(machine_state_lst)
        print("-machine_name_lst : ", machine_name_lst, " - machine_staet_lst : ", machine_state_lst)



def on_subscribe(client, obj, mid, granted_qos):
    #print("Subscribed: "+str(mid)+" "+str(granted_qos))
    pass

def send_msg(message):
    mqtt_client.publish('stock', payload=message, qos=0)
        


# mqtt
mqtt_client = mqtt.Client("mqtt")
mqtt_client.on_connect = on_connect
mqtt_client.on_message = on_message
mqtt_client.on_subscribe = on_subscribe

mqtt_client.connect("192.168.1.5", 1883, 600)
#mqtt_client.connect("192.168.1.4", 1883, 600)
#mqtt_client.subscribe("stock", 0)


@app.route('/test_socketio')
def index():

    return render_template('test_socketio.html')

@socketio.on('connect', namespace='/test_conn')
def test_connect():
   
    global thread
    with thread_lock:
        if thread is None:
            thread = socketio.start_background_task(target=background_thread)

import fcntl

def write_state(state):
    f = open("./tmp.txt", "w")
    fcntl.flock(f, fcntl.LOCK_EX)
    f.write(str(state))
    fcntl.flock(f, fcntl.LOCK_UN)
    f.close()

def read_state():
    f_path = "./tmp.txt"
    res = 0

    if not os.path.exists(f_path):
        return res

    f = open(f_path, "r")
    fcntl.flock(f, fcntl.LOCK_EX)
    line = f.readline().strip()
    if len(line) > 0:
        res = int(line)
    fcntl.flock(f, fcntl.LOCK_UN)
    f.close()

    return res


def background_thread():
    global machine_name_lst
    global machine_state_lst

    while True:
        machine_state_lst = read_state()
        #t = random.randint(1, 100)
        out_s = {"machine_name": machine_name_lst,"msg_type":"work", "machine_state": machine_state_lst}
        #print(out_s)
        socketio.emit('server_response',
                  {'data': out_s},namespace='/test_conn')
        socketio.sleep(0.01)

if __name__ == '__main__':
    mqtt_thread = MQTT_Thread()
    mqtt_thread.start()

    #mqtt_send_thread = MQTT_SendThread()
    #mqtt_send_thread.start()
    
    socketio.run(app, debug=True, host='0.0.0.0', port=8090)
