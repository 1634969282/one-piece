<%@page contentType="text/html;charset=UTF-8"%>
<%@include file="/taglibs.jsp"%>
<%pageContext.setAttribute("currentHeader", "plm");%>
<%pageContext.setAttribute("currentMenu", "plm");%>
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
    <meta charset="utf-8">
    <title>kanban</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link href="${cdnPrefix}/public/bootstrap/3.3.7/css/bootstrap.min.css" rel="stylesheet" id="bootstrap-css">
    <link href="${cdnPrefix}/public/mossle-kanban/0.0.2/kanban.css" rel="stylesheet">

    <script src="${cdnPrefix}/public/jquery/1.12.4/jquery.min.js"></script>
    <script src="${cdnPrefix}/public/bootstrap/3.3.7/js/bootstrap.min.js"></script>

    <link type="text/css" rel="stylesheet" href="${cdnPrefix}/public/userpicker/userpicker.css">
    <script type="text/javascript" src="${cdnPrefix}/public/userpicker/userpicker3.js"></script>

	<script src="${tenantPrefix}/public/mossle-kanban/0.0.2/kanban.js"></script>

	<script type="text/javascript">
var logined = <tags:isUser>true</tags:isUser><tags:isGuest>false</tags:isGuest>;

$(function() {
	if (!logined) {
		return;
	}

	createUserPicker({
		multiple: false,
		url: '${cdnPrefix}/rs/user/search'
	});

	$(document).delegate('.plmIssueEdit', 'click', function() {
		var issueId = $(this).attr('id');
		$.get('${tenantPrefix}/rs/plm/kanbanViewIssue', {
			issueId: issueId
		}, function(result) {
			var data = result.data;
			$('#plmIssueId').val(data.id);
			$('#plmIssueName').val(data.name);
			$('#plmIssueContent').val(data.content);
			$('#plmIssueStep').val(data.step);
			$('#plmIssueAssigneeId').val(data.assigneeId);
			$('#plmIssueAssigneeName').val(data.assigneeName);
			prepareToUpdateIssue();

			$('#issueModal').modal('toggle');
		});
	});

	clearIssueForm();
});

function createIssue() {
	$.post("${tenantPrefix}/rs/plm/kanbanCreateIssue", {
		sprintId: ${plmSprint.id},
		name: $('#plmIssueName').val(),
		content: $('#plmIssueContent').val(),
		step: $('#plmIssueStep').val(),
		assigneeId: $('#plmIssueAssigneeId').val()
	}, function() {
		location.reload();
	});
}

function updateIssue() {
	$.post("${tenantPrefix}/rs/plm/kanbanUpdateIssue", {
		id: $('#plmIssueId').val(),
		name: $('#plmIssueName').val(),
		content: $('#plmIssueContent').val(),
		step: $('#plmIssueStep').val(),
		assigneeId: $('#plmIssueAssigneeId').val()
	}, function() {
		location.reload();
	});
}

function clearIssueForm() {
	$('#plmIssueName').val('');
	$('#plmIssueContent').val('');
}

function prepareToCreateIssue() {
	$('#plmIssueSubmit')[0].onclick = createIssue;
}

function prepareToUpdateIssue(issueId) {
	$('#plmIssueSubmit')[0].onclick = updateIssue;
}
	</script>
  </head>
  <body>
    <div class="container-fluid" style="padding-top:65px;">

      <div id="sortableKanbanBoards" class="row">
<!--
	    <div class="col-md-2">
		  <div>
		    <tags:isGuest>
            <a href="${tenantPrefix}/common/login.jsp" class="btn">
		      <tags:currentUser/> ??????
            </a>
			</tags:isGuest>
			<tags:isUser>
		    <button type="button" class="btn" data-toggle="modal" data-target="#issueModal" onclick="prepareToCreateIssue()">????????????</button>
		    <button type="button" class="btn" data-toggle="modal" data-target="#sprintModal">????????????</button>
		    <a href="${tenantPrefix}/j_spring_security_logout" class="btn">
			  <img src="${tenantPrefix}/rs/avatar?id=<tags:currentUserId/>&width=16" style="width:16px;height:16px;" class="img-circle">
			  <tags:currentUser/> ??????
			</a>
			</tags:isUser>
		  </div>
		  <div>?????????${plmSprint.name}</div>
		  <div>?????????${plmSprint.plmProject.name}</div>
		  <div>?????????${plmSprint.plmConfig.name}</div>
		  <div>?????????<fmt:formatDate value="${plmSprint.startTime}" type="date"/></div>
		  <div>?????????<fmt:formatDate value="${plmSprint.endTime}" type="date"/></div>
		  <div>?????????${plmSprint.status}</div>
          <div>
			<a href="${tenantPrefix}/plm/index.do" class="btn">
		      ????????????
            </a>
		  </div>
		</div>
