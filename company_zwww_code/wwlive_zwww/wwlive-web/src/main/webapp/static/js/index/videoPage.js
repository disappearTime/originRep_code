(function() {
    var pageNo = 1;
    var totalCnt = 0;
    var pageSize = 20;
    var d = 0;
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
    		return;
    	}
    	
    	var cnid = getUrlParam("cnid");
    	
        var i = path + "/external/video/lived?" + "t=" + parseInt(( + new Date() / 1000) / (Math.random() * 1000));
        // console.log("pageNo: " + pageNo);
        $.ajax({
            type: "POST",
            url: i,
            data:{"pageSize" : pageSize, "pageNo" : pageNo, "IMEI" : "0", "userId" : "0", "cnid":cnid},
            timeout: 9000,
    		dataType : 'json',
            success: function(data) {
            	var totalCnt = data.videoCnt;
            	if (totalCnt <= 0) {
                	removeAutoPaging();
                    var html = formatObject(null);
                    $("#playback").append(html);
      	            return;
            	}
            	var videoList = data.videoList;
                innerMoreHtml(videoList, "#playback");
                if (videoList.length < pageSize || ((pageNo * pageSize) == totalCnt)) {
                	removeAutoPaging();
      	            return;
      	        }
                pageNo++; 
                d = 0; 
                Lazy.Init();
            },
            error: function(k, j) {
                //alert("网络超时，点击更多重试");
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
        /*rem布局注释部分*/
        //将PC端的直播封面拉伸, 和移动端保持一致
        var imgW = $(".cover img").width();
        $(".anchor li").height(imgW);
        $(".cover").height(imgW);
        $(".cover img").height(imgW);
    }

    function getUrlParam(name) {
        var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)"); //构造一个含有目标参数的正则表达式对象
        var r = window.location.search.substr(1).match(reg);  //匹配目标参数
        //alert(r);
        if (r != null) return unescape(r[2]); return null; //返回参数值
    }
    
    function formatObject(b) {
    	var html = "";
    	var path = $("#web").val();
    	if (path != null && path != undefined) {
    		path += "";
    	}
    	if (b == null || b == undefined) {
    		html = "<div class='no_live'><div class='no_live no_playback'>" +
    				"<img src='" + path + "/static/images/no_relive.png' alt=''/>" +
    						"<p>暂无精彩回放</p></div></div>";
    	} else {
    		html = "";
    		for (i = 0; i < b.length; i++) {
    			var videoNAME = b[i].videoName;
    			videoNAME = getTrimedStr(videoNAME, 20);
    			/*if (videoNAME.length > 8) {
    				videoNAME = (videoNAME.substr(0, 8) + "...");
    			}*/
    			var coverImg = b[i].coverImg;
    			/*if (coverImg.indexOf("_400") != -1) {
        			coverImg = b[i].coverImg.replace("_400", "_200");
    			}*/
    			html += ("<li onclick='videoInfo(\"" +  b[i].videoId + "\", \"" + b[i].anchorId + "\", \"" + b[i].formatType 
    					+ "\", \"" + b[i].coverImg + "\",\"" + b[i].version + "\",\"" + b[i].ext + "\")'><div class='cover'>" + "<img src='" + coverImg + "' alt='' class='an_redio' />"
    					+ "<div class='top_num'><span>" + b[i].viewers + "</span></div>"
    					+ "<span class='live-title'><em>" + videoNAME + "</em></span></div></li>");
    		}	
    	}
    	return html;
    }
})();