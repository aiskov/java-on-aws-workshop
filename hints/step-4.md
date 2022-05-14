- [Hints for step 4](#hints-for-step-4)
  - [Use S3 to store files](#use-s3-to-store-files)
    - [Create S3 bucket](#create-s3-bucket)
    - [Modify application](#modify-application)
    - [Add access rights](#add-access-rights)
    - [Update application](#update-application)
  - [Use Redis to cache response](#use-redis-to-cache-response)
    - [Modify application](#modify-application-1)
    - [Create security group](#create-security-group)
    - [Create redis instance](#create-redis-instance)
    - [Create configuration](#create-configuration)
    - [Refresh and deploy app](#refresh-and-deploy-app)
    - [Remove stickiness](#remove-stickiness)
    - [Store session in Redis](#store-session-in-redis)
  - [Use SNS](#use-sns)
    - [Send notification about new files uploaded.](#send-notification-about-new-files-uploaded)
    - [Receive email notification.](#receive-email-notification)
  - [Use SQS](#use-sqs)
    - [Write request for image format to SQS](#write-request-for-image-format-to-sqs)
    - [Read and process request for image formatting from SQS](#read-and-process-request-for-image-formatting-from-sqs)

# Hints for step 4

## Use S3 to store files

### Create S3 bucket

* Go to `S3`
* Click `Create bucket`

```yaml
Bucket name: workshop-<some-random-id> # workshop-5466573423167
AWS Region: EU (Ireland) eu-west-1
Object Ownership: ACLs disabled
Block all public access: Yes
Bucket Versioning: Disabled
Tags:
  Role: Workshop
Default encryption: Disabled
Advanced settings:
  Object Lock: Disable
```

### Modify application

* Check changes on `S3` branch.

### Add access rights

* Go to `IAM/Policies`
* Click `Create policy`

```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "Rule0",
            "Effect": "Allow",
            "Action": [
                "s3:PutObject",
                "s3:GetObject"
            ],
            "Resource": "arn:aws:s3:::<data-bucket>/*"
        }
    ]
}
```

```yaml
Tags:
    Role: Workshop

Name: Product-Service-S3
Description: Access to S3 product service.
```

* Go to `IAM/Roles`
* Find and open `Product-Service-Instance`
* Click `Add permissions/Attach policies`
* Find and select `Product-Service-S3`
* Click `Attach policies`

### Update application

* Build new version of application.

```shell
mvn clean package
aws s3 cp ./target/products-1.jar s3://<artifact-store-bucket>/
```

* Go to `EC2/Auto Scaling/Auto Scaling Groups`
* Open `Workshop-Product-SG`
* Click `Edit` in `Launch template`
* Switch version to previous one and save.
* Open `Instance refresh` tab
* Click `Start instance refresh` and proceed 

## Use Redis to cache response

### Modify application

* Check changes on `redis` branch.

### Create security group

* Go to `EC2/Network & Security/Security Groups`
* Click `Create security group`

```yaml
Basic:
  Security group name: workshop-cache-security-group
  Description: Redis workshop cache security group

Inbound rules:
  - Type: Custom TCP
    Port Range: 6369
    Source: <ec2-security-group>
    Description: Access from ec2 to redis
```

### Create redis instance

* Go to `ElastiCache/Redis`
* Click `Create`

```yaml
Cluster engine: Redis
Cluster mode enabled: false

Location: Amazon Cloud

Redis settings:
  Name: workshop-cache
  Description: Workshop cache instance
  Engine version compatibility: 6.2
  Port: 6379
  Parameter group: default.redis6x
  Node type: cache.t3.micro
  Number of replicas: 1
  Multi-AZ: false
  
Advanced Redis settings:
  Subnet group: Create new
  Name: workshop-cache-subnet-group
  Description: Workshop cache subnet group
  VPC ID: <vpc-id>
  Subnets:
    <subnet-id-1>: true
    <subnet-id-2>: true
  Availability zones placement: No preference

Security:
  Security groups: default (sg-1f0a255d)
  Encryption at-rest: false
  Encryption in-transit: true

Logs:
  Slow log: false
  Engine log: false

Import data to cluster: None

Backup:
  Enable automatic backups: false

Maintenance:
  Maintenance window: No preference
  Topic for SNS notification: Disable notifications
  
Tags:
  Role: Workshop
```

### Create configuration

* Go to `AWS Systems Manager/Application Management/Parameter Store`
* Click `Create parameter`

```yaml
- Name: /config/product-service_prod/spring.redis.host
    Tier: Standard
    Type: String
  Data Type: text
    Value: <redis-master-host>
      
- Name: /config/product-service_prod/spring.redis.database
    Tier: Standard
    Type: String
  Data Type: text
    Value: 1   
    
- Name: /config/product-service_prod/spring.redis.password
    Tier: Standard
    Type: String
  Data Type: text
    Value: <redis-password>

- Name: /config/product-service_prod/spring.redis.ssl
    Tier: Standard
    Type: String
  Data Type: text
    Value: true
```

### Refresh and deploy app

* Build new version of application.

```shell
mvn clean package
aws s3 cp ./target/products-1.jar s3://<artifact-store-bucket>/
```

* Go to `EC2/Auto Scaling/Auto Scaling Groups`
* Open `Workshop-Product-SG`
* Click `Edit` in `Launch template`
* Switch version to previous one and save.
* Open `Instance refresh` tab
* Click `Start instance refresh` and proceed

### Remove stickiness

Go to `EC2/Load Balancing/Target Groups`.
* Click on `product-service-nodes` in order to open details.
* Open `Attributes` tab and click `Edit`.

```yaml
Deregistration delay: 300 seconds
Slow start duration: 0 seconds

Load balancing algorithm:
  Round robin: Yes

Stickiness:
  Stickiness type: Load balancer generated cookie

Stickiness duration: 8 hours
```

* Verify that login still works.

## Use SNS
### Send notification about new files uploaded.
TBD

### Receive email notification.
TBD

## Use SQS
### Write request for image format to SQS
TBD

### Read and process request for image formatting from SQS
TBD
