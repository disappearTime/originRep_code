/**
 * Created by Zhuweiwei on 2017/7/4 0004.
 */
$(".editFileWrapper .editPassBg").css("height",$(window).height());
stopPreAjax();
var path = $("#web").val();
var userId = getUrlParam("userId");
var isBindWeixin = "";
/*alert(getAllParam());*/
$("#birthday").focus(function(){
    $(this).blur();//防止调起键盘.
})
function showData(data) {
    /*console.log(data);*/
    var sex = data.sex;
    var strSex = ""//能否给一个默认的性别
    if(sex==0){
        strSex = "女"
    }else if(sex==1){
        strSex="男"
    }
    /*console.log(data.accout);*/
    var account = "";
    if(data.account!="undefined" && data.account!=""){
        account = data.account;
    }
    //是否绑定第三方
    /*alert(data.oauth.WeChat);*/
    if(data.oauth.QQ==1){
        var qqLightSrc = path + "/static/images/QQlight.png";
        $(".QQ").attr("src",qqLightSrc);
    }
    // alert("测试"+data.oauth.WeChat);
    if(data.oauth.WeChat==1){
        var weixinLightSrc = path + "/static/images/weixinlight.png";
        $(".weixin").attr("src",weixinLightSrc);
        isBindWeixin =1;
    }
    var img = '<img onerror="this.src=\''+path+'/static/images/defaultMan.png\'" class="editUserTx" src="'+data.headImg+'" alt="">'
    if(sex==0){
        img = '<img onerror="this.src=\''+path+'/static/images/defaultWoman.png\'" class="editUserTx" src="'+data.headImg+'" alt="">'
    }else if(sex==1){
        img = '<img onerror="this.src=\''+path+'/static/images/defaultMan.png\'" class="editUserTx" src="'+data.headImg+'" alt="">'
    }
    $(".editTx span").before(img);
    $("ul .editAccount em").html(account);
    $("ul .editNickName input").val(data.userName);
    $("ul .editSex em").html(strSex);
    $("ul .editBirth #birthday").val(data.birthday);
    $("ul .editAge em").html(data.age);
    $("ul .editConstell em").html(data.zodiac);
}
function saveDay(){//保存日期
    var birthday = $.trim($("#birthday").val());
    if (path == null || path == undefined) {
        return;
    }
    upLoadMesReload("birthday",birthday);
}
//进入页面获取数据并展示
var i = path + "/external/my/getMyInfo.json?&t=" + parseInt(( + new Date() / 1000) / (Math.random() * 1000)) ;
$.ajax({
    url : i,
    type : "post",
    dataType : "json",
    data : {
        "userId" : userId
    },
    success : function (result) {
        /*console.log(result.data.userInfo);*/
        var data = result.data.userInfo;
        /*console.log(data);*/
        showData(data);
    }
})
var calendar = new lCalendar();
calendar.init({
    'trigger': '#birthday',
    'type': 'date'
});
//更改性别，每次点击切换并保存
$(".editSex .changeSex").click(function () {
    var sex = $(".editSex em").html();
    var upNum;
    if(sex.length==0 || sex==""){
        sex = "男";
        upNum = 1;
    }else if(sex=="男"){
        sex = "女";
        upNum = 0;
    }else{
        sex = "男";
        upNum = 1;
    }
    $(".editSex em").html(sex);
    //上传操作，注意连续点击多次上传产生bug;
    upLoadMes("sex",upNum);
})
//修改昵称上传
$("ul .editNickName .backTip").click(function () {//点击右侧图标显示清空按钮
    var value = $("ul .editNickName input").val();
    $("ul .editNickName input").val("");
    $("ul .editNickName input").focus();
    $("ul .editNickName input").val(value);
})
var limitLen = 8,
    count = 0,
    reg = /[\u4e00-\u9fa5]{1}/g,//中文
    notReg = /\w{1}/g;//非中文
