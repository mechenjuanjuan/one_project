app.controller('contentController',function($scope,contentService){

	//有很多组广告，都起名字太麻烦，提取成一个变量
	$scope.contentList=[];//广告列表

    //根据分类 ID 查询广告
	$scope.findByCategoryId=function(categoryId){
		contentService.findByCategoryId(categoryId).success(
			function(response){
				$scope.contentList[categoryId]=response;
			}
		);		
	}

//	搜索跳转
    $scope.search=function () {
        location.href="http://localhost:9104/search.html#?keywords="+$scope.keywords;
    }
	
});