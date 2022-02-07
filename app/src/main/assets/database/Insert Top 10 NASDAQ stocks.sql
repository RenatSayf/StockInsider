INSERT OR replace INTO search_set (
set_name, 
company_name, 
ticker, 
filing_period, 
trade_period, 
is_purchase, 
is_sale, 
trade_min, 
trade_max, 
is_officer, 
is_director, 
is_ten_percent, 
group_by, 
sort_by,
target,
is_tracked,
is_default) VALUES (
"NASDAQ top 10 stocks",
 "",
 "AAPL MSFT AMZN TSLA NVDA GOOG FB NFLX INTC CSCO",
 1, 3, 1, 0, "", "", 1, 1, 1, 0, 3, "tracking", 1, 1)