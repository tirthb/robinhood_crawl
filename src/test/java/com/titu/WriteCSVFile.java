package com.titu;

import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapWriter;
import org.supercsv.prefs.CsvPreference;

public class WriteCSVFile {
	
	private List<Row> rows;
	
    public WriteCSVFile(List<Row> rows) {
		this.rows = rows;
	}
     
    private static CellProcessor[] getProcessors()
    {
        final CellProcessor[] processors = new CellProcessor[] {
        		
        		/*
        		 * Description	Symbol	Stock Price	Expiration	Investment	Credit	Qty	Credit Ratio	Total Gain	Total Gain %	Max Loss	Profit Target
        		 */
                new NotNull(), // Description
                new NotNull(), // Symbol
                new Optional(), // Expiration
                new Optional(), // Days Left
                new Optional(), // Next Earnings
                new Optional(), // Days Left
                new NotNull(), // Investment
                new Optional(), // Credit
                new NotNull(), // Qty
                new Optional(), // Credit Ratio
                new NotNull(), // Total Gain %
                new NotNull(), // Total Gain
                new Optional(), //Profit Target
                new NotNull(), //Max Loss
                new NotNull(), // Stock price
                new NotNull(), // Break even
                new Optional(), //Percent Distance From Breakeven
                new Optional(), //Optimist Target
                new Optional(), //Pessimist Target
                new Optional(), //Realist Target
        };
        return processors;
    }
     
    public void write() throws Exception {
    	
    	final String[] header = new String[] { "Description", 
								        		"Symbol", 
								        		"Expiration",
								        		"Days Left",
								        		"Next Earnings",
								        		"Days Left (Earnings)",
								        		"Investment",
								        		"Credit", 
								        		"Qty", 
								        		"Credit Ratio", 
								        		"Total Gain", 
								        		"Total Gain %", 
								        		"Max Loss", 
								        		"Profit Target",
								        		"Stock Price",
								        		"Break Even",
								        		"Percent Distance From Breakeven",
								        		"Optimist Target",
								        		"Pessimist Target",
								        		"Realist Target",
								        		};
        
        ICsvMapWriter mapWriter = null;
        try {
                mapWriter = new CsvMapWriter(new FileWriter("portfolio.tsv"),
                        CsvPreference.TAB_PREFERENCE);
                
                final CellProcessor[] processors = getProcessors();
                
                // write the header
                mapWriter.writeHeader(header);
                
                // write the customer maps
                for (Row row : rows) {
                	final Map<String, Object> line = new HashMap<String, Object>();
                	int n = 0;
                    line.put(header[n++], row.getDescription());
                    line.put(header[n++], row.symbol);
                    line.put(header[n++], row.expirationDate);
                    line.put(header[n++], row.getDaysLeftForExpiry());
                    line.put(header[n++], row.getNextEarningsDate());
                    line.put(header[n++], row.getDaysLeftForEarnings());
                    line.put(header[n++], row.getInvestment());
                    line.put(header[n++], row.credit);
                    line.put(header[n++], row.quantity);
                    line.put(header[n++], row.getCreditRatio());
                    line.put(header[n++], row.getTotalGainPercent());
                    line.put(header[n++], row.getTotalGain());
                    line.put(header[n++], row.getProfitTarget());
                    line.put(header[n++], row.getMaxLoss());
                    line.put(header[n++], row.currentStockPrice);
                    line.put(header[n++], row.getBreakEven());
                    line.put(header[n++], row.getPercentDistanceFromBreakeven());
                    line.put(header[n++], row.getOptimistTarget());
                    line.put(header[n++], row.getPessimistTarget());
                    line.put(header[n++], row.getRealistTarget());
                	
                	mapWriter.write(line, header, processors);
				}
        } finally {
                if( mapWriter != null ) {
                        mapWriter.close();
                }
        }
    }
}