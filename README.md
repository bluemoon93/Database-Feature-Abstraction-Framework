To store, update and retrieve data from database management systems (DBMS), software architects use tools, like call level interfaces (CLI), which provide standard functionality to interact with DBMS. These tools are designed to bring together the relational database and object-oriented programming paradigms, but the emergence of the NoSQL paradigm, and particularly new NoSQL DBMS providers, leads to situations where some of the standard functionality provided by CLI are not supported, very often due to their distance from the relational model or due to design constraints. As such, when a system architect needs to evolve, namely from a relational DBMS to a NoSQL DBMS, he must overcome the difficulties conveyed by the features not provided by the NoSQL DBMS. Not only that, but CLI usually forsake applied access control policies. As such, application developers must master the established policies as a means to develop software that is conformant with them. Choosing the wrong NoSQL DBMS risks major issues with applications requesting non-supported features and with unauthorized accesses. 

This project focuses on deploying features that are not so commonly supported by NoSQL DBMS, such as Stored Procedures, Transactions, SavePoints and interactions with local memory structures, through a framework based in a standard CLI. The feature implementation model is defined by modules of our framework, and allows for distributed and fault-tolerant systems to be deployed, which simulate the previously mentioned features and abstract the underlying database features from clients. It is also our goal to integrate our framework with previous work, S-DRACA, a dynamic secure access control architecture for relational applications, where permissions are defined as a sequence of create, read, update and delete expressions. With the integration, we can provide dynamic Role-Based Access Control and other security features to any kind of DBMS. We developed several ways of using each component (locally or distributed) and the framework is built in a modular fashion, which allows several components to be used individually or together, as well as extra features or DBMS to be added by system administrators that wish to adapt the framework to their particular needs.

DFAF (Database Feature Abstraction Framework):

	Requirements:
		- JDK 8
		- Netbeans (optional)

	Deployment:
		- Download and extract http://download.oracle.com/otn-pub/java/jdk/8u25-b17/jdk-8u25-linux-x64.tar.gz
		- "export JAVA_HOME=/home/bluemoon/jdk1.8.0_25/"
		- "java -version" should be 1.8.0_25	
		- Create a sample project using JDBC and accessing Hive, MongoDB, Redis, SQL Server or SQLite
		- Import and use the JDBC_Configurator project to have transactions, fault-tolerance, etc in your project

	Notes:
		- Can be used together with S-DRACA (creating R4N) or stand-alone
		- Refer to DFAF.pdf for more information



S-DRACA (Secure, Distributed and Dynamic Role-Based Access Control Arquitecture):

	Requirements:
		- SQL Server 2012 Express edition (other versions might require changes to the SQL Scripts' syntax)
		- JDK 8
		- Netbeans (optional, but recomended)
		- Visual Studio 2010 or later (To modify the PolicyWatcher - not needed otherwise)

	Deployment:	
		- Install SQL Server 2012 Express Edition with mixed authentication (to allow SQL Users).
		- Execute the Northwind and PolicyServer scripts.
		- Create the user account "dummy" with password "dummy" with credentials to only connect to the database.
		- Create a user account to access the database wth credentials to access and modify the created databases (Northwind and PolicyServer2).
		- Make sure the SQL Server DBMS is accessable via TCP (for JDBC connections).
		- Open Netbeans and open the DACA_* projects.
		- Change the account used to access the database in the DACA_PolicyConfigurator project to the one created.
		- Execute the DACA_PolicyConfigurator project to configure the PolicyServer2 with test data.
		- Change the run configurations of the DACA_PolicyManager so that the username and password of the account created with access and modify the databases are present (the first two parameters). Do the same in the DACA_Example project (main method of the Example class).
		- Compile every project except DACA_Example.
		- Run the DACA_PolicyManager project (required to compile the DACA_Example).
		- Compile the DACA_Example project. (Note that source code is automatically generated on this step. Netbeans might report errors when they no longer exist. If this is the case, try to modify the Example class and save it and wait some time. It might require some attemps until Netbeans detects the generated code). When modifying the DACA_Example project keep the DACA_PolicyManager running.
		- Execute the DACA_Example project for a sample execution of the framework.

	Notes:
		- DACA_Common contains common code required by many of the other projects and security classes, there might still be replicated code from earlier iterations from people before me...
		- DACA_BusinessManager is the core of the client application. It generates the source code of the Business Schemas in runtime and enforces the sequences.
		- DACA_PolicyAnnotation is the Java annotation that retrieves and generates the interfaces of the Business Schemas at compile time.
		- DACA_PolicyManager is the core of the server and mediates the communication between the databases and the client applications.
		- DACA_PolicyConfigurator is used to configure the PolicyServer with queries, roles, etc.
		- DACA_Example is an example of a client application. It also has a profilling class to perform performance tests.
		- Refer to S-DRACA.pdf for more information
