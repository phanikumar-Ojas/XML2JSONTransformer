package com.ebsco.platform.shared.cmsimport.rs.domain;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public interface RTENode {
    
    default void addChild(RTENode child) {}
    
    default List<RTENode> children() {
        return Collections.emptyList();
    }
    
    default void children(List<RTENode> children) {}
    
    default String type() {
        return null;
    }
    
    default void type(String type) {}
    
    default Map<String, Object> getAttrs() {
        return null;
    }
}
