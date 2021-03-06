package com.mossle.user.persistence.manager;

import com.mossle.core.hibernate.HibernateEntityDao;

import com.mossle.user.persistence.domain.PersonInfo;

import org.springframework.stereotype.Repository;

@Repository
public class PersonInfoManager extends HibernateEntityDao<PersonInfo> {
}
