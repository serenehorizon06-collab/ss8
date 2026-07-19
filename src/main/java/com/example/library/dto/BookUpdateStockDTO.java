package com.example.library.dto;

import jakarta.validation.constraints.Min;

public class BookUpdateStockDTO {

    @Min(0)
    private Integer stock;

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }
}
