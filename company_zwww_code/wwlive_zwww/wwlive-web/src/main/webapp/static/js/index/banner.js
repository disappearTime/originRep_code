/**
 * 广告
 */
/*var container = document.getElementById("home_banner");//广告位容器
var dataList = [];//存取广告的数组
var posCount = 0;
var tmpl = (function(){
	var cache = {};

	function tmpl(str, data){//第一个参数是广告类型，普通链接广告还是app下载类广告
		// Figure out if we're getting a template, or if we need to
		// load the template - and be sure to cache the result.
		var fn = !/\W/.test(str) ?
			cache[str] = cache[str] ||
				tmpl(document.getElementById(str).innerHTML) :

			// Generate a reusable function that will serve as a template
			// generator (and which will be cached).
			new Function("obj",
				"var p=[],print=function(){p.push.apply(p,arguments);};" +

				// Introduce the data as local variables using with(){}
				"with(obj){p.push('" +

				// Convert the template into pure JavaScript
				str
					.replace(/[\r\t\n]/g, " ")
					.split("<%").join("\t")
					.replace(/((^|%>)[^\t]*)'/g, "$1\r")
					.replace(/\t=(.*?)%>/g, "',$1,'")
					.split("\t").join("');")
					.split("%>").join("p.push('")
					.split("\r").join("\\'")
				+ "');}var out=p.join('');return p.join('');");
		//console.log(fn(data));
		// Provide some basic currying to the user
		return data ? fn( data ) : fn;
	};
	return tmpl;
})();

//拉取广告的回调处理
function checkForRender(pid,obj,cnt) {
	var d = ( typeof obj == 'string')?JSON.parse(obj):obj;//如果是字符串类型则转成对象
	/!*alert(JSON.stringify(d));*!/
	if(d.ret && d.data.length > 0) {
		dataList.push(d.data);//往数组中添加广告
		renderAd(pid,d.data,cnt);     //渲染广告
	} else {
		setTimeout(function() {
			TencentGDT.NATIVE.loadAd && TencentGDT.NATIVE.loadAd(pid); //拉取广告失败或无广告数据返回时，重新拉取广告
		},2000);
	}
}
//广告渲染
function renderAd(pid,objList,cnt) {
	$(".index-scroll").show();//拉取到广告数不为0时,让index-scroll显示出来
	alert(JSON.stringify(objList));
	posCount ++;
	for(var i = 0; i < objList.length; i++){
		var divObj = document.createElement("div");
		divObj.setAttribute('id','native'+posCount +i);//这是展示广告的document元素
		divObj.setAttribute("class","swiper-slide swiper-no-swiping");
		if(objList[i].is_app){
			divObj.innerHTML = tmpl("tplApp", objList[i]);//调用模板,传入广告对象
		} else {
			divObj.innerHTML = tmpl("tplUrl", objList[i]);
			/!*divObj.innerHTML = '<img class="AD" src="' + objList[i].img_url +'" width="100px">'*!/
			/!*var html = '<div class="swiper-slide swiper-no-swiping"><img id="native'+ posCount+ i +'" class="AD" src="' + objList[i].img_url +'" width="100%"> </div>'*!/
		}
		cnt.appendChild(divObj);//cnt为容器
		/!*$("#home_banner").append(html);*!/
	}
    var toLoop = true;
    if (objList.length <= 1) {
        toLoop = false;
    }
    var mySwiper1 = new Swiper ('.index-scroll', {
        // 如果需要分页器
        pagination: '.index-dots',
        speed : 500, //设置动画持续时间500ms
        autoplay:2000,
        autoplayDisableOnInteraction:false,
        loop:toLoop,
        noSwiping:true
    });
	initClickEvent(pid,objList);//初始话点击事件
	if(objList.length == TencentGDT.length){       //所有广告渲染结束后进行初始化曝光判断
		initExpose(pid,dataList);
	}
}
function initClickEvent(pid,da) {
	alert("数据长度="+da.length);
    //该对象以开发者实际获取到的值为准，这里只展示测试数据
    var  sObj = {
        "down_x": "30",    //按下横坐标；
        "down_y": "45",   //按下纵坐标；
        "up_x": "35",     //弹起横坐标；
        "up_y": "36"     //弹起纵坐标；
    };

    for(var i = 0; i < da.length; i++){
        var cnt = "native"+posCount +i;
        alert("cnt=" + cnt);
        var obj = document.getElementById(cnt);
        alert(obj.getAttribute("id"));
        alert(obj.style.height);
        var a = function(v,cnt){
            return function(event){
                var e = event || window.event;
				var target = e.target;
            	if(target = obj){
                    alert("被点击");
                    var contentObj = {
                        cnt: cnt,                   //cnt为展示广告的document元素id
						/!*s: encodeURIComponent(JSON.stringify(sObj)), *!/       //s参数为点击上报时传递的关于点击的相关字段信息
                        s : sObj,
                        advertisement_id : da[v].advertisement_id,            //广告ID
                        placement_id : pid                 //广告位ID
                    }
					/!*alert(JSON.stringify(contentObj));*!/
                    setTimeout(function () {
                        TencentGDT.NATIVE.doClick && TencentGDT.NATIVE.doClick(contentObj);
                    },0)
                    e.stopPropagation();
					/!*TencentGDT.NATIVE.doClick && TencentGDT.NATIVE.doClick(contentObj); *!/        //调用点击上报接口
				}

            }
        }
        /!*obj.addEventListener("click",a(i,cnt),false);*!/
        $("#home_banner").delegate(obj,"click",a(i,cnt));
    }
}
//初始化曝光：这里只展示初始化曝光，真实场景下需要开发者实时判断广告是否满足有效曝光条件
function initExpose(pid,objList) {
    for (var i = 0; i < objList.length; i++){
        var posElm = objList[i];
        for (var j = 0;  j < posElm.length; j++){
            var a = i+1;
            var elm = document.getElementById("native"+a+j);
            if(document.documentElement.clientHeight - elm.offsetTop > 40) {    //有效曝光的条件：广告展示区域顶部位置及以下至少40像素的区域可见则满足曝光条件
                var obj = {
                    cnt : elm,          //cnt为展示广告的document元素id
                    advertisement_id : posElm[j].advertisement_id,     //广告ID
                    placement_id : pid            //广告位ID
                }
                TencentGDT.NATIVE.doExpose && TencentGDT.NATIVE.doExpose(obj);      //调用曝光上报接口
            }
        }
    }
}
//JS接入
var TencentGDT = TencentGDT || [];
TencentGDT.push({
    placement_id: '5090921338482606',
    app_id:'1105702641',
    type: 'native',//注意是原生广告
    count:3,//拉取广告的数量，必填，默认是3，最高支持10
	/!*muidtype: "1",//1- imei，2-ifa，3-mac,选填
	 muid: "45d290022a1e7a33907e038b9fe39415",//32bit md5加密，再取小写的HEX值，选填*!/
    onComplete: function(obj){              //拉取完广告的回调函数，必填
        checkForRender(this.placement_id,obj,container);
    }
});
(function() {
    var doc=document, h=doc.getElementsByTagName('head')[0], s=doc.createElement('script');
    s.async=true;
    s.src='http://qzs.qq.com/qzone/biz/res/i.js';
    h && h.insertBefore(s,h.firstChild);
})();*/


