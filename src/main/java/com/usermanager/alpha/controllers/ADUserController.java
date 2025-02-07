package com.usermanager.alpha.controllers;


import com.usermanager.alpha.model.ADUser;
import com.usermanager.alpha.services.ADUserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.naming.NamingException;
import java.util.List;

@RestController
@RequestMapping("api/adUsers")
public class ADUserController {

    private final ADUserService adUserService;

    @Autowired
    public ADUserController(final ADUserService adUserService) {
        this.adUserService = adUserService;
    }

    @GetMapping
    public ResponseEntity<List<ADUser>> getAllUsers() {
        return adUserService.findAll()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ADUser> addUser(@Valid @RequestBody ADUser adUser) throws NamingException {
        adUserService.addUser(adUser);
/*        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedADUser.getUsername())
                .toUri();
        return ResponseEntity.created(location).body(savedADUser);*/
        return ResponseEntity.ok(adUser);
    }
}
