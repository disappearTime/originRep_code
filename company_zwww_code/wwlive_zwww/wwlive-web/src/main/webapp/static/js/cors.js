/**
 * Created by Niu Qianghong on 2017-11-06 0006.
 * 跨域访问js, 供PC端访问
 */

/**
 * 获取翻江龙游戏幸运榜
 */
function getLuckList() {
    $.ajax({
        url: "/launch/game/card/lucklist.json",
        type: "POST",
        data: {
            "platform": "iOS",
            "coverKey": "994aa6e6b5c911c6b9bdb85b99900ec8",
            "anchorId": "1633518",
            "model": "iPhone9,1",
            "IMEI": "5C6EB6EF-0408-4006-AD0E-E1112BFE46E9",
            "requestId": "5C6EB6EF04084006AD0EE1112BFE46E9",
            "nonce": "woiqvzao",
            "cnid": "1062",
            "version": "1.4.3"
        },
        success: function (returnData) {
            callback1(returnData);
        }
    });
}

/**
 * 获取翻江龙今日牌面数据
 */
function getTodayFace() {
    $.ajax({
        url: "/launch/game/card/todayface.json",
        type: "POST",
        data: {
            "platform": "iOS",
            "coverKey": "994aa6e6b5c911c6b9bdb85b99900ec8",
            "anchorId": "1633518",
            "model": "iPhone9,1",
            "IMEI": "5C6EB6EF-0408-4006-AD0E-E1112BFE46E9",
            "requestId": "5C6EB6EF04084006AD0EE1112BFE46E9",
            "nonce": "woiqvzao",
            "cnid": "1062",
            "version": "1.4.3"
        },
        success: function (returnData) {
            callback2(returnData);
        }
    });
}