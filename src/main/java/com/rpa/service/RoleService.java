package com.rpa.service;

import java.util.List;

import com.rpa.model.Role;

public interface RoleService {
	List<Role> findByName(String name);
}
