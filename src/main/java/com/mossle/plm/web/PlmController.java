package com.mossle.plm.web;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.mossle.api.auth.CurrentUserHolder;

import com.mossle.core.mapper.BeanMapper;
import com.mossle.core.page.Page;

import com.mossle.plm.persistence.domain.PlmCategory;
import com.mossle.plm.persistence.domain.PlmComment;
import com.mossle.plm.persistence.domain.PlmComponent;
import com.mossle.plm.persistence.domain.PlmConfig;
import com.mossle.plm.persistence.domain.PlmIssue;
import com.mossle.plm.persistence.domain.PlmLog;
import com.mossle.plm.persistence.domain.PlmProject;
import com.mossle.plm.persistence.domain.PlmSprint;
import com.mossle.plm.persistence.domain.PlmVersion;
import com.mossle.plm.persistence.manager.PlmCategoryManager;
import com.mossle.plm.persistence.manager.PlmCommentManager;
import com.mossle.plm.persistence.manager.PlmComponentManager;
import com.mossle.plm.persistence.manager.PlmConfigManager;
import com.mossle.plm.persistence.manager.PlmIssueManager;
import com.mossle.plm.persistence.manager.PlmLogManager;
import com.mossle.plm.persistence.manager.PlmProjectManager;
import com.mossle.plm.persistence.manager.PlmSprintManager;
import com.mossle.plm.persistence.manager.PlmVersionManager;
import com.mossle.plm.service.PlmLogService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("plm")
public class PlmController {
    private static Logger logger = LoggerFactory.getLogger(PlmController.class);
    private PlmProjectManager plmProjectManager;
    private PlmVersionManager plmVersionManager;
    private PlmIssueManager plmIssueManager;
    private PlmCommentManager plmCommentManager;
    private PlmCategoryManager plmCategoryManager;
    private PlmLogManager plmLogManager;
    private PlmSprintManager plmSprintManager;
    private PlmConfigManager plmConfigManager;
    private PlmComponentManager plmComponentManager;
    private PlmLogService plmLogService;
    private CurrentUserHolder currentUserHolder;
    private JdbcTemplate jdbcTemplate;
    private BeanMapper beanMapper = new BeanMapper();

    /**
     * ???????????????????????????????????????.
     */
    @RequestMapping("index")
    public String index(Model model) throws Exception {
        String userId = currentUserHolder.getUserId();

        // ????????????
        List<PlmIssue> todoIssues = plmIssueManager
                .find("from PlmIssue where assigneeId=? and status='active' order by createTime",
                        userId);
        model.addAttribute("todoIssues", todoIssues);

        // ???????????????
        List<PlmIssue> myIssues = plmIssueManager
                .find("from PlmIssue where reporterId=? and status='active' order by createTime",
                        userId);
        model.addAttribute("myIssues", myIssues);

        // sprint
        List<PlmSprint> plmSprints = plmSprintManager.find("from PlmSprint");
        model.addAttribute("plmSprints", plmSprints);

        // project
        List<PlmProject> plmProjects = plmProjectManager.getAll();
        model.addAttribute("plmProjects", plmProjects);

        return "plm/index";
    }

    /**
     * ??????????????????.
     */
    @RequestMapping("projects")
    public String projects(Model model) throws Exception {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        model.addAttribute("list", list);

        List<PlmCategory> plmCategories = plmCategoryManager.getAll("priority",
                true);

        for (PlmCategory plmCategory : plmCategories) {
            Map<String, Object> map = new HashMap<String, Object>();
            list.add(map);
            map.put("plmCategory", plmCategory);

            String hql = "from PlmProject where plmCategory=? order by priority";
            map.put("plmProjects", plmProjectManager.find(hql, plmCategory));
        }

        return "plm/projects";
    }

    /**
     * ??????????????????.
     */
    @RequestMapping("project")
    public String project(@RequestParam("projectId") Long projectId, Model model)
            throws Exception {
        PlmProject plmProject = plmProjectManager.get(projectId);
        model.addAttribute("plmProject", plmProject);

        List<PlmVersion> plmVersions = plmVersionManager.findBy("plmProject",
                plmProject);
        model.addAttribute("plmVersions", plmVersions);

        List<PlmComponent> plmComponents = plmComponentManager.findBy(
                "plmProject", plmProject);
        model.addAttribute("plmComponents", plmComponents);

        List<PlmSprint> plmSprints = plmSprintManager.findBy("plmProject",
                plmProject);
        model.addAttribute("plmSprints", plmSprints);

        String hql = "from PlmIssue where plmProject=?";
        Page page = plmIssueManager.pagedQuery(hql, 1, 10, plmProject);
        model.addAttribute("page", page);

        return "plm/project";
    }

