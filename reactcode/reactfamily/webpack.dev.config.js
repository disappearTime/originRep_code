const path = require('path');
module.exports = {
    /*入口*/
    entry: [
        'react-hot-loader/patch',
        path.join(__dirname, 'src/index.js')
    ], 
    /*输出到dist文件夹，输出文件名字为bundle.js*/
    output: {
        path: path.join(__dirname, './dist'),
        filename: 'bundle.js'
    },
    module:{
        rules: [
            {
                test: /\.js$/,
                use: ['babel-loader?cacheDirectory=true'],
                include: path.join(__dirname,'src')
            }
        ]
    },
    devServer: {
        contentBase: path.join(__dirname, './dist'),
        port: 8080,
        historyApiFallback: true, // 让所有404定位到index.html
        host: '0.0.0.0' // 允许服务器外部访问 比如手机通过ip访问
    }
};