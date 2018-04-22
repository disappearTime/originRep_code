
// $(".userMyWrap ul li span.leftTip").css("margin-top","-8px")

//贵族中心插件显示，独立版310及以上版本有贵族中心
var isdl = $("#is_dl").val();
console.log(isdl)
if (isdl == "dl"){//独立版310以上版本显示贵族中心
    var ver = getUrlParam("version");
    var verS = toNum(ver);
    if (verS>=310){
        $(".noble_center").show();
    }else {
        $(".noble_center").hide();
    }
}else {
    console.log("hh")
    $(".noble_center").show();
}
//最后一个li没有下边框
$("ul li:not(:last)").addClass("bor-bott");
//    updateMyInfoBack();
$(".bindWeixinBg").css("height",$(window).height());
//阻止弹出层之后的滑动事件
$('.bindWeixinBg').bind("touchmove",function(e){
    e.preventDefault();
    e.stopPropagation();
});
$('.bindWeixin').bind("touchmove",function(e){
    e.preventDefault();
    e.stopPropagation();
});
$('.bindWeixinBg').bind("click",function(e){
    $(this).hide();
    $(".bindWeixin").hide();
    e.preventDefault();
    e.stopPropagation();
});
$('.bindWeixin .bindWeixinBtns .span1').bind("click",function(e){
    $(".bindWeixinBg").hide();
    $(".bindWeixin").hide();
    e.preventDefault();
    e.stopPropagation();
});
$('.bindWeixin .bindWeixinBtns .span2').bind("click",function(e){
    var param = getAllParam();
    window.location.href = path + "/external/my/getMyInfo?target=blank&trace=clear&" + param;
    e.preventDefault();
    e.stopPropagation();
});

