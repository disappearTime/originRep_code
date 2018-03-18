const path = require('path');
const glob = require('glob');
const webpack = require('webpack')
const PurifyCSSPlugin = require('purifycss-webpack')
const copyWebpackPlugin= require("copy-webpack-plugin");//输出静态资源
if(process.env.type== "build"){
    var website={
        publicPath:"http://192.168.0.101:1717/"
    }
}else{
    var website={
        publicPath:"http://cdn.jspang.com/" //线上环境
    }
}
const uglifyjs = require('uglifyjs-webpack-plugin')
const htmlPlugin = require('html-webpack-plugin')
const extractTextPlugin = require('extract-text-webpack-plugin')
module.exports = {
    devtool:'eval-source-map',
    entry: {
        a: './src/main.js',
        b: './src/entry2.js',
        jquery:'jquery',
        vue:'vue'
    },
    output: {
        path: path.resolve(__dirname,'dist'),
        filename: '[name].js',
        publicPath: website.publicPath
    },
    module: {
        rules: [
            {
                test: /\.css$/,
                use: extractTextPlugin.extract({
                    fallback: 'style-loader',
                    use:[
                        {
                            loader: 'css-loader',
                            options:{
                                importLoaders:1,
                                modules: true
                            }
                        },
                        'postcss-loader'
                    ]
                })
            },
            {
                test: /\.(png|jpg|gif)/,
                use:[
                    {
                        loader: 'url-loader',
                        options:{
                            limit: 5000,
                            name:'/images/[name].[ext]'
                        }
                    }
                ]
            },
            {
                test: /\.(htm|html)$/i,
                use:['html-withimg-loader']
            },
            {
                test: /\.less$/,
                use: extractTextPlugin.extract({
                    use:[
                        {
                            loader: 'css-loader'
                        },
                        {
                            loader: 'less-loader'
                        }
                    ],
                    fallback: 'style-loader'
                })
            },
            {   
                test: /\.scss$/,
                use: extractTextPlugin.extract({
                    use: [{
                        loader: "css-loader"
                    }, {
                        loader: "sass-loader"
                    }],
                    fallback: "style-loader"
                })
            },
            {
                test:/\.(jsx|js)$/,
                use:{
                    loader: 'babel-loader',
                    options:{
                        presets:[
                            'env','react'
                        ]
                    }
                },
                exclude:/node_modules/
            }
        ]
    },
    plugins: [
        new uglifyjs(),
        new htmlPlugin({
            minify:{
                removeAttributeQuotes:true
            },
            hash:true,
            template:'./src/index.html'
        }),
        new extractTextPlugin('/css/index.css'),
        new PurifyCSSPlugin({
            paths: glob.sync(path.join(__dirname,'src/*.html'))
        }),
        new webpack.ProvidePlugin({
            $: 'jquery'
        }),
        new webpack.BannerPlugin('JSPang版权所有，看官方免费视频到jspang.com收看'),
        new webpack.optimize.CommonsChunkPlugin({//抽离第三方类库
            //name对应入口文件中的名字，我们起的是jQuery
            name:['jquery','vue'],
            //把文件打包到哪里，是一个路径
            filename:"assets/js/[name].js",
            //最小打包的文件模块数，这里直接写2就好
            minChunks:2
        }),
        new copyWebpackPlugin([{
            from:__dirname+'/src/public',
            to:'./public'
        }])
    ],
    devServer: {
        contentBase:path.resolve(__dirname,'dist'),
        host: '192.168.0.101',
        compress: true,
        port: 1717
    },
    watchOptions:{//使用watch热打包的参数配置
        poll:1000,//监测修改的时间（ms）
        aggregateTimeout:500,//防止重复按键，500毫秒内算按键一次
        ignored:/node_modules/,//不监测
    }
}