$("ul .editNickName input").on("input",function () {//表单输入验证
    var value = trimStr($(this).val());
    var resultCn = value.match(reg);
    var resultEn = value.match(notReg);
    var count = 0,
        cnLen = 0,
        enLen = 0,
        zfcount = 0,//字符长度
        resultCn = value.match(reg),
        resultEn = value.match(notReg);
    if(resultCn){
        cnLen = resultCn.length
        count+=cnLen;
        zfcount += cnLen
    }
    if(resultEn){
        enLen = resultEn.length/2
        count+=enLen;
        zfcount += resultEn.length;
    }
    /*console.log(count);*/
    if(count>10){
        /*var finalLen = (cnLen + enLen*2)-1;
        console.log(finalLen);
        if(zfcount>=20){
            finalLen = 20;
        }
        $(this).val($(this).val().substr(0,finalLen));//一直输入拼音最后确认，文字可能超过10个,bug*/
        var num = 0;
        for (var i=0;i<value.length;i++){
            if (value[i].match(reg)){
                num+=1
            }else if (value[i].match(notReg)){
                num+=0.5
            }
            if (num==10){
                $(this).val(value.substr(0,i+1));
                return;
            }else if (num>10){//这种情况下change事件失效，不能保存，
                $(this).val(value.substr(0,i))
                return;
            }
        }
    }
})
var nickIptValue = trimStr($("#nickTxt").val());
$("ul .editNickName input").on("focus",function () {
    nickIptValue = trimStr($(this).val());
})
$("ul .editNickName input").on("blur",function () {
    var reg2 = /^[A-Za-z0-9\u4e00-\u9fa5]+$/;//只能输入字母、数字、汉字的正则
    var value = trimStr($(this).val());
    if (value==nickIptValue){
        return
    }else {
        var count = 0,
            cnLen = 0,
            enLen = 0,
            resultCn = value.match(reg),
            resultEn = value.match(notReg);
        if(resultCn){
            cnLen = resultCn.length
            count+=cnLen;
        }
        if(resultEn){
            enLen = resultEn.length/2
            count+=enLen;
        }
        /*alert("count="+count);*/
        if(value=="" || value.length==0){//不能为空
            /*alert("昵称不能为空")*/
            $(".ancEditMessToast").html("昵称不能为空");
            $(".ancEditMessToast").css({
                "width" : "108px",
                "display" : "block"
            })
            setTimeout(function () {
                $(".ancEditMessToast").hide();
            },1500)
        }else if (count>10){//长度限制
            /*alert("请输1-8个字数字、字母、汉字");*/
            $(".ancEditMessToast").html("请输1-10个字数字、字母、汉字");
            $(".ancEditMessToast").css({
                "width" : "228px",
                "display" : "block"
            })
            setTimeout(function () {
                $(".ancEditMessToast").hide();
            },1500)
            /*var finalLen = (cnLen + enLen*2)-1;
             console.log(finalLen)
             $(this).val(value.substring(0,finalLen));*/
            var str = $.trim($(this).val());
            var newStr = "";
            var characterNum = 0;
            for (var i=0;i<str.length;i++){
                if (str[i].match(reg)){
                    characterNum += 1
                }else if ( str[i].match(notReg) ){
                    characterNum += 0.5
                }
                if (characterNum<=10){
                    newStr += str[i];
                }
            }
            $(this).val(newStr);
        }else if(!reg2.test(value)){
            /*alert("请输1-8个字数字、字母、汉字");*/
            $(".ancEditMessToast").html("请输1-10个字数字、字母、汉字");
            $(".ancEditMessToast").css({
                "width" : "228px",
                "display" : "block"
            })
            setTimeout(function () {
                $(".ancEditMessToast").hide();
            },1500)
        }else{//符合规则进行保存。
            //需要判断昵称是否存在。
            var url = path + "/external/my/updateMyInfo.json?&t=" + parseInt(( + new Date() / 1000) / (Math.random() * 1000)) ;
            $.ajax({
                url : url,
                type : "post",
                dataType : "json",
                data : {
                    "userId" : userId,
                    "userName": value
                },
                success : function (data) {
                    if(data.code==2002){
                        /*alert("昵称已存在");*/
                        $(".ancEditMessToast").html("昵称已存在");
                        $(".ancEditMessToast").css({
                            "width" : "93px",
                            "display" : "block"
                        })
                        setTimeout(function () {
                            $(".ancEditMessToast").hide();
                        },1500)
                    }else{
                        $(".ancEditMessToast").html("已保存昵称");
                        $(".ancEditMessToast").css({
                            "width" : "93px",
                            "display" : "block"
                        })
                        setTimeout(function () {
                            $(".ancEditMessToast").hide();
                        },1500)
                        var headImgSrc = $('.editUserTx')[0].src;
                        var sexL = $("ul .editSex em").html();
                        var sexV = "";
                        if(sexL == "男"){
                            sexV = 1;
                        }else if(sexL == "女"){
                            sexV = 0;
                        }
                        var birthdayL = $("ul .editBirth #birthday").val();
                        var ageL = $("ul .editAge em").html();
                        var zodiacL = $("ul .editConstell em").html();
                        var userNewName = {"fun":"updateUserName", "data":{'headImg':headImgSrc,'userName':value,'sex':sexV,'birthday':birthdayL,'age':ageL,'zodiac':zodiacL}};
                        console.log(JSON.stringify(userNewName));
                        window.stub.jsClient(JSON.stringify(userNewName));

                    }
                },
                error : function () {
                    alert("网络请求失败");
                }
            })
        }
    }

})
//跳转到修改密码页
$(".editFileWrapper li.editPass").click(function () {
    var phoneType = getMobileType();
    if ( phoneType == "iphone" ){
        var obj = new Object();
        var toeditPass = new Object();
        toeditPass.fun = "toPassPage";
        toeditPass.data = obj;
        window.webkit.messageHandlers.jsClient.postMessage(JSON.stringify(toeditPass));
    }else {
        var param = getAllParam();
        window.location.href = path + "/app/my/setData/setPassword?target=blank&trace=clear&" + param;
    }
});
$(".editFileWrapper div.editPass .cancel").click(function () {//点击取消按钮隐藏修改密码框
    $(".editFileWrapper div.editPass").hide(0);
    $(".editFileWrapper .editPassBg").hide();
});
$(".editFileWrapper div.editPassBg").click(function () {//点击取消按钮隐藏修改密码框
    $(".editFileWrapper div.editPass").hide(0);
    $(".editFileWrapper .editPassBg").hide();
});
//修改密码的时候，表单获取焦点时定位方式改为固定定位
// $(".editPass .setNewPass input").on("focus",function () {
//     alert(1);
//     $(".editFileWrapper .editPass").css({
//         "position" : "absolute",
//         "bottom" : "0"
//     });
// })
//原始密码框输入时去掉错误提示
$(".editFileWrapper div.editPass .originPass input").on("focus",function () {
    $(".editFileWrapper div.editPass span i").removeClass("errorTip");
})
$(".editFileWrapper div.editPass .confirm").click(function () {
    var oldPasswd = trimStr($(".editFileWrapper div.editPass .originPass input").val());
    var newPasswd = trimStr($(".editFileWrapper div.editPass .newPass input").val());
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
            var url = path + "/external/my/updateMyInfo.json?&t=" + parseInt(( + new Date() / 1000) / (Math.random() * 1000)) ;
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
                    console.log(data.code);
                    //需要验证原始密码是否输入正确。
                    if(data.code==2004){
                        $(".editFileWrapper div.editPass span i").addClass("errorTip");
                    }else{
                        //弹出toast密码修改成功。
                        $(".editFileWrapper div.editPass").hide();
                        $(".editFileWrapper .successToast").show();
                        setTimeout(function () {
                            $(".editFileWrapper .successToast").hide();
                        },1500)
                    }
                },
                error : function () {
                    alert("网络请求失败");
                }

            })
        }
    }else{
        if(oldPasswd.length==0){
            $(".editFileWrapper div.editPass span i").addClass("errorTip");
        }else{
            $(".editFileWrapper div.editPass span i").removeClass("errorTip");
        }
        if(newPasswd.length==0){
            //弹出toast新密码不得为空
            $(".ancEditMessToast").html("新密码不得为空");
            $(".ancEditMessToast").css({
                "width" : "105px",
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
//更改资料上传
function upLoadMes(mesType,mes) {
    var url = path + "/external/my/updateMyInfo.json?&"+mesType+"="+mes+"&t=" + parseInt(( + new Date() / 1000) / (Math.random() * 1000)) ;
    $.ajax({
        url : url,
        type : "post",
        dataType : "json",
        data : {
            "userId" : userId,
        },
        success : function (data) {

            // console.log($('.editUserTx')[0].src);
            var headImgSrc = $('.editUserTx')[0].src;
            var value = $("ul .editNickName input").val();
            var sexL = $("ul .editSex em").html();
            var sexV = "";
            if(sexL == "男"){
                sexV = 1;
                if($(".editUserTx").attr("src")=="/static/images/defaultWoman.png"){
                    $(".editUserTx").attr("src",path+'/static/images/defaultMan.png');
                }

            }else if(sexL == "女"){
                sexV = 0;
                if($(".editUserTx").attr("src")=="/static/images/defaultMan.png") {
                    $(".editUserTx").attr("src", path+'/static/images/defaultWoman.png');
                }
            }
            var birthdayL = $("ul .editBirth #birthday").val();
            var ageL = $("ul .editAge em").html();
            var zodiacL = $("ul .editConstell em").html();
            var userNewName = {"fun":"updateUserName", "data":{'headImg':headImgSrc,'userName':value,'sex':sexV,'birthday':birthdayL,'age':ageL,'zodiac':zodiacL}};
            console.log(JSON.stringify(userNewName));
            window.stub.jsClient(JSON.stringify(userNewName));
        },
        error : function () {
            alert("网络请求失败");
        }
    })
}
function upLoadMesReload(mesType,mes) {
    var url = path + "/external/my/updateMyInfo.json?&"+mesType+"="+mes+"&t=" + parseInt(( + new Date() / 1000) / (Math.random() * 1000)) ;
    $.ajax({
        url : url,
        type : "post",
        dataType : "json",
        data : {
            "userId" : userId,
        },
        success : function (result) {
            // alert(JSON.stringify(data));
            console.log(JSON.stringify(result));
            if(result.data !=null){
                if(result.data.userInfo.age ==null || result.data.userInfo.age== undefined || result.data.userInfo.age==""){

                }else{
                    // alert("111"+result.data.userInfo.age);
                    $("ul .editAge em").html(result.data.userInfo.age);
                    $("ul .editConstell em").html(result.data.userInfo.zodiac);
                    // alert("222"+result.data.userInfo.zodiac);
                    var headImgSrc = $('.editUserTx')[0].src;
                    var value = $("ul .editNickName input").val();
                    var sexL = $("ul .editSex em").html();
                    var sexV = "";
                    if(sexL == "男"){
                        sexV = 1;
                    }else if(sexL == "女"){
                        sexV = 0;
                    }
                    var birthdayL = $("ul .editBirth #birthday").val();
                    var ageL = $("ul .editAge em").html();
                    var zodiacL = $("ul .editConstell em").html();
                    var userNewName = {"fun":"updateUserName", "data":{'headImg':headImgSrc,'userName':value,'sex':sexV,'birthday':birthdayL,'age':result.data.userInfo.age,'zodiac':result.data.userInfo.zodiac}};
                    console.log(JSON.stringify(userNewName));
                    window.stub.jsClient(JSON.stringify(userNewName));
                }
            }
        },
        error : function () {
            alert("网络请求失败");
        }
    })
}
//跳转到登录页
function toLoginPage() {
    var phoneType = getMobileType();
    if ( phoneType == "iphone" ){
        var obj = new Object();
        var toLoginP = new Object();
        toLoginP.fun = "toLoginPage";
        toLoginP.data = obj;
        window.webkit.messageHandlers.jsClient.postMessage(JSON.stringify(toLoginP));
    }else {
        var param = getAllParam();
        window.location.href = path + "/app/user/switch?target=blank&trace=clear&" + param;
    }
}
//微信绑定
function WeChatBound(){
    var obj = new Object();
    var wechatBound = new Object();
    wechatBound.fun = "boundWeixin";
    wechatBound.data = obj;
    var mobileType = getMobileType();

    if(isBindWeixin != 1){
        /*微信掉起需要时间  toast提示*/
        $(".ancEditMessToast").html("正在启动微信，请稍候");
        $(".ancEditMessToast").css({
            "width" : "198px",
            "display" : "block"
        })
        setTimeout(function () {
            $(".ancEditMessToast").hide();
            if(mobileType=="android"){
                window.stub.jsClient(JSON.stringify(wechatBound));
            }else if(mobileType=="iphone"){
                window.webkit.messageHandlers.jsClient.postMessage(JSON.stringify(wechatBound));
            }else {

            }
        },1500)
    }else{
        /*微信掉起需要时间  toast提示*/
        $(".ancEditMessToast").html("已绑定微信");
        $(".ancEditMessToast").css({
            "width" : "150px",
            "display" : "block"
        })
        setTimeout(function () {
            $(".ancEditMessToast").hide();
        },1500)
    }


}














