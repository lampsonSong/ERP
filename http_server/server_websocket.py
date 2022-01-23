# created @ 20211212 by lampson
# send msg from server to client

##from flask import render_template, request, Flask
##from geventwebsocket.handler import WebSocketHandler
##from gevent.pywsgi import WSGIServer
##
##app = Flask(__name__)
##
##user_socket_list = []
##
##received_msg = "test"
##
##@app.route("/my_socket")
##def my_socket():
##    #global received_msg
##
##    user_socket = request.environ.get("wsgi.websocket")
##
##    if user_socket:
##        user_socket_list.append(user_socket)
##    else:
##        print("please use WebSocket proto")
##
##    while True:
##        #if received_msg:
##        #    print("send : ", received_msg)
##        #    for usocket in user_socket_list:
##        #        try:
##        #            usocket.send(received_msg)
##        #        except:
##        #            continue
##        message = ws.receive()
##        if not message:
##            user_socket_list.remove(ws)
##            break
##
##        for usocket in user_socket_list:
##            try:
##                usocket.send(message)
##            except:
##                continue
##
##@app.route("/hello")
##def hello():
##    return render_template('hello.html')
##
##if __name__ == '__main__':
##    http_serv = WSGIServer(('0.0.0.0',8090), app, handler_class=WebSocketHandler)
##    print("wsgi init start")
##    http_serv.serve_forever()
##
##    #app.run('0.0.0.0', port=8090, debug=True)
##

from geventwebsocket.handler import WebSocketHandler
from gevent.pywsgi import WSGIServer
from flask import Flask,render_template,request
import pickle

app = Flask(__name__)
app.secret_key = 'xfsdfqw'

@app.route('/hello')
def index():
    return render_template('hello.html')

WS_LIST = []

@app.route('/test')
def test():
    ws = request.environ.get('wsgi.websocket')
    if not ws:
        return '请使用WebSocket协议'
    # websocket连接已经成功
    print("ws connected")
    WS_LIST.append(ws)
    while True:
        # 等待用户发送消息，并接受
        message = ws.receive()
        print("received message : ", message)

        # 关闭：message=None
        if not message:
            WS_LIST.remove(ws)
            break

        for item in WS_LIST:
            item.send(message)

    return "asdfasdf"

if __name__ == '__main__':
    http_server = WSGIServer(('0.0.0.0', 8090,), app, handler_class=WebSocketHandler)
    http_server.serve_forever()
