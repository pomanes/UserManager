package com.usermanager.alpha.controllers;

import com.unboundid.ldap.sdk.LDAPException;
import com.usermanager.alpha.model.ADUser;
import com.usermanager.alpha.services.ADUserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/adUsers")
public class ADUserController {

    private final ADUserService adUserService;

    @Autowired
    public ADUserController(final ADUserService adUserService) {
        this.adUserService = adUserService;
    }

    @GetMapping
    public ResponseEntity<List<ADUser>> getAllUsers() throws LDAPException {
        return adUserService.findAll()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

/*    @GetMapping("/{login}")
    public ResponseEntity<ADUser> getUser(@PathVariable("login") String login) throws LDAPException {
        return adUserService.findByUsername(login)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }*/

    @GetMapping("/{id}")
    public ResponseEntity<ADUser> getUserByUuid(@PathVariable("id") UUID id) throws LDAPException {
        return adUserService.findByUuid(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ADUser> createUser(@RequestBody @Valid ADUser adUser) throws LDAPException {
        ADUser savedUser = adUserService.create(adUser);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{username}")
                .buildAndExpand(savedUser.getUsername())
                .toUri();
        return ResponseEntity.created(location).body(savedUser);
    }
}
