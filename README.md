# Cybersecurity Project - 2021/2022

The authors of this project are Massimo Marcon - 17110 - and Ludovica Barozzi - 16851.
The project is a web application that allows users to send and receive emails. 
Starting from an unsecure implementation, several mechanisms were applied to improve its cybersecurity profile. 


## .env

Reuqired environment variables

```env
ACCEPT_EULA=Y
DB_USER=<username>
SA_PASSWORD=<password>
DRIVER_CLASS=com.microsoft.sqlserver.jdbc.SQLServerDriver
DB_URL="jdbc:sqlserver://localhost:1433;databaseName=examDB;encrypt=true;trustServerCertificate=true;"
```
## Installation

The application consists of three components:
- A server (Apache Tomcat)
- A client-side (Google Chrome browser)
- A MSSQL database (running in a Docker container)

## System Requirements
- Java JDK 11
- Docker/Docker Compose  ˆ20.10
- Google Chrome ˆ105
- Eclipse ˆ2021-12
- Apache Tomcat ˆ10.0.13

## Environment setup

#### Eclipse setup
1. Install Eclipse

2. Go to Help -> Install new software... -> Work with: "Latest Eclipse Simultaneous Release - https://download.eclipse.org/releases/latest"

3. In the tab below, expand the "Web, XML, Java EE and OSGi Enterprise Development" checkbox

4. Check the following elements:

	
	* Eclipse Java EE Developer Tools
	
	* Eclipse Java Web Developer Tools
	
	* Eclipse Java Web Developer Tools - JavaScript Support
	
	* Eclipse Web Developer Tools
	
	* Eclipse Web JavaScript Developer Tools
	
	* JST Server Adapters
	
	* JST Server Adapters Extension (Apache Tomcat)


	
5. Click Next two times, then accept the licence and click Finish

6. Restart Eclipse

# Project setup

#### Import the project from GitHub 

1. Download the project from GitHub link "https://github.com/ludobar99/cybersecurityProject.git"

2. Import it with Eclipse

3. Right click -> Configure -> Convert to Maven Project

4. Right click on the project -> Properties -> Deployment Assembly
	- Deploy `Java Build Path Entries` and `Maven dependencies` from source to deploy path
	
5. Click "Finish" -> "Apply and close"

6. Run the following command in the project directory (where the pom.xml file is)

```shell
mvn clean install package
``` 
> **_NOTE:_** If the project build path gives an error (if there is the exclamation mark on the project folder icon), righ click on the project -> go to Java Build Path -> remove the references to the missing libraries.

#### .env file

1. Create a new `assets` folder in the root directory
2. Create an `.env` file in the new `assets` with the properties listed at the top of this document

> **_NOTE:_**  a ready .env.example file has been provided for convenience

3. Right click on the project -> Properties -> Deployment Assembly
	- Add the `assets` folder to the `/` deploy path
	
#### Tomcat server setup
1. Go to Window -> Preferences -> Server -> Runtime Environments -> Add... -> Apache -> Apache Tomcat v10.0 -> Thick 'Create new a local server' -> Next

2. Click 'Download and install...', that should install the latest stable version (currently 10.0.13) -> Choose your favourite folder for Tomcat installation

3. Since now you can see your installed web servers in the Eclipse 'Server' tab, if it is not displayed by default, you can enable it by going to Window -> Show view -> Server

#### Database setup

1. Run docker-compose.dev.yml in the project root

```shell
docker-compose -f docker-compose.dev.yml up
``` 

2. Download DBeaver (Windows has an app in the Marketplace)

3. In DBeaver create a connection to SQL Server DB; go to Database -> New Database Connection -> Search for 'SQL Server' -> Insert DB_USER as username and SA_PASSWORD as password -> Click Finish

4. Under 'master', create a new DB by right-clicking on 'Databases' with name "examDB"

5. Create the DB tables used in the exam project. 
Right click on the newly created DB -> SQL Editor -> New SQL Script
Copy-Paste the SQL script in sqlScript.txt file -> Right click on the editor -> Execute -> Execute SQL Script

Now you are ready to run the Java web application! Send emails to your friends. >:(

## Security Considerations

Following are some general considerations on known weaknesses of the application and some of a couple of ideas to
improve its security.

#### Private Keys Storage
Private keys are saved unencrypted in local storage at the time of account creation. This means ach private key can be
easily retrieved by everyone who has access to the computer. This vulnerability has been left on purpose for the sake of
simplicity, but this problem could be tackled in a couple of ways:
  - The most simple solution would be to wrap the keys with a passphrase. The user's password could theoretically be
  a valid candidate for this job; the private key would then be stored in its wrapped form in local storage and kept
  unwrapped in memory once a successful login has occurred.
  - Another (better) solution would be to create an interface with an external key storage software which is under the
  user's control, thus removing the problem of saving the private keys in an insecure storage.

#### Single Key Pair
A single key pair is being used to both encrypt and sign the contents of an e-mail. This is not optimal and two different
key pairs should be used, one for each task. This is done to limit damage in the case of an eventual key theft, to assign
different validity to each key pair (signing is generally lasts longer) and more generally to separate concerns.

#### Key Rotation
Another improvement that one could implement is key rotation. Currently, a key pair is generated at account registration
and never changed again, but this is not good practice. Keys should have a set validity timeframe, after which the key
stops working and a new keypair is generated. This is usually done, as above, to limit exposure to an eventual key theft.



