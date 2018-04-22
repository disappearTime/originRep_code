var Lazy = {
    Img: null,
    getY: function(b) {
        var a = 0;
        if (b && b.offsetParent) {
            while (b.offsetParent) {
                a += b.offsetTop;
                b = b.offsetParent;
            }
        } else {
            if (b && b.y) {
                a += b.y;
            }
        }
        return a;
    },
    getX: function(b) {
        var a = 0;
        if (b && b.offsetParent) {
            while (b.offsetParent) {
                a += b.offsetLeft;
                b = b.offsetParent;
            }
        } else {
            if (b && b.x) {
                a += b.X;
            }
        }
        return a;
    },
    scrollY: function() {
        var a = document.documentElement;
        return self.pageYOffset || (a && a.scrollTop) || document.body.scrollTop || 0;
    },
    scrollX: function() {
        var a = document.documentElement;
        return self.pageXOffset || (a && a.scrollLeft) || document.body.scrollLeft || 0;
    },
    windowWidth: function() {
        var a = document.documentElement;
        return self.innerWidth || (a && a.clientWidth) || document.body.clientWidth;
    },
    windowHeight: function() {
        var a = document.documentElement;
        return self.innerHeight || (a && a.clientHeight) || document.body.clientHeight;
    },
    CurrentHeight: function() {
        return Lazy.scrollY() + Lazy.windowHeight();
    },
    CurrentWidth: function() {
        return Lazy.scrollX() + Lazy.windowWidth();
    },
    Load: function() {
        Lazy.Init();
        var d = Lazy.CurrentHeight();
        var a = Lazy.CurrentWidth();
        for (_index = 0; _index < Lazy.Img.length; _index++) {
            if ($(Lazy.Img[_index]).attr("lazy") == undefined) {
                $(Lazy.Img[_index]).attr("lazy", "n");
            }
            if ($(Lazy.Img[_index]).attr("lazy") == "y") {
                continue;
            }
            $(Lazy.Img[_index]).bind("error",
            function() {
                if (this.id == "subject") {
                    $(this).attr("src", "");
                } else {
                    $(this).attr("src", "");
                }
            });
            var b = Lazy.getY(Lazy.Img[_index]);
            var c = Lazy.getX(Lazy.Img[_index]);
            if (c < a) {
                if (b < d) {
                    Lazy.Img[_index].src = Lazy.Img[_index].getAttribute("data-src");
                    $(Lazy.Img[_index]).attr("lazy", "y");
                    Lazy.Img[_index].removeAttribute("data-src");
                }
            }
        }
    },
    LoadLbs: function() {
        Lazy.InitLbs();
        var d = Lazy.CurrentHeight();
        var a = Lazy.CurrentWidth();
        for (_index = 0; _index < Lazy.Img.length; _index++) {
            if ($(Lazy.Img[_index]).attr("lazy") == undefined) {
                $(Lazy.Img[_index]).attr("lazy", "n");
            }
            if ($(Lazy.Img[_index]).attr("lazy") == "y") {
                continue;
            }
            $(Lazy.Img[_index]).bind("error",
            function() {
                if (this.id == "subject") {
                    $(this).attr("src", "");
                } else {
                    $(this).attr("src", "");
                }
            });
            var b = Lazy.getY(Lazy.Img[_index]);
            var c = Lazy.getX(Lazy.Img[_index]);
            if (c < a) {
                if (b < d) {
                    Lazy.Img[_index].src = Lazy.Img[_index].getAttribute("data-src");
                    $(Lazy.Img[_index]).attr("lazy", "y");
                }
            }
        }
    },
    LoadX: function() {
        if (Lazy.Img == null) {
            Lazy.Init();
        }
        var a = Lazy.CurrentWidth();
        for (_index = 0; _index < Lazy.Img.length; _index++) {
            if ($(Lazy.Img[_index]).attr("lazy") == undefined) {
                $(Lazy.Img[_index]).attr("lazy", "n");
            }
            if ($(Lazy.Img[_index]).attr("lazy") == "y") {
                continue;
            }
            $(Lazy.Img[_index]).bind("error",
            function() {
                if (this.id == "subject") {
                    $(this).attr("src", "");
                } else {
                    $(this).attr("src", "http://zwsc.ikanshu.cn/images/0000000.png");
                }
            });
            var b = Lazy.getX(Lazy.Img[_index]);
            if (b < a) {
                Lazy.Img[_index].src = Lazy.Img[_index].getAttribute("data-src");
                $(Lazy.Img[_index]).attr("lazy", "y");
            }
        }
    },
    Init: function() {
        var a = document.querySelectorAll("img[data-src]");
        Lazy.Img = a;
    },
};
