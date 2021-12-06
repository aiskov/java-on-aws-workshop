[TOC]

## Create load balancer

* Go to `EC2/Network & Security/Security Groups`
* Click `Create Security Group`

```yaml
Security group name: Workshop-LB-SG
Description: Group for Load Balancer
VPC: <vpc-id>

Inbound rules:
  - Type: HTTP
    Protocol: TCP
    Port Range: 80
    Source: Anywhere 0.0.0.0/0

  - Type: HTTPS
    Protocol: TCP
    Port Range: 443
    Source: Anywhere 0.0.0.0/0

Outbound rules:
  - Type: All Traffic

Tags: 
  Role: Workshop
```

* Go to `EC2/Load Balancing/Target Group`
* Click `Create Target Group`

```yaml
Basic configuration:
	Choose a target type: Instances
	Target group name: product-service-nodes
  Protocol: TCP
  Port: 8080
  VPC: <vpc-id>
  Protocol version: HTTP1
  
Health checks:
	Health check protocol: TCP
	Health check path: /login
	
	Advanced health check settings:
		Port: Traffic port
    Healthy threshold: 3
    Unhealthy threshold: 3
    Timeout: 10 seconds
    Interval: 30 seconds
    Success codes: 200
	
Tags: 
  Role: Workshop
  
Available instances: 
	Instance: <ec2-instance-id>
```

* Go to `EC2/Load Balancing/Load Balancers`
* Load balancer types - Application Load Balancer.

```yaml
Load balancer name: workshop-lb
Scheme: Internal
IP address type: IPv4

Network mapping:
	VPC: <vpc-id>

	Mappings:
		eu-west-1a: <subnet-id-1>
		eu-west-1b: <subnet-id-2>
		
Security groups:
	- <elb-security-group-id>

Listeners and routing:
	- Protocol: HTTP
	  Port: 80
	  Forward to: product-service-nodes
	  
Add-on services:
	AWS Global Accelerator: No
	
Tags:
  Role: Workshop
```

* Wait until ELB `state` will be `Active`.
* Now you can use ELB - host and port `80` to access to the your service. 

## Store artifacts in S3
### Save artifact in S3

Upload manually

* Go to `S3/Buckets`
* Click  `Create bucket`

```yaml
General configuration:
	Bucket name: artifact-store-<some-unique-id>
	AWS Region: eu-west-1
	Copy settings from existing bucket: No

Object Ownership: ACLs disabled (recommended)
Block Public Access settings for this bucket: 
	Block all public access: Yes

Bucket Versioning: Enabled

Tags:
	Role: Workshop
	
Default encryption: Disable

Advanced settings:
	Object Lock: Disable
```

* Go to `S3/Bickets/<artifact-store-bucket>`
* Upload your artifact from project `target/products-1.jar` to root of the bucket.
* Wait until upload process finished.

#### Upload using command line

*Requires to have configured local client.*

```bash
aws s3 cp ./target/products-1.jar s3://artifact-store-c1e9d789d801/
```

### Download artifact from S3

#### Install client

* On your instance try to execute:

```bash
> sudo apt install -y awscli # install AWS command line client
> cd /opt/product-service/
# Try to download from S3
> aws s3 cp s3://artifact-store-c1e9d789d801/products-1.jar . 
fatal error: Unable to locate credentials
```

#### Configure Access

* Go to `IAM/Access Management/Policies`.
* Click `Create Policy`

```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "rule01",
            "Effect": "Allow",
            "Action": [
                "s3:GetObject"
            ],
            "Resource": "arn:aws:s3:::artifact-store-c1e9d789d801/products-1.jar"
        }
    ]
}
```

List is needed only to allow head operation, but because it role on bucket layer we should specify as resource whole bucket. TODO: Clarify

In additional we give full `GetObject*`  TODO: Clarify

* Then continue configuration

```yaml
Tags:
	Role: Workshop

Name: Product-Service-Artifact-Read
Description: Access to artifact of product service that could be used to 
             download it on instances
```

* Go to `IAM/Access Management/Roles`.

* Click `Create Role`

```yaml
AWS Service: Yes
Choose a use case: EC2

Attach permissions policies:
  - Product-Service-Artifact-Read
  
Set permissions boundary:
  Create role without a permissions boundary: Yes
  
Tags:
  Role: Workshop
  
Role name: Product-Service-Instance
Role description: Dedicated role for Product Service instances
```

* Go to `EC2/Instances`
* Select your instance and open `Security` tab.
* You should see empty field `IAM Role`

* Click `Actions/Security/Modify IAM role`
* Set `Product-Service-Instance` as `IAM role`

* Now you should be able to execute on instance

```yaml
> cd /opt/product-service/
> aws s3 cp s3://artifact-store-c1e9d789d801/products-1.jar .
> sudo systemctl restart app-product.service
```

## Externalize application configuration

* Go to `Systems Manager/Application Management/Parameter Store`
* Click `Create parameter`
* Transfer properties from the propertiws file to the 

```yaml
- Name: /product-service/prod/spring.datasource.url
	Tier: Standard
	Type: String
  Data Type: text
	Value: jdbc:mysql://<rds-endpoint>:3306/service_db

- Name: /product-service/prod/spring.datasource.username
	Tier: Standard
	Type: String
  Data Type: text
	Value: <rds-app-user>

- Name: /product-service/prod/spring.datasource.password
	Tier: Standard
	Type: SecureString
  KMS key source: My current account
  KMS key ID: alias/aws/ssm
	Value: <rds-app-password>

- Name: /product-service/prod/app.files.location
	Tier: Standard
	Type: String
  Data Type: text
	Value: /var/product-files/

- Name: /product-service/prod/logging.file.name
	Tier: Standard
	Type: String
  Data Type: text
	Value: /var/log/product-service.log
```





## Automate node configuration

TBD



### Create AMI
TBD

### Add User Data
TBD

## Store data in EFS
TBD

## Configure multiple instance
TBD 

## Configure auto scaling
TBD