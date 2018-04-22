(function() {
    var pageNo = 1;
    var totalCnt = 0;
    var pageSize = 20;
    var d = 0;
    var extra = {};
    setTimeout(g, 500);

    window.onscroll = function() {
        var j = Math.max(document.documentElement.scrollTop, document.body.scrollTop);
        if (j + document.documentElement.clientHeight >= document.documentElement.scrollHeight && !d) {
        	var auto = $("#autopbn");
        	auto.show();
            var l ="<p class=\"loading\"><span class=\"relative\"><b></b>正在加载更多视频</span></p>";
            auto.html(l);
            d = 1;
            setTimeout(g, 2000);
        }
    };
    
    function g() {
    	var path = $("#web").val();
    	if (path == null || path == undefined) {
    		noInfoPage();
    		return;
    	}
    	var cnid = getUrlParam("cnid");
    	
        var i = path + "/external/tab/video/lived.json?" + "t=" + parseInt(( + new Date() / 1000) / (Math.random() * 1000)) + "&cnid=" + cnid;
        // console.log("pageNo: " + pageNo);
        $.ajax({
            type: "POST",
            url: i,
            data:{"pageSize" : pageSize, "pageNo" : pageNo, "IMEI" : "0", "userId" : "0", "extra" : extra},
            timeout: 9000,
    		dataType : 'json',
            success: function(data) {
            	var videoList = data.data.videoList;
            	if (videoList == null 
            			|| videoList == undefined 
            			|| videoList.size <= 0) {
                	removeAutoPaging();
                	if (pageNo <= 1) {
                		noInfoPage();
                	}
      	            return;
            	}
                innerMoreHtml(videoList, "#playback");
                if (videoList.length < pageSize || ((pageNo * pageSize) == totalCnt)) {
                	removeAutoPaging();
      	            return;
      	        }
                pageNo++;
                extra = data.data.page.extra;
                d = 0; 
                Lazy.Init();
            },
            error: function(k, j) {
                //alert("网络超时，点击更多重试");
            	console.log("异常：" + k + " " + j);
            },
        });
    }

    function removeAutoPaging() {
        var auto = $("#autopbn");
    	auto.remove();
        window.onscroll = null;
    }
    
    function noInfoPage() {
    	var path = $("#web").val();
    	if (path == null || path == undefined) {
    		path = "";
    	}
    	html = "<div class='no_live'><div class='no_live no_playback'>" +
		"<img src='" + path + "/static/images/no_relive.png' alt=''/>" +
				"<p>暂无精彩回放</p></div></div>";
    	$("#playback").append(html);
    }
    
    function innerMoreHtml(b, c) {
    	var html = formatObject(b);
        $(c).append(html);
        /*rem布局注释部分*/
        //将PC端的直播封面拉伸, 和移动端保持一致
        var imgW = $(".cover img").width();
        $(".anchor li").height(imgW);
        $(".cover").height(imgW);
        $(".cover img").height(imgW);

        //直播首页第1，2个.cover无间隙
        $(".anchor li .cover").eq(0).css("margin-top","0");
        $(".anchor li .cover").eq(1).css("margin-top","0");
        //直播首页最后两个cover无间隙,判断节点的奇偶
        /*if($(".anchor li").length % 2 == 0){
            $(".anchor li:last .cover").css("margin-bottom","0");
            $(".anchor li:last").prev().find(".cover").css("margin-bottom","0");
        }else{
            $(".anchor li:last .cover").css("margin-bottom","0");
        }*/
    }

    function formatObject(b) {
        /*alert(1);*/
    	var html = "";
    	var path = $("#web").val();
    	if (path != null && path != undefined) {
    		path += "";
    	}
		html = "";
		for (i = 0; i < b.length; i++) {
			var videoNAME = b[i].videoName;
			videoNAME = getTrimedStr(videoNAME, 20);
			/*if (videoNAME.length > 8) {
				videoNAME = (videoNAME.substr(0, 8) + "...");
			}*/
			var coverImg = b[i].coverImg;
			html += ("<li onclick='videoInfo(\"" +  b[i].videoId + "\", \"" + b[i].anchorId + "\", \"" + b[i].formatType 
					+ "\", \"" + b[i].coverImg + "\",\"" + b[i].version + "\",\"" + b[i].ext + "\")'><div class='cover' data-id='" +b[i].anchorId+ "'>" + "<img src='" + coverImg + "' alt='' class='an_redio' />"
					+ "<div class='top_num'><span>" + b[i].viewers + "</span></div>"
					+ "<span class='live-title'><em>" + videoNAME + "</em></span></div></li>");
    	}
    	return html;
		console.log(html);
    }
})();