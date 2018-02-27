require.config({
    shim: {//没有继承AMD规范的第三方插件需要配置
        "underscore": {
            exports: '_'
        },
        "jquery.form": {
            deps: ["jquery"]
        }
    },
    paths: {
        "jquery": ["http://libs.baidu.com/jquery/2.0.3/jquery","js/jquery"],//可以写多个路径，远程加载失败加载本地的
        "a": "js/main.js"
    }
})