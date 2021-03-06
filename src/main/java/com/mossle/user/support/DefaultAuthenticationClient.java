package com.mossle.user.support;

import java.util.List;

import javax.annotation.Resource;

import com.mossle.api.user.AccountStatus;
import com.mossle.api.user.AccountStatusHelper;
import com.mossle.api.user.AuthenticationClient;
import com.mossle.api.user.AuthenticationHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultAuthenticationClient implements AuthenticationClient {
    private static Logger logger = LoggerFactory
            .getLogger(DefaultAuthenticationClient.class);
    private AccountStatusHelper accountStatusHelper;
    private List<AuthenticationHandler> authenticationHandlers;

    public String doAuthenticate(String username, String password, String type,
            String application) {
        logger.debug("do authenticate {} *** {} {}", username, type,
                application);

        boolean locked = accountStatusHelper.isLocked(username, application);

        if (locked) {
            return AccountStatus.LOCKED;
        }

        String result = AccountStatus.FAILURE;

        for (AuthenticationHandler authenticationHandler : authenticationHandlers) {
            if (authenticationHandler.support(type)) {
                result = authenticationHandler.doAuthenticate(username,
                        password, application);

                break;
            }
        }

        if (!AccountStatus.SUCCESS.equals(result)) {
            return result;
        }

        String status = accountStatusHelper.getAccountStatus(username,
                application);

        return status;
    }

    // ~
    @Resource
    public void setAccountStatusHelper(AccountStatusHelper accountStatusHelper) {
        this.accountStatusHelper = accountStatusHelper;
    }

    public void setAuthenticationHandlers(
            List<AuthenticationHandler> authenticationHandlers) {
        this.authenticationHandlers = authenticationHandlers;
    }
}