-->
<c:forEach var="map" items="${list}">

		<!-- doing start -->
		<div class="panel panel-primary kanban-col kanban-col-1" style="width:${92.0/fn:length(list)}%">
		  <div class="panel-heading text-center">
		    ${map.plmStep.name}
		    <i class="fa fa-2x fa-plus-circle pull-right"></i>
		  </div>
		  <div class="panel-body">
		    <div id="${map.plmStep.code}" class="kanban-centered">

<c:forEach var="item" items="${map.plmIssues}">
		      <article class="kanban-entry grab" id="${item.id}" draggable="true">
		        <div class="kanban-entry-inner">

		          <div class="kanban-label">
		            <h2>
					  <a id="${item.id}" href="#" class="plmIssueEdit" data="{name:'${item.name}',assigneeId:'${item.assigneeId}'}"># ${item.id}</a>
					  <div class="pull-right">
					    <img id="${item.id}" src="${tenantPrefix}/rs/avatar?id=${item.assigneeId}&width=32" style="width:32px;">
					  </div>
					</h2>
					<p>${item.name}</p>
		          </div>
		        </div>
		      </article>
</c:forEach>

		    </div>
		  </div>
		  <!--
		  <div class="panel-footer">
		    <a href="#">&nbsp;</a>
		  </div>
		  -->
		</div>
		<!-- doing end -->
</c:forEach>

      </div>
    </div>


    <!-- Static Modal -->
    <div class="modal modal-static fade" id="processing-modal" role="dialog" aria-hidden="true">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-body">
            <div class="text-center">
              <i class="fa fa-refresh fa-5x fa-spin"></i>
              <h4>?????????...</h4>
            </div>
          </div>
        </div>
      </div>
    </div>

<div class="modal fade" id="issueModal">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
        <h4 class="modal-title">????????????</h4>
      </div>
      <div class="modal-body">
<form>
  <input type="hidden" id="plmIssueId" value=""/>
  <div class="form-group">
    <label>?????????${plmSprint.plmProject.name}</label>
  </div>
  <div class="form-group">
    <label for="plmIssueName">??????</label>
    <input type="text" class="form-control" name="name" id="plmIssueName" value="">
  </div>
  <div class="form-group">
    <label for="plmIssueContent">??????</label>
    <textarea type="text" class="form-control" name="content" id="plmIssueContent"></textarea>
  </div>
  <div class="form-group">
    <label for="plmIssueStep">??????</label>
	<select class="form-control" name="step" id="plmIssueStep">
	  <c:forEach var="item" items="${plmSteps}">
	  <option value="${item.code}">${item.name}</option>
	  </c:forEach>
	</select>
  </div>
  <div class="form-group">
    <label for="plmIssueAssignee">?????????</label>
    <div class="input-group userPicker">
	  <input type="hidden" value="<tags:currentUserId/>" name="assigneeId" id="plmIssueAssigneeId">
	  <input type="text" class="form-control" value="<tags:currentUser/>" name="assigneeId_name" id="plmIssueAssignee">
      <span class="input-group-addon glyphicon glyphicon-user"></span>
    </div>
  </div>
</form>
      </div>
	  <div class="modal-footer">
        <button type="button" class="btn btn-default" data-dismiss="modal">??????</button>
        <button type="button" class="btn btn-primary" onclick="createIssue()" id="plmIssueSubmit">??????</button>
      </div>
    </div>
  </div>
</div>

  </body>
</html>
