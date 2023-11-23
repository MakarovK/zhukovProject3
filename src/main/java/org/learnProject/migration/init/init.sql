create table if not exists currencies (
    id serial primary key,
    code varchar(4) not null unique,
    fullName varchar(50) not null unique,
    sign varchar(4) not null unique
);

create table if not exists exchangeRates (
    id serial primary key,
    baseCurrencyId integer not null,
    targetCurrencyId integer not null,
    rate decimal(10, 6) not null,
    foreign key (baseCurrencyId) references currencies (id),
    foreign key (targetCurrencyId) references currencies (id)
);

comment on table currencies is 'Валюты';
    comment on column currencies.id is 'ID валюты';
    comment on column currencies.code is 'Код валюты';
    comment on column currencies.fullName is 'Полное имя валюты';
    comment on column currencies.sign is 'Символ валюты';

comment on table exchangeRates is 'Курсы валют';
    comment on column exchangeRates.id is 'ID курса обмена';
    comment on column exchangeRates.baseCurrencyId is 'ID базовой валюты';
    comment on column exchangeRates.targetCurrencyId is 'ID целевой валюты';
    comment on column exchangeRates.rate is 'Курс обмена единицы базовой валюты к единице целевой валюты';