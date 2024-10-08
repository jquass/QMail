# QMail
Privacy focused alternative to GMail

# Alias

GMail offers a way to use aliaes when providing your email address either by using periods (`ex.am.pl.e@mail.com`, `exam.ple@gmail.com`) or a plus sign (`example+one@gmail.com`, `example+two@gmail.com`).

Both of these appraches allow the recipienct of the email address to figure out your real address and just send to that instead (`example@gmail.com`).

This project intends to make it easy to generate aliases that don't expose the real address at the same time.

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

