package com.mossle.internal.oss.data;

import java.util.List;

import com.mossle.core.csv.CsvCallback;

import com.mossle.internal.oss.persistence.domain.OssRegion;
import com.mossle.internal.oss.persistence.manager.OssRegionManager;

import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OssRegionCallback implements CsvCallback {
    private static Logger logger = LoggerFactory
            .getLogger(OssRegionCallback.class);
    private OssRegionManager ossRegionManager;
    private String defaultTenantId = "1";

    public void process(List<String> list, int lineNo) throws Exception {
        logger.debug("process : {} {}", list, lineNo);
        logger.debug("default tenant id : {}", defaultTenantId);

        String name = list.get(0);

        if (StringUtils.isBlank(name)) {
            logger.warn("name cannot be blank {} {}", lineNo, list);

            return;
        }

        name = name.toLowerCase();

        this.createOrUpdateRegion(name, lineNo);
    }

    public void createOrUpdateRegion(String name, int lineNo) {
        OssRegion ossRegion = ossRegionManager.findUniqueBy("name", name);

        if (ossRegion != null) {
            return;
        }

        // insert
        ossRegion = new OssRegion();
        ossRegion.setName(name);
        ossRegion.setStatus("active");
        ossRegionManager.save(ossRegion);
    }

    // ~
    public void setDefaultTenantId(String defaultTenantId) {
        this.defaultTenantId = defaultTenantId;
    }

    public void setOssRegionManager(OssRegionManager ossRegionManager) {
        this.ossRegionManager = ossRegionManager;
    }
}
