package com.mossle.internal.sendmail.support;

import javax.annotation.Resource;

import com.mossle.api.notification.NotificationDTO;
import com.mossle.api.notification.NotificationHandler;
import com.mossle.client.user.UserClient;

import com.mossle.internal.sendmail.service.SendmailDataService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SendmailNotificationHandler implements NotificationHandler {
    private static Logger logger = LoggerFactory
            .getLogger(SendmailNotificationHandler.class);
    private SendmailDataService sendmailDataService;
    private UserClient userClient;

    public void handle(NotificationDTO notificationDto, String tenantId) {
        String email = null;

        if ("userid".equals(notificationDto.getReceiverType())) {
            email = userClient.findById(notificationDto.getReceiver(), tenantId)
                    .getEmail();
        } else if ("email".equals(notificationDto.getReceiverType())) {
            email = notificationDto.getReceiver();
        } else {
            return;
        }

        try {
            sendmailDataService.send(email, notificationDto.getSubject(),
                    notificationDto.getContent(), "1", tenantId);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    public String getType() {
        return "sendmail";
    }

    @Resource
    public void setSendmailDataService(SendmailDataService sendmailDataService) {
        this.sendmailDataService = sendmailDataService;
    }

    @Resource
    public void setUserClient(UserClient userClient) {
        this.userClient = userClient;
    }
}
