#!/bin/bash
if [ -e .env ]
then
    echo "Found .env file in current directory"
    cp .env $1/.env
    echo "Successfully copied .env file to destination"
else
    echo "Missing .env file in current directory!"
fi