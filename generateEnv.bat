@echo off

SET varPathToFile=\Applications\java-2022-03\Eclipse.app\Contents\MacOS\.env
echo "creating .env file in ... $varPathToFile"
touch "$varPathToFile"
echo "DB_USER="sa""  > $varPathToFile
echo "SA_PASSWORD="Strong.Pwd-123""  >> $varPathToFile
echo "DRIVER_CLASS="com.microsoft.sqlserver.jdbc.SQLServerDriver"" >> $varPathToFile
echo "DB_URL="jdbc:sqlserver://localhost:1433;databaseName=examDB;encrypt=true;trustServerCertificate=true;"" >> $varPathToFile
echo ".env file created."