~~~ mermaid
classDiagram
    Transaction

    Transaction "2" *-- Account

    class Transaction{
        +Double amount
        +String description
        +Account source
        +Account destination
        +String description
        +Date date
    }

    class Account{
        +Person owner
        +Double initial_balance
    }
~~~