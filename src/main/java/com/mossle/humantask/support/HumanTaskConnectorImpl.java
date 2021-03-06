package com.mossle.humantask.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.mossle.api.form.FormConnector;
import com.mossle.api.form.FormDTO;
import com.mossle.api.humantask.HumanTaskConnector;
import com.mossle.api.humantask.HumanTaskConstants;
import com.mossle.api.humantask.HumanTaskDTO;
import com.mossle.api.humantask.HumanTaskDefinition;
import com.mossle.api.humantask.ParticipantDTO;

import com.mossle.core.mapper.BeanMapper;
import com.mossle.core.page.Page;

import com.mossle.humantask.listener.HumanTaskListener;
import com.mossle.humantask.persistence.domain.TaskConfUser;
import com.mossle.humantask.persistence.domain.TaskDeadline;
import com.mossle.humantask.persistence.domain.TaskInfo;
import com.mossle.humantask.persistence.domain.TaskParticipant;
import com.mossle.humantask.persistence.manager.TaskConfUserManager;
import com.mossle.humantask.persistence.manager.TaskDeadlineManager;
import com.mossle.humantask.persistence.manager.TaskInfoManager;
import com.mossle.humantask.persistence.manager.TaskParticipantManager;

import com.mossle.spi.humantask.TaskDefinitionConnector;
import com.mossle.spi.process.InternalProcessConnector;
import com.mossle.spi.process.ProcessTaskDefinition;

import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.util.Assert;

public class HumanTaskConnectorImpl implements HumanTaskConnector {
    private Logger logger = LoggerFactory
            .getLogger(HumanTaskConnectorImpl.class);
    private JdbcTemplate jdbcTemplate;
    private TaskInfoManager taskInfoManager;
    private TaskParticipantManager taskParticipantManager;
    private TaskConfUserManager taskConfUserManager;
    private TaskDeadlineManager taskDeadlineManager;
    private InternalProcessConnector internalProcessConnector;
    private TaskDefinitionConnector taskDefinitionConnector;
    private FormConnector formConnector;
    private BeanMapper beanMapper = new BeanMapper();
    private List<HumanTaskListener> humanTaskListeners;

    // ~
    /**
     * ??????????????????.
     */
    public HumanTaskDTO createHumanTask() {
        return new HumanTaskBuilder().create();
    }

    // ~

    /**
     * ????????????.
     */
    public void removeHumanTask(String humanTaskId) {
        TaskInfo taskInfo = taskInfoManager.get(Long.parseLong(humanTaskId));
        this.removeHumanTask(taskInfo);
    }

    public void removeHumanTaskByTaskId(String taskId) {
        TaskInfo taskInfo = taskInfoManager.findUniqueBy("taskId", taskId);
        this.removeHumanTask(taskInfo);
    }

    public void removeHumanTaskByProcessInstanceId(String processInstanceId) {
        String hql = "from TaskInfo where status='active' and processInstanceId=?";
        List<TaskInfo> taskInfos = taskInfoManager.find(hql, processInstanceId);

        for (TaskInfo taskInfo : taskInfos) {
            this.removeHumanTask(taskInfo);
        }
    }

    public void removeHumanTask(TaskInfo taskInfo) {
        taskInfoManager.removeAll(taskInfo.getTaskDeadlines());
        taskInfoManager.removeAll(taskInfo.getTaskLogs());
        taskInfoManager.remove(taskInfo);
    }

    // ~
    /**
     * ????????????.
     */
    public HumanTaskDTO saveHumanTask(HumanTaskDTO humanTaskDto) {
        return this.saveHumanTask(humanTaskDto, true);
    }