    /**
     * ?????????????????????.
     */
    @RequestMapping("create")
    public String create(@RequestParam("projectId") Long projectId, Model model)
            throws Exception {
        PlmProject plmProject = plmProjectManager.get(projectId);
        model.addAttribute("plmProject", plmProject);

        List<PlmVersion> plmVersions = plmVersionManager.findBy("plmProject",
                plmProject);
        model.addAttribute("plmVersions", plmVersions);

        return "plm/create";
    }

    /**
     * ?????????????????????.
     */
    @RequestMapping("edit")
    public String edit(@RequestParam("id") Long id, Model model)
            throws Exception {
        PlmIssue plmIssue = plmIssueManager.get(id);
        model.addAttribute("plmIssue", plmIssue);

        PlmProject plmProject = plmIssue.getPlmProject();
        model.addAttribute("plmProject", plmProject);

        return "plm/edit";
    }

    /**
     * ????????????.
     */
    @RequestMapping("save")
    public String save(@ModelAttribute PlmIssue plmIssue,
            @RequestParam("projectId") Long projectId) throws Exception {
        plmIssue.setPlmProject(plmProjectManager.get(projectId));
        plmIssue.setReporterId(currentUserHolder.getUserId());
        plmIssue.setCreateTime(new Date());
        plmIssue.setStatus("active");
        plmIssueManager.save(plmIssue);
        plmLogService.issueCreated(plmIssue);

        return "redirect:/plm/issue.do?id=" + plmIssue.getId();
    }

    /**
     * ????????????.
     */
    @RequestMapping("update")
    public String update(@ModelAttribute PlmIssue plmIssue) throws Exception {
        String userId = currentUserHolder.getUserId();
        PlmIssue target = plmIssueManager.get(plmIssue.getId());
        PlmIssue oldIssue = new PlmIssue();
        beanMapper.copy(target, oldIssue);
        beanMapper.copy(plmIssue, target);
        plmIssueManager.save(target);
        plmLogService.issueUpdated(oldIssue, target, userId);

        return "redirect:/plm/issue.do?id=" + plmIssue.getId();
    }

    /**
     * ????????????.
     */
    @RequestMapping("issues")
    public String issues(@ModelAttribute Page page, Model model)
            throws Exception {
        page = plmIssueManager.pagedQuery(
                "from PlmIssue order by createTime desc", page.getPageNo(),
                page.getPageSize());
        model.addAttribute("page", page);

        return "plm/issues";
    }

    /**
     * ????????????.
     */
    @RequestMapping("issue")
    public String issue(@RequestParam("id") Long id, Model model)
            throws Exception {
        String userId = currentUserHolder.getUserId();
        model.addAttribute("currentUserId", userId);

        PlmIssue plmIssue = plmIssueManager.get(id);
        model.addAttribute("plmIssue", plmIssue);
        model.addAttribute("plmProject", plmIssue.getPlmProject());

        List<PlmComment> plmComments = plmCommentManager.find(
                "from PlmComment where plmIssue=? order by createTime desc",
                plmIssue);
        model.addAttribute("plmComments", plmComments);

        List<PlmLog> plmLogs = plmLogManager.find(
                "from PlmLog where plmIssue=? order by logTime desc", plmIssue);
        model.addAttribute("plmLogs", plmLogs);

        return "plm/issue";
    }

    /**
     * ????????????.
     */
    @RequestMapping("view")
    public String view(@RequestParam("id") Long id, Model model)
            throws Exception {
        String userId = currentUserHolder.getUserId();
        model.addAttribute("currentUserId", userId);

        PlmIssue plmIssue = plmIssueManager.get(id);
        model.addAttribute("plmIssue", plmIssue);
        model.addAttribute("plmProject", plmIssue.getPlmProject());

        List<PlmComment> plmComments = plmCommentManager.find(
                "from PlmComment where plmIssue=? order by createTime desc",
                plmIssue);
        model.addAttribute("plmComments", plmComments);

        List<PlmLog> plmLogs = plmLogManager.find(
                "from PlmLog where plmIssue=? order by logTime desc", plmIssue);
        model.addAttribute("plmLogs", plmLogs);

        return "plm/view";
    }

