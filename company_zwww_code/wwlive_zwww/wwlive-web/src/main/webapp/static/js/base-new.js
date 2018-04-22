// JavaScript Document
// zln
$.fn.touchFn=function(){
	return $(this).each(function() {
	$(this).bind("touchstart",function(){
		$(this).addClass("v3_cur");	
	});
	$(this).bind("touchmove",function(){
		$(this).removeClass("v3_cur");
	});
	$(this).bind("touchend",function(){
		$(this).removeClass("v3_cur");
	});
	$(this).bind("swipe",function(){
		$(this).removeClass("v3_cur");
	});
	});
}