$(function() {
	initAD();
});
function initAD() {
	// 获得广告信息
	var cnid = $("#cnid").val();
	var version = $("#version").val();
	var model = $("#model").val();
	var IMEI = $("#IMEI").val();
	var platform = $("#platform").val();
	var path = $("#web").val();
	var userId = getUrlParam("userId");
	if (isBlank(userId)) {
		userId = getUrlParam("loginId");
	}
	if (userId = -1) {
		userId = getUrlParam("loginId");
	}
	if (path == null || path == undefined) {
		$(".index-scroll").hide();
		return;
	}
	if (isBlank(cnid)) {
		$(".index-scroll").hide();
		return;
	}
	if (isBlank(version)) {
		$(".index-scroll").hide();
		return;
	}
	if (isBlank(model)) {
		model = "";
	}
	if (isBlank(IMEI)) {
		IMEI = "";
	}
	if (isBlank(platform)) {
		platform = "";
	}
	$.ajax({
				type : "POST",
				url : path + "/external/banner/get.json",
				dataType : 'JSON',
				data : {
					"cnid" : cnid,
					"version" : version,
					"model" : model,
					"IMEI" : IMEI,
					"userId" : userId,
					"platform" : platform
				},
				success : function(result) {
					var adData = result.data;
					console.log(adData);
					if (adData instanceof Array) {
						if (adData.length > 0) {
							var urlsLength = 0;
							for (var i = 0; i < adData.length; i++) {
								var json = adData[i];
								var advId = json.advId;
								if (advId == "fp-banner") {
									var urlsdata = json.urlsdata;
									/*console.log(urlsdata);*/
									for (var j = 0; j < urlsdata.length; j++) {
										var adName = urlsdata[j].adname;
										var id = urlsdata[j].id;
										urlsLength++;
										$("#home_banner").append(
														"<div data-name='"+ adName +"' data-id='"+ id +"' class='swiper-slide swiper-no-swiping' onclick='sendMSG("
																+ JSON.stringify(urlsdata[j])
																+ ");'><img src='" + urlsdata[j].imageUrl +"' width='100%'> </div>");
									}
								}
							}
							var toLoop = true;
							if (urlsLength <= 1) {
								toLoop = false;
							}
							var count = 0;
							var mySwiper1 = new Swiper ('.index-scroll', {
								  // 如果需要分页器
								  pagination: '.index-dots',
								  speed : 500, //设置动画持续时间500ms
								  autoplay:2000,
								  autoplayDisableOnInteraction:false,
								  loop:toLoop,
								  noSwiping:true,
                                onAutoplay: function(swiper){
								  	/*console.log(swiper);*/
								  	count++;
								  	if(count >= urlsLength){
								  		count = 0;
									}
									var adname = $("#home_banner>div").eq(count).attr("data-name");
								  	var id = $("#home_banner>div").eq(count).attr("data-id");
								  	var i = path + "/external/banner/record.json"
								  	$.ajax({
										url : i,
										type : "post",
										dataType : "json",
										data : {
											"cnid" : cnid,
											"version" : version,
											"model" : model,
											"IMEI" : IMEI,
											"userId" : userId,
											"platform" : platform,
											"jsonstring" : adname,
											"id" : id
										},
										success : function (result) {
											
                                        },
										error : function () {
											
                                        }
									})
                                }

								});
						} else {
							$(".index-scroll").hide();
						}
					}else {
						$(".index-scroll").hide();
					}
				},
				error : function() {
				}
			});
}

