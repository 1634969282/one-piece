package com.mossle.disk.persistence.domain;

// Generated by Hibernate Tools
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * DiskTag 标签.
 * 
 * @author Lingo
 */
@Entity
@Table(name = "DISK_TAG")
public class DiskTag implements java.io.Serializable {
    private static final long serialVersionUID = 0L;

    /** id. */
    private Long id;

    /** 编码. */
    private String code;

    /** 名称. */
    private String name;

    /** 租户. */
    private String tenantId;

    /** . */
    private Set<DiskTagInfo> diskTagInfos = new HashSet<DiskTagInfo>(0);

    public DiskTag() {
    }

    public DiskTag(Long id) {
        this.id = id;
    }

    public DiskTag(Long id, String code, String name, String tenantId,
            Set<DiskTagInfo> diskTagInfos) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.tenantId = tenantId;
        this.diskTagInfos = diskTagInfos;
    }

    /** @return id. */
    @Id
    @Column(name = "ID", unique = true, nullable = false)
    public Long getId() {
        return this.id;
    }

    /**
     * @param id
     *            id.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /** @return 编码. */
    @Column(name = "CODE", length = 50)
    public String getCode() {
        return this.code;
    }

    /**
     * @param code
     *            编码.
     */
    public void setCode(String code) {
        this.code = code;
    }

    /** @return 名称. */
    @Column(name = "NAME", length = 50)
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

    /** @return 租户. */
    @Column(name = "TENANT_ID", length = 64)
    public String getTenantId() {
        return this.tenantId;
    }

    /**
     * @param tenantId
     *            租户.
     */
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    /** @return . */
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "diskTag")
    public Set<DiskTagInfo> getDiskTagInfos() {
        return this.diskTagInfos;
    }

    /**
     * @param diskTagInfos
     *            .
     */
    public void setDiskTagInfos(Set<DiskTagInfo> diskTagInfos) {
        this.diskTagInfos = diskTagInfos;
    }
}
