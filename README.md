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
