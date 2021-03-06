package com.mossle.user.web;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.mossle.core.page.Page;
import com.mossle.core.query.PropertyFilter;

import com.mossle.user.persistence.manager.AccountLogManager;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("user")
public class AccountLogController {
    private AccountLogManager accountLogManager;

    @RequestMapping("account-log-list")
    public String list(@ModelAttribute Page page,
            @RequestParam Map<String, Object> parameterMap, Model model) {
        page.setDefaultOrder("logTime", "DESC");

        List<PropertyFilter> propertyFilters = PropertyFilter
                .buildFromMap(parameterMap);
        page = accountLogManager.pagedQuery(page, propertyFilters);

        model.addAttribute("page", page);

        return "user/account-log-list";
    }

    // ~ ======================================================================
    @Resource
    public void setAccountLogManager(AccountLogManager accountLogManager) {
        this.accountLogManager = accountLogManager;
    }
}
