INSERT INTO inventory (sku, available, version)
VALUES ('HEADPHONE-01', 100, 0)
ON CONFLICT (sku) DO NOTHING;
