/**
 * Created by Zhuweiwei on 2017/7/3 0003.
 */
//密码保存逻辑
//原始密码框输入时去掉错误提示
var path = $("#web").val();
var userId = getUrlParam("userId");
$(".setPassWrap div.editPass .originPass input").on("focus",function () {
    $(".editFileWrapper div.editPass span i").removeClass("errorTip");
})
$(".setPassWrap .savePasswd").click(function () {
    var oldPasswd = trimStr($(".setPassWrap div.editPass .originPass input").val());
    var newPasswd = trimStr($(".setPassWrap div.editPass .newPass input").val());
    var reg = /^[\s]{0,}$/g;//整个字符串为空，或则都是空白字符。
    if(!reg.test(oldPasswd) && !reg.test(newPasswd)){
        if(newPasswd.length<6){
            //弹出toast密码过于简单，请重新输入
            $(".ancEditMessToast").html("密码过于简单，请重新输入");
            $(".ancEditMessToast").css({
                "width" : "228px",
                "display" : "block"
            })
            setTimeout(function () {
                $(".ancEditMessToast").hide();
            },1500)
        }else{
            var token = getUrlParam("token");
            /*alert(token);*/
            var url = path + "/external/my/updateMyInfo.json?t=" + parseInt(( + new Date() / 1000) / (Math.random() * 1000)) ;
            $.ajax({
                url : url,
                type : "post",
                dataType : "json",
                data : {
                    "oldPasswd" : oldPasswd,
                    "password" : newPasswd,
                    "token" : token,
                    "userId" : userId
                },
                success : function (data) {
                    //需要验证原始密码是否输入正确。
                    if(data.code==2004){
                        $(".setPassWrap div.editPass span i").addClass("errorTip");
                    }else{
                        //弹出toast密码修改成功。
                        // $(".setPassWrap .successToast").show();
                        // setTimeout(function () {
                        //     $(".editFileWrapper .successToast").hide();
                        // },1500)
                        var mobileType = getMobileType();
                        if (mobileType=="android"){
                            var backToMyPage = {"fun":"toBeAnchorBack", "data":{"type":"1"}};
                            window.stub.jsClient(JSON.stringify(backToMyPage));
                        }else {
                            var backToiosHome = {"fun" : "toIosHomePage","data" :{}};
                            window.webkit.messageHandlers.jsClient.postMessage(JSON.stringify(backToiosHome));
                        }
                    }
                },
                error : function () {
                    alert("网络请求失败");
                }

            })
        }
    }else{
        if(oldPasswd.length==0){
            $(".setPassWrap div.editPass span i").addClass("errorTip");
        }else{
            $(".setPassWrap div.editPass span i").removeClass("errorTip");
        }
        if(newPasswd.length==0){
            //弹出toast新密码不得为空
            $(".ancEditMessToast").html("新密码不得为空");
            $(".ancEditMessToast").css({
                "width" : "130px",
                "display" : "block"
            })
            setTimeout(function () {
                $(".ancEditMessToast").hide();
            },1500)
        }else if(newPasswd.length<6){
            //弹出toast密码过于简单，请重新输入
            $(".ancEditMessToast").html("密码过于简单，请重新输入");
            $(".ancEditMessToast").css({
                "width" : "220px",
                "display" : "block"
            })
            setTimeout(function () {
                $(".ancEditMessToast").hide();
            },1500)
        }
    }

})
//密码的显示隐藏
var isShowOriPass = false;
var isShowNewPass = false;
$(".editPass .originPass img").click(function () {
    var imgOriSrc = path + "/static/images/eye.png";
    isShowOriPass = !isShowOriPass;
    if(isShowOriPass){
        imgOriSrc = path + "/static/images/eyeOpen.png";
        $(this).attr("src",imgOriSrc);
        $(".editPass .originPass input").prop("type","text");
    }else {
        $(this).attr("src",imgOriSrc);
        $(".editPass .originPass input").prop("type","password");
    }
})
$(".editPass .newPass img").click(function () {
    var imgNewSrc = path + "/static/images/eye.png";
    isShowNewPass = !isShowNewPass;
    if(isShowNewPass){
        imgNewSrc = path + "/static/images/eyeOpen.png";
        $(this).attr("src",imgNewSrc);
        $(".editPass .newPass input").prop("type","text");
    }else {
        $(this).attr("src",imgNewSrc);
        $(".editPass .newPass input").prop("type","password");
    }
})
