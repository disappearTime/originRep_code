var http = require('http');
var file = require('./common/file.js')
http.createServer( (req,res) => {
    if(req.url !== '/favicon.ico'){
        file.writefile('a.txt')
    }
} ).listen(8000)