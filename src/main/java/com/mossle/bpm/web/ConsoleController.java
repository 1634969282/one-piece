package com.mossle.bpm.web;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import javax.servlet.http.HttpServletResponse;

import com.mossle.api.process.ProcessConnector;
import com.mossle.api.tenant.TenantHolder;

import com.mossle.bpm.cmd.ChangeSubTaskCmd;
import com.mossle.bpm.cmd.JumpCmd;
import com.mossle.bpm.cmd.ListActivityCmd;
import com.mossle.bpm.cmd.MigrateCmd;
import com.mossle.bpm.cmd.ProcessDefinitionDiagramCmd;
import com.mossle.bpm.cmd.ReOpenProcessCmd;
import com.mossle.bpm.cmd.SyncProcessCmd;
import com.mossle.bpm.cmd.UpdateProcessCmd;
import com.mossle.bpm.persistence.domain.BpmConfBase;
import com.mossle.bpm.persistence.domain.BpmConfCountersign;
import com.mossle.bpm.persistence.domain.BpmConfForm;
import com.mossle.bpm.persistence.domain.BpmConfListener;
import com.mossle.bpm.persistence.domain.BpmConfNode;
import com.mossle.bpm.persistence.domain.BpmConfNotice;
import com.mossle.bpm.persistence.domain.BpmConfOperation;
import com.mossle.bpm.persistence.domain.BpmConfRule;
import com.mossle.bpm.persistence.domain.BpmConfUser;
import com.mossle.bpm.persistence.domain.BpmProcess;
import com.mossle.bpm.persistence.manager.BpmConfBaseManager;
import com.mossle.bpm.persistence.manager.BpmProcessManager;

import com.mossle.core.page.Page;
import com.mossle.core.util.IoUtils;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;

import org.apache.commons.io.IOUtils;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * ???????????????.
 */
@Controller
@RequestMapping("bpm")
public class ConsoleController {
    private ProcessEngine processEngine;
    private ProcessConnector processConnector;
    private BpmConfBaseManager bpmConfBaseManager;
    private BpmProcessManager bpmProcessManager;
    private TenantHolder tenantHolder;

    /**
     * ????????????.
     */
    @RequestMapping("console-listDeployments")
    public String listDeployments(@ModelAttribute Page page, Model model) {
        String tenantId = tenantHolder.getTenantId();
        page = processConnector.findDeployments(tenantId, page);
        model.addAttribute("page", page);

        return "bpm/console-listDeployments";
    }

    /**
     * ?????????????????????????????????.
     */
    @RequestMapping("console-listDeploymentResourceNames")
    public String listDeploymentResourceNames(
            @RequestParam("deploymentId") String deploymentId, Model model) {
        RepositoryService repositoryService = processEngine
                .getRepositoryService();
        List<String> deploymentResourceNames = repositoryService
                .getDeploymentResourceNames(deploymentId);
        model.addAttribute("deploymentResourceNames", deploymentResourceNames);

        return "bpm/console-listDeploymentResourceNames";
    }

    /**
     * ????????????.
     */
    @RequestMapping("console-removeDeployment")
    public String removeDeployment(
            @RequestParam("deploymentId") String deploymentId) {
        RepositoryService repositoryService = processEngine
                .getRepositoryService();
        List<ProcessDefinition> processDefinitions = repositoryService
                .createProcessDefinitionQuery().deploymentId(deploymentId)
                .list();

        for (ProcessDefinition processDefinition : processDefinitions) {
            String hql = "from BpmConfBase where processDefinitionId=? or (processDefinitionKey=? and processDefinitionVersion=?)";
            List<BpmConfBase> bpmConfBases = bpmConfBaseManager.find(hql,
                    processDefinition.getId(), processDefinition.getKey(),
                    processDefinition.getVersion());

            for (BpmConfBase bpmConfBase : bpmConfBases) {
                for (BpmConfNode bpmConfNode : bpmConfBase.getBpmConfNodes()) {
                    for (BpmConfCountersign bpmConfCountersign : bpmConfNode
                            .getBpmConfCountersigns()) {
                        bpmConfBaseManager.remove(bpmConfCountersign);
                    }

                    for (BpmConfForm bpmConfForm : bpmConfNode
                            .getBpmConfForms()) {
                        bpmConfBaseManager.remove(bpmConfForm);
                    }

                    for (BpmConfListener bpmConfListener : bpmConfNode
                            .getBpmConfListeners()) {
                        bpmConfBaseManager.remove(bpmConfListener);
                    }

                    for (BpmConfNotice bpmConfNotice : bpmConfNode
                            .getBpmConfNotices()) {
                        bpmConfBaseManager.remove(bpmConfNotice);
                    }

                    for (BpmConfOperation bpmConfOperation : bpmConfNode
                            .getBpmConfOperations()) {
                        bpmConfBaseManager.remove(bpmConfOperation);
                    }

                    for (BpmConfRule bpmConfRule : bpmConfNode
                            .getBpmConfRules()) {
                        bpmConfBaseManager.remove(bpmConfRule);
                    }

                    for (BpmConfUser bpmConfUser : bpmConfNode
                            .getBpmConfUsers()) {
                        bpmConfBaseManager.remove(bpmConfUser);
                    }

                    bpmConfBaseManager.remove(bpmConfNode);
                }

                for (BpmProcess bpmProcess : bpmConfBase.getBpmProcesses()) {
                    bpmProcessManager.remove(bpmProcess);
                }

                bpmConfBaseManager.remove(bpmConfBase);
            }
        }

        repositoryService.deleteDeployment(deploymentId, true);

        return "redirect:/bpm/console-listDeployments.do";
    }

