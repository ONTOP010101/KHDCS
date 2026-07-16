package com.app.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;

@TableName("client_sample_price_settings")
public class ClientSamplePriceSetting extends BaseEntity {

    private String codeName;
    private String type;  // "1"=报价1, "2"=报价2
    private String template;
    private String method;
    private BigDecimal profitRate;
    private BigDecimal totalCost;
    private String currencyType;
    private String currencySymbol;
    private String currencyName;
    private BigDecimal exchangeRate;
    private Integer cartonSize;
    private Integer useCubicM;
    private BigDecimal markup;
    private String formulaType;
    private String roundMode;
    private Integer decimals;
    private BigDecimal priceLessThan;
    private String roundMode2;
    private Integer priceDecimals;
    private String customFormula;
    private String applyTo;  // "continue"=继续报价 / "allPriced"=所有已报价 / "current"=当前打钩

    public String getCodeName() { return codeName; }
    public void setCodeName(String codeName) { this.codeName = codeName; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getTemplate() { return template; }
    public void setTemplate(String template) { this.template = template; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public BigDecimal getProfitRate() { return profitRate; }
    public void setProfitRate(BigDecimal profitRate) { this.profitRate = profitRate; }
    public BigDecimal getTotalCost() { return totalCost; }
    public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }
    public String getCurrencyType() { return currencyType; }
    public void setCurrencyType(String currencyType) { this.currencyType = currencyType; }
    public String getCurrencySymbol() { return currencySymbol; }
    public void setCurrencySymbol(String currencySymbol) { this.currencySymbol = currencySymbol; }
    public String getCurrencyName() { return currencyName; }
    public void setCurrencyName(String currencyName) { this.currencyName = currencyName; }
    public BigDecimal getExchangeRate() { return exchangeRate; }
    public void setExchangeRate(BigDecimal exchangeRate) { this.exchangeRate = exchangeRate; }
    public Integer getCartonSize() { return cartonSize; }
    public void setCartonSize(Integer cartonSize) { this.cartonSize = cartonSize; }
    public Integer getUseCubicM() { return useCubicM; }
    public void setUseCubicM(Integer useCubicM) { this.useCubicM = useCubicM; }
    public BigDecimal getMarkup() { return markup; }
    public void setMarkup(BigDecimal markup) { this.markup = markup; }
    public String getFormulaType() { return formulaType; }
    public void setFormulaType(String formulaType) { this.formulaType = formulaType; }
    public String getRoundMode() { return roundMode; }
    public void setRoundMode(String roundMode) { this.roundMode = roundMode; }
    public Integer getDecimals() { return decimals; }
    public void setDecimals(Integer decimals) { this.decimals = decimals; }
    public BigDecimal getPriceLessThan() { return priceLessThan; }
    public void setPriceLessThan(BigDecimal priceLessThan) { this.priceLessThan = priceLessThan; }
    public String getRoundMode2() { return roundMode2; }
    public void setRoundMode2(String roundMode2) { this.roundMode2 = roundMode2; }
    public Integer getPriceDecimals() { return priceDecimals; }
    public void setPriceDecimals(Integer priceDecimals) { this.priceDecimals = priceDecimals; }
    public String getCustomFormula() { return customFormula; }
    public void setCustomFormula(String customFormula) { this.customFormula = customFormula; }
    public String getApplyTo() { return applyTo; }
    public void setApplyTo(String applyTo) { this.applyTo = applyTo; }
}
