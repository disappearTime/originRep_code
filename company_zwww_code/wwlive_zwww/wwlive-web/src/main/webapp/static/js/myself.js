
$.fn.touchFn=function(){
    return $(this).each(function() {
        $(this).bind("touchstart",function(e){
            $(this).addClass("v3_cur");
        });
        $(this).bind("touchmove",function(){
            $(this).removeClass("v3_cur");
        });
        $(this).bind("touchend",function(){
            $(this).removeClass("v3_cur");
        });
        $(this).bind("swipe",function(e){
            $(this).removeClass("v3_cur");
        });
    });
}









