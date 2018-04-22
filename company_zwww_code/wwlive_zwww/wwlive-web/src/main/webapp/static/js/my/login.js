/**
 * Created by Zhuweiwei on 2017/7/14 0014.
 */
var path = $("#web").val();
var userId = getUrlParam("userId");
var token = getUrlParam("token")
//QQ,weixin是否点亮
var oUrl = path + "/external/my/getMyInfo.json?&t=" + parseInt(( + new Date() / 1000) / (Math.random() * 1000)) ;
$.ajax({
    url : oUrl,
    type : "post",
    dataType : "json",
    data : {
        "userId" : userId
    },
    success : function (result) {
        /*console.log(result.data.userInfo);*/
        var data = result.data.userInfo;
        /*console.log(data);*/
        if(data.oauth.QQ==1){
            var qqLightSrc = path + "/static/images/QQlight.png";
            $(".qq").attr("src",qqLightSrc);
        }
        if(data.oauth.WeChat==1){
            var weixinLightSrc = path + "/static/images/weixinlight.png";
            $(".weixin").attr("src",weixinLightSrc);
        }
    }
})


//登录
$(".loginWrapper button").click(function () {
    var param = getAllParam();
    /*console.log(param);*/
    var userNameVal = trimStr($(".loginName input").val());
    var pasVal = trimStr($(".loginPassword input").val());
    if(userNameVal=="" || userNameVal.length==0 || pasVal=="" || pasVal.length==0){
        //如果有输入框为空则不能点击登陆。
    }else {
        var i = path + "/app/user/login/normal.json?" + param;
        $.ajax({
            url : i,
            type : "post",
            dataType : "json",
            data : {
                param : param,
                userName : userNameVal,
                password : pasVal
            },
            success : function (result) {
                console.log(result);
                /*$("#cs").val("会话成功")*/
                var loginRes = result.data.result;
                /*$("#cs").val(loginRes)*/
                if(loginRes==0){
                    //登陆失败且无绑定 显示密码错误 弹出未绑定账号弹窗。
                    /*alert("登陆失败且无绑定 显示密码错误 弹出未绑定账号弹窗")*/
                    $("em.pastip").addClass("errorTip");
                    $(".unboundPopup").animate({
                        "bottom" : "0"
                    },1500,function () {
                    })
                }else if(loginRes==1){
                    /*$("#cs").val("登录成功")*/
                    //登录成功，直接跳转页面(跳转到用户个人页)
                    // window.location.href = path + "/app/my/get?" + param;
                    var LoginInfo = new Object();
                    LoginInfo.fun = "loginSkip";
                    LoginInfo.data = result.data.userInfo;
                    var mobileType = getMobileType();
                    if(mobileType=="android"){
                        window.stub.jsClient(JSON.stringify(LoginInfo));
                    }else if(mobileType=="iphone"){
                        /*LoginInfo.data = result.data.credential;*/
                        window.webkit.messageHandlers.jsClient.postMessage(JSON.stringify(LoginInfo));
                    }else {

                    }
                }else if(loginRes==2){
                    /*$("#cs").val("有绑定")*/
                    var oauths = result.data.oauths;
                    if(oauths != "" && oauths.length>0 && oauths != undefined && oauths!=null){
                        if(oauths.length==1){//只绑定了QQ或者Weixin
                            if(oauths[0].oauthType==1){//微信
                                $("em.pastip").addClass("errorTip");
                                $(".boundPopup p i").html(userNameVal);
                                $(".boundPopup p em").html("微信");
                                $(".boundPopup p span").html(oauths[0].nickname);
                                $(".boundPopup p b").html("微信");
                                $(".boundPopup").animate({
                                    "bottom" : "0"
                                },1500)
                            }else if(oauths[0].oauthType==2){//QQ
                                $("em.pastip").addClass("errorTip");
                                $(".boundPopup p i").html(userNameVal);
                                $(".boundPopup p em").html("QQ");
                                $(".boundPopup p span").html(oauths[0].nickname);
                                $(".boundPopup p b").html("QQ");
                                $(".boundPopup").animate({
                                    "bottom" : "0"
                                },1500)
                            }
                        }else if(oauths.length > 1){//都绑定了
                            for(var i=0;i<oauths.length;i++){
                                if(oauths[i].oauthType==1){//都绑定优先微信
                                    $("em.pastip").addClass("errorTip");
                                    $(".boundPopup p i").html(userNameVal);
                                    $(".boundPopup p em").html("微信");
                                    $(".boundPopup p span").html(oauths[i].nickname);
                                    $(".boundPopup p b").html("微信");
                                    $(".boundPopup").animate({
                                        "bottom" : "0"
                                    },1500)
                                    break;
                                }
                            }
                        }
                    }
                }else if(loginRes==3){
                    /*$("#cs").val("已登录")*/
                    //弹出toast已经登录
                    $(".toast").html("当前账号已登录，无需重复登录");
                    $(".toast").css("width","215px");
                    $(".toast").show();
                    setTimeout(function () {
                        $(".toast").html("");
                        $(".toast").hide();
                    },1500)
                }else{
                    //用户异常。
                }
            }
        })
    }
})
//点击取消按钮，绑定弹窗消失
$(".cancelLeft").click(function () {
    $(".boundPopup").animate({
        "bottom" : "-111px"
    },1500);
})
//表单获取焦点时，改变定位方式
/*$(".checkPos input").on("focus",function () {
    alert(1);
    $(".checkPos").css({
        "position" : "absolute",
        "bottom" : "0"
    })
})*/
//表单获取焦点时，去掉密码错误提示
$("input").on("focus",function () {
    $("em.pastip").removeClass("errorTip");
    $(".unboundPopup").animate({
        "bottom" : "-50px"
    },1500,function () {
    })
})
//复制到剪切板
$(".copyQQ").click(function () {
    $(".copy").show();
    Copy($(".copyQQ").html());
    setTimeout(function(){
        $(".copy").hide();
    },1500);
})
// $(".copy").click(function () {
//     $(this).hide(500);
//     Copy($(".copyQQ").html());
// })

//第三方登录
function weChatLogin() {
    // alert(1)
    var obj = new Object();
    var weixinLogin = new Object();
    weixinLogin.fun = "weixinLogin";
    weixinLogin.data = obj;
    var mobileType = getMobileType();
    //弹出toast已经登录
    $(".toast").html("正在启动微信，请稍候");
    $(".toast").css("width","198px");
    $(".toast").show();
    setTimeout(function () {
        $(".toast").html("");
        $(".toast").hide();
        if(mobileType=="android"){
            window.stub.jsClient(JSON.stringify(weixinLogin));
        }else if(mobileType=="iphone"){
            window.webkit.messageHandlers.jsClient.postMessage(JSON.stringify(weixinLogin));
        }else {

        }
    },1500)

}
















