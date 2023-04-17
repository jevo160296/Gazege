# Instances
~~~ mermaid
classDiagram
    Transaction "2" *-- Account
    Account *-- Person
    Budget *-- Category
    Transaction *-- Category

    class Transaction{
        +Int id
        +Double amount
        +String description
        +Account source
        +Account destination
        +Category category
        +Date date
        +Person aNombreDe
    }

    class Account{
        +Int id
        +Person owner
        +Double initial_balance
        +Account parent
        +Boolean includedInTotal
        +Boolean isIncome
        +Boolean isOutcome
    }

    class Person{
        +Int id
        +String name
        +Int importance
    }

    class Budget{
        +Category category
        +Double value
        +Int each
        +Int frequency
        +Int frequencyType
        +Int budgetType
        +Date startDate
    }

    class Category{
        +Int id
        +String name
        +Category parent
    }
~~~
