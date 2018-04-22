/**
 * Created by Zhuweiwei on 2017/7/3 0003.
 */
//获取当前昵称
var path = $("#web").val();
var userId = getUrlParam("userId");
$("#nickName").focus();//进入页面获取焦点
$("img").addClass("empty");
var value = $("#nickName").val(),
    limitLen = 8,
    count = 0,
    reg = /[\u4e00-\u9fa5]{1}/g,//中文
    notReg = /\w{1}/g;//非中文
var resultCn = value.match(reg);
var resultEn = value.match(notReg);
if(resultCn){
    count+=resultCn.length;
}
if(resultEn){
    count+=resultEn.length/2;
}
$("p em").html(count);//可能存在问题，进入页面时默认的昵称长度大于8，这在之前注册提交时应该要对昵称做长度限制，避免从后台数据中获取的数据长度大于8.
$("#nickName").val("");
$("#nickName").val(value);//重新赋值
//文本框获取焦点时显示X
$("#nickName").focus(function(){
    $("img").addClass("empty");
    $("p i").html("支持1-10个字的汉字、字母、数字");
    $("p i").css("color","#999999")
})
//点击空白处隐藏x.
$(".setNickWrap").on("click",function(e){
    var target = e.target;
    var oIpt = document.getElementsByTagName("input")[0];
    var oImg = document.getElementsByTagName("img")[0];
    if(target==oIpt || target==oImg){
        $("img").addClass("empty")
    }else{
        $("img").removeClass("empty");
    }
})
//文本框输入逻辑，最长10个汉字，2个字符按一个数字。限制输入逻辑有问题
$("#nickName").on("input",function(){
    var value = $(this).val(),
        count = 0,
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
    if(count>10){
        var finalLen = (cnLen + enLen*2)-1;
        $("#nickName").val($("#nickName").val().substring(0,finalLen));//一直输入拼音最后确认，文字可能超过10个,bug
        count = 10;
    }
    $("p em").html(count);
})
//点击右侧x，清空，且获取焦点，调出键盘
$("img").click(function(){
    $(this).addClass("empty");
    $("#nickName").val("");
    $("#nickName").focus();
    $("p em").html(0);
})
//button按钮提交逻辑
$("button").click(function(){
    var value = $("#nickName").val();
    var reg = /^[A-Za-z0-9\u4e00-\u9fa5]+$/;//只能输入字母、数字、汉字的正则
    if(value.length==0){
        $("p i").html("昵称不得为空")
        $("p i").css("color","#ff0000")
    }else{
        if(reg.test(value)){
            var fontL = 0;
            var resultCn = value.match(/[\u4e00-\u9fa5]{1}/g);
            var resultEn = value.match(/\w{1}/g);
            if(resultCn){
                fontL += resultCn.length;
            }
            if(resultEn){
                fontL += resultEn.length/2;
            }
            /*console.log(fontL)*/
            if(fontL>10){
                $("p i").html("支持1-10个字的汉字、字母、数字");
                $("p i").css("color","#ff0000")
            }else {
                //验证通过发送ajax请求，请求成功弹出toast。
                $("p i").html("支持1-10个字的汉字、字母、数字");
                $("p i").css("color","#999999")
                /*alert("保存")*/
                var url = path + "/external/my/updateMyInfo.json?t=" + parseInt(( + new Date() / 1000) / (Math.random() * 1000)) ;
                $.ajax({
                    url: url,
                    type:"post",
                    dataType : "json",
                    data : {
                        "userId" : userId,
                        "userName" : value
                    },
                    success : function (data) {
                        if(data.code==2002){
                            /*alert("昵称已存在");*/
                            $("p i").html("昵称已存在");
                            $("p i").css("color","#ff0000")
                        }else{
                            $(".toast").show();
                            setTimeout(function () {
                                $(".toast").hide();
                                var userNewName = {"fun":"updateLiveUserName", "data":{'userName':value}};
                                /*console.log(JSON.stringify(userNewName));*/
                                window.stub.jsClient(JSON.stringify(userNewName));
                            },1500)
                        }
                    }
                })
            }
        }else{
            $("p i").html("支持1-10个字的汉字、字母、数字");
            $("p i").css("color","#ff0000")
        }
    }
})
function getUrlParam(name) {
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)"); //构造一个含有目标参数的正则表达式对象
    var r = window.location.search.substr(1).match(reg);  //匹配目标参数
    //alert(r);
    if (r != null) return unescape(r[2]); return null; //返回参数值
}