    public HumanTaskDTO saveHumanTask(HumanTaskDTO humanTaskDto,
            boolean triggerListener) {
        // process first
        Long id = null;

        if (humanTaskDto.getId() != null) {
            try {
                id = Long.parseLong(humanTaskDto.getId());
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
        }

        TaskInfo taskInfo = new TaskInfo();

        if (id != null) {
            taskInfo = taskInfoManager.get(id);
        }

        beanMapper.copy(humanTaskDto, taskInfo, HumanTaskDTO.class,
                TaskInfo.class);
        logger.debug("action : {}", humanTaskDto.getAction());
        logger.debug("comment : {}", humanTaskDto.getComment());
        logger.debug("action : {}", taskInfo.getAction());
        logger.debug("comment : {}", taskInfo.getComment());

        if (humanTaskDto.getParentId() != null) {
            taskInfo.setTaskInfo(taskInfoManager.get(Long
                    .parseLong(humanTaskDto.getParentId())));
        }

        taskInfoManager.save(taskInfo);

        if (triggerListener) {
            // create
            if ((id == null) && (humanTaskListeners != null)) {
                for (HumanTaskListener humanTaskListener : humanTaskListeners) {
                    try {
                        humanTaskListener.onCreate(taskInfo);
                    } catch (Exception ex) {
                        logger.error(ex.getMessage(), ex);
                    }
                }
            }

            humanTaskDto.setAssignee(taskInfo.getAssignee());
            humanTaskDto.setOwner(taskInfo.getOwner());
        }

        humanTaskDto.setId(Long.toString(taskInfo.getId()));

        return humanTaskDto;
    }

    /**
     * ?????????????????????????????????.
     */
    public HumanTaskDTO saveHumanTaskAndProcess(HumanTaskDTO humanTaskDto) {
        return this.saveHumanTask(humanTaskDto, true);
    }

    public HumanTaskDTO findHumanTaskByTaskId(String taskId) {
        TaskInfo taskInfo = taskInfoManager.findUniqueBy("taskId", taskId);
        HumanTaskDTO humanTaskDto = new HumanTaskDTO();
        humanTaskDto = convertHumanTaskDto(taskInfo);

        return humanTaskDto;
    }

    public List<HumanTaskDTO> findHumanTasksByProcessInstanceId(
            String processInstanceId) {
        List<TaskInfo> taskInfos = taskInfoManager
                .find("from TaskInfo where processInstanceId=? order by createTime asc",
                        processInstanceId);

        return this.convertHumanTaskDtos(taskInfos);
    }

    public HumanTaskDTO findHumanTask(String humanTaskId) {
        Assert.hasText(humanTaskId, "humanTaskId????????????");

        TaskInfo taskInfo = taskInfoManager.get(Long.parseLong(humanTaskId));

        return this.convertHumanTaskDto(taskInfo);
    }

    public List<HumanTaskDTO> findSubTasks(String parentTaskId) {
        List<TaskInfo> taskInfos = taskInfoManager.findBy("taskInfo.id",
                Long.parseLong(parentTaskId));

        return this.convertHumanTaskDtos(taskInfos);
    }

    /**
     * ??????????????????.
     */
    public FormDTO findTaskForm(String humanTaskId) {
        HumanTaskDTO humanTaskDto = this.findHumanTask(humanTaskId);

        FormDTO formDto = null;

        if (humanTaskDto.getTaskId() != null) {
            // formDto = internalProcessConnector.findTaskForm(humanTaskDto
            // .getTaskId());
            com.mossle.spi.humantask.FormDTO taskFormDto = taskDefinitionConnector
                    .findForm(humanTaskDto.getCode(),
                            humanTaskDto.getProcessDefinitionId());

            if (taskFormDto == null) {
                logger.info(
                        "cannot find form by code : {}, processDefinition : {}",
                        humanTaskDto.getCode(),
                        humanTaskDto.getProcessDefinitionId());
            } else {
                formDto = new FormDTO();
                formDto.setCode(taskFormDto.getKey());

                List<String> operations = taskDefinitionConnector
                        .findOperations(humanTaskDto.getCode(),
                                humanTaskDto.getProcessDefinitionId());
                formDto.getButtons().addAll(operations);
                formDto.setActivityId(humanTaskDto.getCode());
                formDto.setProcessDefinitionId(humanTaskDto
                        .getProcessDefinitionId());
            }
        } else {
            formDto = new FormDTO();
            formDto.setCode(humanTaskDto.getForm());
            formDto.setActivityId(humanTaskDto.getCode());
            formDto.setProcessDefinitionId(humanTaskDto
                    .getProcessDefinitionId());
        }

        if (formDto == null) {
            logger.error("cannot find form : {}", humanTaskId);

            return new FormDTO();
        }

        formDto.setTaskId(humanTaskId);

        FormDTO contentFormDto = formConnector.findForm(formDto.getCode(),
                humanTaskDto.getTenantId());

        if (contentFormDto == null) {
            logger.error("cannot find form : {}", formDto.getCode());

            return formDto;
        }

        formDto.setRedirect(contentFormDto.isRedirect());
        formDto.setUrl(contentFormDto.getUrl());
        formDto.setContent(contentFormDto.getContent());

        return formDto;
    }

    /**
     * ??????????????????????????????????????????.
     */
    public List<HumanTaskDefinition> findHumanTaskDefinitions(
            String processDefinitionId) {
        List<ProcessTaskDefinition> processTaskDefinitions = internalProcessConnector
                .findTaskDefinitions(processDefinitionId);

        List<HumanTaskDefinition> humanTaskDefinitions = new ArrayList<HumanTaskDefinition>();

        for (ProcessTaskDefinition processTaskDefinition : processTaskDefinitions) {
            HumanTaskDefinition humanTaskDefinition = new HumanTaskDefinition();
            beanMapper.copy(processTaskDefinition, humanTaskDefinition);
            humanTaskDefinitions.add(humanTaskDefinition);
        }

        return humanTaskDefinitions;
    }

    /**
     * ???????????????????????????????????????????????????.
     */
    public void configTaskDefinitions(String businessKey,
            List<String> taskDefinitionKeys, List<String> taskAssignees) {
        if (taskDefinitionKeys == null) {
            return;
        }

        int index = 0;

        for (String taskDefinitionKey : taskDefinitionKeys) {
            String taskAssignee = taskAssignees.get(index++);
            String hql = "from TaskConfUser where businessKey=? and code=?";
            TaskConfUser taskConfUser = taskConfUserManager.findUnique(hql,
                    businessKey, taskDefinitionKey);

            if (taskConfUser == null) {
                taskConfUser = new TaskConfUser();
            }

            taskConfUser.setBusinessKey(businessKey);
            taskConfUser.setCode(taskDefinitionKey);
            taskConfUser.setValue(taskAssignee);
            taskConfUserManager.save(taskConfUser);
        }
    }

    /**
     * ????????????.
     */
    public void completeTask(String humanTaskId, String userId, String action,
            String comment, Map<String, Object> taskParameters) {
        Assert.hasText(humanTaskId, "humanTaskId????????????");
        logger.info("completeTask humanTaskId : {}, userId : {}, comment: {}",
                humanTaskId, userId, comment);

        HumanTaskDTO humanTaskDto = this.findHumanTask(humanTaskId);

        if (humanTaskDto == null) {
            throw new IllegalStateException("???????????????");
        }

        humanTaskDto.setStatus("complete");
        humanTaskDto.setCompleteTime(new Date());
        humanTaskDto.setAction("??????");

        if (StringUtils.isNotBlank(action)) {
            humanTaskDto.setAction(action);
        }

        if (StringUtils.isNotBlank(comment)) {
            humanTaskDto.setComment(comment);
        }

        Long longTaskId = Long.parseLong(humanTaskDto.getId());
        List<TaskDeadline> taskDeadlines = taskDeadlineManager.find(
                "from TaskDeadline where taskInfo.id=?", longTaskId);

        for (TaskDeadline taskDeadline : taskDeadlines) {
            taskDeadlineManager.remove(taskDeadline);
        }

        // ??????????????????
        if ("copy".equals(humanTaskDto.getCategory())) {
            humanTaskDto.setStatus("complete");
            humanTaskDto.setCompleteTime(new Date());
            humanTaskDto.setAction("??????");
            this.saveHumanTask(humanTaskDto, false);

            return;
        }

        // ??????startEvent??????
        if ("startEvent".equals(humanTaskDto.getCategory())) {
            humanTaskDto.setStatus("complete");
            humanTaskDto.setAction("??????");
            humanTaskDto.setCompleteTime(new Date());
            this.saveHumanTask(humanTaskDto, false);
            internalProcessConnector.signalExecution(humanTaskDto
                    .getExecutionId());

            return;
        }

        logger.debug("{}", humanTaskDto.getDelegateStatus());

        // ??????????????????
        if ("pending".equals(humanTaskDto.getDelegateStatus())) {
            humanTaskDto.setStatus("active");
            humanTaskDto.setDelegateStatus("resolved");
            humanTaskDto.setAssignee(humanTaskDto.getOwner());
            humanTaskDto.setAction("??????");
            this.saveHumanTask(humanTaskDto, false);
            internalProcessConnector.resolveTask(humanTaskDto.getTaskId());

            return;
        }

        // ????????????????????????
        if ("pendingCreate".equals(humanTaskDto.getDelegateStatus())) {
            humanTaskDto.setCompleteTime(new Date());
            humanTaskDto.setDelegateStatus("resolved");
            humanTaskDto.setStatus("complete");
            humanTaskDto.setAction("??????");
            this.saveHumanTask(humanTaskDto, false);

            if (humanTaskDto.getParentId() != null) {
                HumanTaskDTO targetHumanTaskDto = this
                        .findHumanTask(humanTaskDto.getParentId());
                targetHumanTaskDto.setStatus("active");

                if (targetHumanTaskDto.getParentId() == null) {
                    targetHumanTaskDto.setDelegateStatus("resolved");
                }

                this.saveHumanTask(targetHumanTaskDto, false);
            }

            return;
        }

        this.saveHumanTask(humanTaskDto, false);

        // ????????????
        if ("vote".equals(humanTaskDto.getCatalog())
                && (humanTaskDto.getParentId() != null)) {
            HumanTaskDTO parentTask = this.findHumanTask(humanTaskDto
                    .getParentId());
            boolean completed = true;

            for (HumanTaskDTO childTask : parentTask.getChildren()) {
                if (!"complete".equals(childTask.getStatus())) {
                    completed = false;

                    break;
                }
            }

            if (completed) {
                parentTask.setAssignee(parentTask.getOwner());
                parentTask.setOwner("");
                parentTask.setStatus("complete");
                parentTask.setCompleteTime(new Date());
                parentTask.setAction("??????");
                this.saveHumanTask(parentTask, false);
                internalProcessConnector.completeTask(humanTaskDto.getTaskId(),
                        userId, taskParameters);
            }
        } else {
            internalProcessConnector.completeTask(humanTaskDto.getTaskId(),
                    userId, taskParameters);
        }

        if (humanTaskListeners != null) {
            Long id = null;

            try {
                id = Long.parseLong(humanTaskDto.getId());
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }

            if (id == null) {
                return;
            }

            TaskInfo taskInfo = taskInfoManager.get(id);

            for (HumanTaskListener humanTaskListener : humanTaskListeners) {
                try {
                    humanTaskListener.onComplete(taskInfo);
                } catch (Exception ex) {
                    logger.error(ex.getMessage(), ex);
                }
            }
        }
    }

    /**
     * ????????????.
     */
    public Page findPersonalTasks(String userId, String tenantId, int pageNo,
            int pageSize) {
        String hql = "from TaskInfo where assignee=? and tenantId=? and status='active' order by id desc";
        Page page = taskInfoManager.pagedQuery(hql, pageNo, pageSize, userId,
                tenantId);
        List<TaskInfo> taskInfos = (List<TaskInfo>) page.getResult();
        List<HumanTaskDTO> humanTaskDtos = this.convertHumanTaskDtos(taskInfos);
        page.setResult(humanTaskDtos);

        return page;
    }

    /**
     * ????????????.
     */
    public Page findFinishedTasks(String userId, String tenantId, int pageNo,
            int pageSize) {
        String hql = "from TaskInfo where assignee=? and tenantId=? and status='complete' order by id desc";
        Page page = taskInfoManager.pagedQuery(hql, pageNo, pageSize, userId,
                tenantId);
        List<TaskInfo> taskInfos = (List<TaskInfo>) page.getResult();
        List<HumanTaskDTO> humanTaskDtos = this.convertHumanTaskDtos(taskInfos);
        page.setResult(humanTaskDtos);

        return page;
    }

    /**
     * ????????????.
     */
    public Page findGroupTasks(String userId, String tenantId, int pageNo,
            int pageSize) {
        List<String> partyIds = new ArrayList<String>();
        partyIds.addAll(this.findGroupIds(userId));
        partyIds.addAll(this.findUserIds(userId));

        logger.debug("party ids : {}", partyIds);

        if (partyIds.isEmpty()) {
            return new Page();
        }

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("partyIds", partyIds);
        map.put("tenantId", tenantId);

        String hql = "select distinct t from TaskInfo t join t.taskParticipants p "
                + "with p.ref in (:partyIds) where t.tenantId=:tenantId and t.assignee=null and t.status='active' "
                + "order by id desc";
        Page page = taskInfoManager.pagedQuery(hql, pageNo, pageSize, map);

        // List<PropertyFilter> propertyFilters = PropertyFilter
        // .buildFromMap(parameterMap);
        // propertyFilters.add(new PropertyFilter("EQS_status", "active"));
        // propertyFilters.add(new PropertyFilter("INLS_assignee", null));
        List<TaskInfo> taskInfos = (List<TaskInfo>) page.getResult();
        List<HumanTaskDTO> humanTaskDtos = this.convertHumanTaskDtos(taskInfos);
        page.setResult(humanTaskDtos);

        return page;
    }

    /**
     * ????????????.
     */
    public Page findDelegateTasks(String userId, String tenantId, int pageNo,
            int pageSize) {
        String hql = "from TaskInfo where owner=? and tenantId=? and status='active' order by id desc";
        Page page = taskInfoManager.pagedQuery(hql, pageNo, pageSize, userId,
                tenantId);
        List<TaskInfo> taskInfos = (List<TaskInfo>) page.getResult();
        List<HumanTaskDTO> humanTaskDtos = this.convertHumanTaskDtos(taskInfos);
        page.setResult(humanTaskDtos);

        return page;
    }

    /**
     * ????????????.
     */
    public void claimTask(String humanTaskId, String userId) {
        TaskInfo taskInfo = taskInfoManager.get(Integer.parseInt(humanTaskId));

        if (taskInfo.getAssignee() != null) {
            throw new IllegalStateException("task " + humanTaskId
                    + " already be claimed by " + taskInfo.getAssignee());
        }

        taskInfo.setAssignee(userId);
        taskInfoManager.save(taskInfo);
    }

    /**
     * ????????????.
     */
    public void releaseTask(String humanTaskId, String comment) {
        TaskInfo taskInfo = taskInfoManager.get(Integer.parseInt(humanTaskId));

        taskInfo.setAssignee(null);
        taskInfoManager.save(taskInfo);
    }

    /**
     * ??????.
     */
    public void transfer(String humanTaskId, String userId, String comment) {
        HumanTaskDTO humanTaskDto = this.findHumanTask(humanTaskId);
        humanTaskDto.setOwner(humanTaskDto.getAssignee());
        humanTaskDto.setAssignee(userId);
        this.saveHumanTask(humanTaskDto, false);

        internalProcessConnector.transfer(humanTaskDto.getTaskId(),
                humanTaskDto.getAssignee(), humanTaskDto.getOwner());
    }

    /**
     * ????????????.
     */
    public void cancel(String humanTaskId, String userId, String comment) {
        HumanTaskDTO humanTaskDto = this.findHumanTask(humanTaskId);
        humanTaskDto.setAssignee(humanTaskDto.getOwner());
        humanTaskDto.setOwner("");
        this.saveHumanTask(humanTaskDto, false);
    }

    /**
     * ????????????????????????????????????.
     */
    public void rollbackActivity(String humanTaskId, String activityId,
            String comment) {
        HumanTaskDTO humanTaskDto = findHumanTask(humanTaskId);

        if (humanTaskDto == null) {
            throw new IllegalStateException("???????????????");
        }

        String taskId = humanTaskDto.getTaskId();
        internalProcessConnector.rollback(taskId, activityId, null);
    }

    /**
     * ???????????????????????????????????????.
     */
    public void rollbackActivityLast(String humanTaskId, String activityId,
            String comment) {
        HumanTaskDTO humanTaskDto = findHumanTask(humanTaskId);

        if (humanTaskDto == null) {
            throw new IllegalStateException("???????????????");
        }

        String taskId = humanTaskDto.getTaskId();
        internalProcessConnector.rollbackAuto(taskId, activityId);
    }

    /**
     * ???????????????????????????????????????.
     */
    public void rollbackActivityAssignee(String humanTaskId, String activityId,
            String userId, String comment) {
        HumanTaskDTO humanTaskDto = findHumanTask(humanTaskId);

        if (humanTaskDto == null) {
            throw new IllegalStateException("???????????????");
        }

        String taskId = humanTaskDto.getTaskId();
        internalProcessConnector.rollback(taskId, activityId, userId);
    }

    /**
     * ????????????????????????????????????.
     */
    public void rollbackPrevious(String humanTaskId, String comment) {
        HumanTaskDTO humanTaskDto = findHumanTask(humanTaskId);

        if (humanTaskDto == null) {
            throw new IllegalStateException("???????????????");
        }

        String taskId = humanTaskDto.getTaskId();
        internalProcessConnector.rollback(taskId, null, null);
    }

    /**
     * ???????????????????????????????????????.
     */
    public void rollbackPreviousLast(String humanTaskId, String comment) {
        HumanTaskDTO humanTaskDto = findHumanTask(humanTaskId);

        if (humanTaskDto == null) {
            throw new IllegalStateException("???????????????");
        }

        String taskId = humanTaskDto.getTaskId();
        internalProcessConnector.rollbackAuto(taskId, null);
    }

    /**
     * ???????????????????????????????????????.
     */
    public void rollbackPreviousAssignee(String humanTaskId, String userId,
            String comment) {
        HumanTaskDTO humanTaskDto = findHumanTask(humanTaskId);

        if (humanTaskDto == null) {
            throw new IllegalStateException("???????????????");
        }

        String taskId = humanTaskDto.getTaskId();
        internalProcessConnector.rollback(taskId, null, userId);
    }

    /**
     * ???????????????????????????????????????.
     */
    public void rollbackStart(String humanTaskId, String comment) {
        HumanTaskDTO humanTaskDto = findHumanTask(humanTaskId);

        if (humanTaskDto == null) {
            throw new IllegalStateException("???????????????");
        }

        String taskId = humanTaskDto.getTaskId();
        String processDefinitionId = humanTaskDto.getProcessDefinitionId();
        String processInstanceId = humanTaskDto.getProcessInstanceId();
        String activityId = this.internalProcessConnector
                .findInitialActivityId(processDefinitionId);
        String initiator = this.internalProcessConnector
                .findInitiator(processInstanceId);
        internalProcessConnector.rollback(taskId, activityId, initiator);
    }

    /**
     * ????????????????????????.
     */
    public void rollbackInitiator(String humanTaskId, String comment) {
        HumanTaskDTO humanTaskDto = findHumanTask(humanTaskId);

        if (humanTaskDto == null) {
            throw new IllegalStateException("???????????????");
        }

        humanTaskDto.setAction("?????????????????????");
        humanTaskDto.setComment(comment);
        this.saveHumanTask(humanTaskDto, false);

        String taskId = humanTaskDto.getTaskId();
        String processDefinitionId = humanTaskDto.getProcessDefinitionId();
        String processInstanceId = humanTaskDto.getProcessInstanceId();
        String initiator = this.internalProcessConnector
                .findInitiator(processInstanceId);
        String activityId = this.internalProcessConnector
                .findFirstUserTaskActivityId(processDefinitionId, initiator);
        this.internalProcessConnector.rollback(taskId, activityId, initiator);
        // event
        this.internalProcessConnector.fireEvent("reject",
            humanTaskDto.getBusinessKey(), humanTaskDto.getAssignee(), humanTaskDto.getCode(), humanTaskDto.getName());
    }

    /**
     * ??????.
     */
    public void withdraw(String humanTaskId, String comment) {
        HumanTaskDTO humanTaskDto = findHumanTask(humanTaskId);

        if (humanTaskDto == null) {
            throw new IllegalStateException("???????????????");
        }

        internalProcessConnector.withdrawTask(humanTaskDto.getTaskId());
    }

    /**
     * ??????.
     */
    public void delegateTask(String humanTaskId, String userId, String comment) {
        HumanTaskDTO humanTaskDto = this.findHumanTask(humanTaskId);
        humanTaskDto.setOwner(humanTaskDto.getAssignee());
        humanTaskDto.setAssignee(userId);
        humanTaskDto.setDelegateStatus("pending");
        this.saveHumanTask(humanTaskDto, false);
        internalProcessConnector.delegateTask(humanTaskDto.getTaskId(), userId);
    }

    /**
     * ???????????????.
     */
    public void delegateTaskCreate(String humanTaskId, String userId,
            String comment) {
        HumanTaskDTO humanTaskDto = this.findHumanTask(humanTaskId);
        humanTaskDto.setDelegateStatus("pendingCreate");
        humanTaskDto.setStatus("pending");
        this.saveHumanTask(humanTaskDto, false);

        HumanTaskDTO targetHumanTaskDto = this.createHumanTask();
        beanMapper.copy(humanTaskDto, targetHumanTaskDto);
        targetHumanTaskDto.setStatus("active");
        targetHumanTaskDto.setParentId(humanTaskDto.getId());
        targetHumanTaskDto.setId(null);
        targetHumanTaskDto.setOwner(humanTaskDto.getAssignee());
        targetHumanTaskDto.setAssignee(userId);

        this.saveHumanTask(targetHumanTaskDto, false);

        if (humanTaskDto.getParentId() == null) {
            humanTaskDto.setOwner(humanTaskDto.getAssignee());
            humanTaskDto.setAssignee(userId);
            // ??????????????????????????????bpm?????????
            internalProcessConnector.delegateTask(humanTaskDto.getTaskId(),
                    userId);
            humanTaskDto.setAssignee(humanTaskDto.getOwner());
            humanTaskDto.setOwner(null);
            this.saveHumanTask(humanTaskDto, false);
        }
    }

    /**
     * ??????.
     */
    public void communicate(String humanTaskId, String userId, String comment) {
        HumanTaskDTO humanTaskDto = this.findHumanTask(humanTaskId);
        HumanTaskDTO target = new HumanTaskDTO();
        beanMapper.copy(humanTaskDto, target);
        target.setId(null);
        target.setCatalog(HumanTaskConstants.CATALOG_COMMUNICATE);
        target.setAssignee(userId);
        target.setParentId(humanTaskId);
        target.setMessage(comment);

        this.saveHumanTask(target, false);
    }

    /**
     * ??????.
     */
    public void callback(String humanTaskId, String userId, String comment) {
        HumanTaskDTO humanTaskDto = this.findHumanTask(humanTaskId);
        humanTaskDto.setStatus("complete");
        humanTaskDto.setCompleteTime(new Date());
        humanTaskDto.setAction("??????");
        humanTaskDto.setComment(comment);
        this.saveHumanTask(humanTaskDto, false);
    }

    /**
     * ??????.
     */
    public void skip(String humanTaskId, String userId, String comment) {
        HumanTaskDTO humanTaskDto = this.findHumanTask(humanTaskId);
        humanTaskDto.setStatus("complete");
        humanTaskDto.setCompleteTime(new Date());
        humanTaskDto.setAction("??????");
        humanTaskDto.setComment(comment);
        humanTaskDto.setOwner(humanTaskDto.getAssignee());
        humanTaskDto.setAssignee(userId);
        this.saveHumanTask(humanTaskDto, false);
        internalProcessConnector.completeTask(humanTaskDto.getTaskId(), userId,
                Collections.<String, Object> emptyMap());
    }

    public List<String> findGroupIds(String userId) {
        String groupSql = "select ps.PARENT_ENTITY_ID as ID from PARTY_STRUCT ps,PARTY_ENTITY child,PARTY_TYPE type"
                + " where ps.CHILD_ENTITY_ID=child.ID and child.TYPE_ID=type.ID and type.TYPE='1' and child.REF=?";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(groupSql,
                userId);
        List<String> partyIds = new ArrayList<String>();

        for (Map<String, Object> map : list) {
            partyIds.add(map.get("ID").toString());
        }

        return partyIds;
    }

    public List<String> findUserIds(String userId) {
        String userSql = "select pe.ID as ID from PARTY_ENTITY pe,PARTY_TYPE type"
                + " where pe.TYPE_ID=type.ID and type.TYPE='1' and pe.REF=?";
        List<Map<String, Object>> list = jdbcTemplate.queryForList(userSql,
                userId);
        List<String> partyIds = new ArrayList<String>();

        for (Map<String, Object> map : list) {
            partyIds.add(map.get("ID").toString());
        }

        return partyIds;
    }

    // ~ ==================================================
    public List<HumanTaskDTO> convertHumanTaskDtos(
            Collection<TaskInfo> taskInfos) {
        List<HumanTaskDTO> humanTaskDtos = new ArrayList<HumanTaskDTO>();

        for (TaskInfo taskInfo : taskInfos) {
            humanTaskDtos.add(convertHumanTaskDto(taskInfo));
        }

        return humanTaskDtos;
    }

    public HumanTaskDTO convertHumanTaskDto(TaskInfo taskInfo) {
        if (taskInfo == null) {
            return null;
        }

        HumanTaskDTO humanTaskDto = new HumanTaskDTO();
        beanMapper.copy(taskInfo, humanTaskDto);

        if (taskInfo.getTaskInfo() != null) {
            humanTaskDto.setParentId(Long.toString(taskInfo.getTaskInfo()
                    .getId()));
        }

        if (!taskInfo.getTaskInfos().isEmpty()) {
            List<HumanTaskDTO> children = this.convertHumanTaskDtos(taskInfo
                    .getTaskInfos());
            humanTaskDto.setChildren(children);
        }

        return humanTaskDto;
    }

    public void saveParticipant(ParticipantDTO participantDto) {
        TaskParticipant taskParticipant = new TaskParticipant();
        taskParticipant.setRef(participantDto.getCode());
        taskParticipant.setType(participantDto.getType());
        taskParticipant.setTaskInfo(taskInfoManager.get(Long
                .parseLong(participantDto.getHumanTaskId())));
        taskParticipantManager.save(taskParticipant);
    }

    public long findPersonalTaskCount(String userId, String tenantId) {
        String hql = "select count(*) from TaskInfo where assignee=? and tenantId=? and status='active'";

        return taskInfoManager.getCount(hql, userId, tenantId);
    }

    public long findGroupTaskCount(String userId, String tenantId) {
        List<String> partyIds = new ArrayList<String>();
        partyIds.addAll(this.findGroupIds(userId));
        partyIds.addAll(this.findUserIds(userId));

        logger.debug("party ids : {}", partyIds);

        if (partyIds.isEmpty()) {
            return 0L;
        }

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("partyIds", partyIds);
        map.put("tenantId", tenantId);

        String hql = "select count(distinct t) from TaskInfo t join t.taskParticipants p "
                + "with p.ref in (:partyIds) where t.tenantId=:tenantId and t.assignee=null and t.status='active' ";

        return taskInfoManager.getCount(hql, map);
    }

    // ~ ==================================================
    @Resource
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Resource
    public void setTaskInfoManager(TaskInfoManager taskInfoManager) {
        this.taskInfoManager = taskInfoManager;
    }

    @Resource
    public void setTaskParticipantManager(
            TaskParticipantManager taskParticipantManager) {
        this.taskParticipantManager = taskParticipantManager;
    }

    @Resource
    public void setTaskConfUserManager(TaskConfUserManager taskConfUserManager) {
        this.taskConfUserManager = taskConfUserManager;
    }

    @Resource
    public void setTaskDeadlineManager(TaskDeadlineManager taskDeadlineManager) {
        this.taskDeadlineManager = taskDeadlineManager;
    }

    @Resource
    public void setInternalProcessConnector(
            InternalProcessConnector internalProcessConnector) {
        this.internalProcessConnector = internalProcessConnector;
    }

    @Resource
    public void setTaskDefinitionConnector(
            TaskDefinitionConnector taskDefinitionConnector) {
        this.taskDefinitionConnector = taskDefinitionConnector;
    }

    @Resource
    public void setFormConnector(FormConnector formConnector) {
        this.formConnector = formConnector;
    }

    public void setHumanTaskListeners(List<HumanTaskListener> humanTaskListeners) {
        this.humanTaskListeners = humanTaskListeners;
    }
}
