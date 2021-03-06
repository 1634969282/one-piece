package com.mossle.cms.web.view;

import java.util.Date;

import javax.annotation.Resource;

import com.mossle.api.auth.CurrentUserHolder;

import com.mossle.cms.persistence.domain.CmsArticle;
import com.mossle.cms.persistence.domain.CmsComment;
import com.mossle.cms.persistence.manager.CmsArticleManager;
import com.mossle.cms.persistence.manager.CmsCommentManager;

import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("cms/view-comment")
public class CmsViewCommentController {
    private CmsArticleManager cmsArticleManager;
    private CmsCommentManager cmsCommentManager;
    private CurrentUserHolder currentUserHolder;

    @RequestMapping("submit")
    public String submit(@ModelAttribute CmsComment cmsComment,
            @RequestParam("articleId") Long articleId,
            RedirectAttributes redirectAttributes) {
        CmsArticle cmsArticle = cmsArticleManager.get(articleId);

        String userId = currentUserHolder.getUserId();
        cmsComment.setCmsArticle(cmsArticle);
        cmsComment.setCreateTime(new Date());
        cmsComment.setUserId(userId);
        cmsCommentManager.save(cmsComment);

        return "redirect:/cms/view/" + cmsArticle.getCmsCatalog().getCode()
                + "/" + articleId;
    }

    @RequestMapping("reply")
    @ResponseBody
    public String reply(@RequestParam("articleId") Long articleId,
            @RequestParam("commentId") Long commentId,
            @RequestParam("content") String content) {
        CmsComment parent = cmsCommentManager.get(commentId);
        Long conversation = parent.getId();

        if (parent.getConversation() != null) {
            conversation = parent.getConversation();
        }

        String userId = currentUserHolder.getUserId();
        CmsComment cmsComment = new CmsComment();
        cmsComment.setCmsArticle(cmsArticleManager.get(articleId));
        cmsComment.setCreateTime(new Date());
        cmsComment.setUserId(userId);
        cmsComment.setConversation(conversation);
        cmsComment.setContent(content);
        cmsComment.setCmsComment(parent);
        cmsCommentManager.save(cmsComment);

        return "true";
    }

    // ~ ======================================================================
    @Resource
    public void setCmsArticleManager(CmsArticleManager cmsArticleManager) {
        this.cmsArticleManager = cmsArticleManager;
    }

    @Resource
    public void setCmsCommentManager(CmsCommentManager cmsCommentManager) {
        this.cmsCommentManager = cmsCommentManager;
    }

    @Resource
    public void setCurrentUserHolder(CurrentUserHolder currentUserHolder) {
        this.currentUserHolder = currentUserHolder;
    }
}
