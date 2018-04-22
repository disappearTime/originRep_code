/**
 * Created by Administrator on 2017/5/17 0017.
 */
(function() {
    var pageNo = 1;
    var totalCnt = 0;
    var pageSize = 10;
    var d = 0;
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
        var listPage = $("#listPage").val();
        if (listPage == null || listPage == undefined) {
            noIncomePage();
            return;
        }
        var i = path + "/launch/anchor/income/list.json?" + "dense_fog=" + parseInt(( + new Date() / 1000) / (Math.random() * 1000)) + "&" + listPage;
        $.ajax({
            type: "POST",
            url: i,
            data:{"pageSize" : pageSize, "pageNo" : pageNo},
            timeout: 9000,
            dataType : 'json',
            success: function(data) {
                if (data == null || data.data == null
                    || data.data.incomeList == null || data.data.incomeList.length <= 0) {
                    removeAutoPaging();
                    if (pageNo <= 1) {
                        noIncomePage();
                    }
                    return;
                }
                var incomeList = data.data.incomeList;
                innerMoreHtml(incomeList, "#detail-list");
                if (incomeList.length < pageSize) {
                    removeAutoPaging();
                    return;
                }
                pageNo++;
                d = 0;
                Lazy.Init();
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
        html = "<div class='detail-list bor-t'><ul>";
        for (i = 0; i < b.length; i++) {
            html += ("<li>");
            var videoName = b[i].videoName;
            if (videoName.length > 8) {
                videoName = videoName.substring(0, 8) + "...";
            }
            if (b[i].videoStatus == 4) {
                html += ("<p><span onclick='videoInfo4IOS(\"" + b[i].videoId + "\");'><b>"
                + videoName + "</b><img src='" + path + "/static/images/rplay.png' alt='' /></span><em>" + b[i].createTime + "</em></p>");
            } else {
                html += ("<p>" + videoName + "<em>" + b[i].createTime + "</em></p>");
            }
            html += ("<div class='details'>");
            html += ("<div class='detail-con'>");
            html += ("<img src='" + path + "/static/images/income-num.png'/><span>" + (b[i].income / 100)+ "</span>");
            html += ("<img src='" + path + "/static/images/gift-num.png' alt='' class='gift-num'/><span>" + b[i].goodsNum + "</span>");
            html += ("</div>");
            if (b[i].goodsNum > 0) {
                html += ("<a onclick='videoGift(\"" + b[i].videoId + "\");'>礼品详情</a>");
            }
            html += ("</div></li>");
        }
        html += "</ul></div>";
        return html;
    }

    function noIncomePage() {
        var path = $("#web").val();
        if (path == null || path == undefined) {
            path = "";
        }
        var html = "<div class='live-nothing'><img class='no-sr' src='" + path + "/static/images/no-income.png' alt=''><p>暂无收入详细</p></div>";
        $("#detail-list").append(html);
    }

})();