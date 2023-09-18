INSERT INTO orders3 (
    SalesPerson_IsExternalLogonProvider,
    DeliveryMethod_DeliveryMethodName,
    DeliveryInstructions,
    OrderInternalComments,
    SalesPerson_OtherLanguages,
    Customer_CustomerValidFrom,
    OrderComments,
    Customer_CustomerFaxNumber,
    OrderID,
    Customer_StandardDiscountPercentage,
    Customer_WebsiteURL,
    Customer_IsOnCreditHold,
    Customer_RunPosition,
    CustomerPurchaseOrderNumber,
    Customer_CustomerName,
    SalesPerson_SalesPersonPhoneNumber,
    IsUndersupplyBackordered,
    SalesPerson_SearchName,
    Customer_IsStatementSent,
    Customer_CustomerLastEditedBy,
    SalesPerson_IsSalesperson,
    SalesPerson_SalespersonValidTo,
    SalesPerson_SalesPersonID,
    Customer_CustomerValidTo,
    Customer_CustomerID,
    Customer_DeliveryAddressLine2,
    Customer_DeliveryAddressLine1,
    DeliveryMethod_DeliveryMethodID,
    SalesPerson_FullName,
    Customer_CreditLimit,
    Customer_PostalAddressLine2,
    Customer_PostalAddressLine1,
    DeliveryMethod_DeliveryMethodLastValidTo,
    SalesPerson_SalesPersonFaxNumber,
    SalesPerson_IsSystemUser,
    Customer_PaymentDays,
    SalesPerson_PreferredName,
    Customer_CustomerPhoneNumber,
    SalesPerson_IsPermittedToLogon,
    Customer_AccountOpenedDate,
    DeliveryMethod_DeliveryMethodLastValidFrom,
    SalesPerson_SalespersonValidFrom,
    SalesPerson_UserPreferences,
    SalesPerson_EmailAddress,
    OrderDate,
    Customer_DeliveryRun,
    Customer_PostalPostalCode,
    Customer_DeliveryPostalCode,
    SalesPerson_SalesPersonCustomFields,
    ExpectedDeliveryDate,
    LastEditedWhen,
    SalesPerson_IsEmployee,
    SalesPerson_LogonName,
    SalesPerson_HashedPassword
)
VALUES (?, ?, ?, ?, ?, to_timestamp(?, 'YYYY-MM-DDTHH:MI:SSZ'), ?, ?, ?, ?::numeric, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, to_timestamp(?, 'YYYY-MM-DDTHH:MI:SSZ'), ?, to_timestamp(?, 'YYYY-MM-DDTHH:MI:SSZ'), ?, ?, ?, ?, ?, ?, ?, ?, to_timestamp(?, 'YYYY-MM-DDTHH:MI:SSZ'), ?, ?, ?, ?, ?, ?, to_timestamp(?, 'YYYY-MM-DDTHH:MI:SSZ'), to_timestamp(?, 'YYYY-MM-DDTHH:MI:SSZ'), to_timestamp(?, 'YYYY-MM-DDTHH:MI:SSZ'), ?, ?, to_timestamp(?, 'YYYY-MM-DDTHH:MI:SSZ'), ?, ?, ?, ?, to_timestamp(?, 'YYYY-MM-DDTHH:MI:SSZ'), to_timestamp(?, 'YYYY-MM-DDTHH:MI:SSZ'), ?, ?, ?);