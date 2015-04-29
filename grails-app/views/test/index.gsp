<html>
<body>

Testing


Resources found to help put all of this together:
<pre>

https://github.com/spring-projects/spring-boot/tree/master/spring-boot-samples/spring-boot-sample-websocket-tomcat




http://hsilomedus.me/index.php/websockets-in-java/

http://kimrudolph.de/blog/spring-4-websockets-tutorial/

https://github.com/hsilomedus/web-sockets-samples

https://github.com/shangmin1990/websocket



</pre>



	<script src="https://cdn.jsdelivr.net/sockjs/0.3.4/sockjs.min.js"></script>
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