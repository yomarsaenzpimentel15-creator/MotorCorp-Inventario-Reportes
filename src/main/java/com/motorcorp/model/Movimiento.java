package com.motorcorp.model;
import java.time.LocalDateTime;
public record Movimiento(long id,long repuestoId,String codigo,String repuesto,String tipo,int cantidad,String motivo,LocalDateTime fecha) {}