    /**
     * ????????????.
     */
    @RequestMapping("saveComment")
    public String saveComment(@RequestParam("issueId") Long issueId,
            @RequestParam("content") String content) throws Exception {
        PlmIssue plmIssue = plmIssueManager.get(issueId);
        PlmComment plmComment = new PlmComment();
        plmComment.setContent(content);
        plmComment.setPlmIssue(plmIssue);
        plmComment.setUserId(currentUserHolder.getUserId());
        plmComment.setCreateTime(new Date());

        Integer priority = jdbcTemplate.queryForObject(
                "select max(priority) from PLM_COMMENT where ISSUE_ID=?",
                Integer.class, plmIssue.getId());

        if (priority != null) {
            priority++;
        } else {
            priority = 1;
        }

        plmComment.setPriority(priority);
        plmCommentManager.save(plmComment);
        plmLogService.commentCreated(plmComment);

        // update
        plmIssue.setUpdateTime(new Date());
        plmIssueManager.save(plmIssue);

        return "redirect:/plm/issue.do?id=" + plmIssue.getId();
    }

    /**
     * ????????????.
     */
    @RequestMapping("updateComment")
    public String updateComment(@RequestParam("id") Long id,
            @RequestParam("content") String content) throws Exception {
        PlmComment plmComment = plmCommentManager.get(id);
        plmComment.setContent(content);
        plmCommentManager.save(plmComment);
        plmLogService.commentUpdated(plmComment);

        return "redirect:/plm/issue.do?id=" + plmComment.getPlmIssue().getId();
    }

    /**
     * ????????????.
     */
    @RequestMapping("complete")
    public String complete(@RequestParam("id") Long id) throws Exception {
        String userId = currentUserHolder.getUserId();
        PlmIssue plmIssue = plmIssueManager.get(id);
        plmIssue.setStatus("complete");
        plmIssue.setCompleteTime(new Date());
        plmIssueManager.save(plmIssue);
        plmLogService.issueCompleted(plmIssue, userId);

        return "redirect:/plm/issue.do?id=" + plmIssue.getId();
    }

    /**
     * ????????????.
     */
    @RequestMapping("reopen")
    public String reopen(@RequestParam("id") Long id) throws Exception {
        String userId = currentUserHolder.getUserId();
        PlmIssue plmIssue = plmIssueManager.get(id);
        plmIssue.setStatus("active");
        plmIssue.setCompleteTime(null);
        plmIssueManager.save(plmIssue);
        plmLogService.issueReopened(plmIssue, userId);

        return "redirect:/plm/issue.do?id=" + plmIssue.getId();
    }

    /**
     * ????????????.
     */
    @RequestMapping("claim")
    public String claim(@RequestParam("id") Long id) throws Exception {
        String userId = currentUserHolder.getUserId();
        PlmIssue plmIssue = plmIssueManager.get(id);

        if (plmIssue == null) {
            logger.info("cannot find issue {}", id);

            return "redirect:/plm/issue.do?id=" + id;
        }

        plmIssue.setAssigneeId(currentUserHolder.getUserId());
        plmIssueManager.save(plmIssue);
        plmLogService.issueClaimed(plmIssue, userId);

        return "redirect:/plm/issue.do?id=" + plmIssue.getId();
    }

    /**
     * ????????????.
     */
    @RequestMapping("assign")
    public String assign(@RequestParam("id") Long id,
            @RequestParam("userId") String userId) throws Exception {
        PlmIssue plmIssue = plmIssueManager.get(id);
        PlmIssue oldIssue = new PlmIssue();
        beanMapper.copy(plmIssue, oldIssue);
        plmIssue.setAssigneeId(userId);
        plmIssueManager.save(plmIssue);
        plmLogService.issueAssigned(oldIssue, plmIssue,
                currentUserHolder.getUserId());

        return "redirect:/plm/issue.do?id=" + plmIssue.getId();
    }

    /**
     * ??????????????????.
     */
    @RequestMapping("sprints")
    public String sprint(Model model) throws Exception {
        List<PlmSprint> plmSprints = plmSprintManager.getAll();
        model.addAttribute("plmSprints", plmSprints);

        return "plm/sprints";
    }

