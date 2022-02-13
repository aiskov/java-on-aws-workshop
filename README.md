Instruction for workshop
========================

- [Instruction for workshop](#instruction-for-workshop)
  - [How to use](#how-to-use)
  - [Legend](#legend)
  - [Steps](#steps)
    - [Step 1 - Run application locally hints](#step-1---run-application-locally-hints)
    - [Step 2 - Deploy application hints](#step-2---deploy-application-hints)
    - [Step 3 - Use multiple instance hints](#step-3---use-multiple-instance-hints)
    - [Step 4 - Introduce CI/CD](#step-4---introduce-cicd)
    - [Step 5 - Use caches](#step-5---use-caches)
    - [Step 6 - Migrate to S3 object store](#step-6---migrate-to-s3-object-store)
    - [Step 7 - Dockerize application](#step-7---dockerize-application)
    - [Step 8 - Configure monitoring, tracing & metrics](#step-8---configure-monitoring-tracing--metrics)
    - [Step 9 - Migrate to Cognito](#step-9---migrate-to-cognito)
    - [Step 10 - Extract part of functionality to Lambda](#step-10---extract-part-of-functionality-to-lambda)
    - [Step 11 - Configure image processing after upload](#step-11---configure-image-processing-after-upload)
    - [Step 12 - Migrate docker part to Kubernates](#step-12---migrate-docker-part-to-kubernates)
    - [Step 13 - Automate configuration](#step-13---automate-configuration)
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
1. What could be a problem for that installation? 
1. How we can scale that application?
1. Is our data safe?

**Achievements:**
1. We have managed database, so we have simplified maintains.
1. Application deployed on cloud.
1. We have basic failover for cases when application failures or server could be restarted.

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
   2. https://towardsaws.com/how-to-externalize-spring-boot-properties-to-an-aws-system-manager-parameter-store-2a945b1e856f
4. Automate node configuration
   1. Create AMI
   2. Add User Data
   3. Get jar from S3
5. Store data in EFS
6. Configure multiple instance
   1. One on demand instance
   2. +2 spot instances
7. Configure auto scaling

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

### Step 12 - Migrate docker part to Kubernates

TBD

### Step 13 - Automate configuration

#### Option 1: Using CloudFormation
#### Option 2: Using Terraform