# ConsumerApp

A Kafka consumer application implementing the exactly once semantic:

- Kafka cannot handle distributed transactions with an external system (e.g. DBMS)
- to achieve the exactly once semantic, a consumer does not rely on the built-in offset storage
- the results of the consumption are being stored in a relational database, storing the offset in the database as well can allow committing both the results and offset in a single transaction.

This application is just an experiment / showcase of how to use the Kafka API, but since it does not use an external storage, it cannot be used in a real situation.