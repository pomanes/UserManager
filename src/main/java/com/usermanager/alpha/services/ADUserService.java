package com.usermanager.alpha.services;

import com.unboundid.ldap.sdk.LDAPException;
import com.usermanager.alpha.model.ADUser;
import com.usermanager.alpha.repositories.ADUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ADUserService {

    private final ADUserRepository adUserRepository;

    @Autowired
    public ADUserService(ADUserRepository adUserRepository) {
        this.adUserRepository = adUserRepository;
    }

    public Optional<List<ADUser>> findAll() throws LDAPException {
        return Optional.of(adUserRepository.findAll());
    }

    public Optional<ADUser> findByUsername(String username) throws LDAPException {
        return Optional.of(adUserRepository.findByUsername(username));
    }

    public Optional<ADUser> findByUuid(UUID uuid) throws LDAPException {
        return Optional.of(adUserRepository.findByUuid(uuid));
    }

    public ADUser create(ADUser user) throws LDAPException {
        return adUserRepository.create(user);
    }

}