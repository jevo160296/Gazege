# Instances
~~~ mermaid
classDiagram
    Transaction

    Transaction "2" *-- Account
    Account "1" *-- Person

    class Transaction{
        +Int id
        +Double amount
        +String description
        +Account source
        +Account destination
        +Date date
    }

    class Account{
        +Int id
        +Person owner
        +Double initial_balance
    }

    class Person{
        +Int id
        +String name
    }
~~~
