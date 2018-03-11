var path = require('path');
module.exports = {
    entry: './app/index.js',
    output: {
        filename: 'index.js',
        path: path.resolve(__dirname,'dist'),
        publicPath: 'temp/'
    },
    module:{
        loaders:[
            {
                test:/\.js$/,
                exclude:/node_modules/,
                loaders:"babel-loader",
                query:{
                    presets:['es2015','react']
                }
            }
        ]
    },
    devServer:{
        contentBase:'./',
        host:'localhost',
        compress:true,
        port:1717
    }
}