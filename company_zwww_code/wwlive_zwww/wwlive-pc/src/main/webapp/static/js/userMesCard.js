/**
 * Created by Administrator on 2017/9/9 0009.
 */
function userInfo(userId) {
    if (userId == null || userId == undefined) {
        return;
    }
    $.ajax({
        type: "POST",
        url: "/pc/user/detail.json",
        data: {"userId":userId},
        dataType: "json",
        success: function (result) {
            if (result != null) {
                var userInfo = result.data;
                $("#user_img").attr("src", userInfo.headImg);//user_img
                var userCg = "";
                if(userInfo.acctType == 1) {//chao guan
                    userCg = "<b class='b-cg'>超管</b>";
                } else if (userInfo.isAdmin == 1) {//fang guan
                    userCg = "<b class='b-fg'>房管</b>";
                }

//	                        if(userInfo.acctType == 0 && userInfo.isAdmin == 0){
                var nobles = userInfo.nobles;
                var nobleImgs = "";
                if(nobles.length>0){
                    for(var i =0;i< nobles.length; i++) {
                        if(nobles[i]==1) { nobleImgs += "<img src='/static/images/nobleImages/sheng-right.png'>" }
                        if(nobles[i]==2) { nobleImgs += "<img src='/static/images/nobleImages/long-right.png'>" }
                        if(nobles[i]==3) { nobleImgs += "<img src='/static/images/nobleImages/hei-right.png'>" }
                        if(nobles[i]==4) { nobleImgs += "<img src='/static/images/nobleImages/mofa-right.png'>" }
                        if(nobles[i]==5) { nobleImgs += "<img src='/static/images/nobleImages/zi-right.png'>" }
                        if(nobles[i]==6) { nobleImgs += "<img src='/static/images/nobleImages/shendian-right.png'>" }
                    }
                    $("#noble").html(nobleImgs);
                }

                var sex = "";
                if (userInfo.sex == '0') {
                    sex = "<img class='sexType'  src='/static/images/woman.png' alt='' />";
                } else if(userInfo.sex == '1'){
                    sex = "<img class='sexType'  src='/static/images/man.png' alt='' />";
                }
                //活动结束是否有土豪徽章
                var localRich = "";
                if(userInfo.medals!="" && userInfo.medals!=null && userInfo.medals!=undefined && userInfo.medals.length>0 ){
                    for(var j=0;j<userInfo.medals.length;j++){
                        if(userInfo.medals[j]=="土豪勋章"){
                            localRich = "<img class='localRich' src='${rc.contextPath}/static/images/localRich.png' alt=''>"
                        }
                    }
                }
                // 是否送过礼标识
                var payIcon = "";
                if(userInfo.followNum > 0){
                    payIcon = "<img class='pay-icon' src='/static/images/pay.png' alt=''>";
                }
                $("#user_name_sex").html(localRich+payIcon + userInfo.userName + userCg + sex);

                //$("#user_name_sex").html(userInfo.userName + "<img  src='/static/images/" + sex + ".png' alt='' />");
                var age = "";
                if (userInfo.zodiac != undefined && userInfo.zodiac != "") {
                    age = "年龄：" + userInfo.age + "岁        " + userInfo.zodiac;
                } else {
                    age = "年龄：" + userInfo.age + "岁    双鱼座";
                }
                $("#user_age").html(age);
                if (userInfo.followNum != null) {
                    $("#follower").html("<div>" + userInfo.followNum+"</div>" + "<span>关注数</span>");
                }
                if (userInfo.contrib != null) {
                    $("#contri").html("<div>" + userInfo.contrib+"</div>" + "<span>贡献值</span>");
                }
                console.log(userInfo.contrib != null && userInfo.contrib != "");
                if (userInfo.rank != null && userInfo.rank != "") {
                    $("#rankall").html("<div>" + userInfo.rank+"</div>" + "<span>全站排名</span>");
                }
                $(".anchor_data").fadeIn();
                $(".shadowUp").fadeIn();
            }
        },
        error: function () {
            $(".sure_div p").text("获得用户信息异常请稍后。");
            $(".sure_div , .shadowUp").show();
        }
    });
}
function closeData() {
    $(".anchor_data").fadeOut();
    $(".shadowUp").fadeOut();
}
