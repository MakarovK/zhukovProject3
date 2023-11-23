insert into currencies(code, fullName, sign)
values
    ('AUD', 'Australian dollar', 'A$'),
    ('EUR', 'EURO', '€'),
    ('USD', 'American dollar', '$'),
    ('RUB', 'Russian ruble', '₽');


insert into exchangerates (baseCurrencyId, targetCurrencyId, rate)
values (1, 2, 0.67),  -- AUD к EUR
       (1, 3, 0.75),  -- AUD к USD
       (4, 2, 0.011), -- RUB к EUR
       (4, 3, 0.012), -- RUB к USD
       (2, 3, 1.18),  -- EUR к USD
       (2, 4, 90.2),  -- EUR к RUB
       (3, 4, 75.3); -- USD к RUB

select * from exchangerates;