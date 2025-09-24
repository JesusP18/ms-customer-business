package com.customer.business.model.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProductDTOTest {

    @Test
    @DisplayName("Cobertura completa de constructores, getters y setters")
    void testAllMethods() {
        ProductDTO dto = new ProductDTO();
        dto.setId("1");
        dto.setCustomerId("c1");
        dto.setCategory("LIABILITY");
        dto.setType("ACCOUNT");
        dto.setSubType("SAVINGS");

        assertEquals("1", dto.getId());
        assertEquals("c1", dto.getCustomerId());
        assertEquals("LIABILITY", dto.getCategory());
        assertEquals("ACCOUNT", dto.getType());
        assertEquals("SAVINGS", dto.getSubType());

        ProductDTO dto2 = new ProductDTO("2", "c2", "ASSET", "LOAN", "CURRENT");
        assertEquals("2", dto2.getId());
        assertEquals("c2", dto2.getCustomerId());
        assertEquals("ASSET", dto2.getCategory());
        assertEquals("LOAN", dto2.getType());
        assertEquals("CURRENT", dto2.getSubType());
    }
}
