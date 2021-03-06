<%@page language="java" pageEncoding="UTF-8" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%pageContext.setAttribute("ctx", request.getContextPath());%>
<c:set var="cdnPrefix" value="${ctx}/cdn"/>
<c:set var="tenantPrefix" value="${ctx}" scope="request"/>
<tags:appProp/>
<tags:config name="application.baseUrl" var="baseUrl"/>
