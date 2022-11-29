@echo off

SET varPathToFile=\Applications\java-2022-03\Eclipse.app\Contents\MacOS\.env
echo "creating .env file..."
echo "%varPathToFile%"
touch "%varPathToFile%"
echo "DB_USER="sa"" REM UNKNOWN: {"type":"Redirect","op":{"text":">","type":"great"},"file":{"text":"$varPathToFile","expansion":[{"loc":{"start":0,"end":13},"parameter":"varPathToFile","type":"ParameterExpansion"}],"type":"Word"}}
echo "SA_PASSWORD="Strong.Pwd-123"" REM UNKNOWN: {"type":"Redirect","op":{"text":">>","type":"dgreat"},"file":{"text":"$varPathToFile","expansion":[{"loc":{"start":0,"end":13},"parameter":"varPathToFile","type":"ParameterExpansion"}],"type":"Word"}}
echo "DRIVER_CLASS="com.microsoft.sqlserver.jdbc.SQLServerDriver"" REM UNKNOWN: {"type":"Redirect","op":{"text":">>","type":"dgreat"},"file":{"text":"$varPathToFile","expansion":[{"loc":{"start":0,"end":13},"parameter":"varPathToFile","type":"ParameterExpansion"}],"type":"Word"}}
echo "DB_URL="jdbc:sqlserver://localhost:1433;databaseName=examDB;encrypt=true;trustServerCertificate=true;"" REM UNKNOWN: {"type":"Redirect","op":{"text":">>","type":"dgreat"},"file":{"text":"$varPathToFile","expansion":[{"loc":{"start":0,"end":13},"parameter":"varPathToFile","type":"ParameterExpansion"}],"type":"Word"}}
echo ".env file created."