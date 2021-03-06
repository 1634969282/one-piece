package com.mossle.disk.persistence.domain;

// Generated by Hibernate Tools
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * DiskLog 文件日志.
 * 
 * @author Lingo
 */
@Entity
@Table(name = "DISK_LOG")
public class DiskLog implements java.io.Serializable {
    private static final long serialVersionUID = 0L;

    /** 主键. */
    private Long id;

    /** 名称. */
    private String name;

    /** 备注. */
    private String description;

    /** 类型. */
    private String type;

    /** 文件或文件夹. */
    private Long sourceId;

    /** 上级文件夹. */
    private Long parentId;

    /** 空间. */
    private Long spaceId;

    /** 分类. */
    private String catalog;

    /** 旧值. */
    private String oldValue;

    /** 新值. */
    private String newValue;

    /** 创建人. */
    private String creator;

    /** 创建时间. */
    private Date createTime;

    /** 更新人. */
    private String updater;

    /** 更新时间. */
    private Date updateTime;

    /** 状态. */
    private String status;

    /** 引用类型. */
    private String refType;

    /** 引用值. */
    private String refValue;

    public DiskLog() {
    }

    public DiskLog(Long id) {
        this.id = id;
    }

    public DiskLog(Long id, String name, String description, String type,
            Long sourceId, Long parentId, Long spaceId, String catalog,
            String oldValue, String newValue, String creator, Date createTime,
            String updater, Date updateTime, String status, String refType,
            String refValue) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.sourceId = sourceId;
        this.parentId = parentId;
        this.spaceId = spaceId;
        this.catalog = catalog;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.creator = creator;
        this.createTime = createTime;
        this.updater = updater;
        this.updateTime = updateTime;
        this.status = status;
        this.refType = refType;
        this.refValue = refValue;
    }

    /** @return 主键. */
    @Id
    @Column(name = "ID", unique = true, nullable = false)
    public Long getId() {
        return this.id;
    }

    /**
     * @param id
     *            主键.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /** @return 名称. */
    @Column(name = "NAME", length = 200)
    public String getName() {
        return this.name;
    }

    /**
     * @param name
     *            名称.
     */
    public void setName(String name) {
        this.name = name;
    }

    /** @return 备注. */
    @Column(name = "DESCRIPTION")
    public String getDescription() {
        return this.description;
    }

    /**
     * @param description
     *            备注.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /** @return 类型. */
    @Column(name = "TYPE", length = 50)
    public String getType() {
        return this.type;
    }

    /**
     * @param type
     *            类型.
     */
    public void setType(String type) {
        this.type = type;
    }

    /** @return 文件或文件夹. */
    @Column(name = "SOURCE_ID")
    public Long getSourceId() {
        return this.sourceId;
    }

    /**
     * @param sourceId
     *            文件或文件夹.
     */
    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
    }

    /** @return 上级文件夹. */
    @Column(name = "PARENT_ID")
    public Long getParentId() {
        return this.parentId;
    }

    /**
     * @param parentId
     *            上级文件夹.
     */
    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    /** @return 空间. */
    @Column(name = "SPACE_ID")
    public Long getSpaceId() {
        return this.spaceId;
    }

    /**
     * @param spaceId
     *            空间.
     */
    public void setSpaceId(Long spaceId) {
        this.spaceId = spaceId;
    }

    /** @return 分类. */
    @Column(name = "CATALOG", length = 50)
    public String getCatalog() {
        return this.catalog;
    }

    /**
     * @param catalog
     *            分类.
     */
    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    /** @return 旧值. */
    @Column(name = "OLD_VALUE", length = 200)
    public String getOldValue() {
        return this.oldValue;
    }

    /**
     * @param oldValue
     *            旧值.
     */
    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    /** @return 新值. */
    @Column(name = "NEW_VALUE", length = 200)
    public String getNewValue() {
        return this.newValue;
    }

    /**
     * @param newValue
     *            新值.
     */
    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    /** @return 创建人. */
    @Column(name = "CREATOR", length = 64)
    public String getCreator() {
        return this.creator;
    }

    /**
     * @param creator
     *            创建人.
     */
    public void setCreator(String creator) {
        this.creator = creator;
    }

    /** @return 创建时间. */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATE_TIME", length = 26)
    public Date getCreateTime() {
        return this.createTime;
    }

    /**
     * @param createTime
     *            创建时间.
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /** @return 更新人. */
    @Column(name = "UPDATER", length = 64)
    public String getUpdater() {
        return this.updater;
    }

    /**
     * @param updater
     *            更新人.
     */
    public void setUpdater(String updater) {
        this.updater = updater;
    }

    /** @return 更新时间. */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "UPDATE_TIME", length = 26)
    public Date getUpdateTime() {
        return this.updateTime;
    }

    /**
     * @param updateTime
     *            更新时间.
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    /** @return 状态. */
    @Column(name = "STATUS", length = 50)
    public String getStatus() {
        return this.status;
    }

    /**
     * @param status
     *            状态.
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /** @return 引用类型. */
    @Column(name = "REF_TYPE", length = 50)
    public String getRefType() {
        return this.refType;
    }

    /**
     * @param refType
     *            引用类型.
     */
    public void setRefType(String refType) {
        this.refType = refType;
    }

    /** @return 引用值. */
    @Column(name = "REF_VALUE", length = 200)
    public String getRefValue() {
        return this.refValue;
    }

    /**
     * @param refValue
     *            引用值.
     */
    public void setRefValue(String refValue) {
        this.refValue = refValue;
    }
}
