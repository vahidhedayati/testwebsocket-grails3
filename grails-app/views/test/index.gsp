<html>
<body>

Testing spring websocket

Open your web console  / developer console and look for console.log responses:



<!--<script src="https://cdn.jsdelivr.net/sockjs/0.3.4/sockjs.min.js"></script>-->
<script type="text/javascript">


//I can now fallback to longpoll and do IE9!!!
//var socket = new SockJS("ws://localhost:8080/echo");
var socket = new WebSocket("ws://localhost:8080/echo");

socket.onopen = function() {
  //event handler when the connection has been established
  socket.send('Hello');
};
socket.onmessage = function(message) {
  //event handler when data has been received from the server
  console.log(message.data);
  //alert(message.data);
};
socket.onclose = function() {
//event handler when the socket has been properly closed
}
socket.onerror = function() {
//event handler when an error has occurred during communication
}


</script>

</body>
</html>
