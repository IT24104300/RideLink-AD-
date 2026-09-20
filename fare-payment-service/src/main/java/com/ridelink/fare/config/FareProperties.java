package com.ridelink.fare.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@ConfigurationProperties(prefix = "ridelink.fare")
public class FareProperties {

    private String currency = "LKR";
    private BigDecimal base = new BigDecimal("150");
    private BigDecimal perKm = new BigDecimal("80");
    private BigDecimal perMin = new BigDecimal("5");

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getBase() {
        return base;
    }

    public void setBase(BigDecimal base) {
        this.base = base;
    }

    public BigDecimal getPerKm() {
        return perKm;
    }

    public void setPerKm(BigDecimal perKm) {
        this.perKm = perKm;
    }

    public BigDecimal getPerMin() {
        return perMin;
    }

    public void setPerMin(BigDecimal perMin) {
        this.perMin = perMin;
    }
}
