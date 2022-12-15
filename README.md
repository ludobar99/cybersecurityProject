# README

# Cybersecurity Project - 2021/2022

The authors of this project are Massimo Marcon - 17110 - and Ludovica Barozzi - 16851.
The project is a web application that allows users to send and receive emails. 
Starting from an unsecure implementation, several mechanisms were applied to improve its cybersecurity profile. 


## .env

Reuqired environment variables and defaults

```env
ACCEPT_EULA=Y
DB_USER=sa
SA_PASSWORD=Strong.Pwd-123
DRIVER_CLASS=com.microsoft.sqlserver.jdbc.SQLServerDriver
DB_URL=jdbc:sqlserver://localhost:1433;databaseName=examDB;encrypt=true;trustServerCertificate=true;
```
## Installation

The application consists of three components:
- A server (Apache Tomcat 10.0.13)
- A client-side (Google Chrome browser)
- A MSSQL database (running in a Docker container)

# Environment setup

#### Java version
1. Install Java JDK 11

#### Eclipse setup
1. Install Eclipse latest version (currently 2021-12)

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

#### Tomcat server setup
1. Go to Window -> Preferences -> Server -> Runtime Environments -> Add... -> Apache -> Apache Tomcat v10.0 -> Thick 'Create new a local server' -> Next

2. Click 'Download and install...', that should install the latest stable version (currently 10.0.13) -> Choose your favourite folder for Tomcat installation

3. Since now you can see your installed web servers in the Eclipse 'Server' tab, if it is not displayed by default, you can enable it by going to Window -> Show view -> Server

# Project setup

#### Import the project from GitHub 

1. Download the project from GitHub link "https://github.com/ludobar99/cybersecurityProject.git"

2. Import it with Eclipse

3. Right click -> convert project in Maven project

4. Right click on the project -> Properties -> Deployment Assembly -> ***add maven***

#### Database setup

1. Run docker-compose.dev.yml in the project root

```shell
docker-compose -f docker-compose.dev.yml up
``` 

