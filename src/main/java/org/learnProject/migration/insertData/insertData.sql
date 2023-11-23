insert into currencies(code, fullName, sign)
values
    ('AUD', 'Australian dollar', 'A$'),
    ('EUR', 'EURO', '€'),
    ('USD', 'American dollar', '$'),
    ('RUB', 'Russian ruble', '₽');


insert into exchangerates (baseCurrencyId, targetCurrencyId, rate)
values (1, 2, 0.67),  -- AUD к EUR
       (3, 1, 1.52),  -- USD к AUD
       (4, 2, 0.011), -- RUB к EUR
       (4, 3, 0.012), -- RUB к USD
       (2, 3, 1.18),  -- EUR к USD
       (3, 4, 88.58); --  USD к RUB

select * from exchangerates;