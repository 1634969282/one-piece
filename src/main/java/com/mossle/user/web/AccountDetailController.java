package com.mossle.user.web;

import java.util.List;

import javax.annotation.Resource;

import com.mossle.user.persistence.domain.AccountAvatar;
import com.mossle.user.persistence.domain.AccountCredential;
import com.mossle.user.persistence.domain.AccountDevice;
import com.mossle.user.persistence.domain.AccountInfo;
import com.mossle.user.persistence.domain.AccountLog;
import com.mossle.user.persistence.domain.AccountOnline;
import com.mossle.user.persistence.domain.PersonInfo;
import com.mossle.user.persistence.manager.AccountAvatarManager;
import com.mossle.user.persistence.manager.AccountCredentialManager;
import com.mossle.user.persistence.manager.AccountDeviceManager;
import com.mossle.user.persistence.manager.AccountInfoManager;
import com.mossle.user.persistence.manager.AccountLogManager;
import com.mossle.user.persistence.manager.AccountOnlineManager;
import com.mossle.user.persistence.manager.PersonInfoManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("user")
public class AccountDetailController {
    private static Logger logger = LoggerFactory
            .getLogger(AccountDetailController.class);
    private AccountInfoManager accountInfoManager;
    private AccountCredentialManager accountCredentialManager;
    private AccountAvatarManager accountAvatarManager;
    private PersonInfoManager personInfoManager;
    private AccountDeviceManager accountDeviceManager;
    private AccountLogManager accountLogManager;
    private AccountOnlineManager accountOnlineManager;

    @RequestMapping("account-detail-index")
    public String index(@RequestParam("infoId") Long infoId, Model model) {
        AccountInfo accountInfo = this.accountInfoManager.get(infoId);

        if (accountInfo == null) {
            logger.info("cannot find accountInfo : {}", infoId);

            return null;
        }

        PersonInfo personInfo = this.personInfoManager.findUniqueBy("code",
                accountInfo.getCode());

        model.addAttribute("accountInfo", accountInfo);
        model.addAttribute("personInfo", personInfo);

        return "user/account-detail-index";
    }

    @RequestMapping("account-detail-password")
    public String password(@RequestParam("infoId") Long infoId, Model model) {
        String hql = "from AccountCredential where accountInfo.id=?";
        List<AccountCredential> accountCredentials = this.accountCredentialManager
                .find(hql, infoId);

        model.addAttribute("accountCredentials", accountCredentials);

        AccountInfo accountInfo = this.accountInfoManager.get(infoId);
        model.addAttribute("accountInfo", accountInfo);

        return "user/account-detail-password";
    }

    @RequestMapping("account-detail-avatar")
    public String avatar(@RequestParam("infoId") Long infoId, Model model) {
        String hql = "from AccountAvatar where accountInfo.id=?";
        List<AccountAvatar> accountAvatars = this.accountAvatarManager.find(
                hql, infoId);

        model.addAttribute("accountAvatars", accountAvatars);

        AccountInfo accountInfo = this.accountInfoManager.get(infoId);
        model.addAttribute("accountInfo", accountInfo);

        return "user/account-detail-avatar";
    }

    @RequestMapping("account-detail-log")
    public String log(@RequestParam("infoId") Long infoId, Model model) {
        AccountInfo accountInfo = this.accountInfoManager.get(infoId);
        String hql = "from AccountLog where username=? order by id desc";
        List<AccountLog> accountLogs = this.accountLogManager.find(hql,
                accountInfo.getUsername());

        model.addAttribute("accountLogs", accountLogs);

        model.addAttribute("accountInfo", accountInfo);

        return "user/account-detail-log";
    }

    @RequestMapping("account-detail-device")
    public String device(@RequestParam("infoId") Long infoId, Model model) {
        String hql = "from AccountDevice where accountInfo.id=?";
        List<AccountDevice> accountDevices = this.accountDeviceManager.find(
                hql, infoId);

        model.addAttribute("accountDevices", accountDevices);

        AccountInfo accountInfo = this.accountInfoManager.get(infoId);
        model.addAttribute("accountInfo", accountInfo);

        return "user/account-detail-device";
    }

    @RequestMapping("account-detail-token")
    public String token(@RequestParam("infoId") Long infoId, Model model) {
        AccountInfo accountInfo = this.accountInfoManager.get(infoId);
        String hql = "from AccountOnline where account=? order by id desc";
        List<AccountOnline> accountOnlines = this.accountOnlineManager.find(
                hql, Long.toString(accountInfo.getId()));

        model.addAttribute("accountOnlines", accountOnlines);

        model.addAttribute("accountInfo", accountInfo);

        return "user/account-detail-token";
    }

    @RequestMapping("account-detail-person")
    public String person(@RequestParam("infoId") Long infoId, Model model) {
        AccountInfo accountInfo = this.accountInfoManager.get(infoId);
        PersonInfo personInfo = this.personInfoManager.findUniqueBy("code",
                accountInfo.getCode());

        model.addAttribute("accountInfo", accountInfo);
        model.addAttribute("personInfo", personInfo);

        return "user/account-detail-person";
    }

    // ~ ======================================================================
    @Resource
    public void setAccountInfoManager(AccountInfoManager accountInfoManager) {
        this.accountInfoManager = accountInfoManager;
    }

    @Resource
    public void setAccountCredentialManager(
            AccountCredentialManager accountCredentialManager) {
        this.accountCredentialManager = accountCredentialManager;
    }

    @Resource
    public void setAccountAvatarManager(
            AccountAvatarManager accountAvatarManager) {
        this.accountAvatarManager = accountAvatarManager;
    }

    @Resource
    public void setPersonInfoManager(PersonInfoManager personInfoManager) {
        this.personInfoManager = personInfoManager;
    }

    @Resource
    public void setAccountDeviceManager(
            AccountDeviceManager accountDeviceManager) {
        this.accountDeviceManager = accountDeviceManager;
    }

    @Resource
    public void setAccountLogManager(AccountLogManager accountLogManager) {
        this.accountLogManager = accountLogManager;
    }

    @Resource
    public void setAccountOnlineManager(
            AccountOnlineManager accountOnlineManager) {
        this.accountOnlineManager = accountOnlineManager;
    }
}
