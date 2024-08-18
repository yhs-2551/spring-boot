<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>


<%@ include file="./includes/header.jsp" %>
            <div class="row">
                <div class="col-lg-12">
                    <h1 class="page-header">Tables</h1>
                </div>
                <!-- /.col-lg-12 -->
            </div>
            <!-- /.row -->
            <div class="row">
                <div class="col-lg-12">
                    <div class="panel panel-default">
                        <div class="panel-heading">
                           Board List Page
                           <button id="regBtn" type="button" class="btn btn-xs pull-right">Register New Board</button>
                        </div>
                        <!-- /.panel-heading -->
                        <div class="panel-body">
                            <table width="100%" class="table table-striped table-bordered table-hover" id="dataTables-example">
                                <thead>
                                    <tr>
                                        <th>번호</th>
                                        <th>제목</th>
                                        <th>작성자</th>
                                        <th>작성일</th>
                                        <th>수정일</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach items="${list}" var="board">
                                        <tr>
                                            <td><c:out value="${board.bno}" /></td>
                                            <td>
                                            <a class='move' href='<c:out value="${board.bno}"/>'>
                                            <c:out value="${board.title}" /></a>
                                            </td>
                                            </td>
                                            <td><c:out value="${board.writer}" /></td>
                                            <td><fmt:formatDate pattern="yyyy-MM-dd" value="${board.regdate}" /></td>
                                            <td><fmt:formatDate pattern="yyyy-MM-dd" value="${board.updateDate}" /></td>
                                        </tr>
                                      </c:forEach>
                                </tbody>
                            </table>

 	                <div class='row'>
 					    <div class="col-lg-12">

                          <form id='searchForm' action="/board/list" method='get'>
                              <select name='type'>
                                  <option value="" <c:if test="${empty pageMaker.cri.type}">selected</c:if>>--</option>
                                  <option value="T" <c:if test="${pageMaker.cri.type eq 'T'}">selected</c:if>>제목</option>
                                  <option value="C" <c:if test="${pageMaker.cri.type eq 'C'}">selected</c:if>>내용</option>
                                  <option value="W" <c:if test="${pageMaker.cri.type eq 'W'}">selected</c:if>>작성자</option>
                                  <option value="TC" <c:if test="${pageMaker.cri.type eq 'TC'}">selected</c:if>>제목 or 내용</option>
                                  <option value="TW" <c:if test="${pageMaker.cri.type eq 'TW'}">selected</c:if>>제목 or 작성자</option>
                                  <option value="TWC" <c:if test="${pageMaker.cri.type eq 'TWC'}">selected</c:if>>제목 or 내용 or 작성자</option>
                              </select>
                              <input type='text' name='keyword' value='<c:out value="${pageMaker.cri.keyword}"/>' />
                              <input type='hidden' name='pageNum' value='<c:out value="${pageMaker.cri.pageNum}"/>' />
                              <input type='hidden' name='amount' value='<c:out value="${pageMaker.cri.amount}"/>' />
                              <button class='btn btn-default'>Search</button>
                          </form>
 					</div>
 				</div>

                              <!--번호 출력 부분은 c:out을 이용하면 좋지만 편의상 및 가독성의 문제로 EL로 처리-->
                            <div class='pull-right'>
                                <ul class="pagination">
                                    <c:if test="${pageMaker.prev}">
                                        <li class="paginate_button previous">
                                            <a href="${pageMaker.startPage - 1}">Previous</a>
                                        </li>
                                    </c:if>

                                    <c:forEach var="num" begin="${pageMaker.startPage}" end="${pageMaker.endPage}">
                                        <li class="paginate_button ${pageMaker.cri.pageNum == num ? 'active' : ''}">
                                            <a href="${num}">${num}</a>
                                        </li>
                                    </c:forEach>

                                    <c:if test="${pageMaker.next}">
                                        <li class="paginate_button next">
                                            <a href="${pageMaker.endPage + 1}">Next</a>
                                        </li>
                                    </c:if>
                                </ul>
                            </div>
                            <!--  end pagination -->
                        </div>

                         <!-- 아래 코드 c:out을 이용하면 좋지만 편의상 및 가독성의 문제로 EL로 처리-->


                        <form id="actionForm" action="/board/list" method="get">

                            <input type='hidden' name='type' value="${pageMaker.cri.type}">
                            <input type='hidden' name='keyword' value="${pageMaker.cri.keyword}">

                            <input type="hidden" name="pageNum" value="${pageMaker.cri.pageNum}">
                            <input type="hidden" name="amount" value="${pageMaker.cri.amount}">


                        </form>



                        <!-- Modal 모달창에 대한 코드는 pages/notifications.html 참고 -->
                        <div class="modal fade" id="myModal" tabindex="-1" role="dialog"
                            aria-labelledby="myModalLabel" aria-hidden="true">
                            <div class="modal-dialog">
                                <div class="modal-content">
                                    <div class="modal-header">
                                        <button type="button" class="close" data-dismiss="modal"
                                            aria-hidden="true">&times;</button>
                                        <h4 class="modal-title" id="myModalLabel">Modal title</h4>
                                    </div>
                                    <div class="modal-body">처리가 완료되었습니다.</div>
                                    <div class="modal-footer">
                                        <button type="button" class="btn btn-default"
                                            data-dismiss="modal">Close</button>
                                        <button type="button" class="btn btn-primary">Save
                                            changes</button>
                                    </div>
                                </div>
                                <!-- /.modal-content -->
                            </div>
                            <!-- /.modal-dialog -->
                        </div>
                        <!-- /.modal -->

                        </div>
                        <!-- /.panel-body -->
                    </div>
                    <!-- /.panel -->
                </div>
                <!-- /.col-lg-6 -->
            </div>
            <!-- /.row -->

<script type="text/javascript">
$(document).ready(() => {

   const result = '<c:out value="${result}" />';

   const checkModal = (result) => {

    // 등록하면 -> 목록 -> 목록에서 조회 -> 뒤로가기 하면 등록했을때 목록 페이지에서 나오는 모달창이 다시 보이게 되면 아래 코드 추가(즉 뒤로가기 문제 해결 방법)

    // history.replaceState({}, null, null);

    //    if (result === "" || history.state) {
    //      return;
    //    }

        if (result === "") {
            return;
        }

        if (parseInt(result) > 0) {
            $(".modal-body").html("게시글" + parseInt(result) + "번이 등록되었습니다.");
        }
        $("#myModal").modal("show");
   }
    checkModal(result);

    $("#regBtn").on("click", () => {
        self.location = "/board/register";
    });


    const actionForm = $("#actionForm");

    $(".paginate_button a").on("click", function(e) {

        e.preventDefault();

        actionForm.find("input[name='pageNum']").val($(this).attr("href"));

        actionForm.submit();

    });

    $(".move").on("click", function(e) {
        e.preventDefault();

        actionForm.append(
            "<input type='hidden' name='bno' value='" + $(this).attr("href") + "'>"
        );

        actionForm.attr("action", "/board/get");
        actionForm.submit();
    });

})

   const searchForm = $("#searchForm");

   $("#searchForm button").on("click", function(e) {
        if(!searchForm.find("option:selected").val()) {
            alert("검색종류를 선택하세요");
            return false;
        }

        if (!searchForm.find("input[name='keyword']").val()) {
            alert("키워드를 입력하세요");
            return false;
        }

         searchForm.find("input[name='pageNum']").val("1");

        e.preventDefault();

        searchForm.submit();
   });

</script>

<%@ include file="./includes/footer.jsp" %>
