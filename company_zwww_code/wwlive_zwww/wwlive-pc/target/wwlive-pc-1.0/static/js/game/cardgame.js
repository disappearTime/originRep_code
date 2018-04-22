/**
 * Created by zhuweiwei on 2017/10/30 0030.
 */
//获取路径（）URL地址
var oUrl = window.location.href;
// console.log(oUrl)
var reg1 = /pc.zb.cread.com/;
// console.log(reg1.test(oUrl));
var reg2 = /hd.pc.live.cread.com/;
var reg3 = /pc.live.cread.com/;
var path_;
if(reg1.test(oUrl)){//PC主播端测试环境
    path_ = "http://zb.cread.com"
}else if(reg2.test(oUrl)){//PC主播端灰度环境
    path_ = "http://hd.app.live.cread.com"
}else if(reg3.test(oUrl)){//PC主播端正式环境
    path_ = "http://app.live.cread.com"
}
// if(oUrl.match(/pc.zb.cread.com/)){//PC主播端测试环境
//     path_ = "http://zb.cread.com"
// }else if(oUrl.match(/hd.pc.live.cread.com/)){//PC主播端灰度环境
//     path_ = "http://hd.app.live.cread.com"
// }else if(oUrl.match(/pc.live.cread.com/)){//PC主播端正式环境
//     path_ = "http://app.live.cread.com"
// }
var anchorId = $("#anchorId").val();
var videoId = localStorage.getItem("videoId");
//获取主播封面
$.ajax({
    //部到灰度环境需要改灰度环境域名
    url : path_+"/launch/anchor/gameinfo.json",
    type : "post",
    dataType : "json",
    data : {
        "anchorId" : anchorId,
        "platform":"iOS",
        "coverKey":"994aa6e6b5c911c6b9bdb85b99900ec8",
        "model":"iPhone9,1",
        "IMEI":"5C6EB6EF-0408-4006-AD0E-E1112BFE46E9",
        "requestId":"5C6EB6EF04084006AD0EE1112BFE46E9",
        "nonce":"woiqvzao",
        "cnid":"1062",
        "version":"1.4.3"
    },
    success : function (data) {
        /*console.log(data);*/
        if (data.data && data.data.cardFaceUrl){
            $("#view").css({
                "background-image" : "url("+ data.data.cardFaceUrl +"),url(/static/images/game/upimg.png)"
            })
            $(".upload").addClass("modify")
        }else {
            $(".upload").removeClass("modify")
        }
    }
})
var gameflag = false;
$(".game_card").on("click",function () {
    gameflag = !gameflag;
    if (gameflag){
        $(this).attr("src","../../../static/images/game/close.png")
        $(".game").show();
        $(".game").animate({
            "right" : 0
        },1000)
    }else {
        $(this).attr("src","../../../static/images/game/card.png")
        $(".game").animate({
            "right" : "-100%"
        },1000,function () {
            $(".game").hide();
            $(".game_nav img").eq(0).attr("src","../../../static/images/game/paihang.png")
            $(".game_nav img").eq(1).attr("src","../../../static/images/game/gamecard.png")
            $(".tab_card>div").eq(0).addClass("show").siblings().removeClass("show");
            $(".game_nav img").eq(2).hide();
        })

    }
})
//今日牌面，游戏排行切换
$(".game_nav img").on("click",function () {
    var index = $(this).index();
    if(index<2){
        $(".game_nav img").eq(2).show()
        if (index==0){//显示排行
            $(this).attr("src","../../../static/images/game/paihang_light.png")
            $(".game_nav img").eq(1).attr("src","../../../static/images/game/gamecard.png")
            $(".tab_card>div").eq(1).addClass("show").siblings().removeClass("show")
        }else if(index==1){//显示牌面
            $(this).attr("src","../../../static/images/game/gamecard_light.png")
            $(".game_nav img").eq(0).attr("src","../../../static/images/game/paihang.png")
            $(".tab_card>div").eq(2).addClass("show").siblings().removeClass("show")
        }
    }else {
        $(".game_nav img").eq(2).hide()
        $(".game_nav img").eq(0).attr("src","../../../static/images/game/paihang.png")
        $(".game_nav img").eq(1).attr("src","../../../static/images/game/gamecard.png")
        $(".tab_card>div").eq(0).addClass("show").siblings().removeClass("show")
    }
})
//焦点轮播
function resort(now,len){
    prev = (now - 1);
    next = (now + 1);
    if(now==0){
        prev = (len -1)
    }else if(now==(len-1)){
        next = 0;
    }
    for (var i=0;i<len;i++) {
        $(".banner ul li").eq(i).removeClass("center")
        $(".banner ul li").eq(i).removeClass("prev")
        $(".banner ul li").eq(i).removeClass("next")
    }
    $(".banner ul li").eq(now).addClass("center")
    $(".banner ul li").eq(prev).addClass("prev")
    $(".banner ul li").eq(next).addClass("next")
}
var now = 0,
    prev,
    next;
