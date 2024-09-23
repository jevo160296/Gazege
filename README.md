# Gazege
App para gestionar gastos.

# Guía para contribuir
1. Crear una nueva rama en donde se harán los cambios
2. Antes de realizar pull request, se debe incrementar la versión de la app
3. Hacer pull request a main indicando los cambios que se realizaron
4. Crear los apk build de release y debug
5. Cargar los apk en un nuevo release indicando la versión

# ERD
~~~ mermaid
erDiagram
    TRANSACTION
    PROMISSORY_NOTE
    ACCOUNT
    BUDGET
    CATEGORY
    PERSON
    TRANSACTION 1+--1 ACCOUNT : source
    TRANSACTION 1+--1 ACCOUNT : destination
    TRANSACTION 1+--1 CATEGORY : has
    TRANSACTION 1+--1 PERSON : as
    ACCOUNT 1+--1 PERSON : owns
    ACCOUNT 1--1 ACCOUNT : parent
    BUDGET 1+--1 CATEGORY : has
    CATEGORY  1--1 CATEGORY : parent
    PROMISSORY_NOTE 1--1 PERSON : source
    PROMISSORY_NOTE 1--1 PERSON : destination
  ~~~