function showBindWeixin(){
    $(".bindWeixinBg").show();
    $(".bindWeixin").show();
}
function isShowPerson(){
    //版本控制 免电去掉个人资料 设置
    var isSingleApp = getUrlParam("app");
//        alert(isSingleApp);
    if(isSingleApp && isSingleApp == "dl"){
        $(".personData").show();
        $(".setSingle").show();
    }else{
        $(".personData").hide();
        $(".setSingle").hide();
    }
}
/*isShowPerson();*/
/*alert(getAllParam());*/
var isNewVersion = $("#isNew").val();
var path = $("#web").val();
var userId = $("#userId").val();
var sex = $("#sex").val();
var token = $("#token").val();
/*console.log(userId);*/
function toconsume() {
    window.location.href = path + "/external/user/toconsume?target=blank&trace=clear&userId=" + userId;
}
//跳转到修改资料页
function toEditFile() {
    var nonce = $("#nonce").val();
    var coverKey = $("#coverKey").val();
    var requestId = $("#requestId").val();
    var cnid = $("#cnid").val();
    var version = $("#version").val();
    var model = $("#model").val();
    var IMEI = $("#IMEI").val();
    var platform = $("#platform").val();
    var deviceToken = $("#deviceToken").val();
    var pushType = $("#pushType").val();
    //版本控制 免电去掉点击头像修改资料
    var isSingleApp = getUrlParam("app");
    if(isSingleApp && isSingleApp == "dl"){
        $(".bindWeixinTip").hide();
        $(".bindWerixinNow").hide();
        var param = getAllParam();
        window.location.href = path + "/external/my/getMyInfo?target=blank&trace=clear&userId=" + userId+"&token="+token+"&nonce="+nonce+"&coverKey="+coverKey+"&requestId="+requestId+"&cnid="+cnid+"&version="+version+
            "&model="+model+"&IMEI="+IMEI+"&platform="+platform+"&deviceToken="+deviceToken+"&pushType="+pushType;
    }else{

    }
}
function hideBindTip() {
    $(".bindWeixinTip").hide();
}
//埋点
function md() {
    var md = new Object();
    md.fun = "myCenterToAncClick";
    md.data = {

    }
    window.stub.jsClient(JSON.stringify(md));
}
//跳转到成为主播页
function toBeAnc() {
    md();
    window.location.href = path + "/external/app/become/anchor?target=blank&trace=clear&userId=" + userId;
}
//跳转到设置页
function toSetting() {
    var nonce = $("#nonce").val();
    var coverKey = $("#coverKey").val();
    var requestId = $("#requestId").val();
    var cnid = $("#cnid").val();
    var version = $("#version").val();
    var model = $("#model").val();
    var IMEI = $("#IMEI").val();
    var platform = $("#platform").val();
    var deviceToken = $("#deviceToken").val();
    var pushType = $("#pushType").val();
    window.location.href = path + "/external/user/setting?target=blank&trace=clear&isNewVersion=" +isNewVersion +"&userId="+userId+"&token="+token+"&nonce="+nonce+"&coverKey="+coverKey+"&requestId="+requestId+"&cnid="+cnid+"&version="+version+
        "&model="+model+"&IMEI="+IMEI+"&platform="+platform+"&deviceToken="+deviceToken+"&pushType="+pushType;
}
//跳转到贵族中心
function toNoble() {
    var nonce = $("#nonce").val();
    var coverKey = $("#coverKey").val();
    var requestId = $("#requestId").val();
    var cnid = $("#cnid").val();
    var version = $("#version").val();
    var model = $("#model").val();
    var IMEI = $("#IMEI").val();
    var platform = $("#platform").val();
    /*var deviceToken = $("#deviceToken").val();*/
    /*var pushType = $("#pushType").val();*/
    window.location.href = path + "/app/nobility/my/nobilitypage?userId=" + userId +"&coverKey="+coverKey+"&requestId="+requestId+"&cnid="+cnid+"&version="+version+
        "&model="+model+"&IMEI="+IMEI+"&platform="+platform + "&nonce=" + nonce + "&target=blank&test=2222233455566";
}
//用于刷新页面信息
function updateMyInfoBack(id,tokenL){
    userId = id;
    token = tokenL;
    var path = $("#web").val();
    var userIdL = id;
    var userName = $("#userName").html();
    console.log("userName============="+userName);
    var sexold = $(".sexold").attr("data-sex");
    console.log("sexold============="+sexold);
    var headImgSrc = $(".uerMyTx").attr("src");
    var myfollowNum = $("#myfollowNum").html();
    var mycontrib = $("#mycontrib").html();
    var myrank = $("#myrank").html();
    var diamonds = $("#diamonds").html();
    var isNewVersion = $("#isNew").val();
    console.log("diamonds============="+$.trim(diamonds));
    var i = path + "/external/my/getMyInfo.json?&t=" + parseInt(( + new Date() / 1000) / (Math.random() * 1000)) ;
    $.ajax({
        url : i,
        type : "post",
        dataType : "json",
        data : {
            "userId" : userIdL
        },
        success : function (result) {
            /*alert(JSON.stringify(result));*/
            var userInfo = result.data.userInfo;
            // alert(JSON.stringify(userInfo));
            //用户昵称
            if(userInfo.userName != userName){
                $("#userName").html(userInfo.userName);
            }
            //用户性别
            if(userInfo.sex != sexold){
                sex = userInfo.sex;
                if(userInfo.sex=="0"){
                    $(".sexold").attr("src",path+"/static/images/women.png");
                    $(".sexold").attr("data-sex","0");
                    if($(".uerMyTx").attr("src")=="/static/images/defaultMan.png") {
                        $(".uerMyTx").attr("src", path+'/static/images/defaultWoman.png');
                    }
                }else if(userInfo.sex=="1"){
                    $(".sexold").attr("src",path+"/static/images/mans.png");
                    $(".sexold").attr("data-sex","1");
                    if($(".uerMyTx").attr("src")=="/static/images/defaultWoman.png"){
                        $(".uerMyTx").attr("src",path+'/static/images/defaultMan.png');
                    }
                }
            }
            //用户头像
            // alert();
            if(userInfo.headImg != headImgSrc){
                if(userInfo.headImg != ""){
                    $(".uerMyTx").attr("src",userInfo.headImg);
                    $(".headImg1").attr("src",userInfo.headImg);
                }else{
                    $(".headImg1").attr("src",path+'/static/images/myUserCenterBg.png');
                    if(userInfo.sex == 1){
                            $(".uerMyTx").attr("src",path+'/static/images/defaultMan.png');
                    }else if(userInfo.sex == 0){
                            $(".uerMyTx").attr("src", path+'/static/images/defaultWoman.png');
                    }
                }

            }
            //用户贡献值
            if(userInfo.contrib != $.trim(mycontrib)){
                $("#mycontrib").html(userInfo.contrib);
            }
            //用户排行
            if(userInfo.rank != $.trim(myrank)){
                $("#myrank").html(userInfo.rank);
            }
            //用户关注数
            if(userInfo.followNum != $.trim(myfollowNum)){
                $("#myfollowNum").html(userInfo.followNum);
            }
            //用户余额
            if(userInfo.diamond != $.trim(diamonds)){
                var diamondnow = userInfo.diamond+"";
                $("#diamonds").html(diamondnow.replace(".0", ""));
            }
            //是否有新版本
            if(userInfo.isNewVersion != $.trim(isNewVersion)){
                if(userInfo.isNewVersion != 1){
                    $(".userMyWrap ul li .rightTip .isNewVersion1").hide();
                    $(".userMyWrap ul li .rightTip .isNewVersion").hide();
                }else{
                    $(".userMyWrap ul li .rightTip .isNewVersion").show();
                }
            }
        },error : function (param) {
            alert("哎呀，服务器开小差了~");
        }
    })
}
//用于充值完成后调用，判断用户是否绑定微信，没有绑定的话弹框提示绑定微信
function myBindWeixin(id,tokenL){
    userId = id;
    token = tokenL;
    var path = $("#web").val();
    var userIdL = id;
    var i = path + "/external/my/getMyInfo.json?&t=" + parseInt(( + new Date() / 1000) / (Math.random() * 1000)) ;
    $.ajax({
        url : i,
        type : "post",
        dataType : "json",
        data : {
            "userId" : userIdL
        },
        success : function (result) {
            var userInfo = result.data.userInfo;
            console.log(JSON.stringify(userInfo));
            //用户是否绑定微信
            if(userInfo.oauth.WeChat != 1){
                $(".bindWeixinTip").show();
                $(".bindWerixinNow").show();
            }
        }
    })
}

function nofind(){
    var img=event.srcElement;
    if(sex == 0){
        img.src=path+"/static/images/defaultWoman.png";
    }else{
        img.src=path+"/static/images/defaultMan.png";
    }
    // img.onerror=null; //控制不要一直跳动
}