Instruction for workshop
========================

- [Instruction for workshop](#instruction-for-workshop)
  - [How to use](#how-to-use)
  - [Legend](#legend)
  - [Steps](#steps)
    - [Step 1 - Run application locally hints](#step-1---run-application-locally-hints)
    - [Step 2 - Deploy application hints](#step-2---deploy-application-hints)
    - [Step 3 - Use multiple instance hints](#step-3---use-multiple-instance-hints)
    - [Step 4 - Use AWS services hints](#step-4---use-aws-services-hints)
    - [Step 5 - Introduce CI/CD](#step-5---introduce-cicd)
    - [Step 6 - Dockerize application](#step-6---dockerize-application)
    - [Step 7 - Configure monitoring, tracing & metrics](#step-7---configure-monitoring-tracing--metrics)
    - [Step 8 - Migrate to Cognito](#step-8---migrate-to-cognito)
    - [Step 9 - Extract part of functionality to Lambda](#step-9---extract-part-of-functionality-to-lambda)
    - [Step 10 - Configure image processing after upload](#step-10---configure-image-processing-after-upload)
    - [Step 11 - Migrate docker part to Kubernates](#step-11---migrate-docker-part-to-kubernates)
    - [Step 12 - Automate configuration](#step-12---automate-configuration)
      - [Option 1: Using CloudFormation](#option-1-using-cloudformation)
      - [Option 2: Using Terraform](#option-2-using-terraform)

## How to use
Try to go throw steps.

Solution described in directory `./hints`, but generally you shouldn't use
it until stuck.

Use [Work-notes](Work-notes.md) file to store parameters that you should keep in mind. When you use hints in some
places you will see placeholders `<...>` what generally mean that you should use some value saved in the Work Notes.

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

### Step 1 - Run application locally [hints](hints/step-1.md)
Goal is understood how the application works.  

**ToDo:**
1. Import project to IDE
1. Review code 
1. Run database from `./env`
1. Run application
1. Prepare executable jar file
1. Verify that jar is executable

### Step 2 - Deploy application [hints](hints/step-2.md)
Goal is to deploy application as it is.

**ToDo:**
1. Create RDS database. (Expected price: 15 USD/month)
   * Instance Class: db.t3.micro
   * Storage: General Purpose - 20GiB
   * Multi-AZ deployment: Do not create a standby instance
   * User: <rds-admin-user>
   * Password: <rds-admin-password>
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

**Control Questions:**
1. Describe configuration properties that will be different on production and why?
2. What could be a problem for that installation? 
3. How can we scale that application?
4. Is our data safe?

**Achievements:**
1. We have managed database, so we have simplified maintains.
2. Application deployed on cloud.
3. We have basic failover for cases when application failures or server could be restarted.

### Step 3 - Use multiple instance [hints](hints/step-3.md)
Goal is to create installation that deployed in several Availability Zones, use Load Balancer and Auto Scaling in order 
to keep high availability and adapt deployment to load. 

In order to minimize costs we will use spot instances.

Faster deployment will be done using AMI and User Data that will load last version of artifact from S3 artifact-store 
bucket. 

Data will be shared using network file system - EFS.

**ToDo:**
1. Create load balancer
   1. Target load balancer to instance
   2. Allow connection only from load balancer
2. Store artifacts in S3
   1. Bucket name: `artifact-store-<some-unique-id>`
   2. Put your jar to bucket
3. Externalize application configuration
   1. Use AWS Param Store in order to store properties 
   2. Use `org.springframework.cloud:spring-cloud-starter-aws-parameter-store-config`
   3. https://towardsaws.com/how-to-externalize-spring-boot-properties-to-an-aws-system-manager-parameter-store-2a945b1e856f
4. Automate node configuration
   1. Create AMI
   2. Add User Data
   3. Get jar from S3
5. Configure multiple instance and auto scaling
   1. One on demand instance
   2. +2 spot instances
   3. Set up scaling based rule:
      1. +1 when CPU > 80
      2. -1 when CPU < 30
   4. Apply rolling update by changing instance type 
   5. Ensure that user able normally login and use service with two servers.
6. Store data in EFS
   1. Mount EFS file system and configure to use it as storage in application.
   2. Update AMI in order to mount that file system automatically on 

**Control Questions:**
1. How do we able configure installation to scale before predicated high load?
2. How do wa able use param store with different environments?
3. How could we describe our deployment procedure?
4. How does scale in affects to users? 

**Achievements:**
1. We have application that able to scale and heal itself in case of node failure.
2. Node configured using external properties source that could be updated on the fly.
3. We are able to apply rolling updates on application.
4. We increase security because now users doesn't see concrete servers, but load balancer.

### Step 4 - Use AWS services [hints](hints/step-3.md)
Goal is to optimize work of the service using AWS services. 

We prefer to use S3 in order to minimize costs of storage, add versioning and shared links.

Use shared cache in order to provide better performance, and minimize amount of request to AWS API's from different
application instances.

**ToDo:**
1. Use S3 to store files
   1. Replace EFS store with S3
   2. Use `software.amazon.awssdk:s3`
   3. Download files from S3 using application as a proxy
   4. Upload files to S3 using application as a proxy
   5. Generate pre signed link to share files without authentication
   6. (Not ready) Enable versioning, allow receiving previous versions of the file.
2. Use Redis to cache response
   1. Add redis to local environment
   2. Use
      1. `org.springframework.boot:spring-boot-starter-data-redis`
      2. `org.springframework.boot:spring-boot-starter-cache`
   3. Implement caching of shared links with TTL 1day
3. Use Dynamo to keep session
   1. Remove stickiness
   2. Store session in Dynamo
4. Use SNS
   1. Send notification about new files uploaded.
   2. Receive email notification.
5. Use SQS
   1. Write request for image format to SQS
   2. Read and process request for image formatting from SQS

**Control Questions:**
1. TBD

**Achievements:**
1. TBD

### Step 5 - Introduce CI/CD
TBD

### Step 6 - Dockerize application
TBD

### Step 7 - Configure monitoring, tracing & metrics
TBD

### Step 8 - Migrate to Cognito
TBD

### Step 9 - Extract part of functionality to Lambda
TBD

### Step 10 - Configure image processing after upload
TBD

### Step 11 - Migrate docker part to Kubernates
TBD

### Step 12 - Automate configuration

#### Option 1: Using CloudFormation
TBD

#### Option 2: Using Terraform
TBD