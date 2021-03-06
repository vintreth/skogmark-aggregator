<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags/templates" %>
<%@ taglib prefix="w" tagdir="/WEB-INF/tags/widgets" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<t:main title="Посты на модерации">
    <jsp:attribute name="content">
        <div class="row">
            <div class="col-12">
                <div class="card">
                    <div class="card-body">
                        <h4 class="card-title">Посты в очереди модерации</h4>
                        <h6 class="card-subtitle">Отображение всех постов, ожидающих проверки</h6>
                    </div>
                    <div class="table-responsive">
                        <table id="posts-table" class="table table-hover">
                            <tr>
                                <th>#</th>
                                <th>Канал</th>
                                <th>Заголовок</th>
                                <th>Текст</th>
                                <th>Изображения</th>
                                <th>Время создания</th>
                                <th>Время изменения</th>
                            </tr>
                            <c:forEach items="${posts}" var="post">
                                <tr>
                                    <td><a href="/admin/moderation/posts/${post.id}/">${post.id}</a></td>
                                    <td><a href="/admin/moderation/posts/${post.id}/">${post.channel}</a></td>
                                    <td><a href="/admin/moderation/posts/${post.id}/">${post.title}</a></td>
                                    <td><a href="/admin/moderation/posts/${post.id}/">${post.previewText}</a></td>
                                    <td>
                                        <a href="/admin/moderation/posts/${post.id}/">
                                        <c:if test="${post.imagesSize > 0}">
                                            <span class="label label-success label-rounded">${post.imagesSize}</span>
                                        </c:if>
                                        <c:if test="${post.imagesSize <= 0}">
                                            <span class="label label-danger label-rounded">Нет</span>
                                        </c:if>
                                        </a>
                                    </td>
                                    <td><a href="/admin/moderation/posts/${post.id}/">${post.createdDt}</a></td>
                                    <td><a href="/admin/moderation/posts/${post.id}/">${post.changedDt}</a></td>
                                </tr>
                            </c:forEach>
                        </table>
                    </div>
                    <w:pagination baseUrl="${baseUrl}" pages="${pages}"/>
                </div>
            </div>
        </div>
    </jsp:attribute>
</t:main>