    /**
     * ????????????.
     */
    @RequestMapping("console-create")
    public String create() {
        return "bpm/console-create";
    }

    /**
     * ????????????????????????.
     */
    @RequestMapping("console-process-input")
    public String processInput() {
        return "bpm/console-process-input";
    }

    /**
     * ????????????????????????.
     */
    @RequestMapping("console-process-upload")
    public String processUpload(@RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes) throws Exception {
        String tenantId = tenantHolder.getTenantId();
        String fileName = file.getOriginalFilename();
        Deployment deployment = processEngine.getRepositoryService()
                .createDeployment()
                .addInputStream(fileName, file.getInputStream())
                .tenantId(tenantId).deploy();
        List<ProcessDefinition> processDefinitions = processEngine
                .getRepositoryService().createProcessDefinitionQuery()
                .deploymentId(deployment.getId()).list();

        for (ProcessDefinition processDefinition : processDefinitions) {
            processEngine.getManagementService().executeCommand(
                    new SyncProcessCmd(processDefinition.getId()));
        }

        return "redirect:/bpm/console-listProcessDefinitions.do";
    }

    /**
     * ????????????.
     */
    @RequestMapping("console-deploy")
    public String deploy(@RequestParam("xml") String xml) throws Exception {
        RepositoryService repositoryService = processEngine
                .getRepositoryService();
        ByteArrayInputStream bais = new ByteArrayInputStream(
                xml.getBytes("UTF-8"));
        Deployment deployment = repositoryService.createDeployment()
                .addInputStream("process.bpmn20.xml", bais).deploy();
        List<ProcessDefinition> processDefinitions = repositoryService
                .createProcessDefinitionQuery()
                .deploymentId(deployment.getId()).list();

        for (ProcessDefinition processDefinition : processDefinitions) {
            processEngine.getManagementService().executeCommand(
                    new SyncProcessCmd(processDefinition.getId()));
        }

        return "redirect:/bpm/console-listProcessDefinitions.do";
    }

    /**
     * ????????????????????????.
     */
    @RequestMapping("console-listProcessDefinitions")
    public String listProcessDefinitions(@ModelAttribute Page page, Model model) {
        String tenantId = tenantHolder.getTenantId();
        page = processConnector.findProcessDefinitions(tenantId, page);
        model.addAttribute("page", page);

        return "bpm/console-listProcessDefinitions";
    }

    /**
     * ??????????????????.
     */
    @RequestMapping("console-suspendProcessDefinition")
    public String suspendProcessDefinition(
            @RequestParam("processDefinitionId") String processDefinitionId) {
        RepositoryService repositoryService = processEngine
                .getRepositoryService();
        repositoryService.suspendProcessDefinitionById(processDefinitionId,
                true, null);

        return "redirect:/bpm/console-listProcessDefinitions.do";
    }

    /**
     * ??????????????????.
     */
    @RequestMapping("console-activeProcessDefinition")
    public String activeProcessDefinition(
            @RequestParam("processDefinitionId") String processDefinitionId) {
        RepositoryService repositoryService = processEngine
                .getRepositoryService();

        repositoryService.activateProcessDefinitionById(processDefinitionId,
                true, null);

        return "redirect:/bpm/console-listProcessDefinitions.do";
    }

    /**
     * ????????????????????????.
     */
    @RequestMapping("console-graphProcessDefinition")
    public void graphProcessDefinition(
            @RequestParam("processDefinitionId") String processDefinitionId,
            HttpServletResponse response) throws Exception {
        Command<InputStream> cmd = new ProcessDefinitionDiagramCmd(
                processDefinitionId);

        InputStream is = processEngine.getManagementService().executeCommand(
                cmd);
        response.setContentType("image/png");

        IOUtils.copy(is, response.getOutputStream());
    }

