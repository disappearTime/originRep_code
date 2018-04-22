/**
 * Created by zhuweiwei on 2017/9/12 0012.
 */
(function() {
    var pageNo = 1;
    var totalCnt = 0;
    var pageSize = 10;
    var d = 0;
    var params = getAllParam();
    setTimeout(g, 500);

    window.onscroll = function() {
        var j = Math.max(document.documentElement.scrollTop, document.body.scrollTop);
        if (j + document.documentElement.clientHeight >= document.documentElement.scrollHeight && !d) {
            var auto = $("#autopbn");
            auto.show();
            auto.html("<p class=\"loading\"><span class=\"relative\"><b></b>正在加载更多记录</span></p>");
            d = 1;
            setTimeout(g, 2000);
        }
    };

    function g() {
        var path = $("#web").val();
        if (path == null || path == undefined) {
            noIncomePage();
            return;
        }
        if (params == null || params == undefined) {
            noIncomePage();
            return;
        }
        var i = path + "/launch/anchor/details.json?" + "dense_fog=" + parseInt(( + new Date() / 1000) / (Math.random() * 1000)) + "&" + params;
        $.ajax({
            type: "post",
            url: i,
            data:{"pageSize" : pageSize, "pageNo" : pageNo},
            timeout: 9000,
            dataType : 'json',
            success: function(data) {
                console.log("会话成功");
                console.log(data);
                if (data == null || data.data == null
                    || data.data.incomeList == null || data.data.incomeList.length <= 0) {
                    removeAutoPaging();
                    if (pageNo <= 1) {
                        noIncomePage();
                    }
                    return;
                }
                var incomeList = data.data.incomeList;
                console.log(incomeList);
                innerMoreHtml(incomeList, ".giftDetail");
                if (incomeList.length < pageSize) {
                    removeAutoPaging();
                    return;
                }
                pageNo++;
                d = 0;
                /*Lazy.Init();*/
            },
            error: function(k, j) {
                //alert("网络超时，点击更多重试");
                console.log(k + "失败" + j);
            },
        });
    }

    function removeAutoPaging() {
        var auto = $("#autopbn");
        auto.remove();
        window.onscroll = null;
    }

    function innerMoreHtml(b, c) {
        var html = formatObject(b);
        $(c).append(html);
    }

    function formatObject(b) {
        var html = "";
        var path = $("#web").val();
        if (path != null && path != undefined) {
            path += "";
        } else {
            path = "";
        }
        for (var i=0;i<b.length;i++){
            var level = 0,
                goodsCnt = 0,
                userTx = "",
                nobleImg = "",
                fg = "",
                sexImg = "",
                count = 0,
                orderType = "";
            if (b[i].headImg){
                userTx = "<img onerror='this.src =\""+ path +"/static/images/user_cover.png\" ' class='userTx' src='"+ b[i].headImg +"'/>"
            }else {
                userTx = "<img class='userTx' src='"+ path +"/static/images/user_cover.png'/>"
            }
            if (b[i].level && b[i].level!=0){
                if (b[i].level==1){//最低等级贵族
                    nobleImg = "<img class='nobleImg' src='"+ path +"/static/images/sqx.png'/>"
                }else if(b[i].level==2){
                    nobleImg = "<img class='nobleImg' src='"+ path +"/static/images/lqs.png'/>"
                }else if(b[i].level==3){
                    nobleImg = "<img class='nobleImg' src='"+ path +"/static/images/hqs.png'/>"
                }else if(b[i].level==4){
                    nobleImg = "<img class='nobleImg' src='"+ path +"/static/images/mfqs.png'/>"
                }else if(b[i].level==5){
                    nobleImg = "<img class='nobleImg' src='"+ path +"/static/images/zjqs.png'/>"
                }else if(b[i].level==6){//最高等级贵族
                    nobleImg = "<img class='nobleImg' src='"+ path +"/static/images/sdqs.png'/>"
                }
            }
            if (b[i].isAdmin){
                if (b[i].isAdmin ==1){
                    fg = "<img src='"+ path +"/static/images/zb_fg.png' alt='' />"
                }
            }
            if (b[i].sex){
                if (b[i].sex == 0){
                    sexImg = "<img src='"+ path +"/static/images/women.png' alt='' />"
                }else if (b[i].sex == 1){
                    sexImg = "<img src='"+ path +"/static/images/mans.png' alt='' />"
                }

            }
            if (b[i].orderType == 0){
                orderType = "普通礼物"
            }else if(b[i].orderType == 1){
                orderType = "贵族礼物"
            }else if(b[i].orderType == 4){
                orderType = "弹幕"
            }else if (b[i].orderType == 5){
                orderType = "骑士"
            }
            if (b[i].goodsCnt){
                count = b[i].goodsCnt;
            }
            html += ("<li data-id='"+ b[i].loginId +"'><div class='left'>" + userTx
                 + nobleImg + "</div><div class='right'>"
                 +  "<p class='userMes'>"+ b[i].userName + fg + sexImg
                 +  "<em>"+ b[i].createTime +"</em></p>"
                 +  "<p class='giftMes'>送了主播<span>"+ b[i].goodsName +"</span>(<em>"+ orderType +"</em>)*<i>"+ b[i].goodsCnt +"</i></p></div></li>")
        }
        return html;
    }

    function noIncomePage() {
        var path = $("#web").val();
        if (path == null || path == undefined) {
            path = "";
        }
        var html = "<div class='live-nothing'><img class='no-sr' src='" + path + "/static/images/no-income.png' alt=''><p>暂无礼品详细</p></div>";
        $("body").append(html);
    }
    //点击弹窗
    $(".giftDetail").delegate("li","click",function () {
        $(".userMesCard-gift").show();
        $("body").addClass("overHidden");
        var loginId = $(this).attr("data-id");
        console.log(loginId);
        sendAjax(loginId);
    })
    //关闭弹窗
    $(".mask-layer").click(function () {
        $(".noble-box").html("")
        $(".bg-box").html("")
        $(".card").css({
            "background" : "#ffffff"
        })
        $(".userMesCard-gift").hide();
        $("body").removeClass("overHidden")
    })
    $(".close").click(function () {
        $(".noble-box").html("")
        $(".bg-box").html("")
        $(".card").css({
            "background" : "#ffffff"
        })
        $(".userMesCard-gift").hide();
        $("body").removeClass("overHidden")
    })
    function sendAjax(loginId) {
        var path = $("#web").val();
        var oUrl = path + "/launch/user/getinfo?" + params;
        console.log(oUrl);
        $.ajax({
            url : oUrl,
            type :"post",
            dataType : "json",
            data : {
                viewId : loginId
            },
            success : function (result) {
                console.log(result);
                if (result==null || result==undefined){
                    result;
                }
                if (result.data == null || result.data==undefined || result.data.userInfo == null || result.data.userInfo == undefined){
                    return
                }
                var data = result.data.userInfo;
                console.log(data);
                var html = "";
                var noblesImgStr = "";
                var zodiac = "双鱼座";
                if (data.zodiac){
                    zodiac = data.zodiac;
                }
                if (data.isRichest == 1){
                    html+="<img style='margin-right: 5px;' src='"+ path +"/static/images/localRich.png' alt='' />"
                }
                if (data.userName){
                    html+=data.userName;
                }
                if (data.acctType==1){//超管
                    html+="<img src='"+ path +"/static/images/zb_cg.png' alt='' />"
                }else if (data.acctType==2){//房管
                    html+="<img src='"+ path +"/static/images/zb_fg.png' alt='' />"
                }
                if (data.sex ==0){
                    html+="<img src='"+ path +"/static/images/women.png' alt='' />"
                }else if (data.sex==1){
                    html+="<img src='"+ path +"/static/images/mans.png' alt='' />"
                }
                if (data.nobles!=null && data.nobles!=undefined && data.nobles.length>0){
                    var noblesarr = data.nobles;
                    for (var i =0;i<noblesarr.length;i++){
                        if (noblesarr[i]==1){
                            noblesImgStr += "<img class='nobleImg' src='"+ path +"/static/images/sqx.png'/>"
                        }else if(noblesarr[i]==2){
                            noblesImgStr += "<img class='nobleImg' src='"+ path +"/static/images/lqs.png'/>"
                        }else if(noblesarr[i]==3){
                            noblesImgStr += "<img class='nobleImg' src='"+ path +"/static/images/hqs.png'/>"
                        }else if(noblesarr[i]==4){
                            noblesImgStr += "<img class='nobleImg' src='"+ path +"/static/images/mfqs.png'/>"
                        }else if(noblesarr[i]==5){
                            noblesImgStr += "<img class='nobleImg' src='"+ path +"/static/images/zjqs.png'/>"
                        }else if(noblesarr[i]==6){
                            noblesImgStr += "<img class='nobleImg' src='"+ path +"/static/images/sdqs.png'/>"
                        }
                    }
                    //背景图
                    var qsbgImgStr = "";
                    if(noblesarr[0]==6){//神殿骑士
                        $(".card").css({
                            "background" : "url("+ path +"/static/images/nobleCenter/sdqsbg.png) no-repeat center center",
                            "background-size" : "cover"
                        })
                        /*$(".tx-box").append("<img class='wg' src='"+ path+"/static/images/nobleCenter/wg.png' />")
                        $(".tx-box").append("<img class='hb' src='"+ path+"/static/images/nobleCenter/wg.png' />")*/
                        qsbgImgStr = "<img class='wg' src='"+ path+"/static/images/nobleCenter/wg.png' />"
                                   + "<img class='sdhb' src='"+ path +"/static/images/nobleCenter/sdhb.png' />"
                    }else if (noblesarr[0]==5){
                        $(".card").css({
                            "background" : "url("+ path +"/static/images/nobleCenter/zjqsbg.png) no-repeat center center",
                            "background-size" : "cover"
                        })
                        qsbgImgStr = "<img class='zjhb hb' src='"+ path +"/static/images/nobleCenter/zjhb.png' />"
                    }else if (noblesarr[0]==4){
                        $(".card").css({
                            "background" : "url("+ path +"/static/images/nobleCenter/mfqsbg.png) no-repeat center center",
                            "background-size" : "cover"
                        })
                        qsbgImgStr = "<img class='mfhb hb' src='"+ path +"/static/images/nobleCenter/mfhb.png' />"
                    }else if (noblesarr[0]==3){
                        $(".card").css({
                            "background" : "url("+ path +"/static/images/nobleCenter/hqsbg.png) no-repeat center center",
                            "background-size" : "cover"
                        })
                        qsbgImgStr = "<img class='hqhb hb' src='"+ path +"/static/images/nobleCenter/hqhb.png' />"
                    }else if (noblesarr[0]==2){
                        $(".card").css({
                            "background" : "url("+ path +"/static/images/nobleCenter/lqsbg.png) no-repeat center center",
                            "background-size" : "cover"
                        })
                        qsbgImgStr = "<img class='lqhb hb' src='"+ path +"/static/images/nobleCenter/lqhb.png' />"
                    }else if (noblesarr[0]==1){
                        $(".card").css({
                            "background" : "url("+ path +"/static/images/nobleCenter/sqsbg.png) no-repeat center center",
                            "background-size" : "cover"
                        })
                        /*qsbgImgStr = "<img class='zjhb' src='"+ path +"/static/images/nobleCenter/zjhb.png' />"*/
                    }
                }
                //贵族图标
                $(".noble-box").html(noblesImgStr);

                $(".nickname").html(html);
                $(".tx").attr("src",data.headImg);
                //贵族花边 皇冠等
                $(".bg-box").html(qsbgImgStr);

                $(".personal-mes em").html(data.age);
                $(".personal-mes i").html(zodiac);
                $(".gz p").html(data.followNum)
                $(".gxz p").html(data.contrib)
                $(".ph p").html(data.rank)
                $(".gag").attr("data-id",data.userId);
            },
            error : function (k,i) {
                console.log(k +"....."+ i);
            }
        })
    }
    //禁言
    /*$(".gag").click(function () {
        var params = getAllParam();
        var path = $("#web").val();
        var userId = $(this).attr("data-id");
        console.log(userId);
        var oUrl = path + "/launch/user/mute?"+params;
        $.ajax({
            type : "post",
            url : oUrl,
            dataType : "json",
            data : {
                userId : userId,
                duration : 10
            },
            success : function (result) {
                console.log(result);
                if (result.data!=null &&result.data!=undefined){
                    if (result.data.result==1){
                        alert("禁言成功")
                    }else if(result.data.result==0){
                        alert("禁言失败");
                    }
                }
            }
        })
    })*/
    //回到个人主页
    $(".grzy").click(function () {
        var obj = new Object();
        var selfPage = new Object();
        selfPage.fun = "backToSelfPage";
        selfPage.data = obj;
        window.webkit.messageHandlers.jsClient.postMessage(JSON.stringify(selfPage));
    })
})();










