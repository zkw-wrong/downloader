package com.apkpure.components.greendao.db;

import java.util.Map;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.AbstractDaoSession;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.identityscope.IdentityScopeType;
import org.greenrobot.greendao.internal.DaoConfig;

import com.apkpure.components.downloader.db.bean.DownloadTaskBean;

import com.apkpure.components.greendao.db.DownloadTaskBeanDao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 * 
 * @see org.greenrobot.greendao.AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig downloadTaskBeanDaoConfig;

    private final DownloadTaskBeanDao downloadTaskBeanDao;

    public DaoSession(Database db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        downloadTaskBeanDaoConfig = daoConfigMap.get(DownloadTaskBeanDao.class).clone();
        downloadTaskBeanDaoConfig.initIdentityScope(type);

        downloadTaskBeanDao = new DownloadTaskBeanDao(downloadTaskBeanDaoConfig, this);

        registerDao(DownloadTaskBean.class, downloadTaskBeanDao);
    }
    
    public void clear() {
        downloadTaskBeanDaoConfig.clearIdentityScope();
    }

    public DownloadTaskBeanDao getDownloadTaskBeanDao() {
        return downloadTaskBeanDao;
    }

}