function isBlank(string) {
	if (string == null || string == undefined) {
		return true;
	}
	if (string.replace(/(^s*)|(s*$)/g, "").length ==0) {
		return true;
	}
}

function sendMSG(obj) {
	/*alert(JSON.stringify(obj));*/
    if (obj != null && obj != undefined) {
        if (obj.adurltype == 1) {//跳转到网页
        	record(obj.adname,obj.id);
        	var go = new Object();
        	go.type = 1;
        	go.url = obj.quoteUrl;
        	go.showAd = 0;
        	/*console.log(JSON.stringify(go));*/
        	window.stub.jsCreateNewWebActivity(JSON.stringify(go));
//        	window.location.href = obj.quoteUrl;
        }
        if (obj.adurltype == 2) {//跳转
        	var quoteUrl = obj.quoteUrl;
        	/*console.log(quoteUrl);*/
        	/*alert(quoteUrl)*/
        	var jsonAnchorId = JSON.parse(quoteUrl);
        	/*alert(jsonAnchorId)*/
        	if(jsonAnchorId.type==1){//跳转到
                var live = new Object();
                live.fun = "userCenter";
                live.data = "activity";
                /*alert(jsonAnchorId.url)*/
                // live.url = "http://zb.cread.com/external/baberead"
                live.url = jsonAnchorId.url;
				// alert(JSON.stringify(live));
				/*console.log(JSON.stringify(live))*/
                //判断机型
                var u = navigator.userAgent;
                if (u.indexOf('Android') > -1 || u.indexOf('Linux') > -1) {//安卓手机
					/*alert("安卓手机");*/
                    window.stub.jsClient(JSON.stringify(live));
                } else if (u.indexOf('iPhone') > -1) {//苹果手机
					/*alert("苹果手机");*/
                    window.webkit.messageHandlers.jsClient.postMessage(JSON.stringify(live));
                } else if (u.indexOf('Windows Phone') > -1) {//winphone手机
					/*alert("winphone手机");*/
                }
			}else{//跳转到直播间
                /*console.log(jsonAnchorId.anchorId);*/
                getVideoInfo(jsonAnchorId.anchorId);
			}
        }
    }
}

function record(adname,id) {
	var cnid = getUrlParam("cnid");
	var version = getUrlParam("version");
	var model = getUrlParam("model");
	var IMEI = getUrlParam("IMEI");
	var userId = getUrlParam("userId");
	var platform = getUrlParam("platform");
	if (isBlank(cnid)) {
		return;
	}
	if (isBlank(version)) {
		return;
	}
	if (isBlank(model)) {
		model = "";
	}
	if (isBlank(IMEI)) {
		IMEI = "";
	}
	if (isBlank(platform)) {
		platform = "";
	}
	if (isBlank(userId)) {
		userId = getUrlParam("loginId");
	}
	if (userId = -1) {
		userId = getUrlParam("loginId");
	}
	if (isBlank(userId)) {
		userId = "";
	}
	var path = $("#web").val();
	if (path == null || path == undefined) {
		return;
	}
	$.ajax({
		type : "POST",
		url : path + "/external/banner/click.json",
		dataType : 'JSON',
		data : {
			"cnid" : cnid,
			"version" : version,
			"model" : model,
			"IMEI" : IMEI,
			"userId" : userId,
			"platform" : platform,
			"jsonstring" : adname,
			"id" : id
		},
		success : function(result) {
			
		},
		error : function() {
		}
	});
}

function getVideoInfo(anchorId) {
	if (anchorId == null || anchorId == undefined) {
		return;
	}
	var path = $("#web").val();
	if (path == null || path == undefined) {
		return;
	}
	$.ajax({
		type : "POST",
		url : path + "/external/banner/video.json",
		dataType : 'JSON',
		data : {
			"anchorId" : anchorId
		},
		success : function(result) {
			var videoStatus = result.videoStatus;
			if (videoStatus == 1) {
				livingInfo(result.videoId, result.chatroomId, result.anchorId, result.formatType, result.coverImg)
			} else if (videoStatus == 4) {
				videoInfo(result.videoId, result.anchorId, result.formatType, result.coverImg);
			}
			
		},
		error : function(e) {
        	/*console.log(e);*/
		}
	});
}


