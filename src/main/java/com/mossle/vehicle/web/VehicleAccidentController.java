package com.mossle.vehicle.web;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.mossle.api.tenant.TenantHolder;
import com.mossle.api.user.UserConnector;

import com.mossle.core.export.Exportor;
import com.mossle.core.export.TableModel;
import com.mossle.core.mapper.BeanMapper;
import com.mossle.core.page.Page;
import com.mossle.core.query.PropertyFilter;
import com.mossle.core.spring.MessageHelper;

import com.mossle.vehicle.persistence.domain.VehicleAccident;
import com.mossle.vehicle.persistence.domain.VehicleDriver;
import com.mossle.vehicle.persistence.domain.VehicleInfo;
import com.mossle.vehicle.persistence.manager.VehicleAccidentManager;
import com.mossle.vehicle.persistence.manager.VehicleDriverManager;
import com.mossle.vehicle.persistence.manager.VehicleInfoManager;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("vehicle")
public class VehicleAccidentController {
    private VehicleAccidentManager vehicleAccidentManager;
    private VehicleInfoManager vehicleInfoManager;
    private VehicleDriverManager vehicleDriverManager;
    private Exportor exportor;
    private BeanMapper beanMapper = new BeanMapper();
    private UserConnector userConnector;
    private MessageHelper messageHelper;
    private TenantHolder tenantHolder;

    @RequestMapping("vehicle-accident-list")
    public String list(@ModelAttribute Page page,
            @RequestParam Map<String, Object> parameterMap, Model model) {
        String tenantId = tenantHolder.getTenantId();
        List<PropertyFilter> propertyFilters = PropertyFilter
                .buildFromMap(parameterMap);
        propertyFilters.add(new PropertyFilter("EQS_tenantId", tenantId));
        page = vehicleAccidentManager.pagedQuery(page, propertyFilters);

        model.addAttribute("page", page);

        return "vehicle/vehicle-accident-list";
    }

    @RequestMapping("vehicle-accident-input")
    public String input(@RequestParam(value = "id", required = false) Long id,
            Model model) {
        if (id != null) {
            VehicleAccident vehicleAccident = vehicleAccidentManager.get(id);
            model.addAttribute("model", vehicleAccident);
        }

        model.addAttribute("vehicleInfos", vehicleInfoManager.getAll());
        model.addAttribute("vehicleDrivers", vehicleDriverManager.getAll());

        return "vehicle/vehicle-accident-input";
    }

    @RequestMapping("vehicle-accident-save")
    public String save(@ModelAttribute VehicleAccident vehicleAccident,
            @RequestParam("infoId") Long infoId,
            @RequestParam("driverId") Long driverId,
            @RequestParam Map<String, Object> parameterMap,
            RedirectAttributes redirectAttributes) {
        String tenantId = tenantHolder.getTenantId();
        VehicleAccident dest = null;

        Long id = vehicleAccident.getId();

        if (id != null) {
            dest = vehicleAccidentManager.get(id);
            beanMapper.copy(vehicleAccident, dest);
        } else {
            dest = vehicleAccident;
            dest.setTenantId(tenantId);
        }

        dest.setVehicleInfo(vehicleInfoManager.get(infoId));
        dest.setVehicleDriver(vehicleDriverManager.get(driverId));

        vehicleAccidentManager.save(dest);

        messageHelper.addFlashMessage(redirectAttributes, "core.success.save",
                "????????????");

        return "redirect:/vehicle/vehicle-accident-list.do";
    }

    @RequestMapping("vehicle-accident-remove")
    public String remove(@RequestParam("selectedItem") List<Long> selectedItem,
            RedirectAttributes redirectAttributes) {
        List<VehicleAccident> vehicleAccidents = vehicleAccidentManager
                .findByIds(selectedItem);

        vehicleAccidentManager.removeAll(vehicleAccidents);

        messageHelper.addFlashMessage(redirectAttributes,
                "core.success.delete", "????????????");

        return "redirect:/vehicle/vehicle-accident-list.do";
    }

    @RequestMapping("vehicle-accident-export")
    public void export(@ModelAttribute Page page,
            @RequestParam Map<String, Object> parameterMap,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        String tenantId = tenantHolder.getTenantId();
        List<PropertyFilter> propertyFilters = PropertyFilter
                .buildFromMap(parameterMap);
        propertyFilters.add(new PropertyFilter("EQS_tenantId", tenantId));
        page = vehicleAccidentManager.pagedQuery(page, propertyFilters);

        List<VehicleAccident> vehicleAccidents = (List<VehicleAccident>) page
                .getResult();

        TableModel tableModel = new TableModel();
        tableModel.setName("vehicle info");
        tableModel.addHeaders("id", "name");
        tableModel.setData(vehicleAccidents);
        exportor.export(request, response, tableModel);
    }

    // ~ ======================================================================
    @Resource
    public void setVehicleAccidentManager(
            VehicleAccidentManager vehicleAccidentManager) {
        this.vehicleAccidentManager = vehicleAccidentManager;
    }

    @Resource
    public void setVehicleInfoManager(VehicleInfoManager vehicleInfoManager) {
        this.vehicleInfoManager = vehicleInfoManager;
    }

    @Resource
    public void setVehicleDriverManager(
            VehicleDriverManager vehicleDriverManager) {
        this.vehicleDriverManager = vehicleDriverManager;
    }

    @Resource
    public void setExportor(Exportor exportor) {
        this.exportor = exportor;
    }

    @Resource
    public void setUserConnector(UserConnector userConnector) {
        this.userConnector = userConnector;
    }

    @Resource
    public void setMessageHelper(MessageHelper messageHelper) {
        this.messageHelper = messageHelper;
    }

    @Resource
    public void setTenantHolder(TenantHolder tenantHolder) {
        this.tenantHolder = tenantHolder;
    }
}
