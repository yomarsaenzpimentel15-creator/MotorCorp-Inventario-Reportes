package com.motorcorp.model;
import java.math.BigDecimal;
public record Repuesto(long id,String codigo,String nombre,String categoria,BigDecimal precio,int stock,int stockMinimo) {}
