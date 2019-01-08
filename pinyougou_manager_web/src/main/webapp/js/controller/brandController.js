//品牌控制层
//$scope双向控制，传brandService.js的brandService过来
app.controller('brandController',function ($scope,$controller, brandService) {
    //重点：继承
    //$controller 也是 angular 提供的一个服务，可以实现伪继承，实际上就是与 BaseController 共享$scope
    //传基础控制baseController
    $controller('baseController',{$scope:$scope});

    //查询品牌列表
    $scope.findAll=function () {
        brandService.findAll().success(
            function (response) {
                $scope.list=response;
            }
        );
    }

    //分页
    $scope.findPage=function (page,rows) {
        brandService.findPage(page,rows).success(
            function (response) {
                $scope.list=response.rows;//显示当前页数
                $scope.paginationConf.totalItems=response.total;//更新总记录数
            }
        );
    }

    //增加  修改
    $scope.save=function () {
        var serviceObject;//服务层对象
        if($scope.entity.id != null){//如果有id
            serviceObject = brandService.update($scope.entity);//修改
        }else {
            serviceObject = brandService.add($scope.entity);//添加
        }
        //上面判断回调函数,复制给serviceObject
        //brandService.add/update()
        serviceObject.success(
            function (response) {
                if(response.success){
                    $scope.reloadList();//刷新
                }else {
                    alert(response.message);
                }
            }
        )
    }

    //根据id查询
    $scope.findOne=function(id){
        brandService.findOne(id).success(
            function (response) {
                $scope.entity=response;
            }
        )
    }

    //批量删除
    $scope.dele=function () {
           brandService.dele($scope.selectIds).success(
                function (response) {
                    if(response.success){
                        $scope.reloadList();//刷新
                        $scope.selectIds=[];//初始化
                    }
                }
            );
    }

    //条件查询（搜索）
    $scope.searchEntity={};//定义搜索对象 （初始化）

    $scope.search=function (page, rows) {
        brandService.search(page,rows,$scope.searchEntity).success(
            function (response) {
                $scope.list=response.rows;//显示当前页数据
                $scope.paginationConf.totalItems=response.total;//更新总记录数
            }
        );
    }
})