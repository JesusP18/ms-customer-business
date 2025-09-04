package com.customer.business.service;

import com.customer.business.model.ProductReportResponse;
import reactor.core.publisher.Flux;

import java.time.LocalDate;

/**
 * Servicio que genera reportes de productos del banco.
 */
public interface ReportService {
    /**
     * Genera un reporte de productos del banco en un rango de fechas.
     * @param from fecha inicial
     * @param to fecha final
     * @return flujo de reportes de productos
     */
    Flux<ProductReportResponse> generateProductReport(LocalDate from, LocalDate to);
}