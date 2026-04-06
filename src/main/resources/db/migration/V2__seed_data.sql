-- Seed mutual funds
INSERT INTO mutual_funds (id, name, category, current_nav) VALUES
    ('MF001', 'HDFC Flexi Cap Fund',              'Equity', 25.50),
    ('MF002', 'ICICI Prudential Bluechip Fund',    'Equity', 72.30),
    ('MF003', 'SBI Magnum Gilt Fund',              'Debt',   48.10),
    ('MF004', 'Axis Liquid Fund',                  'Debt',   2400.75),
    ('MF005', 'Kotak Balanced Advantage Fund',     'Hybrid', 15.80)
ON CONFLICT (id) DO NOTHING;

-- Seed users
INSERT INTO users (id, name) VALUES
    ('U001', 'Rahul Sharma'),
    ('U002', 'Priya Mehta'),
    ('U003', 'Amit Patel')
ON CONFLICT (id) DO NOTHING;

