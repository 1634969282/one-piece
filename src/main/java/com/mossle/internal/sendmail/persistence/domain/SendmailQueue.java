package com.mossle.internal.sendmail.persistence.domain;

// Generated by Hibernate Tools
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * SendmailQueue .
 * 
 * @author Lingo
 */
@Entity
@Table(name = "SENDMAIL_QUEUE")
public class SendmailQueue implements java.io.Serializable {
    private static final long serialVersionUID = 0L;

    /** null. */
    private Long id;

    /** null. */
    private SendmailTemplate sendmailTemplate;

    /** null. */
    private SendmailApp sendmailApp;

    /** null. */
    private SendmailConfig sendmailConfig;

    /** null. */
    private String subject;

    /** null. */
    private String sender;

    /** null. */
    private String receiver;

    /** null. */
    private String cc;

    /** null. */
    private String bcc;

    /** null. */
    private String content;

    /** null. */
    private String attachment;

    /** null. */
    private String data;

    /** null. */
    private Date createTime;

    /** null. */
    private String status;

    /** null. */
    private String info;

    /** null. */
    private String tenantId;

    /** null. */
    private String businessKey;

    /** null. */
    private String catalog;

    /** null. */
    private String batch;

    public SendmailQueue() {
    }

    public SendmailQueue(Long id) {
        this.id = id;
    }

    public SendmailQueue(Long id, SendmailTemplate sendmailTemplate,
            SendmailApp sendmailApp, SendmailConfig sendmailConfig,
            String subject, String sender, String receiver, String cc,
            String bcc, String content, String attachment, String data,
            Date createTime, String status, String info, String tenantId,
            String businessKey, String catalog, String batch) {
        this.id = id;
        this.sendmailTemplate = sendmailTemplate;
        this.sendmailApp = sendmailApp;
        this.sendmailConfig = sendmailConfig;
        this.subject = subject;
        this.sender = sender;
        this.receiver = receiver;
        this.cc = cc;
        this.bcc = bcc;
        this.content = content;
        this.attachment = attachment;
        this.data = data;
        this.createTime = createTime;
        this.status = status;
        this.info = info;
        this.tenantId = tenantId;
        this.businessKey = businessKey;
        this.catalog = catalog;
        this.batch = batch;
    }

    /** @return null. */
    @Id
    @Column(name = "ID", unique = true, nullable = false)
    public Long getId() {
        return this.id;
    }

    /**
     * @param id
     *            null.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /** @return null. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TEMPLATE_ID")
    public SendmailTemplate getSendmailTemplate() {
        return this.sendmailTemplate;
    }

    /**
     * @param sendmailTemplate
     *            null.
     */
    public void setSendmailTemplate(SendmailTemplate sendmailTemplate) {
        this.sendmailTemplate = sendmailTemplate;
    }

    /** @return null. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "APP_ID")
    public SendmailApp getSendmailApp() {
        return this.sendmailApp;
    }

    /**
     * @param sendmailApp
     *            null.
     */
    public void setSendmailApp(SendmailApp sendmailApp) {
        this.sendmailApp = sendmailApp;
    }

    /** @return null. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CONFIG_ID")
    public SendmailConfig getSendmailConfig() {
        return this.sendmailConfig;
    }

    /**
     * @param sendmailConfig
     *            null.
     */
    public void setSendmailConfig(SendmailConfig sendmailConfig) {
        this.sendmailConfig = sendmailConfig;
    }

    /** @return null. */
    @Column(name = "SUBJECT", length = 50)
    public String getSubject() {
        return this.subject;
    }

    /**
     * @param subject
     *            null.
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /** @return null. */
    @Column(name = "SENDER", length = 200)
    public String getSender() {
        return this.sender;
    }

    /**
     * @param sender
     *            null.
     */
    public void setSender(String sender) {
        this.sender = sender;
    }

    /** @return null. */
    @Column(name = "RECEIVER", length = 200)
    public String getReceiver() {
        return this.receiver;
    }

    /**
     * @param receiver
     *            null.
     */
    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    /** @return null. */
    @Column(name = "CC", length = 200)
    public String getCc() {
        return this.cc;
    }

    /**
     * @param cc
     *            null.
     */
    public void setCc(String cc) {
        this.cc = cc;
    }

    /** @return null. */
    @Column(name = "BCC", length = 200)
    public String getBcc() {
        return this.bcc;
    }

    /**
     * @param bcc
     *            null.
     */
    public void setBcc(String bcc) {
        this.bcc = bcc;
    }

    /** @return null. */
    @Column(name = "CONTENT", length = 65535)
    public String getContent() {
        return this.content;
    }

    /**
     * @param content
     *            null.
     */
    public void setContent(String content) {
        this.content = content;
    }

    /** @return null. */
    @Column(name = "ATTACHMENT", length = 200)
    public String getAttachment() {
        return this.attachment;
    }

    /**
     * @param attachment
     *            null.
     */
    public void setAttachment(String attachment) {
        this.attachment = attachment;
    }

    /** @return null. */
    @Column(name = "DATA", length = 65535)
    public String getData() {
        return this.data;
    }

    /**
     * @param data
     *            null.
     */
    public void setData(String data) {
        this.data = data;
    }

    /** @return null. */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATE_TIME", length = 26)
    public Date getCreateTime() {
        return this.createTime;
    }

    /**
     * @param createTime
     *            null.
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /** @return null. */
    @Column(name = "STATUS", length = 50)
    public String getStatus() {
        return this.status;
    }

    /**
     * @param status
     *            null.
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /** @return null. */
    @Column(name = "INFO", length = 200)
    public String getInfo() {
        return this.info;
    }

    /**
     * @param info
     *            null.
     */
    public void setInfo(String info) {
        this.info = info;
    }

    /** @return null. */
    @Column(name = "TENANT_ID", length = 50)
    public String getTenantId() {
        return this.tenantId;
    }

    /**
     * @param tenantId
     *            null.
     */
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    /** @return null. */
    @Column(name = "BUSINESS_KEY", length = 100)
    public String getBusinessKey() {
        return this.businessKey;
    }

    /**
     * @param businessKey
     *            null.
     */
    public void setBusinessKey(String businessKey) {
        this.businessKey = businessKey;
    }

    /** @return null. */
    @Column(name = "CATALOG", length = 50)
    public String getCatalog() {
        return this.catalog;
    }

    /**
     * @param catalog
     *            null.
     */
    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    /** @return null. */
    @Column(name = "BATCH", length = 100)
    public String getBatch() {
        return this.batch;
    }

    /**
     * @param batch
     *            null.
     */
    public void setBatch(String batch) {
        this.batch = batch;
    }
}
