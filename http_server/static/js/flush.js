function onload() {
    $(document).ready(function() {
        namespace='/test_conn'
        var socket = io.connect('ws://192.168.1.5:8090/test_conn');
        //或者使用 var socket = io.connect(location.protocol + '//' + document.domain + ':' + location.port + namespace);
        
        socket.on('server_response', function(res) {
            var msg = res.data;
            console.log(msg);
            document.getElementById("random").innerHTML = '<p>'+msg+'</p>';
        }); 
   	});
}
