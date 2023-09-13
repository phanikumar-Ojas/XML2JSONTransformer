package com.ebsco.platform.shared.cmsimport.rs.service;

@FunctionalInterface
public interface DependOn<T> {
	
	T dependency();
}
