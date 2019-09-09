package com.titu;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Row {
	
	public String url;
	private String description;
	public Boolean isPut;
	public Boolean isCall;
	public Boolean isSpread;
	public String symbol;
	public String expirationDate;
	public Integer quantity;
	public Float marketValue;
	public Float currentStockPrice;
	public Float cost;
	public Float credit;
	private Float profitTarget;
	private Float averageCredit;
	private Float investment;
	private Float creditRatio;
	private Float totalGain;
	private Float totalGainPercent;
	private Float maxLoss;
	private Float spreadMin;
	private Float spreadMax;
	private Float strikePrice;
		
	public Row() {}
	public Row(String description, String url) {
		this.url = url;
		setDescription(description);
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
		
		isSpread = description.contains("/");
		isCall = description.contains("Call");
		isPut = description.contains("Put");
		
		Pattern pattern = Pattern.compile("[A-Z]+");
        Matcher matcher = pattern.matcher(description);
        if(matcher.find()) {
         symbol = description.substring(matcher.start(), matcher.end());
        }
        
        pattern = Pattern.compile("\\$[\\d|\\.|,]+");
        matcher = pattern.matcher(description);
        if (isSpread) {
            int count = 0;
            while(matcher.find()) {
                Float value = Float.valueOf(description.substring(matcher.start(), matcher.end()).replaceAll("(\\$|,)", ""));
                if (count == 0) {spreadMax = value;}
                if (count == 1) {spreadMin = value;}
                count++;
            }
        } else {
        	if(matcher.find()) {
        		strikePrice = Float.valueOf(description.substring(matcher.start(), matcher.end()).replaceAll("(\\$|,)", ""));
            }
        }
	}
	
	public Float getInvestment() {
		
		if (isSpread) {
			BigDecimal up = BigDecimal.valueOf(spreadMax);
    		BigDecimal down = BigDecimal.valueOf(spreadMin);
    		BigDecimal qty = BigDecimal.valueOf(quantity);
    		investment = up.subtract(down).multiply(qty).multiply(BigDecimal.valueOf(100L)).abs().floatValue();
		} else {
			investment = cost;
		}
		
		return investment;
	}
	
	public Float getAverageCredit() {
		
		if (isSpread) {
			BigDecimal qty = BigDecimal.valueOf(quantity);
			BigDecimal bdCredit = BigDecimal.valueOf(credit);
			this.averageCredit = bdCredit.divide(qty, 2, RoundingMode.HALF_UP).floatValue();
		}
		
		return averageCredit;
	}
	
	public Float getTotalGain() {
		
		BigDecimal marketValue = BigDecimal.valueOf(this.marketValue);
		
		if (isSpread) {
			BigDecimal credit = BigDecimal.valueOf(this.credit);
			this.totalGain = credit.add(marketValue).setScale(2, RoundingMode.HALF_UP).floatValue();
		} else {
			BigDecimal cost = BigDecimal.valueOf(this.cost);
			this.totalGain = marketValue.subtract(cost).setScale(2, RoundingMode.HALF_UP).floatValue();
		}
		
		return totalGain;
	}
	
	public Float getTotalGainPercent() {
		
		BigDecimal totalGain = BigDecimal.valueOf(getTotalGain());
		
		if (isSpread) {
			BigDecimal credit = BigDecimal.valueOf(this.credit);
			this.totalGainPercent = totalGain.divide(credit, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100L)).floatValue();
		} else {
			BigDecimal cost = BigDecimal.valueOf(this.cost);
			this.totalGainPercent = totalGain.divide(cost, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100L)).floatValue();
		}
		
		return totalGainPercent;
	}
	
	public Float getMaxLoss() {
		
		BigDecimal investment = BigDecimal.valueOf(this.investment);
		
		if (isSpread) {
			BigDecimal credit = BigDecimal.valueOf(this.credit);
			this.maxLoss = investment.subtract(credit).setScale(2, RoundingMode.HALF_UP).floatValue();
		} else {
			this.maxLoss = cost;
		}
		
		return maxLoss*(-1);
	}
	
	public Float getCreditRatio() {
		
		if (isSpread) {
			BigDecimal investment = BigDecimal.valueOf(this.investment);
			BigDecimal credit = BigDecimal.valueOf(this.credit);
			this.creditRatio = credit.divide(investment, 2, RoundingMode.HALF_UP).floatValue();
		}
		
		return creditRatio;
	}
	
	public Float getProfitTarget() {
		
		if (isSpread) {
			BigDecimal credit = BigDecimal.valueOf(this.credit);
			this.profitTarget = credit.multiply(BigDecimal.valueOf(0.9)).setScale(2, RoundingMode.HALF_UP).floatValue();
		}
		
		return profitTarget;
	}

	public Float getOptimistTarget() {
		if (isSpread && isPut) {
			if (currentStockPrice > spreadMin) {
				return getProfitTarget();
			} else {
				return getMaxLoss();
			}
		}
		return null;
	}
	
	public Float getPessimistTarget() {
		if (isSpread && isPut) {
			if (currentStockPrice > spreadMax) {
				return getProfitTarget();
			} else {
				return getMaxLoss();
			}
		}
		return null;
	}
	
	public Float getRealistTarget() {
		if (isSpread && isPut) {
			if (currentStockPrice > getBreakEven()) {
				return getProfitTarget();
			} else {
				return getMaxLoss();
			}
		}
		return null;
	}
	
	public Float getBreakEven() {
		BigDecimal qty = BigDecimal.valueOf(this.quantity);
		
		if (isPut && isSpread) {
			BigDecimal credit = BigDecimal.valueOf(this.credit);
			BigDecimal spreadMax = BigDecimal.valueOf(this.spreadMax);
			BigDecimal creditPerUnit = credit.divide(BigDecimal.valueOf(100L)).divide(qty, 2, RoundingMode.HALF_UP);
			BigDecimal breakEven = spreadMax.subtract(creditPerUnit);
			return breakEven.floatValue();
		} else if (isCall && !isSpread) {
			BigDecimal cost = BigDecimal.valueOf(this.cost);
			BigDecimal strikePrice = BigDecimal.valueOf(this.strikePrice);
			BigDecimal costPerUnit = cost.divide(BigDecimal.valueOf(100L)).divide(qty, 2, RoundingMode.HALF_UP);
			BigDecimal breakEven = strikePrice.subtract(costPerUnit);
			return breakEven.floatValue();
		}
		
		return null;
	}
	
	public Float getPercentDistanceFromBreakeven() {
		
		if (getBreakEven() == null) {
			return null;
		}
		
		BigDecimal currentStockPrice = BigDecimal.valueOf(this.currentStockPrice);
		BigDecimal breakEven = BigDecimal.valueOf(getBreakEven());
		return currentStockPrice.subtract(breakEven).divide(breakEven, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100L)).floatValue();
	}
	
	public Long getDaysLeft() {
		
		if (expirationDate != null) {
			
			Date expDate = null;
			String pattern = "yyyy-MM-dd";
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

			try {
				expDate = simpleDateFormat.parse(expirationDate);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			
			long startTime = new Date().getTime();
			long endTime = expDate.getTime();
			long diffTime = endTime - startTime;
			long diffDays = diffTime / (1000 * 60 * 60 * 24);
			
			return diffDays;
			
		}
		
		return null;
	}
	
	@Override
	public String toString() {
		return "Row ["
				+ (url != null ? "url =" + url + ", " : "")
				+ (getDescription() != null ? "getDescription()=" + getDescription() + ", " : "")
				+ (isPut != null ? "isPut=" + isPut + ", " : "")
				+ (isCall != null ? "isCall=" + isCall + ", " : "")
				+ (isSpread != null ? "isSpread=" + isSpread + ", " : "")
				+ (symbol != null ? "symbol=" + symbol + ", " : "")
				+ (expirationDate != null ? "expirationDate=" + expirationDate + ", " : "")
				+ (getDaysLeft() != null ? "getDaysLeft()=" + getDaysLeft() + ", " : "")
				+ (quantity != null ? "quantity=" + quantity + ", " : "")
				+ (marketValue != null ? "marketValue=" + marketValue + ", " : "")
				+ (currentStockPrice != null ? "currentStockPrice=" + currentStockPrice + ", " : "")
				+ (cost != null ? "cost=" + cost + ", " : "") + (credit != null ? "credit=" + credit + ", " : "")
				+ (spreadMin != null ? "spreadMin=" + spreadMin + ", " : "")
				+ (spreadMax != null ? "spreadMax=" + spreadMax + ", " : "")
				+ (getInvestment() != null ? "getInvestment()=" + getInvestment() + ", " : "")
				+ (getAverageCredit() != null ? "getAverageCredit()=" + getAverageCredit() + ", " : "")
				+ (getTotalGain() != null ? "getTotalGain()=" + getTotalGain() + ", " : "")
				+ (getTotalGainPercent() != null ? "getTotalGainPercent()=" + getTotalGainPercent() + ", " : "")
				+ (getMaxLoss() != null ? "getMaxLoss()=" + getMaxLoss() + ", " : "")
				+ (getCreditRatio() != null ? "getCreditRatio()=" + getCreditRatio() + ", " : "")
				+ (getProfitTarget() != null ? "getProfitTarget()=" + getProfitTarget() : "")
				+ (getOptimistTarget() != null ? ", getOptimistTarget()=" + getOptimistTarget() : "")
				+ (getPessimistTarget() != null ? ", getPessimistTarget()=" + getPessimistTarget() : "")
				+ (getBreakEven() != null ? ", getBreakEven()=" + getBreakEven() : "")
				+ (getPercentDistanceFromBreakeven() != null ? ", getPercentDistanceFromBreakeven()=" + getPercentDistanceFromBreakeven() : "")
				+ "]";
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Row other = (Row) obj;
		if (description == null) {
				return false;
		} else if (!description.equals(other.description))
			return false;
		return true;
	}

}
