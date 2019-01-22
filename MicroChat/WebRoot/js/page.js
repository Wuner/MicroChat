(function($, window, document, undefined) {
	/**
	 * myPageCount：总记录数<br>
	 * myPageSize：一页显示几条记录数<br>
	 * myVisiblePages：显示几个按钮<br>
	 * loadData：加载数据函数<br>
	 * @param {int} myPageCount
	 * @param {int} myPageSize
	 * @param {int} myVisiblePages
	 * @param {Function} loadData
	 */
	$.page = function(myPageCount, myPageSize, myVisiblePages,loadData) {
		var countindex = myPageCount % myPageSize > 0 ? (myPageCount / myPageSize) + 1 : (myPageCount / myPageSize);
		$.jqPaginator('#pagination', {
			totalPages: parseInt(countindex),
			visiblePages: parseInt(myVisiblePages),
			currentPage: 1,
			prev: '<li class="prev"><a href="javascript:;">上一页</a></li>',
			next: '<li class="next"><a href="javascript:;">下一页</a></li>',
			page: '<li class="page"><a href="javascript:;">{{page}}</a></li>',
			onPageChange: function(num, type) {
				if(type == "change") {
					loadData(num);
				}
			}
		});
	}
})(jQuery, window, document);