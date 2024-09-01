# QMail
Privacy focused alternative to GMail

# Dependencies

This project is designed to run on AWS. 

It starts by using SES to recieve emails and store them in S3.

This project includes a job to read the emails from S3, process them, and store them in DynamoDB.

The UI is an Angular App that's being hosted on S3 as a static website.

The Docker container uses [swag](https://docs.linuxserver.io/general/swag/) with a reverse proxy to provide HTTPS

## Usage

Copy .env.example file to .env and fill out details

`mvn package`

`docker compose up web`

If running in production, also start swag

`docker compose up swag`

