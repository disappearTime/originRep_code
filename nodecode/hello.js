var http = require('http');//请求协议
var name = require('./common/module.js')
http.createServer( function(request,response){
    response.writeHead(200,{'Content-Type':'text/html;charset=utf-8'})
    console.log(request.url)
    console.log("log in server");//在服务端打印 而且打印两次，有两次请求，一次是根目录'/',一个是'/favicon.ico' 可以用这个做判断
    if(request.url !== '/favicon.ico'){
        console.log('be log only once')
        response.write(name.sayname());//在客户端浏览器打印内容
        response.end();//这个一定要加上，表示响应结束，否则浏览器会一直转，也可以在end输出内容 例如：response.end('hello')
    }
} ).listen(8000)