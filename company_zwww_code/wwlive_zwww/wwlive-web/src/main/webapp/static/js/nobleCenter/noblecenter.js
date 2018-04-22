/**
 * Created by Administrator on 2017/8/30 0030.
 */
var path = $("#web").val();
var nobleIndex = 0; //6中贵族激活状态的贵族索引 0-5
if (getUrlParam("nobleIndex") && getUrlParam("nobleIndex")!=0 && getUrlParam("nobleIndex")!="" && getUrlParam("nobleIndex")!=null && getUrlParam("nobleIndex")!= undefined){
    nobleIndex = getUrlParam("nobleIndex");
}
$(".nobleTabList li").eq(nobleIndex).find("span").addClass("active");
if (nobleIndex==0){
    $(".nobleLogoImg").attr("src",path + "/static/images/nobleCenter/sheng.png");
    $(".nobleLogoName img").attr("src",path + "/static/images/nobleCenter/shengart.png");
}else if (nobleIndex==1){
    $(".nobleLogoImg").attr("src",path + "/static/images/nobleCenter/long.png");
    $(".nobleLogoName img").attr("src",path + "/static/images/nobleCenter/longart.png");
}else if (nobleIndex==2){
    $(".nobleLogoImg").attr("src",path + "/static/images/nobleCenter/hei.png");
    $(".nobleLogoName img").attr("src",path + "/static/images/nobleCenter/heiart.png");
}else if (nobleIndex==3){
    $(".nobleLogoImg").attr("src",path + "/static/images/nobleCenter/mofa.png");
    $(".nobleLogoName img").attr("src",path + "/static/images/nobleCenter/mofaart.png");
}else if (nobleIndex==4){
    $(".nobleLogoImg").attr("src",path + "/static/images/nobleCenter/zi.png");
    $(".nobleLogoName img").attr("src",path + "/static/images/nobleCenter/zijingart.png");
}else if (nobleIndex==5){
    $(".nobleLogoImg").attr("src",path + "/static/images/nobleCenter/shendian.png");
    $(".nobleLogoName img").attr("src",path + "/static/images/nobleCenter/shendianart.png");
}
var isFirst = true;//是否首次开通
var nobleName = "圣骑士";
var payType;//支付方式 0-微信 1-支付宝 2-钻石
var canKaitong = true;//是否能够开通贵族，控制立即开通按钮的颜色
var param = getAllParam();//通用参数
var issqsEnd = 0;//圣骑士是否过期 0表示还没有过期，1表示快要过期
var sqsEndTime = "";
var sqsDy = "";
var sqsDyEndTime = "";
var islqsEnd = 0;
var lqsEndTime = "";
var lqsDy = "";
var lqsDyEndTime = "";
var ishqsEnd = 0;
var hqsEndTime = "";
var hqsDy = "";
var hqsDyEndTime = "";
var ismfqsEnd = 0;
var mfqsEndTime = "";
var mfqsDy = "";
var mfqsDyEndTime = "";
var iszjqsEnd = 0;
var zjqsEndTime = "";
var zjqsDy = "";
var zjqsDyEndTime = "";
var issdqsEnd = 0;
var sdqsEndTime = "";
var sdqsDy = "";
var sdqsDyEndTime = "";
var paladin = "57";
var dragon = "58";
var black = "59";
var magic = "60";
var bauhinia = "61";
var temple = "62";
var nobleTime = JSON.parse($("#nobleTime").val());
/*alert(JSON.stringify(nobleTime));*/
if (nobleTime && nobleTime!=null && nobleTime!=undefined && nobleTime.length>0){
    for (var i=0;i<nobleTime.length;i++){
        if (nobleTime[i].goodsId == paladin){
            issqsEnd = 1;
            sqsEndTime = nobleTime[i].endTime;
            if (nobleTime[i].balance){
                sqsDy = nobleTime[i].balance;
            }
            if (nobleTime[i].couponCndTime){
                sqsDyEndTime = nobleTime[i].couponCndTime
            }
        }else if (nobleTime[i].goodsId == dragon){
            islqsEnd = 1;
            lqsEndTime = nobleTime[i].endTime;
            if (nobleTime[i].balance){
                lqsDy = nobleTime[i].balance;
            }
            if (nobleTime[i].couponCndTime){
                lqsDyEndTime = nobleTime[i].couponCndTime
            }
        }else if (nobleTime[i].goodsId == black){
            ishqsEnd = 1;
            hqsEndTime = nobleTime[i].endTime;
            if (nobleTime[i].balance){
                hqsDy = nobleTime[i].balance;
            }
            if (nobleTime[i].couponCndTime){
                hqsDyEndTime = nobleTime[i].couponCndTime
            }
        }else if (nobleTime[i].goodsId == magic){
            ismfqsEnd = 1;
            mfqsEndTime = nobleTime[i].endTime;
            if (nobleTime[i].balance){
                mfqsDy = nobleTime[i].balance;
            }
            if (nobleTime[i].couponCndTime){
                mfqsDyEndTime = nobleTime[i].couponCndTime
            }

        }
        else if (nobleTime[i].goodsId == bauhinia){
            iszjqsEnd = 1;
            zjqsEndTime = nobleTime[i].endTime;
            if (nobleTime[i].balance){
                zjqsDy = nobleTime[i].balance;
            }
            if (nobleTime[i].couponCndTime){
                zjqsDyEndTime = nobleTime[i].couponCndTime
            }
        }else if (nobleTime[i].goodsId == temple){
            issdqsEnd = 1;
            sdqsEndTime = nobleTime[i].endTime;
            if (nobleTime[i].balance){
                sdqsDy = nobleTime[i].balance;
            }
            if (nobleTime[i].couponCndTime){
                sdqsDyEndTime = nobleTime[i].couponCndTime
            }
        }
    }
}
/*if (issqsEnd==1){
    $(".nobleTl").show();
    $(".nobleqs em").html("圣")
    $(".nobleqs i").html(sqsEndTime)
    $(".nobleDyq em").html(sqsDy)
    $(".nobleDyq i").html(sqsDyEndTime)
}*/
if (nobleIndex==0){
    if (issqsEnd==1){
        $(".nobleTl").show();
        $(".nobleqs em").html("圣")
        $(".nobleqs i").html(sqsEndTime)
        $(".nobleDyq em").html(sqsDy)
        $(".nobleDyq i").html(sqsDyEndTime)
    }
}else if (nobleIndex==1){
    if (islqsEnd==1){
        $(".nobleTl").show();
        $(".nobleqs em").html("龙")
        $(".nobleqs i").html(lqsEndTime)
        $(".nobleDyq em").html(lqsDy)
        $(".nobleDyq i").html(lqsDyEndTime)
    }
}else if (nobleIndex==2){
    if (ishqsEnd==1){
        $(".nobleTl").show();
        $(".nobleqs em").html("黑")
        $(".nobleqs i").html(hqsEndTime)
        $(".nobleDyq em").html(hqsDy)
        $(".nobleDyq i").html(hqsDyEndTime)
    }
}else if (nobleIndex==3){
    if (ismfqsEnd==1){
        $(".nobleTl").show();
        $(".nobleqs em").html("魔法")
        $(".nobleqs i").html(mfqsEndTime)
        $(".nobleDyq em").html(mfqsDy)
        $(".nobleDyq i").html(mfqsDyEndTime)
    }
}else if (nobleIndex==4){
    if (iszjqsEnd==1){
        $(".msqq").show();
        $(".nobleTl").show();
        $(".nobleqs em").html("紫荆")
        $(".nobleqs i").html(zjqsEndTime)
        $(".nobleDyq em").html(zjqsDy)
        $(".nobleDyq i").html(zjqsDyEndTime)
    }
}else if (nobleIndex==5){
    if (issdqsEnd==1){
        $(".msqq").show();
        $(".nobleTl").show();
        $(".nobleqs em").html("神殿")
        $(".nobleqs i").html(sdqsEndTime)
        $(".nobleDyq em").html(sdqsDy)
        $(".nobleDyq i").html(sdqsDyEndTime)
    }
}
$(function(){
    $(".noblePrivileges ul li").css("height",$(".noblePrivileges ul li").width());
    $(".noblePayContainer").css("height",$(window).height());
    //阻止弹出层之后的滑动事件
    $('.noblePayContainer').bind("touchmove",function(e){
        e.preventDefault();
        e.stopPropagation();
    });
    $('.noblePay').bind("touchmove",function(e){
        e.preventDefault();
        e.stopPropagation();
    });
    //切换支付方式
    payTypeChange();
    //自动续费切换
    /*checkToggle();*/
    //骑士logo图片
    var LogoImgs = [
        path + "/static/images/nobleCenter/sheng.png",
        path + "/static/images/nobleCenter/long.png",
        path + "/static/images/nobleCenter/hei.png",
        path + "/static/images/nobleCenter/mofa.png",
        path + "/static/images/nobleCenter/zi.png",
        path + "/static/images/nobleCenter/shendian.png"
    ];
    //骑士名称图片数组
    var nobleArts = [
        path + "/static/images/nobleCenter/shengart.png",
        path + "/static/images/nobleCenter/longart.png",
        path + "/static/images/nobleCenter/heiart.png",
        path + "/static/images/nobleCenter/mofaart.png",
        path + "/static/images/nobleCenter/zijingart.png",
        path + "/static/images/nobleCenter/shendianart.png"
    ];

    $(".nobleTabList li span").unbind("click").click(function(){
        // alert($(this).html());
        $(".nobleTabList li span").removeClass("active");
        $(this).addClass("active");
        var index = $(this).attr("index");
        nobleIndex = index;
        if (index==0){
            $(".noblename").html("圣骑士");
            $(".msqq").hide();
            if (issqsEnd==1){
                $(".nobleTl").show();
                $(".nobleqs em").html("圣")
                $(".nobleqs i").html(sqsEndTime)
                $(".nobleDyq em").html(sqsDy)
                $(".nobleDyq i").html(sqsDyEndTime)
            }else {
                $(".nobleTl").hide();
            }
        }else if (index==1){
            $(".noblename").html("龙骑士");
            $(".msqq").hide();
            if (islqsEnd==1){
                $(".nobleTl").show();
                $(".nobleqs em").html("龙")
                $(".nobleqs i").html(lqsEndTime)
                $(".nobleDyq em").html(lqsDy)
                $(".nobleDyq i").html(lqsDyEndTime)
            }else {
                $(".nobleTl").hide();
            }
        }else if (index==2){
            $(".noblename").html("黑骑士");
            $(".msqq").hide();
            if (ishqsEnd==1){
                $(".nobleTl").show();
                $(".nobleqs em").html("黑")
                $(".nobleqs i").html(hqsEndTime)
                $(".nobleDyq em").html(hqsDy)
                $(".nobleDyq i").html(hqsDyEndTime)
            }else {
                $(".nobleTl").hide();
            }
        }else if (index==3){
            $(".noblename").html("魔法骑士");
            $(".msqq").hide();
            if (ismfqsEnd==1){
                $(".nobleTl").show();
                $(".nobleqs em").html("魔法")
                $(".nobleqs i").html(mfqsEndTime)
                $(".nobleDyq em").html(mfqsDy)
                $(".nobleDyq i").html(mfqsDyEndTime)
            }else {
                $(".nobleTl").hide();
            }
        }else if (index==4){
            $(".noblename").html("紫荆骑士");
            $(".nobleqs em").html("紫荆")
            if (iszjqsEnd==1){
                $(".msqq").show();
                $(".nobleTl").show();
                $(".nobleqs em").html("紫荆")
                $(".nobleqs i").html(zjqsEndTime)
                $(".nobleDyq em").html(zjqsDy)
                $(".nobleDyq i").html(zjqsDyEndTime)
            }else {
                $(".msqq").hide();
                $(".nobleTl").hide();
            }
        }else if (index==5){
            $(".noblename").html("神殿骑士");
            if (issdqsEnd==1){
                if (index==5){
                    $(".msqq").show();
                }
                $(".nobleTl").show();
                $(".nobleqs em").html("神殿")
                $(".nobleqs i").html(sdqsEndTime)
                $(".nobleDyq em").html(sdqsDy)
                $(".nobleDyq i").html(sdqsDyEndTime)
            }else {
                $(".msqq").hide();
                $(".nobleTl").hide();
            }
        }
        $(".nobleLogoImg").attr("src",LogoImgs[index]);
        $(".nobleLogoName img").attr("src",nobleArts[index]);
        /*$(".nobleContent1").css("background-image","url("+imgar[index].src+")");*/

        var picforNoble1 = [
            path + "/static/images/nobleCenter/youyan-gray.png",
            /*path + "/static/images/nobleCenter/fanzuan-gray.png",*/
            path + "/static/images/nobleCenter/caokong-gray.png",
            path + "/static/images/nobleCenter/zhaogao-gray.png",
            path + "/static/images/nobleCenter/xiaomi-gray.png",
            path + "/static/images/nobleCenter/changyan-gray.png"
        ];
        var picforNoble2 = [
            path + "/static/images/nobleCenter/youyan.png",
            /*path + "/static/images/nobleCenter/fanzuan.png",*/
            path + "/static/images/nobleCenter/caokong.png",
            path + "/static/images/nobleCenter/zhaogao.png",
            path + "/static/images/nobleCenter/xiaomi.png",
            path + "/static/images/nobleCenter/changyan.png"
        ];
        var index2 = parseInt(index)+7;
        //动态展示贵族特权列表
        for (var i = 7; i <= index2; i++) {
            /*if(index2==10){
                $(".noblePrivileges li img").eq(i).attr("src",picforNoble2[i-6]);
                $(".noblePrivileges li img").eq(i+1).attr("src",picforNoble2[i-5]);
            }else if(index2==11){
                $(".noblePrivileges li img").eq(i).attr("src",picforNoble2[i-6]);
            }else{
                $(".noblePrivileges li img").eq(i).attr("src",picforNoble2[i-6]);
            }*/
            $(".noblePrivileges li img").eq(i).attr("src",picforNoble2[i-7]);
        };
        for (var i = index2; i <= 12; i++) {
            /*if(index2==10){
                $(".noblePrivileges li img").eq(i+1).attr("src",picforNoble1[i-5]);
            }else if(index2==11){

            }else{
                $(".noblePrivileges li img").eq(i).attr("src",picforNoble1[i-6]);
            }*/
            $(".noblePrivileges li img").eq(i).attr("src",picforNoble1[i-7]);
        };
    });
    //点击弹窗灰色背景隐藏弹窗
    $(".noblePayContainer").on("click",function (ev) {
        $(this).hide();
        $(".noblePay").hide();
        $(".payType div").removeClass("noblePay-active");
        $(".limit").removeClass("opacity-1");
        $(".noblePayBtn").removeClass("grey");
        $(".limit span").html("")
        $(".uploadHeadImg").hide();
        //弹窗回复默认值
        $(".noblePay .line-sp").html("");
        $(".noblePay .noblePrice").html("");
        $(".noblePay .backdiamond").html("");
        $(".noblePay .backdiyong").html("");
        /*$(".noblePay .givediamond").html("");
        $(".noblePrice .givediyong em").html("");*/
    })
});
//点击立即开通按钮弹出弹窗逻辑
function kaitong(){
    /*alert(nobleIndex);*/
    maidian(0);
    $(".noblePayContainer").show();
    if(nobleIndex <2){//场景一,开通圣骑士和龙骑士
        $(".noblePay1").show();
        $(".noblePay1 .weixinPay").addClass("noblePay-active");
        if( nobleIndex == 0 ){//开通圣骑士
            $(".noblePay1 .nobleType").html("圣骑士");
            getNoblePrice(paladin,showData);
        }else if( nobleIndex == 1 ){
            $(".noblePay1 .nobleType").html("龙骑士");
            getNoblePrice(dragon,showData);
        }
    }else if( nobleIndex>=2 &&nobleIndex<5 ){//场景二，开通黑骑士、魔法骑士、紫荆骑士
        $(".noblePay2").show();
        if (nobleIndex > 2) {
            /*$(".noblePay2 .zhifubaoPay").addClass("noblePay-active").siblings().removeClass("noblePay-active");*/
        }else {
            $(".noblePay2 .weixinPay").addClass("noblePay-active").siblings().removeClass("noblePay-active");
        }
        if( nobleIndex == 2 ){//开通黑骑士
            $(".noblePay2 .nobleType").html("黑骑士");
            getNoblePrice(black,showData);

        }else if( nobleIndex == 3 ){//开通魔法骑士
            $(".noblePay2 .nobleType").html("魔法骑士");
            getNoblePrice(magic,showData);

        }else if(nobleIndex == 4 ){//开通紫荆骑士
            $(".noblePay2 .nobleType").html("紫荆骑士");
            getNoblePrice(bauhinia,showData);
        }
    }else {//场景三,开通神殿骑士
        $(".noblePay3").show();
        /*$(".noblePay3 .zuanshiPay").addClass("noblePay-active");*/
        getNoblePrice(temple,showData);
    }
}
function hidenoblePay(){
    $(".noblePayContainer").hide();
    $(".noblePay2").hide();
    $(".noblePay1").hide();
    $(".noblePay3").hide();
    $(".uploadHeadImg").hide();
    $(".payType div").removeClass("noblePay-active");
    canKaitong = true;
    $(".limit").removeClass("opacity-1");
    $(".noblePayBtn").removeClass("grey");
    $(".limit span").html("");
    //弹窗回复默认值
    $(".noblePay .line-sp em").html("");
    $(".noblePay .noblePrice em").html("");
    $(".noblePay .backdiamond").html("");
    $(".noblePay .backdiyong").html("");
    /*$(".noblePay .givediamond").html("");
    $(".noblePrice .givediyong em").html("");*/
}

