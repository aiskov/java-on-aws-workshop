Instruction for workshop
========================

[TOC]

## How to use

Try to go throw steps.

Solution described in directory `./hints`, but generally you shouldn't use
it until stuck.

## Legend

You have application that implemented as monolith that contains front and backend.

Your task is
1. Deploy it 
1. Simplify maintains. 
1. Make it high available
1. Improve performance (via scaling)
1. Make it auto scalable
1. Configure logs aggregations 
1. Configure tracing 
1. Configure self-healing
1. Configure configure alerts

## Steps

Here described steps to pass workshop:

### Step 1 - Run application locally

Goal is understood how application works.  



**ToDo:**

1. Import project to IDE
1. Review code 
1. Run database from `./env`
1. Run application
1. Prepare executable jar file
1. Verify that jar is executable

### Step 2 - Deploy application 

Goal is to deploy application as it is.



**ToDo:**

1. Create RDS database. (Expected price: 15 USD/month)
   * Instance Class: db.t3.micro
   * Storage: General Purpose - 20GiB
   * Multi-AZ deployment: Do not create a standby instance
   * User: admin
   * Password: akyBULhfbzQpqUw-wuvwDL7D2MsCMi3e
   * Tags:
      * Role: Workshop
1. Create EC2 instance, install java.
   * Open ports: 8080, 22
   * Without additional disks
   * Tags:
      * Role: Workshop
1. Modify DB Security Role to allow access from ec2 instance to database
1. Create schema & fill it with test data
   * Dedicated user for application
   * Execute `env/data.sql`
1. Add additional disk to EC2 instance
   1. 10GiB mounted as `/var/product-files`
   1. Type gp3
   1. Encrypted with `(default) aws/ebs`
   1. https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/ebs-using-volumes.html

1. Upload Jar
   1. Directory: `/opt/product-service`

1. Start application
1. Make service from application
   1. https://www.baeldung.com/linux/run-java-application-as-service
   1. https://stackoverflow.com/questions/62832339/systemd-service-wont-start-after-reboot
   1. Make ip static using elastic IP 


Control Questions:
1. Describe configuration properties that will be different on production and why?

Achievements:

1. We have managed database, so we have simplified maintains of installation.
1. Application deployed on cloud.

### Step 3 - Use multiple instance

1. Create load balancer
2. Store artifacts in S3
3. Automate node configuration
   1. Create AMI
   2. Add User Data
4. Store data in EFS
5. Configure multiple instance
   1. One on demand instance
   2. +2 spot instances
6. Configure auto scaling

### Step 4 - Introduce CI/CD 

TBD

### Step 5 - Use caches

TBD

### Step 6 - Migrate to S3 object store

TBD

### Step 7 - Dockerize application

TBD

### Step 8 - Configure monitoring, tracing & metrics

TBD

### Step 9 - Migrate to Cognito

TBD

### Step 10 - Extract part of functionality to Lambda

TBD

### Step 11 - Configure image processing after upload

TBD

### Step 12 - Migrate docer part to Kubernates

TBD