    /**
     * ?????????????????????xml.
     */
    @RequestMapping("console-viewXml")
    public void viewXml(
            @RequestParam("processDefinitionId") String processDefinitionId,
            HttpServletResponse response) throws Exception {
        RepositoryService repositoryService = processEngine
                .getRepositoryService();
        ProcessDefinition processDefinition = repositoryService
                .createProcessDefinitionQuery()
                .processDefinitionId(processDefinitionId).singleResult();
        String resourceName = processDefinition.getResourceName();
        InputStream resourceAsStream = repositoryService.getResourceAsStream(
                processDefinition.getDeploymentId(), resourceName);
        response.setContentType("text/xml;charset=UTF-8");
        IOUtils.copy(resourceAsStream, response.getOutputStream());
    }

    /**
     * ????????????????????????.
     */
    @RequestMapping("console-listProcessInstances")
    public String listProcessInstances(@ModelAttribute Page page, Model model) {
        String tenantId = tenantHolder.getTenantId();
        page = processConnector.findProcessInstances(tenantId, page);
        model.addAttribute("page", page);

        return "bpm/console-listProcessInstances";
    }

    /**
     * ??????????????????.
     */
    @RequestMapping("console-removeProcessInstance")
    public String removeProcessInstance(
            @RequestParam("processInstanceId") String processInstanceId,
            @RequestParam("deleteReason") String deleteReason) {
        RuntimeService runtimeService = processEngine.getRuntimeService();
        runtimeService.deleteProcessInstance(processInstanceId, deleteReason);

        return "redirect:/bpm/console-listProcessInstances.do";
    }

    /**
     * ??????????????????.
     */
    @RequestMapping("console-suspendProcessInstance")
    public String suspendProcessInstance(
            @RequestParam("processInstanceId") String processInstanceId) {
        RuntimeService runtimeService = processEngine.getRuntimeService();
        runtimeService.suspendProcessInstanceById(processInstanceId);

        return "redirect:/bpm/console-listProcessInstances.do";
    }

    /**
     * ??????????????????.
     */
    @RequestMapping("console-activeProcessInstance")
    public String activeProcessInstance(
            @RequestParam("processInstanceId") String processInstanceId) {
        RuntimeService runtimeService = processEngine.getRuntimeService();
        runtimeService.activateProcessInstanceById(processInstanceId);

        return "redirect:/bpm/console-listProcessInstances.do";
    }

    /**
     * ?????????????????????????????????.
     */
    @RequestMapping("console-deleteProcessInstance")
    public String deleteProcessInstance(@RequestParam("id") String id) {
        processEngine.getRuntimeService().deleteProcessInstance(id, "delete");
        processEngine.getHistoryService().deleteHistoricProcessInstance(id);

        return "redirect:/bpm/console-listProcessInstances.do";
    }

    /**
     * ??????????????????.
     */
    @RequestMapping("console-listTasks")
    public String listTasks(@ModelAttribute Page page, Model model) {
        String tenantId = tenantHolder.getTenantId();
        page = processConnector.findTasks(tenantId, page);
        model.addAttribute("page", page);

        return "bpm/console-listTasks";
    }

    // The task cannot be deleted because is part of a running process
    /**
     * ????????????????????????.
     */
    @RequestMapping("console-listHistoricProcessInstances")
    public String listHistoricProcessInstances(@ModelAttribute Page page,
            Model model) {
        String tenantId = tenantHolder.getTenantId();
        page = processConnector.findHistoricProcessInstances(tenantId, page);

        model.addAttribute("page", page);

        return "bpm/console-listHistoricProcessInstances";
    }

    /**
     * ????????????????????????.
     */
    @RequestMapping("console-listHistoricActivityInstances")
    public String listHistoricActivityInstances(@ModelAttribute Page page,
            Model model) {
        String tenantId = tenantHolder.getTenantId();
        page = processConnector.findHistoricActivityInstances(tenantId, page);
        model.addAttribute("page", page);

        return "bpm/console-listHistoricActivityInstances";
    }

    /**
     * ??????????????????.
     */
    @RequestMapping("console-listHistoricTasks")
    public String listHistoricTasks(@ModelAttribute Page page, Model model) {
        String tenantId = tenantHolder.getTenantId();
        page = processConnector.findHistoricTaskInstances(tenantId, page);
        model.addAttribute("page", page);

        return "bpm/console-listHistoricTasks";
    }