function payTypeChange(){
    $(".noblePay1 .payType div").unbind("click").click(function(){
        $(".noblePay1 .payType div").removeClass("noblePay-active");
        $(this).addClass("noblePay-active");
        /*payType = $(this).index();
        if(canKaitong){
            $(".noblePayBtn").removeClass("grey");
        }else {
            $(".noblePayBtn").addClass("grey");
        }*/
    });
    $(".noblePay2 .payType div").unbind("click").click(function(){
        $(".noblePay2 .payType div").removeClass("noblePay-active");
        $(this).addClass("noblePay-active");
        payType = $(this).index();
        /*console.log(payType);*/
        if (nobleIndex>2 && nobleIndex<5){
            var realPrice = parseInt($(".noblePay2 .noblePrice em").html());
            if (payType == 0){
                if (realPrice <= 3000){
                    canKaitong = true;
                }else {
                    canKaitong = false;
                }
            }else {
                canKaitong = true;
            }
            if(canKaitong){
                $(".noblePayBtn").removeClass("grey");
                $(".limit").removeClass("opacity-1");
            }else {
                $(".noblePayBtn").addClass("grey");
                $(".limit").addClass("opacity-1");
                $(".limit span").html("3000")
            }
        }
    });
    $(".noblePay3 .payType div").unbind("click").click(function(){
        $(".noblePay3 .payType div").removeClass("noblePay-active");
        $(this).addClass("noblePay-active");
        payType = $(this).index();
        /*console.log(payType);*/
        if (payType == 0){
            canKaitong = false;
        }else if(payType == 1) {
            var realPrice = parseInt($(".noblePay3 .noblePrice em").html());
            /*console.log(realPrice);*/
            if (realPrice <= 100000){
                canKaitong = true
            }else {
                canKaitong = false;
            }
            /*console.log(canKaitong);*/
        }else {
            canKaitong = true;
        }
        if(canKaitong){
            $(".noblePayBtn").removeClass("grey");
            $(".limit").removeClass("opacity-1");
        }else {
            $(".noblePayBtn").addClass("grey");
            $(".limit").addClass("opacity-1");
            if (payType ==0){
                $(".limit span").html("3000");
            }else if(payType == 1){
                $(".limit span").html("100000");
            }

        }
    });
}
function loading(){
    //页面正在加载toast
    $(".nobleToast-loading").css({
        "top":$(window).height()/2-24
    });
    $(".nobleToast-loading").show();
    /*setTimeout(function () {
        $(".nobleToast-loading").hide();
    },1500)*/
}
function kaitongPay(){
    maidian(1);
    if ( nobleIndex == 0 ){//开通圣骑士
        payType = $(".noblePay1 .payType div.noblePay-active").index();
        /*console.log(payType)*/
        if ( payType == 0 ){//微信支付
            loading();
            toPay(2,paladin,0,1);
        }else if ( payType == 1 ){//支付宝支付
            loading();
            toPay(3,paladin,0,1)
        }
    }else if (nobleIndex == 1){//开通龙骑士
        payType = $(".noblePay1 .payType div.noblePay-active").index();
        if ( payType == 0 ){//微信支付
            loading();
            toPay(2,dragon,0,1)
        }else if ( payType == 1 ){//支付宝支付
            loading();
            toPay(3,dragon,0,1)
        }

    }else if (nobleIndex == 2){//开通黑骑士
        payType = $(".noblePay2 .payType div.noblePay-active").index();
        if ( payType == 0 ){//微信支付
            loading();
            if (isFirst){
                toPay(2,black,1,0)
            }else {
                toPay(2,black,1,1)
            }
        }else if ( payType == 1 ){//支付宝支付
            loading();
            if (isFirst){
                toPay(3,black,1,0)
            }else {
                toPay(3,black,1,1)
            }
        }
    }else if (nobleIndex == 3){//开通魔法骑士
        payType = $(".noblePay2 .payType div.noblePay-active").index();
        if ( payType == 0 ){//微信支付
            /*alert("微信单笔最高限额3000元")*/
            /*alert(canKaitong);*/;
            if(canKaitong){
                loading();
                //调起微信支付
                if (isFirst){
                    toPay(2,magic,1,0)
                }else {
                    toPay(2,magic,1,1)
                }
            }else {
                //无法开通，不执行操作
            }
        }else if ( payType == 1 ){//支付宝支付
            loading();
            if (isFirst){
                toPay(3,magic,1,0)
            }else {
                toPay(3,magic,1,1)
            }
        }

    }else if (nobleIndex == 4){//开通紫荆骑士
        payType = $(".noblePay2 .payType div.noblePay-active").index();
        if ( payType == 0 ){//微信支付
            if(canKaitong){
                loading();
                //调起微信支付
                if (isFirst){
                    toPay(2,bauhinia,1,0)
                }else {
                    toPay(2,bauhinia,1,1)
                }
            }else {
                //无法开通，不执行操作
            }
        }else if ( payType == 1 ){//支付宝支付
            loading();
            if (isFirst){
                toPay(3,bauhinia,1,0)
            }else {
                toPay(3,bauhinia,1,1)
            }
        }

    }else if (nobleIndex == 5){//开通神殿骑士
        payType = $(".noblePay3 .payType div.noblePay-active").index();
        if ( payType == 0 ){//微信支付
            //无法开通，不执行操作
        }else if ( payType == 1 ){//支付宝支付
            if (canKaitong){
                loading();
                if (isFirst){
                    toPay(3,temple,1,0)
                }else {
                    toPay(3,temple,1,1)
                }
                //调起支付宝支付。
            }else {
                //无法开通不执行操作
            }
        }else if( payType == 2 ){//砖石支付
            diamondPay();
        }
    }
}
//自动续费切换按钮
/*function checkToggle(){
    $(".zidongCheck img").unbind("click").click(function(){
        $(".zidongCheck-p").toggleClass("opacity-0");
        if(checkXufei == true){
            $(".noblePayBtn").removeClass("grey");
            $(this).attr("src",path + "/static/images/nobleCenter/nocheck.png");
            $(".nobleToast").css({
                "width":"300px",
                "margin-left":"-150px",
                "top":$(window).height()/2-24
            });
            if (payType == 1){//选择支付宝支付时，取消自动续费不弹窗。

            }else {
                $(".nobleToast").html("您取消自动续费，将无法享受次月5折优惠");
                $(".nobleToast").show();
                setTimeout(function () {
                    $(".nobleToast").hide();
                },1500)
            }
            checkXufei = false;
        }else{
            if(payType == 1){
                $(".noblePayBtn").addClass("grey");
            }
            $(this).attr("src",path + "/static/images/nobleCenter/check.png");
            checkXufei = true;
        }
    });
}*/
//获取到骑士价格
function getNoblePrice(goodsId,fn) {
    var oUrl = path +  "/app/nobility/my/getMyNobilityPrice.json?&" + param;
    $.ajax({
        url : oUrl,
        type : "post",
        data : {
            goodsId : goodsId
        },
        dataType : "json",
        success : function (result) {
            /*console.log(result);*/
            var data = result.data;
            console.log(JSON.stringify(data))
            fn(data);
        },
        error : function () {

        }
    })
}
function showData(data) {
    console.log(data);
    /*console.log(data);*/
    //console.log(nobleIndex);
    if(nobleIndex<2){//圣骑士或者龙骑士
        /*$(".noblePay1 .noblePrice").html( "<em class='orange'>"+ data.price +"元</em>/月");*/
        if (data.isFirstBuy == 0){//首次购买
            $(".noblePay1 .jin").show();
            isFirst = true;
            /*$(".noblePay1 .noblePrice").html(data.price + "元/月");*/
            $(".noblePay1 .ktfont").html("后续")
            $(".noblePay1 .zsfont").html("+")
            var dp = data.price - data.voucher
            var dp_ = dp;
            if (dp>=1000){
                dp_ = (dp/1000)+"千"
            }
            if (dp>=10000){
                dp_ = (dp/10000)+"万"
            }
            var discountprice = dp_ + "元"
            if (nobleIndex == 0){
                $(".noblePay1 .givediamond").html(data.diamond);
                $(".noblePay1 .givediamond").removeClass("palered")
                $(".noblePay1 .givediyong").html("<em class=''></em>圣骑士");
                $(".noblePay1 .noblePrice").html("<em class=''>"+ data.price +"元</em>/月")
                $(".noblePay1 .discountprice").html(discountprice);
                $(".noblePay1 .diyong").html("开通立返<span class='backdiamond'>"+ data.firstDiamond +"</span>钻，赠<span class='backdiyong'>"+ data.firstVoucher +"</span><i class='backdiyong'>元</i>圣骑士抵用券")
            }else if(nobleIndex == 1){
                $(".noblePay1 .givediamond").html(data.diamond);
                $(".noblePay1 .givediamond").removeClass("palered")
                $(".noblePay1 .givediyong").html("<em class=''></em>龙骑士");
                $(".noblePay1 .noblePrice").html("<em class=''>"+ data.price +"元</em>/月")
                $(".noblePay1 .discountprice").html(discountprice);
                $(".noblePay1 .diyong").html("开通立返<span class='backdiamond'>"+ data.firstDiamond +"</span>钻，赠<span class='backdiyong'>"+ data.firstVoucher +"</span><i class='backdiyong'>元</i>龙骑士抵用券")
            }
            /*$(".noblePay2 .givediamond").html("600");//暂时写死
             $(".noblePay2 .givediyong").html("1900元黑骑士")*/
        }else if(data.isFirstBuy == 1){//非首次购买
            $(".noblePay1 .jin").hide();
            $(".noblePay1 .ktfont").html("使抵用券开通");
            $(".noblePay1 .zsfont").html("，再赠");
            isFirst = false;
            $(".noblePay1 .noblePrice").html("<em class=''>"+ data.salePrice +"元</em>/月");
            $(".noblePay1 .line-sp").html("<em class=''>"+ data.price +"元</em>/月");
            if (nobleIndex == 0){
                $(".noblePay1 .givediamond").html(data.diamond);
                $(".noblePay1 .givediamond").addClass("palered")
                $(".noblePay1 .givediyong").html("<em class='palered'>"+ data.voucher +"元</em>圣骑士");
                $(".noblePay1 .diyong").html("已使用：<span class='line-sp'><em class=''>"+ data.voucher +"元</em>圣骑士抵用券</span>");
            }else if (nobleIndex == 1){
                $(".noblePay1 .givediamond").html(data.diamond);
                $(".noblePay1 .givediamond").addClass("palered")
                $(".noblePay1 .givediyong").html("<em class='palered'>"+ data.voucher +"元</em>龙骑士");
                $(".noblePay1 .diyong").html("已使用：<span class='line-sp'><em class=''>"+ data.voucher +"元</em>龙骑士抵用券</span>");
            }
        }
    }else if (nobleIndex >= 2 && nobleIndex < 5 ){
        if (data.isFirstBuy == 0){//首次购买
            $(".noblePay2 .jin").show();
            $(".noblePay2 .ktfont").html("后续")
            $(".noblePay2 .zsfont").html("+")
            isFirst = true;
            var dp1 = data.price - data.voucher
            var dp1_ = dp1;
            if (dp1>=1000){
                dp1_ = (dp1/1000)+"千"
            }
            if (dp1>=10000){
                dp1_ = (dp1/10000)+"万"
            }
            var discountprice1 = dp1_ + "元"
            var dm = data.diamond;
            var dm_ = dm
            if (dm>=1000){
                dm_ = dm/1000+"千"
            }
            if(dm>=10000) {
                dm_ = dm/10000+"万"
            }
            var dv = data.voucher;
            var dv_ = dv;
            if (dv>=1000){
                dv_ = (dv/1000)+"千"
            }
            if (dv>=10000){
                dv_ = (dv/10000)+"万"
            }
            var dft = data.firstDiamond;
            var dft_ = dft;
            if (dft>=1000){
                dft_ = (dft/1000)+"千"
            }
            if (dft>=10000){
                dft_ = (dft/10000)+"万"
            }
            var dfv = data.firstVoucher;
            var dfv_ = dfv;
            if (dfv>=1000){
                dfv_ = (dfv/1000)+"千"
            }
            if (dfv>=10000){
                dfv_ = (dfv/10000)+"万"
            }
            $(".noblePay2 .noblePrice").html("<em class=''>"+ data.price +"元</em>/月");
            if (nobleIndex == 2){
                $(".noblePay2 .givediamond").html(dm_);
                $(".noblePay2 .givediamond").removeClass("palered")
                $(".noblePay2 .givediyong").html("<em class=''></em>黑骑士");
                $(".noblePay2 .discountprice").html(discountprice1);
                $(".noblePay2 .diyong").html("开通立返<span class='backdiamond'>" + dft_ + "</span>钻，赠<span class='backdiyong'>" + dfv_ + "</span><i class=''>元</i>黑骑士抵用券")
            }else if(nobleIndex == 3){
                $(".noblePay2 .givediamond").html(dm_);
                $(".noblePay2 .givediamond").removeClass("palered")
                $(".noblePay2 .givediyong").html("<em class=''></em>魔法骑士");
                $(".noblePay2 .discountprice").html(discountprice1);
                $(".noblePay2 .diyong").html("开通立返<span class='backdiamond'>" + dft_ + "</span>钻，赠<span class='backdiyong'>" + dfv_ + "</span><i class=''>元</i>魔法骑士抵用券")
                var realPrice = parseInt($(".noblePay2 .noblePrice em").html());
                if (realPrice<=3000){
                    $(".noblePay2 .weixinPay").addClass("noblePay-active").siblings().removeClass("noblePay-active")
                }else {
                    $(".noblePay2 .zhifubaoPay").addClass("noblePay-active").siblings().removeClass("noblePay-active")
                }
            }else if (nobleIndex == 4){
                var fz5 = data.diamond;
                var fz5_ = fz5
                if (fz5 >= 1000){
                    fz5_ = (fz5/1000) + "千"
                }
                if (fz5 >= 10000){
                    fz5_ = (fz5/10000) + "万"
                }
                var fdy5 = data.voucher;
                if (fdy5 > 10000){
                    fdy5 = (fdy5/10000) + "万"
                }
                var ktfz1 =  data.firstDiamond
                var ktfz1_ = ktfz1
                if (ktfz1 >= 1000){
                    ktfz1_ = (ktfz1/1000) + "千"
                }
                if (ktfz1>=10000){
                    ktfz1_ = (ktfz1/10000) + "万"
                }
                var ktfdy1 =  data.firstVoucher
                var ktfdy1_ = ktfdy1
                if (ktfdy1 >= 1000){
                    ktfdy1_ = (ktfdy1/1000) + "千"
                }
                if (ktfdy1>=10000){
                    ktfdy1_ = (ktfdy1/10000) + "万"
                }
                $(".noblePay2 .givediamond").html(fz5_);
                $(".noblePay2 .givediamond").removeClass("palered")
                $(".noblePay2 .givediyong").html("<em class=''></em>紫荆骑士");
                $(".noblePay2 .discountprice").html(discountprice1);
                $(".noblePay2 .diyong").html("开通立返<span class='backdiamond'>" + ktfz1_ + "</span>钻，赠<span class='backdiyong'>" + ktfdy1_ + "</span><i class=''>元</i>紫荆骑士抵用券")
                var realPrice = parseInt($(".noblePay2 .noblePrice em").html());
                if (realPrice<=3000){
                    $(".noblePay2 .weixinPay").addClass("noblePay-active").siblings().removeClass("noblePay-active")
                }else {
                    $(".noblePay2 .zhifubaoPay").addClass("noblePay-active").siblings().removeClass("noblePay-active")
                }
            }
            /*$(".noblePay2 .givediamond").html("600");//暂时写死
            $(".noblePay2 .givediyong").html("1900元黑骑士")*/
        }else if(data.isFirstBuy == 1){//非首次购买
            $(".noblePay2 .ktfont").html("使抵用券开通");
            $(".noblePay2 .zsfont").html("，再赠");
            var dm = data.diamond;
            var dm_ = dm;
            if (dm>=1000){
                dm_ = dm/1000+"千"
            }
            if(dm>=10000) {
                dm_ = dm/10000+"万"
            }
            var dv = data.voucher;
            var dv_ = dv
            if (dv>=1000){
                dv_ = (dv/1000)+"千"
            }
            if (dv>=10000){
                dv_ = (dv/10000)+"万"
            }
            var dvb = data.voucher;
            var dvb_ = dvb;
            if (dvb>=1000){
                dvb_ = (dvb/1000)+"千"
            }
            if (dvb>=10000){
                dvb_ = (dvb/10000)+"万"
            }
            $(".noblePay2 .jin").hide();
            isFirst = false;
            $(".noblePay2 .noblePrice").html("<em class=''>"+ data.salePrice +"元</em>/月");
            $(".noblePay2 .line-sp").html("<em class=''>"+ data.price +"元</em>/月");
            if (nobleIndex == 2){
                $(".noblePay2 .givediamond").html(dm_);
                $(".noblePay2 .givediamond").addClass("palered")
                $(".noblePay2 .givediyong").html("<em class='palered'>"+ dv_ +"元</em>黑骑士");
                $(".noblePay2 .diyong").html("已使用：<span class='line-sp'><em class=''>"+ dvb_ +"元</em>黑骑士抵用券</span>");
            }else if (nobleIndex == 3){
                $(".noblePay2 .givediamond").html(dm_);
                $(".noblePay2 .givediamond").addClass("palered")
                $(".noblePay2 .givediyong").html("<em class='palered'>"+ dv_ +"元</em>魔法骑士");
                $(".noblePay2 .diyong").html("已使用：<span class='line-sp'><em class=''>"+ dvb_ +"元</em>魔法骑士抵用券</span>");
                var realPrice = parseInt($(".noblePay2 .noblePrice em").html());
                if (realPrice<=3000){
                    $(".noblePay2 .weixinPay").addClass("noblePay-active").siblings().removeClass("noblePay-active")
                }else {
                    $(".noblePay2 .zhifubaoPay").addClass("noblePay-active").siblings().removeClass("noblePay-active")
                }
            }else if (nobleIndex == 4){
                var fz4 = data.diamond;
                var fz4_ = fz4
                if (fz4 >= 1000){
                    fz4_ = (fz4/1000) + "千"
                }
                if(fz4>=10000){
                    fz4_ = (fz4/10000) + "万"
                }
                var fdy4 = data.voucher;
                if (fdy4 > 10000){
                    fdy4 = (fdy4/10000) + "万"
                }
                var ktfdy2 =  data.voucher
                var ktfdy2_ = ktfdy2;
                if (ktfdy2 >= 1000){
                    ktfdy2_ = ( ktfdy2/1000 ) + "千"
                }
                if(ktfdy2>=10000){
                    ktfdy2_ = ( ktfdy2/10000 ) + "万"
                }
                $(".noblePay2 .givediamond").html(fz4_);
                $(".noblePay2 .givediamond").addClass("palered")
                $(".noblePay2 .givediyong").html("<em class='palered'>"+ fdy4 +"元</em>紫荆骑士");
                $(".noblePay2 .diyong").html("已使用：<span class='line-sp'><em class=''>"+ ktfdy2_ +"元</em>紫荆骑士抵用券</span>");
                var realPrice = parseInt($(".noblePay2 .noblePrice em").html());
                if (realPrice<=3000){
                    $(".noblePay2 .weixinPay").addClass("noblePay-active").siblings().removeClass("noblePay-active")
                }else {
                    $(".noblePay2 .zhifubaoPay").addClass("noblePay-active").siblings().removeClass("noblePay-active")
                }
            }
        }
    }else if (nobleIndex == 5){
        if (data.isFirstBuy == 0){//首次购买
            $(".noblePay3 .ktfont").html("后续")
            $(".noblePay3 .zsfont").html("+")
            $(".noblePay3 .jin").show();
            var discountprice1 = ((data.price - data.voucher)/10000) + "万元"
            $(".noblePay3 .payType").removeClass("closezsPay");
            $(".noblePay3 .zuanshiPay").addClass("noblePay-active");
            var fz = data.diamond;
            if (fz > 10000){
                fz = (fz/10000) + "万"
            }
            var fdy = data.voucher;
            if (fdy > 10000){
                fdy = (fdy/10000) + "万"
            }
            var ktfz3 = data.firstDiamond
            if (ktfz3 > 10000){
                ktfz3 = (ktfz3/10000) + "万"
            }
            var ktfdy3 = data.firstVoucher;
            if (ktfdy3 > 10000){
                ktfdy3 = (ktfdy3/10000) + "万"
            }
            $(".noblePay3 .givediamond").html(fz);
            $(".noblePay3 .givediamond").removeClass("palered")
            /*$(".noblePay3 .givediyong").html(fdy);*/
            $(".noblePay3 .hidd").hide();
            $(".noblePay3 .givediyong").removeClass("palered");
            $(".noblePay3 .fanzuandiyong i").removeClass("palered")
            isFirst = true;
            $(".noblePay3 .noblePrice").html("<em class=''>"+ data.price +"元</em>/月");
            $(".noblePay3 .discountprice").html(discountprice1);
            $(".noblePay3 .diyong").html("开通立返<span class='backdiamond'>" + ktfz3 + "</span>钻，赠<span class='backdiyong'>" + ktfdy3 + "</span><i class=''>元</i>神殿骑士抵用券")
            /*$(".noblePay3 .givediamond").html("600");//暂时写死
             $(".noblePay3 .givediyong").html("1900元黑骑士")*/
        }else if(data.isFirstBuy == 1){//非首次购买
            $(".noblePay3 .ktfont").html("使抵用券开通");
            $(".noblePay3 .zsfont").html("，再赠");
            $(".noblePay3 .jin").hide();
            $(".noblePay3 .payType").addClass("closezsPay");
            $(".noblePay3 .zhifubaoPay").addClass("noblePay-active");
            var fz1 = data.diamond;
            if (fz1 > 10000){
                fz1 = (fz1/10000) + "万"
            }
            var fdy1 = data.voucher;
            if (fdy1 > 10000){
                fdy1 = (fdy1/10000) + "万"
            }
            $(".noblePay3 .givediamond").html(fz1);
            $(".noblePay3 .givediamond").addClass("palered")
            $(".noblePay3 .givediyong").html(fdy1);
            $(".noblePay3 .hidd").show();
            $(".noblePay3 .givediyong").addClass("palered");
            $(".noblePay3 .fanzuandiyong i").addClass("palered");
            isFirst = false;
            $(".noblePay3 .noblePrice").html("<em class=''>"+ data.salePrice +"元</em>/月");
            $(".noblePay3 .line-sp").html("<em class=''>"+ data.price +"元</em>/月");
            $(".noblePay3 .diyong").html("已使用：<span class='line-sp'><em class=''>"+ fdy1 +"元</em>神殿骑士抵用券</span>");
        }
    }
}
//点击立即开通生成订单
function toPay(payType,goodsId,splitCavalier,lastParam){//参数分别为支付方式，骑士Id,骑士分类，是否自动续费或是否首次购买 生成订单
    var Oparams = {
        "goodsId" : goodsId,
        "payType" : payType,
        "splitCavalier" : splitCavalier
    };
    if (splitCavalier == 0){
        Oparams.isRenew = lastParam
    }
    if (splitCavalier == 1){
        Oparams.isFirstBuy = lastParam
    }
    var oUrl = path + "/app/nobility/my/insertMyNobilityOrder.json?" + param;
    $.ajax({
        url : oUrl,
        type : "post",
        dataType : "json",
        data : Oparams,
        success : function (result) {
            console.log(result);
            //请求成功后隐藏toast
            $(".nobleToast-loading").hide();
            if (result == null) {//是否增加页面过期的提示
                return;
            }
            if (result.code != 0) {
                return;
            }
            if (payType == 2) {//微信
                wexinCharge(result, payType,goodsId);
                /*timer = setInterval(function () {
                    getPayResult(result.data.orderNo);
                },2000)*/
            } else if (payType == 3) {
                aliCharge(result, payType,goodsId);
                /*timer = setInterval(function () {
                    getPayResult(result.data.orderNo);
                },2000)*/
            }
        },
        error : function (k,i) {
            console.log("充值失败")
        }
    })
}
//微信充值
function wexinCharge(result, chargeType,goodsId) {//微信
    if (result == null) {
        return;
    }
    /*console.log(result);*/
    var data = new Object();
    data.fun = "weChatCharge";
    data.data = result.data;
    data.data.goodsId = goodsId
    if (result.data == null || result.data == undefined) {
        return;
    }
    window.stub.jsClient(JSON.stringify(data));
}
//支付宝充值
function aliCharge(result, chargeType,goodsId) {//支付宝
    if (result == null) {
        return;
    }
    var data = new Object();
    data.fun = "aliCharge";
    data.data = result.data;
    data.data.way = "1";
    data.data.goodsId = goodsId;
    data.data.page = "noblePage";
    if (result.data == null || result.data == undefined) {
        return;
    }
    window.stub.jsClient(JSON.stringify(data));
}
//钻石充值
function diamondPay(){
    var oUrl = path + "/app/nobility/my/insertMyNobilityOrder.json?" + param;
    console.log(oUrl);
    $.ajax({
        url : oUrl,
        type : "post",
        dataType : "json",
        data : {
            goodsId : temple,
            payType : 4,
            splitCavalier : 1,
            isFirstBuy : 0
        },
        success : function (result) {
            console.log(result);
            var payResult;
            if (result == null){
                return;
            }
            if (result.data.result){
                payResult = result.data.result;
                if (payResult == 0){
                    $(".errorToast").show();
                    $(".nobleToast").html("支付失败");
                    setTimeout(function () {
                        $(".errorToast").html("");
                        $(".errorToast").hide();
                    },1500)
                }else if (payResult == 1){
                    hidenoblePay();
                    $(".nobleToast").show();
                    $(".nobleToast i").html("恭喜您已成功开通神殿骑士");
                    setTimeout(function () {
                        $(".nobleToast i").html("");
                        $(".nobleToast").hide();
                    },1500)
                    showNobleEndTime(temple)
                    /*isToUploadImg();*/
                }else if (payResult == 2){
                    //钻石不够，跳到充值页
                    $(".errorToast").show();
                    $(".errorToast").html("钻石不够");
                    setTimeout(function () {
                        $(".errorToast").html("");
                        $(".errorToast").hide();
                    },1500)
                    var mobileType = getMobileType();
                    if (mobileType == "android"){
                        var path = $("#web").val();
                        if (path == null || path == undefined) {
                            return;
                        }
                        var nonce = getUrlParam("nonce")
                        var coverKey = getUrlParam("coverKey")
                        var requestId = getUrlParam("requestId")
                        var userId = getUrlParam("userId")
                        var cnid = getUrlParam("cnid")
                        var version = getUrlParam("version")
                        var model = getUrlParam("model")
                        var IMEI = getUrlParam("IMEI")
                        var platform = getUrlParam("platform");
                        /*alert("userId=" + userId)*/
                        if (userId == null || userId == "" || userId == undefined) {
                            return;
                        }
                        if (userId == -1) {
                            return;
                        }
                        var isSingleApp = getUrlParam("app");
                        if(isSingleApp && isSingleApp=="dl"){
                            window.location.href = path + "/app/my/rechage/page?nonce=" + nonce
                                + "&coverKey=" + coverKey + "&requestId="  + requestId + "&userId="  + userId
                                + "&cnid="  + cnid + "&version="  + version + "&model="  + model + "&IMEI="  + IMEI
                                + "&platform=" + platform + "&way=0&target=blank"+"&app="+isSingleApp;
                        }else{
                            window.location.href = path + "/app/my/rechage/page?nonce=" + nonce
                                + "&coverKey=" + coverKey + "&requestId="  + requestId + "&userId="  + userId
                                + "&cnid="  + cnid + "&version="  + version + "&model="  + model + "&IMEI="  + IMEI
                                + "&platform=" + platform + "&way=0&target=blank";
                        }
                    }else {//ios由客户端跳转
                        var toChargePage = new Object();
                        var obj = new Object();
                        toChargePage.fun = "toChargePage";
                        toChargePage.data = obj;
                        window.webkit.messageHandlers.jsClient.postMessage(JSON.stringify(toChargePage));
                    }
                }
            }

        },
        error : function (k,i) {
            console.log("支付失败");
        }
    })
}
//充值完成后是否显示上传头像框，用户之前没有上传过头像则弹出弹窗
function isToUploadImg() {
    var params = getAllParam();
    var oUrl = path + "/external/user/headimg/isavailable.json?" + params;
    $.ajax({
        url : oUrl,
        type : "post",
        dataType : "json",
        success : function (result) {
            if (result == null){
                return;
            }
            if (result.code == 1){//用户没有上传过头像
                /*alert(result.code);*/
                $(".noblePayContainer").show();
                $(".uploadHeadImg").show();
            }
        },
        error : function (k,i) {
            console.log("请求失败")
        }
    })
}
//充值完成后，如果用户没有上传过头像，需上传头像
function uploadHeadImg(){
    var mobileType = getMobileType();
    var obj = new Object();
    var upImg = new Object();
    upImg.fun = "upImg";
    upImg.data = obj;
    if (mobileType == "android"){
        window.stub.jsClient(JSON.stringify(upImg));
    }else if(mobileType == "iphone"){
        window.webkit.messageHandlers.jsClient.postMessage(JSON.stringify(upImg));
    }
}
//查询支付结果
function getPayResult(orderNo,type,result,goodsId){//参数：订单号、支付类型、支付结果,goodsId
    if(type == 0){//微信支付由前端查询支付结果
        /*timer = setInterval(function () {
            sendAjax(orderNo);
        },2000)*/
        var params = getAllParam();
        var oUrl = path + "/app/user/wxquery.json?" + params;
        $.ajax({
            url : oUrl,
            type :"post",
            data : {
                "fog" : orderNo
            },
            dataType : "json",
            success : function (result) {
                /*alert(JSON.stringify(result));*/
                if (result == null || result == undefined){
                    $(".errorToast").show();
                    $(".errorToast").html("支付失败");
                    setTimeout(function () {
                        $(".errorToast").html("");
                        $(".errorToast").hide();
                    },1500)
                    return;
                }
                if (result.data!=null && result.data!=undefined){
                    if (result.data == 1){//支付成功
                        $(".nobleToast").show();
                        if(goodsId ==paladin){
                            $(".nobleToast i").html("恭喜您已成功开通圣骑士");
                        }else if (goodsId==dragon){
                            $(".nobleToast i").html("恭喜您已成功开通龙骑士");
                        }else if (goodsId==black){
                            $(".nobleToast i").html("恭喜您已成功开通黑骑士");
                        }else if (goodsId==magic){
                            $(".nobleToast i").html("恭喜您已成功开通魔法骑士");
                        }else if (goodsId==bauhinia){
                            $(".nobleToast i").html("恭喜您已成功开通紫荆骑士");
                        }else if (goodsId==temple ){
                            $(".nobleToast i").html("恭喜您已成功开通神殿骑士");
                        }
                        setTimeout(function () {
                            $(".nobleToast i").html("");
                            $(".nobleToast").hide();
                        },1500)
                        hidenoblePay();
                        showNobleEndTime(goodsId)
                        /*isToUploadImg();*/
                    }else if(result.data == 0) {
                        $(".errorToast").show();
                        $(".errorToast").html("支付失败");
                        setTimeout(function () {
                            $(".errorToast").html("");
                            $(".errorToast").hide();
                        },1500)
                    }else if (result.data==2){
                        $(".errorToast").show();
                        $(".errorToast").html("支付失败");
                        setTimeout(function () {
                            $(".errorToast").html("");
                            $(".errorToast").hide();
                        },1500)
                    }
                }
            },
            error : function (k,j) {
                alert("请求失败")
            }
        })
    }else if (type == 1){//支付宝支付客户端直接返回支付结果
        if (result==0){//支付失败
            $(".errorToast").show();
            $(".errorToast").html("充值失败");
            setTimeout(function () {
                $(".errorToast").html("");
                $(".errorToast").hide();
            },1500)
        }else if (result == 1){//支付成功
            $(".nobleToast").show();
            if(goodsId ==paladin){
                $(".nobleToast i").html("恭喜您已成功开通圣骑士");
            }else if (goodsId==dragon){
                $(".nobleToast i").html("恭喜您已成功开通龙骑士");
            }else if (goodsId==black){
                $(".nobleToast i").html("恭喜您已成功开通黑骑士");
            }else if (goodsId==magic){
                $(".nobleToast i").html("恭喜您已成功开通魔法骑士");
            }else if (goodsId==bauhinia){
                $(".nobleToast i").html("恭喜您已成功开通紫荆骑士");
            }else if (goodsId==temple ){
                $(".nobleToast i").html("恭喜您已成功开通神殿骑士");
            }
            setTimeout(function () {
                $(".nobleToast i").html("");
                $(".nobleToast").hide();
            },1500)
            showNobleEndTime(goodsId)
            hidenoblePay();
            /*isToUploadImg();*/
        }
    }
}
//反复查询
function sendAjax(orderNo){
    var Ourl = path + "/external/my/nobility/checkSuccessByNo.json?&orderNo=" + orderNo;
    $.ajax({
        url : Ourl,
        type : "post",
        dataType : "json",
        success : function (result) {
            if(result.code == 0){
                alert("充值成功");
                clearInterval(timer);
                hidenoblePay();
                /*isToUploadImg();*/
            }else if(result.code == 1){
                alert("充值失败")
            }
        },
        error : function (k,i) {
            console.log("查询失败")
        }
    })
}
//头像上传成功后，隐藏头像上传的弹窗
function hideImgbox() {
    hidenoblePay();
}
//充值成功后显示贵到期时间
function showNobleEndTime(goodsId){
    var params = getAllParam();
    var oUrl = path + "/external/my/noble.json?" + params;
    $.ajax({
        url : oUrl,
        type : "post",
        data : {
            "goodsId" : goodsId
        },
        dataType : "json",
        success : function (result) {
            /*alert(JSON.stringify(result));*/
            if (result == null || result==undefined){
                return
            }
            var data = result.data
            if (data.goodsId && data.goodsId == paladin){
                issqsEnd = 1;
                sqsEndTime = data.effectiveEndTime;
                sqsDy = data.balance/100;
                sqsDyEndTime = data.couponEndTime;
                $(".nobleTl").show();
                $(".msqq").hide();
                $(".nobleqs em").html("圣")
                $(".nobleqs i").html(sqsEndTime)
                $(".nobleDyq em").html(sqsDy)
                $(".nobleDyq i").html(sqsDyEndTime)
            }else if (data.goodsId && data.goodsId == dragon){
                islqsEnd = 1;
                lqsEndTime = data.effectiveEndTime;
                lqsDy = data.balance/100;
                lqsDyEndTime = data.couponEndTime;
                $(".nobleTl").show();
                $(".msqq").hide();
                $(".nobleqs em").html("龙")
                $(".nobleqs i").html(lqsEndTime)
                $(".nobleDyq em").html(lqsDy)
                $(".nobleDyq i").html(lqsDyEndTime)
            }else if (data.goodsId && data.goodsId == black){
                ishqsEnd = 1;
                hqsEndTime = data.effectiveEndTime;
                hqsDy = data.balance/100;
                hqsDyEndTime = data.couponEndTime;
                $(".nobleTl").show();
                $(".msqq").hide();
                $(".nobleqs em").html("黑")
                $(".nobleqs i").html(hqsEndTime)
                $(".nobleDyq em").html(hqsDy)
                $(".nobleDyq i").html(hqsDyEndTime)
            }else if (data.goodsId && data.goodsId == magic){
                ismfqsEnd = 1;
                mfqsEndTime = data.effectiveEndTime;
                mfqsDy = data.balance/100;
                mfqsDyEndTime = data.couponEndTime;
                $(".nobleTl").show();
                $(".msqq").hide();
                $(".nobleqs em").html("魔法")
                $(".nobleqs i").html(mfqsEndTime)
                $(".nobleDyq em").html(mfqsDy)
                $(".nobleDyq i").html(mfqsDyEndTime)
            }else if (data.goodsId && data.goodsId == bauhinia){
                iszjqsEnd = 1;
                zjqsEndTime = data.effectiveEndTime;
                zjqsDy = data.balance/100;
                zjqsDyEndTime = data.couponEndTime;
                $(".nobleTl").show();
                $(".msqq").show();
                $(".nobleqs em").html("紫荆")
                $(".nobleqs i").html(zjqsEndTime)
                $(".nobleDyq em").html(zjqsDy)
                $(".nobleDyq i").html(zjqsDyEndTime)
            }else if (data.goodsId && data.goodsId == temple){
                issdqsEnd = 1;
                sdqsEndTime = data.effectiveEndTime;
                sdqsDy = data.balance/100;
                sdqsDyEndTime = data.couponEndTime;
                $(".nobleTl").show();
                $(".msqq").show();
                $(".nobleqs em").html("神殿")
                $(".nobleqs i").html(sdqsEndTime)
                $(".nobleDyq em").html(sdqsDy)
                $(".nobleDyq i").html(sdqsDyEndTime)
            }
        },
        error : function (k,i) {
            console.log(k);
        }
    })
}
//埋点
function maidian(num) {
    var maidian = new Object();
    maidian.fun = "nobleCenter";
    maidian.data = {
        "nobleIndex" : nobleIndex,
        "type" : num
    }
    window.stub.jsClient(JSON.stringify(maidian));
}












