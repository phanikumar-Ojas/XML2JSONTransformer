package com.ebsco.platform.shared.cmsimport.rs.repository;

public interface ReferenceBinder<BY, TO> {
	
	TO bind(BY criteria);
}
