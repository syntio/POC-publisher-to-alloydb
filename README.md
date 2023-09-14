# Ingesting data from SQLServer to AlloyDB using Dataphos Publisher

This repository contains Docker Compose, configuration, and source code files needed for running a data pipeline from an on-prem SQLServer database to GCP AlloyDB.

Docker Compose and configuration files for running Publisher are located in the [publisher](/publisher/) directory. 

An Apache Beam application for ingesting data from Pub/Sub to AlloyDB is located in the [dataflow](/dataflow/) directory. 

## Publisher

Dataphos Publisher is used to ingest data from an on-prem SQLServer database and publish business objects to Pub/Sub.

The business objects were formatted based on an example retail use case. The following is a truncated business object formatted by the Publisher:

```
{
   "Customer":{
      "AccountOpenedDate":"2013-01-01 00:00:00 +0000 UTC",
      "CustomerID":545,
      "CustomerLastEditedBy":"\u003cnil\u003e",
      "CustomerName":"Wingtip Toys (Jamison, IA)",
      "CustomerPhoneNumber":"(319) 555-0100",
      "CustomerValidFrom":"2013-01-01T00:00:00Z",
      "CustomerValidTo":"9999-12-31T23:59:59.9999999Z",
      "DeliveryAddressLine1":"Unit 90",
      "DeliveryAddressLine2":"1984 Esposito Road",
      "DeliveryPostalCode":"90010",
      ...
   },
   "CustomerPurchaseOrderNumber":"12781",
   "DeliveryInstructions":"null",
   "DeliveryMethod":{
      "DeliveryMethodID":3,
      "DeliveryMethodLastValidFrom":"2013-01-01T00:00:00Z",
      "DeliveryMethodLastValidTo":"9999-12-31T23:59:59.9999999Z",
      "DeliveryMethodName":"Delivery Van"
   },
   "ExpectedDeliveryDate":"2013-01-02 00:00:00 +0000 UTC",
   "IsUndersupplyBackordered":"true",
   "LastEditedWhen":"2013-01-02T11:00:00Z",
   "OrderDate":"2013-01-01 00:00:00 +0000 UTC",
   "OrderID":77,
   "OrderLines":[
      {
         "Description":"Halloween skull mask (Gray) L",
         "OrderLineID":147,
         "OrderLineLastEditedWhen":"2013-01-02T11:00:00Z",
         "PackageType":{
            "PackageTypeID":7,
            "PackageTypeName":"Each",
            "PackageTypeValidFrom":"2013-01-01T00:00:00Z",
            "PackageTypeValidTo":"9999-12-31T23:59:59.9999999Z"
         },
         "PickedQuantity":36,
         "PickingCompletedWhen":"2013-01-02T11:00:00Z",
         "Quantity":36,
         "StockItem":{
            "Barcode":"null",
            "Brand":"null",
            "LeadTimeDays":12,
            "MarketingComments":"null",
            "QuantityPerOuter":12,
            "Size":"L",
            "StockItemID":148,
            "StockItemName":"Halloween skull mask (Gray) L",
            ...
         },
         "TaxRate":"15.000000",
         "UnitPrice":"18.000000"
      },
      {
         "Description":"32 mm Anti static bubble wrap (Blue) 20m",
         "OrderLineID":146,
         "OrderLineLastEditedWhen":"2013-01-02T11:00:00Z",
         "PackageType":{
            "PackageTypeID":7,
            "PackageTypeName":"Each",
            "PackageTypeValidFrom":"2013-01-01T00:00:00Z",
            "PackageTypeValidTo":"9999-12-31T23:59:59.9999999Z"
         },
         "PickedQuantity":90,
         "PickingCompletedWhen":"2013-01-02T11:00:00Z",
         "Quantity":90,
         "StockItem":{
            "Barcode":"null",
            "Brand":"null",
            "IsChillerStock":"false",
            "LeadTimeDays":14,
            "MarketingComments":"null",
            "QuantityPerOuter":10,
            "Size":"20m",
            "StockItemID":172,
            "StockItemName":"32 mm Anti static bubble wrap (Blue) 20m",
            ...
         },
         "TaxRate":"15.000000",
         "UnitPrice":"48.000000"
      }
   ],
   "SalesPerson":{
      "EmailAddress":"anthonyg@wideworldimporters.com",
      "FullName":"Anthony Grosse",
      "HashedPassword":"/ظ8\ufffd\ufffdwxɐ\ufffdd\u0007:\ufffd\u003c\u000e\ufffd\u0001\ufffdc\ufffdj\ufffd\ufffdtW\ufffdi\u0018\u0019\ufffd",
      "IsEmployee":"true",
      "IsExternalLogonProvider":"false",
      "IsPermittedToLogon":"true",
      "IsSalesperson":"true",
      ....
   }
}
```

The bussines object represents an order. The order contains all relevant information regarding the customer, sales person, order lines, delivery information, and so on. Publisher provides data ready for business analytics.

### SQLServer source

Publisher fetches data from an SQLServer database deployed on a Hetzner machine. A truncated example retail dataset provided by [Microsoft](https://learn.microsoft.com/en-us/sql/samples/wide-world-importers-what-is?view=sql-server-ver16) was used. Relevant tables are: Orders, OrderLines, DeliveryMethod, Customer, People, StockItems.

### Configuration

Publisher configuration is located in the [configuration](/publisher/configuration/) directory.
The source is the SQLServer database and the destination is a Pub/Sub topic called "publisher-orders".

The Publisher worker (instance) is configured to fetch data in 1 month increments, starting from the earliest data in the source database ("2013-01-01 11:00:00"), group data by the order ID, and format the previously described busineess objects. Data is serialized using the Avro format and published to Pub/Sub.

### Environment
The [environment](/publisher/environment/) directory contains all required environment variables for Publisher to run. No configuration here is needed. However, [Postgres](/publisher/environment/environment_postgres.env) and [Publisher Metadata database](/publisher/environment/environment_metadata.env) could be configured with a different username/database name/password.

### Secrets

Publishing to Pub/Sub requires Publisher to authenticate with GCP. A GCP Service account with the "Pub/Sub Publisher" role.

Add a JSON service account key for that service account and download it. Paste the contents of the key to the [pubsub-sa-key.json](/publisher/secrets/pubsub-sa-key.json) file.


### Deployment

Deployment of Publisher is done using Docker Compose files. Before running deployment commands, open a terminal and position yourself in the [Publisher deployment](/publisher/deployment/) directory.

To create the Publisher infrastructure, run the following command:

```
docker-compose -f infrastructure.yaml up -d
```

The Publisher worker should be created after it is added in the Publisher Metadata database using the CLI.

To create the worker run the following command:

```
docker-compose -f worker.yaml up
```

After the worker processes all available data, it will run indefinitely in continous mode, until it is manually shut down. You could also define an "endFetchValue" in the instance configuration to stop the worker after it has processed data up to that point.

### CLI

To add the required configuration to Publisher metadata database, use the [CLI executable](/publisher/cli/publisher-cli.exe). The CLI connects to the Manager component of Publisher.

To add the configuration, position yourself in the "publisher" directory and run the following commands in order:

Create the source:
```
./cli/publisher-cli.exe source create ./configuration/source.yaml http://localhost:8080
```

Create the destination:
```
./cli/publisher-cli.exe destination create ./configuration/destination.yaml http://localhost:8080
```

Create the instance:
```
./cli/publisher-cli.exe instance create ./configuration/instance.yaml http://localhost:8080
```

Now, run the worker as previously decribed.
