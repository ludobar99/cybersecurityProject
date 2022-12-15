@echo off

IF "-e" ".env" (
  echo "Found .env file in current directory"
  COPY  ".env" "%~1\.env"
  echo "Successfully copied .env file to destination"
) ELSE (
  echo "Missing .env file in current directory!"
)