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
            var l ="<p class=\"loading\"><span class=\"relative\"><b></b>正在加载更多记录</span></p>";
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

		var listPage = $("#listPage").val();
    	if (listPage == null || listPage == undefined) {
    		noInfoPage();
    		return;
    	}
    	
        var i = path + "/launch/video/goods.json?" + "dense_fog=" + parseInt(( + new Date() / 1000) / (Math.random() * 1000)) + "&" + listPage;
        // console.log("pageNo: " + pageNo);
        $.ajax({
            type: "POST",
            url: i,
            data:{"pageSize" : pageSize, "pageNo" : pageNo},
            timeout: 9000,
    		dataType : 'json',
            success: function(data) {
            	
            	if (data.data == null || data.data.goodsList == null 
            			|| data.data.goodsList == undefined) {
                	removeAutoPaging();
					if (pageNo <= 1) {
	      	            noInfoPage();
					}
      	            return;
            	}
            	var goodsList = data.data.goodsList;
                innerMoreHtml(goodsList, "#gift-list");
                if (goodsList.length < pageSize) {
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
    	}
		html = "<div class='gift-list bor-t'><ul>";
		for (i = 0; i < b.length; i++) {
			html += ("<li>");
			html += ("<img onclick='getUserInfo(\"" + b[i].loginId + "\");' src='" + b[i].headImg + "' alt='' class='gift-pic' onerror='this.src=\"" + path + "/static/images/user_cover.png\"'/><div class='gift-con'>");
			var acctType = "";
			if (b[i].acctType == 1) {
				acctType = "<img src='" + path + "/static/images/zb_cg.png' alt=''>";
			} else if (b[i].acctType == 2) {
				acctType = "<img src='" + path + "/static/images/zb_fg.png' alt=''>";
			}
			html += ("<h3><em onclick='getUserInfo(\"" + b[i].loginId + "\");'>" + b[i].userName + acctType + "</em>");
			if (b[i].sex == 0) {
				html += ("<img src='" + path + "/static/images/sex.png' alt='' /></h3>");
			} else if (b[i].sex == 1) {
				html += ("<img src='" + path + "/static/images/sex1.png' alt='' /></h3>");
			} else {
				html += (" </h3>");
			}
			html += ("<p><span>送了主播<em>" + b[i].goodsName + "</em></span><img src='" + b[i].goodsImg + "' alt='' /><b>x" + b[i].totalNum + "</b></p>");
			
			html += ("</div>");
			html += ("<span class='gift-time'>" + b[i].creatTime + "</span>");
			html += ("</li>");
			
		}
		html += "</ul></div>";
    	return html;
    }

    function noInfoPage() {
    	var path = $("#web").val();
    	if (path == null || path == undefined) {
    		path = "";
    	}
    	var html = "<div class='live-nothing'><img class='no-sr' src='" + path + "/static/images/no-gift.png' alt=''><p>暂无礼品详细</p></div>";
        $("#gift-list").append(html);
    }
})();