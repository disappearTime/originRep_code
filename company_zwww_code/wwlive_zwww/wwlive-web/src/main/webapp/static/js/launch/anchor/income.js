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
    	console.log(listPage);
    	if (listPage == null || listPage == undefined) {
    		noIncomePage();
    		return;
    	}
        var i = path + "/launch/anchor/videoincome.json?" + "dense_fog=" + parseInt(( + new Date() / 1000) / (Math.random() * 1000)) + "&" + listPage;
        $.ajax({
            type: "post",
            url: i,
            data:{"pageSize" : pageSize, "pageNo" : pageNo},
            timeout: 9000,
    		dataType : 'json',
            success: function(data) {
            	console.log(data);
            	if (data == null || data.data == null
            			|| data.data.anchorIncome == null || data.data.anchorIncome.length <= 0) {
                	removeAutoPaging();
            		if (pageNo <= 1) {
                		noIncomePage();
                	}
      	            return;
            	}
            	var incomeList = data.data.anchorIncome;
            	console.log(incomeList);
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
		for (var i=0;i<b.length;i++){
			var goodsIncome = 0,
			     nobleIncome = 0,
			     barrageIncome = 0,
			     levelIncome = 0,
				 backpackIncome = 0,
				 count = 0,
				 videoId = "";
			if (b[i].goodsIncome){
				goodsIncome = b[i].goodsIncome/100;
			}
			if (b[i].nobleIncome){
                nobleIncome = b[i].nobleIncome/100
			}
			if (b[i].barrageIncome){
                barrageIncome = b[i].barrageIncome/100;
			}
			if (b[i].levelIncome){
                levelIncome = b[i].levelIncome/100;
			}
			if (b[i].backpackIncome){
				backpackIncome = b[i].backpackIncome/100;
			}
			if (b[i].count){
				count = b[i].count/100
			}
			if (b[i].videoId){
				videoId = b[i].videoId;
			}
			html += ("<div class='income-box'><h3>"+ b[i].videoName +"<span>"+ b[i].createTime +"</span></h3><div class='gift-box'>"
                 + "<p>普通礼品收入<span>￥"+ goodsIncome +"</span></p><p>背包礼品收入<span>￥"+ backpackIncome +"</span></p><p>贵族礼品收入<span>￥"+ nobleIncome +"</span></p><ul>"
                 + "<li>弹幕收入<span>￥"+ barrageIncome +"</span></li><li>贵族收入<span>￥"+ levelIncome +"</span></li></ul>"
                 + "<div data-booleans='true' class='control-btn'><span>收起</span><img src='"+ path +"/static/images/updowntoggle.png' alt=''></div>"
                 + "</div><div class='totalIncome bor-bott'><span>直播总收入："+ count +"</span></div>"
                 + "<div data-vid = '"+ videoId + "' class='giftDetail'>礼品详细<img src='"+ path +"/static/images/back1.png' alt=''></div>"
                 + "<div class='income-cut'></div></div>")
		}
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