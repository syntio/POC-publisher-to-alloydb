-- Number of sales per salesperson for each year
SELECT o.salesperson_fullname AS sales_person_name, date_part('YEAR', o.lasteditedwhen) AS year_of_sale, count(*) AS number_of_sales
FROM orders3 o
GROUP BY salesperson_fullname, date_part('YEAR', lasteditedwhen)
ORDER BY number_of_sales DESC;

-- Total amount spent by each customer per month in 2020
SELECT date_part('MONTH', o.lasteditedwhen) AS month_of_year, customer_customername AS customer_name, sum(ol.unitprice) AS total_expenses
FROM orders3 o
INNER JOIN orderlines3 ol ON o.orderid = ol.orderid
WHERE date_part('YEAR', o.lasteditedwhen) = 2020
GROUP BY date_part('MONTH', o.lasteditedwhen), o.customer_customername 
ORDER BY month_of_year;

-- Total amount spent by each customer per year
SELECT date_part('YEAR', o.lasteditedwhen) AS purchase_year, customer_customername AS customer_name, sum(ol.unitprice) AS total_expenses
FROM orders3 o
INNER JOIN orderlines3 ol ON o.orderid = ol.orderid
GROUP BY date_part('YEAR', o.lasteditedwhen), o.customer_customername 
ORDER BY purchase_year;