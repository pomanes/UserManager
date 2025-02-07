package com.usermanager.alpha.repositories;

import com.usermanager.alpha.model.ADUser;
import org.springframework.data.ldap.repository.LdapRepository;

public interface ADUserRepository extends LdapRepository<ADUser> {
}
