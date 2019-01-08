app.controller('payController', function ($scope,$location, payService) {
//    本地生成二维码
    $scope.createNative = function () {
        payService.createNative().success(
            function (response) {
                $scope.money = (response.total_fee / 100).toFixed(2);//金额保留两位小数
                $scope.out_trade_no = response.out_trade_no;//订单号

                //生成二维码
                var qr = new QRious({
                    element: document.getElementById('qrious'),
                    size: 250,
                    value: response.code_url,
                    level: 'H'
                });
                //查询支付状态
                queryPayStatus(response.out_trade_no);
            }
        );
    }

    //查询支付状态
    queryPayStatus = function (out_trade_no) {
        payService.queryPayStatus(out_trade_no).success(
            function (response) {
                if (response.success) {//如果成功
                    location.href = "paysuccess.html#?money=" + $scope.money;
                } else {//失败
                    if (response.message == '二维码超时') {
                        $scope.createNative();//重新生成二维码
                    } else {
                        location.href = "payfail.html";
                    }
                }
            }
        );
    }

//    获取金额
    $scope.getMoney = function () {
        return $location.search()['money'];
    }
})