$(".control_btn").on("click",function(){
    var len = $(".banner ul li").length;
    if($(this).index()==0){
        now--;
        if(now<0){
            now = (len - 1)
        }
    }else{
        now++;
        if(now>(len-1)){
            now = 0
        }
    }
    resort(now,len);
})
///今日牌面
function getPaimian() {
    $.ajax({
        //部到灰度环境需要改灰度环境域名
        url : path_+"/launch/game/card/todayface.json",
        type : "post",
        dataType : "json",
        data : {
            "platform":"iOS",
            "coverKey":"994aa6e6b5c911c6b9bdb85b99900ec8",
            "anchorId":"1633518",
            "model":"iPhone9,1",
            "IMEI":"5C6EB6EF-0408-4006-AD0E-E1112BFE46E9",
            "requestId":"5C6EB6EF04084006AD0EE1112BFE46E9",
            "nonce":"woiqvzao",
            "cnid":"1062",
            "version":"1.4.3"
        },
        success : function (result) {
            /*console.log("here")*/
            /*console.log(result)*/
            var cardlist = result.data.cardList;
            /*console.log(cardlist);*/
            if (cardlist && cardlist.length>0){
                var len = cardlist.length;
                for (var i=0;i<len;i++){
                    var index;
                    var gamePoint = cardlist[i].gameGiftPoints;
                    if (gamePoint=="A"){
                        index = 0
                    }else if (gamePoint=="K"){
                        index = 1
                    }else if (gamePoint=="Q"){
                        index = 2
                    }else if (gamePoint=="J"){
                        index = 3
                    }else if(gamePoint=="10"){
                        index = 4
                    }else if (gamePoint=="9"){
                        index = 5
                    }else if (gamePoint=="8"){
                        index = 6
                    }else if (gamePoint=="7"){
                        index = 7
                    }else if (gamePoint=="6"){
                        index = 8
                    }else if (gamePoint=="5"){
                        index = 9
                    }else if (gamePoint=="4"){
                        index = 10
                    }else if (gamePoint=="3"){
                        index = 11
                    }else if (gamePoint=="2"){
                        index = 12
                    }
                    if (cardlist[i].cardFace && cardlist[i].cardFace!=null && cardlist[i].cardFace!=undefined){
                        $(".banner ul li").eq(index).css({
                            "background" : "url("+ cardlist[i].cardFace +") no-repeat center center"
                        })
                    }
                    if (cardlist[i].isOnAir==1){
                        $(".banner ul li").eq(index).find("img").show();
                    }
                }
            }
        },
        error : function (k,i) {
            console.log(k)
        }
    })
}
getPaimian()
//上传图片
var myscroll_1 = new iScroll("iscroll_wrap1",{
    vScrollbar : true,
    momentum : true,
    checkDOMChanges : true,
    onBeforeScrollStart : function (e) {
        e.preventDefault();
    }
})
$("#iscroll_wrap1").mouseenter(function(){
    /*$("body").addClass("overHidden");*/
    disable_scroll()
})
$("#iscroll_wrap1").mouseleave(function(){
    /*$("body").removeClass("overHidden");*/
    enable_scroll()
})
//图片预览
function previewImg(file){
    if(file.files && file.files[0]){
        if (!file.files[0].type.match(/image.*/)){//非图片
            alert("请选择图片")
            return;
        }
        var reader = new FileReader();
        var view = $(".preview #view");
        reader.onload = function(evt){
            /*console.log(evt.target.result)*/
            $("#form_upimg").ajaxSubmit({
                url: "/anchor/cardface/upload.json",
                type: "post",
                data: {
                    "anchorId" : anchorId,
                    "videoId" : videoId,
                    "platform":"iOS",
                    "coverKey":"994aa6e6b5c911c6b9bdb85b99900ec8",
                    "model":"iPhone9,1",
                    "IMEI":"5C6EB6EF-0408-4006-AD0E-E1112BFE46E9",
                    "requestId":"5C6EB6EF04084006AD0EE1112BFE46E9",
                    "nonce":"woiqvzao",
                    "cnid":"1062",
                    "version":"1.4.3"
                },
                success: function (result) {
                    /*console.log(result);*/
                    if (result.result==0){
                        $(".uperrorToast").show();
                    }else {
                        view.css({
                            "background-image" : "url("+ evt.target.result +")"
                        })
                        $(".upload").addClass("modify")
                        getPaimian()
                    }
                }
            });
        }
        reader.readAsDataURL(file.files[0])
    }else if ((navigator.userAgent.indexOf('MSIE') >= 0)
        && (navigator.userAgent.indexOf('Opera') < 0)) {
        alert("抱歉，你的浏览器不支持 FileReader")
    }
}
//游戏排行
$.ajax({
    //部到灰度环境需要改灰度环境域名
    url : path_+"/launch/game/card/lucklist.json",
    type : "post",
    data : {
        "platform":"iOS",
        "coverKey":"994aa6e6b5c911c6b9bdb85b99900ec8",
        "anchorId":"1633518",
        "model":"iPhone9,1",
        "IMEI":"5C6EB6EF-0408-4006-AD0E-E1112BFE46E9",
        "requestId":"5C6EB6EF04084006AD0EE1112BFE46E9",
        "nonce":"woiqvzao",
        "cnid":"1062",
        "version":"1.4.3"
    },
    dataType : "json",
    timeOut : "9000",
    success : function (result) {
        /*console.log(result)*/
        var html ="";
        if (result.data.luckyList && result.data.luckyList.length>0){
            var luckyList = result.data.luckyList;
            for (var i=0;i<luckyList.length;i++){
                var userName = luckyList[i].userName;
                var bigAward = (luckyList[i].giftPrice)+"";
                if (bigAward.length>7){
                    bigAward = bigAward.substring(0,7)
                }
                if (userName.length>3){
                    userName = userName.substring(0,3)+".."
                }
                html += ("<li><img onerror='this.src=\"/static/images/game/user_cover.png\"' class='tx' src='"+ luckyList[i].headImg +"'/><em>"+ userName +"</em>&nbsp;&nbsp;"
                + "<i>价值：</i><em>"+ bigAward +"</em>&nbsp;&nbsp;<i>大奖：</i><em>"+ luckyList[i].giftNum +"</em><span>"+ (i+1) +"</span></li>")
            }
        }else {
            for (var i = 0;i<3;i++){
                html += ("<li><img class='tx' src='/static/images/game/user_cover.png'/><em>虚位以待</em>&nbsp;&nbsp;"
                + "<i>价值：</i><em>0</em>&nbsp;&nbsp;<i>大奖：</i><em>0</em><span>"+ (i+1) +"</span></li>")
            }
        }
        $("#iscroll_wrap2 ul").append(html);
        var myscroll_2 = new iScroll("iscroll_wrap2",{
            vScrollbar : true,
            momentum : true,
            checkDOMChanges : true,
            onBeforeScrollStart : function (e) {
                e.preventDefault();
            }
        })
    },
    error : function (k,i) {
        /*console.log("请求失败。。。。")*/
        console.log(k)
    }
})
$("#iscroll_wrap2").mouseenter(function(){
    /*$("body").addClass("overHidden");*/
    disable_scroll()
})
$("#iscroll_wrap2").mouseleave(function(){
    /*$("body").removeClass("overHidden");*/
    enable_scroll()
})
//阻止浏览器的滚动
// left: 37, up: 38, right: 39, down: 40,
// spacebar: 32, pageup: 33, pagedown: 34, end: 35, home: 36
var keys = [37, 38, 39, 40];