    /**
     * ??????????????????.
     */
    @RequestMapping("sprint")
    public String sprint(@RequestParam("sprintId") Long sprintId, Model model)
            throws Exception {
        PlmSprint plmSprint = plmSprintManager.get(sprintId);
        model.addAttribute("plmSprint", plmSprint);

        List<PlmSprint> plmSprints = plmSprintManager.findBy("plmProject",
                plmSprint.getPlmProject());
        List<PlmSprint> plmSprintTargets = new ArrayList<PlmSprint>();

        for (PlmSprint plmSprintTarget : plmSprints) {
            if (plmSprintTarget.getId().equals(sprintId)) {
                continue;
            }

            plmSprintTargets.add(plmSprintTarget);
        }

        model.addAttribute("plmSprints", plmSprintTargets);

        String hql = "from PlmIssue where plmSprint=?";
        Page page = plmIssueManager.pagedQuery(hql, 1, 100, plmSprint);
        model.addAttribute("page", page);

        return "plm/sprint";
    }

    /**
     * ????????????.
     */
    @RequestMapping("sprint-input")
    public String sprintInput(
            @RequestParam(value = "id", required = false) Long id, Model model)
            throws Exception {
        if (id != null) {
            PlmSprint plmSprint = plmSprintManager.get(id);
            model.addAttribute("model", plmSprint);
        }

        List<PlmProject> plmProjects = plmProjectManager.getAll();
        model.addAttribute("plmProjects", plmProjects);

        List<PlmConfig> plmConfigs = plmConfigManager.getAll();
        model.addAttribute("plmConfigs", plmConfigs);

        return "plm/sprint-input";
    }

    /**
     * ????????????.
     */
    @RequestMapping("sprint-save")
    public String sprintSave(@ModelAttribute PlmSprint plmSprint,
            @RequestParam("configId") Long configId,
            @RequestParam("projectId") Long projectId) {
        PlmSprint dest = null;

        Long id = plmSprint.getId();

        if (id != null) {
            dest = plmSprintManager.get(id);
            beanMapper.copy(plmSprint, dest);
        } else {
            dest = plmSprint;
            dest.setStatus("active");
        }

        dest.setPlmConfig(plmConfigManager.get(configId));
        dest.setPlmProject(plmProjectManager.get(projectId));
        plmSprintManager.save(dest);

        return "redirect:/plm/sprints.do";
    }

    /**
     * ???????????????????????????.
     */
    @RequestMapping("sprint-change")
    public String sprintChange(@RequestParam("sprintId") Long sprintId,
            @RequestParam("targetSprintId") Long targetSprintId,
            @RequestParam("issueIds") List<Long> issueIds) {
        PlmSprint plmSprint = plmSprintManager.get(targetSprintId);

        for (Long issueId : issueIds) {
            PlmIssue plmIssue = plmIssueManager.get(issueId);
            plmIssue.setPlmSprint(plmSprint);
            plmIssueManager.save(plmIssue);
        }

        return "redirect:/plm/sprint.do?sprintId=" + sprintId;
    }

    @Resource
    public void setPlmProjectManager(PlmProjectManager plmProjectManager) {
        this.plmProjectManager = plmProjectManager;
    }

    @Resource
    public void setPlmVersionManager(PlmVersionManager plmVersionManager) {
        this.plmVersionManager = plmVersionManager;
    }

    @Resource
    public void setPlmIssueManager(PlmIssueManager plmIssueManager) {
        this.plmIssueManager = plmIssueManager;
    }

    @Resource
    public void setPlmCommentManager(PlmCommentManager plmCommentManager) {
        this.plmCommentManager = plmCommentManager;
    }

    @Resource
    public void setPlmCategoryManager(PlmCategoryManager plmCategoryManager) {
        this.plmCategoryManager = plmCategoryManager;
    }

    @Resource
    public void setPlmLogManager(PlmLogManager plmLogManager) {
        this.plmLogManager = plmLogManager;
    }

    @Resource
    public void setPlmSprintManager(PlmSprintManager plmSprintManager) {
        this.plmSprintManager = plmSprintManager;
    }

    @Resource
    public void setPlmConfigManager(PlmConfigManager plmConfigManager) {
        this.plmConfigManager = plmConfigManager;
    }

    @Resource
    public void setPlmComponentManager(PlmComponentManager plmComponentManager) {
        this.plmComponentManager = plmComponentManager;
    }

    @Resource
    public void setPlmLogService(PlmLogService plmLogService) {
        this.plmLogService = plmLogService;
    }

    @Resource
    public void setCurrentUserHolder(CurrentUserHolder currentUserHolder) {
        this.currentUserHolder = currentUserHolder;
    }

    @Resource
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
}