    // ~ ======================================================================
    /**
     * ???????????????????????????????????????????????????.
     */
    @RequestMapping("console-prepareJump")
    public String prepareJump(@RequestParam("executionId") String executionId,
            Model model) {
        Command<Map<String, String>> cmd = new ListActivityCmd(executionId);

        Map activityMap = processEngine.getManagementService().executeCommand(
                cmd);

        model.addAttribute("activityMap", activityMap);

        return "bpm/console-prepareJump";
    }

    /**
     * ?????????.
     */
    @RequestMapping("console-jump")
    public String jump(@RequestParam("executionId") String executionId,
            @RequestParam("activityId") String activityId) {
        Command<Object> cmd = new JumpCmd(executionId, activityId);

        processEngine.getManagementService().executeCommand(cmd);

        return "redirect:/bpm/console-listTasks.do";
    }

    /**
     * ???????????????????????????xml.
     */
    @RequestMapping("console-beforeUpdateProcess")
    public String beforeUpdateProcess(
            @RequestParam("processDefinitionId") String processDefinitionId,
            Model model) throws Exception {
        ProcessDefinition processDefinition = processEngine
                .getRepositoryService().getProcessDefinition(
                        processDefinitionId);
        InputStream is = processEngine.getRepositoryService()
                .getResourceAsStream(processDefinition.getDeploymentId(),
                        processDefinition.getResourceName());
        String xml = IoUtils.readString(is);

        model.addAttribute("xml", xml);

        return "bpm/console-beforeUpdateProcess";
    }

    /**
     * ?????????????????????????????????.
     */
    @RequestMapping("console-doUpdateProcess")
    public String doUpdateProcess(
            @RequestParam("processDefinitionId") String processDefinitionId,
            @RequestParam("xml") String xml) throws Exception {
        byte[] bytes = xml.getBytes("utf-8");
        UpdateProcessCmd updateProcessCmd = new UpdateProcessCmd(
                processDefinitionId, bytes);
        processEngine.getManagementService().executeCommand(updateProcessCmd);

        return "redirect:/bpm/console-listProcessInstances.do";
    }

    /**
     * ??????????????????.
     */
    @RequestMapping("console-migrateInput")
    public String migrateInput(
            @RequestParam("processInstanceId") String processInstanceId,
            Model model) {
        model.addAttribute("processInstanceId", processInstanceId);
        model.addAttribute("processDefinitions", processEngine
                .getRepositoryService().createProcessDefinitionQuery().list());

        return "bpm/console-migrateInput";
    }

    /**
     * ??????????????????.
     */
    @RequestMapping("console-migrateSave")
    public String migrateInput(
            @RequestParam("processInstanceId") String processInstanceId,
            @RequestParam("processDefinitionId") String processDefinitionId) {
        processEngine.getManagementService().executeCommand(
                new MigrateCmd(processInstanceId, processDefinitionId));

        return "redirect:/bpm/console-listProcessInstances.do";
    }

    /**
     * ??????????????????.
     */
    @RequestMapping("console-reopen")
    public String reopen(
            @RequestParam("processInstanceId") String processInstanceId) {
        processEngine.getManagementService().executeCommand(
                new ReOpenProcessCmd(processInstanceId));

        return "redirect:/bpm/console-listHistoricProcessInstances.do";
    }

    /**
     * ???????????????????????????????????????????????????????????????.
     */
    @RequestMapping("console-addSubTaskInput")
    public String addSubTaskInput(@RequestParam("taskId") String taskId) {
        return "bpm/console-addSubTaskInput";
    }

    /**
     * ???????????????.
     */
    @RequestMapping("console-addSubTask")
    public String addSubTask(@RequestParam("taskId") String taskId,
            @RequestParam("userId") String userId) {
        processEngine.getManagementService().executeCommand(
                new ChangeSubTaskCmd(taskId, userId));

        return "redirect:/bpm/console-listTasks.do";
    }

    /**
     * ??????????????????.
     */
    @RequestMapping("console-completeTask")
    public String completeTask(@RequestParam("taskId") String taskId) {
        processEngine.getTaskService().complete(taskId);

        return "redirect:/bpm/console-listTasks.do";
    }

    // ~ ======================================================================
    @Resource
    public void setProcessEngine(ProcessEngine processEngine) {
        this.processEngine = processEngine;
    }

    @Resource
    public void setProcessConnector(ProcessConnector processConnector) {
        this.processConnector = processConnector;
    }

    @Resource
    public void setBpmConfBaseManager(BpmConfBaseManager bpmConfBaseManager) {
        this.bpmConfBaseManager = bpmConfBaseManager;
    }

    @Resource
    public void setBpmProcessManager(BpmProcessManager bpmProcessManager) {
        this.bpmProcessManager = bpmProcessManager;
    }

    @Resource
    public void setTenantHolder(TenantHolder tenantHolder) {
        this.tenantHolder = tenantHolder;
    }
}
