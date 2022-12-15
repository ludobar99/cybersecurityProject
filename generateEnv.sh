#!/bin/bash

varPathToFile=/Users/marconms/Downloads/apache-tomcat-10.0.27/bin/.env
echo "creating .env file..."
echo $varPathToFile

touch $varPathToFile

echo "DB_USER=\"sa\"" > $varPathToFile
echo "SA_PASSWORD=\"Strong.Pwd-123\"" >> $varPathToFile
echo "DRIVER_CLASS=\"com.microsoft.sqlserver.jdbc.SQLServerDriver\"" >> $varPathToFile
echo "DB_URL=\"jdbc:sqlserver://localhost:1433;databaseName=examDB;encrypt=true;trustServerCertificate=true;\"" >> $varPathToFile

echo ".env file created."