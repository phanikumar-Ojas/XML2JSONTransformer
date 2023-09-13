package com.ebsco.platform.shared.cmsimport.rs.repository;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.ebsco.platform.shared.cmsimport.rs.domain.Product;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class NumCode2ProductBinder implements ReferenceBinder<String, Product> {
    
    private static final Map<String, String> PRODUCT_TITLE_2_DB_NUM_CODE = Map.of(
            "ers", "1",
            "t5o", "2",
            "t6o", "3",
            "tol", "4");
    
    private final Map<String, Product> dbNumCode2Product;
    
    public NumCode2ProductBinder(Collection<Product> products) {
        log.info("Init product reference binder ...");
        this.dbNumCode2Product = new HashMap<>();
        for (Product product : products) {
            dbNumCode2Product.put(PRODUCT_TITLE_2_DB_NUM_CODE.get(product.getTitle()), product);
        }
        log.info("done: {}", dbNumCode2Product);
    }

    public Product bind(String dbNumCode) {
        return dbNumCode2Product.get(dbNumCode);
    }
}
