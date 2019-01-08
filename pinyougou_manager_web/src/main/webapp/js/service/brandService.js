//品牌服务层
//把$http内置服务的请求路劲抽取出来
app.service('brandService',function ($http) {
        //this就是上面的参数$http
        //查询品牌列表
        this.findAll=function () {
            return $http.get('../brand/findAll.do');
        }

        //分页
        this.page=function (page,rows) {
            return $http.get('../brand/findPage.do?page='+page +'&rows='+rows);
        }

        //增加
        this.add=function (entity) {
            return $http.post('../brand/add.do',entity);
        }

        //修改
        this.update=function (entity) {
            return $http.post('../brand/update.do',entity);
        }

    //根据id查询
        this.findOne=function (id) {
            return  $http.get('../brand/findOne.do?id='+id);
        }

    //删除
        this.dele=function (ids) {
            return  $http.get('../brand/delete.do?ids='+ids);
        }

    //条件查询
        this.search=function (page,rows,searchEntity) {
            return $http.post('../brand/search.do?page='+page +'&rows='+rows, searchEntity);
        }
    //下拉列表数据
        this.selectOptionList=function () {
            return $http.get('../brand/selectOptionList.do');
        }
})