//基本控制层
//$scope双向控制
app.controller('baseController',function ($scope) {

    //刷新列表
    $scope.reloadList=function () {
        $scope.search($scope.paginationConf.currentPage,$scope.paginationConf.itemsPerPage);
    }

    //分页控住
    $scope.paginationConf={
        currentPage: 1,//当前
        totalItems: 10,//总记录数
        itemsPerPage: 10,//每页条数
        perPageOptions: [10, 20, 30, 40, 50],//下拉框
        onChange: function(){
            $scope.reloadList();//重新加载
        }
    };

    //勾选的id集合
    $scope.selectIds=[];

    //用户勾选复选框
    $scope.updateSelection=function ($event,id) {
        if($event.target.checked){//当前input的checked属性
            $scope.selectIds.push(id); //添加到集合
        }else {
            var index = $scope.selectIds.indexOf(id);
            $scope.selectIds.splice(index,1); //移出id
        }
    }
});