function preventDefault(e) {
    e = e || window.event;
    if (e.preventDefault)
        e.preventDefault();
    e.returnValue = false;
}

function keydown(e) {
    for (var i = keys.length; i--;) {
        if (e.keyCode === keys[i]) {
            preventDefault(e);
            return;
        }
    }
}

function wheel(e) {
    preventDefault(e);
}

function disable_scroll() {
    if (window.addEventListener) {
        window.addEventListener('DOMMouseScroll', wheel, false);
    }
    window.onmousewheel = document.onmousewheel = wheel;
    document.onkeydown = keydown;
}

function enable_scroll() {
    if (window.removeEventListener) {
        window.removeEventListener('DOMMouseScroll', wheel, false);
    }
    window.onmousewheel = document.onmousewheel = document.onkeydown = null;
}
//上传头像失败弹窗关闭
function closeUpImgToast() {
    $(".uperrorToast").hide();
}
$("body").on("click",function (ev) {
    var target = ev.target;
    var uperror = document.getElementsByClassName("uperrorToast")[0];
    var objP1 = document.getElementsByClassName("tip1_error")[0]
    var objP2 = document.getElementsByClassName("tip2_error")[0]
    if (target!=uperror && target!= objP1 && target!=objP2){
        closeUpImgToast();
    }
    ev.stopPropagation();
})