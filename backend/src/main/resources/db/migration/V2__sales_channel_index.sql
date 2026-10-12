-- The Sales ledger filters by maker, optionally by channel, and always by date range.
-- This composite index serves that query shape (maker, channel, date) directly.
CREATE INDEX idx_sales_maker_channel_date ON sales(maker_id, channel_id, sold_at);
