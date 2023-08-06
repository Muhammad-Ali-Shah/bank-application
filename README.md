# Bank application
## Introduction
In this project, I created a mock **client-server bank application** where you can create different
bank accounts and simulate transactions between them. However, bank records have to be kept secure 
and transferred securely, without exposing data. So this project includes **RSA asymmetric encryption** to transfer 
data between the client and the server and **AES symmetric encryption** to store the records in an
encrypted format that cannot be decoded without the key. User records are stored in a 
**PostgreSQL** database. This project was coded in **Kotlin** using **JavaFX** for the GUI and 
**Java database connectivity** to access the database. **Kotlinx coroutines** were also used for
concurrent execution of server tasks.

## Sample credentials
### Master account 
This account holds **1,000,000,000** currency  
First Name: Master  
Last Name: Banker  
Date of Birth: 01/01/2001  
Sort code: 07-07-04  
Account Number: 91597560

### Other account
First name: John  
Last name: Doe  
Date of Birth: 04/08/2007  
Sort code: 07-07-04  
Account number: 89585803