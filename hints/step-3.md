- [Hints for step 3](#hints-for-step-3)
  - [Create load balancer](#create-load-balancer)
    - [Security group for load balancer](#security-group-for-load-balancer)
    - [Target for load balancer](#target-for-load-balancer)
    - [Load balancer configuration](#load-balancer-configuration)
    - [Allow connection only from load balancer](#allow-connection-only-from-load-balancer)
  - [Store artifacts in S3](#store-artifacts-in-s3)
    - [Create bucket](#create-bucket)
    - [Upload artifact manually](#upload-artifact-manually)
    - [Upload artifact using command line](#upload-artifact-using-command-line)
    - [Download artifact from S3](#download-artifact-from-s3)
      - [Install client](#install-client)
      - [Configure Access](#configure-access)
  - [Externalize application configuration](#externalize-application-configuration)
    - [Create properties](#create-properties)
    - [Modify application to connect](#modify-application-to-connect)
    - [Verify that properties loaded from AWS](#verify-that-properties-loaded-from-aws)
    - [Refresh properties without restart](#refresh-properties-without-restart)
    - [Stop using app.properties on server](#stop-using-appproperties-on-server)
  - [Automate node configuration](#automate-node-configuration)
    - [Create AMI](#create-ami)
      - [Cleanup server](#cleanup-server)
      - [Create instance with user data](#create-instance-with-user-data)
  - [Configure multiple instance and auto scaling](#configure-multiple-instance-and-auto-scaling)
    - [Clean up](#clean-up)
    - [Create autoscaling group](#create-autoscaling-group)
    - [Apply rolling update by changing instance type](#apply-rolling-update-by-changing-instance-type)
    - [Set up usage of spot instances](#set-up-usage-of-spot-instances)
    - [Set up scaling based rule](#set-up-scaling-based-rule)
      - [Verify scaling](#verify-scaling)
      - [Enable stickiness](#enable-stickiness)
  - [Store data in EFS](#store-data-in-efs)
    - [Mount to instances](#mount-to-instances)

# Hints for step 3

## Create load balancer

### Security group for load balancer
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

### Target for load balancer
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

### Load balancer configuration
* Go to `EC2/Load Balancing/Load Balancers`
* Load balancer types - Application Load Balancer.

```yaml
Load balancer name: Workshop-LB
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
* Now you can use ELB - host and port `80` to access to your service. 

### Allow connection only from load balancer
* Go to `EC2/Network & Security/Security Groups`
* Choose group with name `Workshop-Product-App-SG`
* Click `Actions/Edit inbound rules`
* Change rules to achieve:

```yaml
Inbound Rules:
  -
    Type: SSH
    Protocol: TCP
    Port Range: 22
    Source: 0.0.0.0/0
    Description: SSH port

  -
    Type: Custom TCP Rule
    Protocol: TCP
    Port Range: 8080
    Source: <elb-security-group-id>
    Description: Application port for ELB
```

* Try to load page using elastic IP address. It should fail with `ERR_CONNECTION_TIMED_OUT` in chrome.
* Try to load page using ELB

## Store artifacts in S3

### Create bucket
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

### Upload artifact manually
Upload manually

* Go to `S3/Bickets/<artifact-store-bucket>`
* Upload your artifact from project `target/products-1.jar` to root of the bucket.
* Wait until upload process finished.

### Upload artifact using command line
*Requires to have configured local client.*

```bash
> aws s3 cp ./target/products-1.jar s3://<artifact-store-bucket>/
```

### Download artifact from S3

#### Install client
* On your instance try to execute:

```bash
> sudo apt install -y awscli # install AWS command line client
> cd /opt/product-service/
# Try to download from S3
> aws s3 cp s3://<artifact-store-bucket>/products-1.jar . 
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
            "Resource": "arn:aws:s3:::<artifact-store-bucket>/products-1.jar"
        }
    ]
}
```

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
> aws s3 cp s3://<artifact-store-bucket>/products-1.jar .
> sudo systemctl restart app-product.service
```

## Externalize application configuration

### Create properties
* Go to `Systems Manager/Application Management/Parameter Store`
* Click `Create parameter`
* Transfer properties from the properties file to the AWS Param Store

```yaml
- Name: /config/product-service_prod/spring.datasource.url
    Tier: Standard
    Type: String
  Data Type: text
    Value: jdbc:mysql://<rds-endpoint>:3306/service_db

- Name: /config/product-service_prod/spring.datasource.username
    Tier: Standard
    Type: String
  Data Type: text
    Value: <rds-app-user>

- Name: /config/product-service_prod/spring.datasource.password
    Tier: Standard
    Type: SecureString
  KMS key source: My current account
  KMS key ID: alias/aws/ssm
    Value: <rds-app-password>

- Name: /config/product-service_prod/app.files.location
    Tier: Standard
    Type: String
  Data Type: text
    Value: /var/product-files/

- Name: /config/product-service_prod/logging.file.name
    Tier: Standard
    Type: String
  Data Type: text
    Value: /var/log/product-service.log
```

### Modify application to connect
Modify `pom.xml`

```xml
    <properties>
...
        <spring-cloud.version>2020.0.4</spring-cloud.version>
    </properties>

...
 
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

...
  <dependencies>
    ...
    <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-bootstrap</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-aws-parameter-store-config</artifactId>
            <version>2.2.6.RELEASE</version>
        </dependency>
    ...
  </dependencies>
...
```

Add `src/main/resources/bootstrap.yaml`

```yaml
aws:
  paramstore:
    enabled: true # Switch it to false if you want to work offline
    name: product-service
    prefix: /config
    profileSeparator: _
```

### Verify that properties loaded from AWS
* Add new property

```yaml
- Name: /config/product-service/server.port
    Tier: Standard
    Type: String
  Data Type: text
    Value: 8888
```

* Start application locally, it should start to use 8888 port.

### Refresh properties without restart
* Modify `application.yaml` to expose `refresh` endpoint. 

```yaml
management:
  endpoints:
    web:
      exposure:
        include: env, refresh
```

* Start application locally.
* Change `server.port` to `9999`

```yaml
- Name: /config/product-service/server.port
    Tier: Standard
    Type: String
  Data Type: text
    Value: 9999
```

* Add new property

```yaml
- Name: /config/product-service/app.version
    Tier: Standard
    Type: String
  Data Type: text
    Value: TEST
```

* Refresh context

```bash
> curl -X POST http://127.0.0.1:8888/actuator/refresh
["app.version", "server.port"]
```

* Now you could check that port and version shown after login is still the same.
* Update java code to resolve properties dynamically 

```java
Index: src/main/java/com/aiskov/aws/products/domain/ProductController.java
IDEA additional info:
Subsystem: com.intellij.openapi.diff.impl.patch.CharsetEP
<+>UTF-8
===================================================================
diff --git a/src/main/java/com/aiskov/aws/products/domain/ProductController.java b/src/main/java/com/aiskov/aws/products/domain/ProductController.java
--- a/src/main/java/com/aiskov/aws/products/domain/ProductController.java    (revision 0665a55e95fcff0326a419dfe26ea5f995e54dee)
+++ b/src/main/java/com/aiskov/aws/products/domain/ProductController.java    (date 1638826059656)
@@ -2,6 +2,7 @@
 
 import lombok.RequiredArgsConstructor;
 import org.springframework.beans.factory.annotation.Value;
+import org.springframework.core.env.Environment;
 import org.springframework.core.io.FileSystemResource;
 import org.springframework.http.ResponseEntity;
 import org.springframework.stereotype.Controller;
@@ -28,8 +29,7 @@
     @Value("${app.files.location}")
     private String filesLocation;
 
-    @Value("${app.version}")
-    private String appVersion;
+    private final Environment environment;
 
     @GetMapping("/")
     public ModelAndView list() {
@@ -37,7 +37,7 @@
 
         modelAndView.setViewName("list.html");
         modelAndView.addObject("products", this.productService.getProducts());
-        modelAndView.addObject("version", this.appVersion);
+        modelAndView.addObject("version", this.environment.getProperty("app.version", "-"));
         modelAndView.addObject("hostname", HOST_NAME);
 
         return modelAndView;


```
* Restart application
* Remove properties:
  * `/config/product-service/app.version`
  * `/config/product-service/server.port`
  
* Refresh context

```bash
> curl -X POST http://127.0.0.1:9999/actuator/refresh
["app.version", "server.port"]
```

* Now you should see that up version is updated and server port is still the same.

### Stop using app.properties on server
On EC2 instance modify file `/etc/systemd/system/app-product.service` to replace 
`--spring.config.import=app.properties` with `--spring.profiles.active=prod`

**NB:** *You need to add `--add-opens java.base/java.lang=ALL-UNNAMED` to the 
java parameters because of bug in the AWS library, otherwise you could get 
`InaccessibleObjectException`*

```properties
[Unit]
Description=Product service application
After=syslog.target network.target

[Service]
SuccessExitStatus=143

User=root
Group=ubuntu

Type=simple

WorkingDirectory=/opt/product-service
ExecStart=java -jar --add-opens java.base/java.lang=ALL-UNNAMED products-1.jar --spring.profiles.active=prod
ExecStop=/bin/kill -15 $MAINPID

Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

* Go to `IAM/Access Management/Policies`.
* Click `Create Policy`

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "Rule0",
      "Effect": "Allow",
      "Action": "ssm:GetParametersByPath",
      "Resource": [
        "arn:aws:ssm:eu-west-1:669171167111:parameter/config/product-service*",
        "arn:aws:ssm:eu-west-1:669171167111:parameter/config/application*"
      ]
    }
  ]
}
```

* Then continue configuration

```yaml
Tags:
    Role: Workshop

Name: Product-Service-Params-Load
Description: Access to parameters of product service.
```

* Go to `IAM/Access Management/Roles`.
* Find `Product-Service-Instance`, and click on it.
* Click `Attach policies`.
* Find `Product-Service-Params-Load` and select it.
* Click `Attach policy` at the bottom of the screen.

On your instance execute

```bash
> aws --region eu-west-1 ssm get-parameters-by-path --path /config/product-service_prod
> aws --region eu-west-1 ssm get-parameters-by-path --path /config/product-service
```

Now you see that properties could be loaded from the node. 

```bash
> sudo systemctl daemon-reload # Reload needed to refresh configuration
> sudo systemctl restart app-product.service # Restart service
```

## Automate node configuration

### Create AMI

#### Cleanup server
* Go to `EC2/Instances`
* Select your instance `<ec2-instance-id>`
* Open storage tab
* Choose root device (it should have device name `/dev/sda1` and size 8 Gib) and click on volume id.
* One volumes list mark it as selected
* Click on `Actions/Create snapshot`

```yaml
Description: Snapshot for AMI

Tags:
  Role: Workshop
```

* Go to `EC2/Elastic Block Storage/Snapshots`
* Wait until snapshot status will be `Completed` (Generally it takes some time).
* Select snapshot
* Click on `Actions/Create image from snapshot`

```yaml
Image name: Workshop-Product-App-AMI-v01
Description: Image for product app
Architecture: x86_64
Root device name: /dev/sda1
Virtualization type: hardware-assist technology
Boot mode: Use default

Block device mappings:
  - 
    Name: Volume 1
    Device type: Root
    Device name: /dev/sda1
    Size (GiB): 8
    Volume type: General Purpose SSD (gp2)
    Termination behavior: Delete on termination
````

* *Optional:* Try to run new instance from AMI without instance data.
  * On screen `Choose AMI` in process of creation of new instance you should choose `My AMIs` on left menu.
  * Then process will be the same as before. 
  * You should be able to connect to the server using ssh and check that all required files on the place, but service 
    shouldn't be started because of lack of role. 

### Add User Data

#### Prepare user data
```bash
#!/bin/bash
aws s3 cp s3://<artifact-store-bucket>/products-1.jar /opt/product-service/
systemctl restart app-product.service
```

* Then make change in `src/main/resources/application.yaml` to have `app.version` to `0.2`.
* Build and upload new artifact version to the server.

```bash
> mvn clean package
...
> aws s3 cp ./target/products-1.jar s3://<artifact-store-bucket>/
```

#### Create instance with user data

* Go to `EC2/Instances` & click `Launch instances`
* Left panel: `Ny AMis`
* Click `Select` button on `Workshop-Product-App-AMI-v01`
* Choose `t2.micro` type and click 'Next: Configure Instance Details'

```yaml
Number of instances: 1

Purchasing option: 
  Request Spot instances: No

Network: vpc-xxxxx | default (default)
Subnet: No preferences # For now, it doesn't matter
Auto-assign Public IP: Use subnet networks

Placement group:
  Add instance to placement group: No

Capacity Reservation: Open # We have no reservation

Domain join directory: No directory # We have no configuration
IAM role: Product-Service-Instance 
```

* Click `Next: Add Storage`
* Keep as it is with 8 GiB disk.
* Click `Next: Add tag` and add

```yaml
Role: Workshop
```

* Click `Next: Configure Security Group`
* Choose `Assign a security group: Select an existing security group`
* Select `<ec2-security-group-id>`
* Click `Review and Launch`
* Click `Launch` and acknowledge access to SSH keys.
* Go to `EC2/Instances` and wait until your instances will be accessible.
* Go to `EC2/Load Balancing/Target Groups`
* Select `product-service-nodes` and open `Targets` tab
* Remove current instance from target by clicking deregister with selected instance.
* Click `Register targets`
* And add new target created from AMI.

## Configure multiple instance and auto scaling

### Clean up

* Go to `EC2/Instances`
* Select all instances created for workshop.
* Click `Instance State/Terminate Instance`

### Create autoscaling group

* Go to `EC2/Launch Templates`
* Click `Create launch template` and fill form

```yaml
Launch template name: Workshop-Product-Template
Template version description: Init
Auto Scaling guidance: Yes

Template tags:
  Role: Workshop

Source template: No

Launch template contents:
  Application and OS Images: 
    My AMIs:
      Amazon Machine Image (AMI): Workshop-Product-App-AMI-v01

Instance type Info:
  Instance type: t2.micro

Key pair (login):
  Key pair name: <Your key>

Network settings:
  Subnet: Don't include in launch template # We will specify it in SG
  
  Firewall (security groups):
    Select existing security group:
      Common security groups: Workshop-LB-SG
      
  Advanced network configuration: No

Configure storage:
  - Size: 8 GiB
    Type: gp2

Resource tags:
  Name: Product-Service # Resource types: Instances
  Role: Workshop # Resource types: Instances, Volumes

Advanced Details:
  Purchasing option Info:
    Request Spot Instances: No
  
  IAM instance profile: Product-Service-Instance
  Hostname type: Don't include in launch template
  
  DNS Hostname: Skip
  Termination protection Info: Don't include in launch template
  Detailed CloudWatch monitoring Info: Don't include in launch template
  Elastic GPU Info: Don't include in launch template
  
  Elastic inference: 
    Add Elastic Inference accelerators: No
    
  Credit specification Info: Don't include in launch template
  Placement group name Info: Don't include in launch template

  Create new placement group: Don't include in launch template
  EBS-optimized instance Info: Don't include in launch template
  Capacity reservation Info: Don't include in launch template
  Tenancy Info: Don't include in launch template
  RAM disk ID Info: Don't include in launch template
  Kernel ID Info: Don't include in launch template
  License configurations Info: Don't include in launch template

  Metadata accessible Info: Don't include in launch template
  Metadata version Info: Don't include in launch template
  Metadata response hop limit Info: Don't include in launch template
  Allow tags in metadata Info: Don't include in launch template

  Don't include in launch template: Don't include in launch template
  
  User data: <user-data>
```

* Go to `EC2/Auto Scaling/Auto Scaling Groups`
* Click `Create an Auto Scaling group`

```yaml
Auto Scaling group name: Workshop-Product-SG
Launch template:
  Launch template: Workshop-Product-Template
  Version: Default (1)

Network:
  VPC: <vpc-id>
  Availability Zones and subnets: <select all public subnets>

Instance type requirements: Do not override specification of template

Load balancing:
  Attach to an existing load balancer: Yes
  Choose from your load balancer target groups: Yes
  Existing load balancer target groups: product-service-nodes

Health checks:
  ELB: Yes
  Health check grace period: 300

Group size:
  Desired capacity: 1
  Minimum capacity: 1
  Maximum capacity: 2

Scaling policies:
  Target tracking scaling policy: No

Add notifications: Skip

Tags:
  Role: Workshop
```

* Click workshop name in order to open details.
* Click `Activity` tab and wait until the status becomes `Successful`.
* Click `Instance management` tab and see list of instances.

### Apply rolling update by changing instance type

* Go to `EC2/Launch Templates`.
* Select template with name `Launch template name`.
* Click `Actions/Modify template (Create new version)`.
* Change `Instance type` to `t2.nano`.
* Click `Create template version`.
* Click `View launch templates`.
* Click `Actions/Set default version`.
* Set `Template version` to the latest one and click `Set as default version`.

**NB: If you skip set default version, next versions will be created from the default.**

* Go to `EC2/Auto Scaling/Auto Scaling Group`.
* Select `Workshop-Product-SG` and click `Edit`.
* Scroll to `Launch template` and change version to latest.
* Click update.
* Click to `Workshop-Product-SG` in order to show details.
* Open `Instance refresh`.
* Click `Start instance refresh`.

```yaml
Refresh settings:
  Minimum healthy percentage: 100%
  Instance warmup: 300 seconds

  Checkpoints:
    Enable checkpoints: No

  Skip matching:
    Enable skip matching: Yes

  Desired configuration: Skip
```

* Click `Start instance refresh`.
* Stay un `Instance refresh` tab and wait until the refresh activity status becomes `Successful`.

### Set up usage of spot instances

* Go to `EC2/Auto Scaling/Auto Scaling groups`.
* Click on name `Workshop-Product-SG` in order to open details.
* Click `Override launch template` in `Instance type requirements`.

```yaml
Instance type requirements:
  Manually add instance types: Yes
  Types: t2.micro, t3.micro
  
Instance purchase options:
  On-demand: 0%
  Spot: 100%

  Include On-Demand base capacity: Yes
  On-Demand Instances: 1

Allocation strategies:
  Spot allocation strategy: Capacity optimized
  Prioritize instance types: No
  Capacity rebalance: Yes
```

### Set up scaling based rule 

* Go to `CloudWatch/Alarms`.
* Click `Create alarm`.
* Click `Select metric`, select `EC2/By Auto Scaling Group/CPUUtilization`.

```yaml
Metric:
  Metric name: CPUUtilization
  Auto Scaling Group Name: Workshop-Product-SG
  Statistic: Average
  Period: 5 minutes

Conditions:
  Threshold type: Static
  Whenever CPUUtilization is...: Greater
  than...: 80
```

* Click `Remove` on `Notification`.
* Click `Next`.

```yaml
Name and description:
  Alarm name: Workshop-CPU-Usage-Too-Big
  Alarm description: 
```

* Click `Create alarm`.
* Click `Select metric`, select `EC2/By Auto Scaling Group/CPUUtilization`.

```yaml
Metric:
  Metric name: CPUUtilization
  Auto Scaling Group Name: Workshop-Product-SG
  Statistic: Average
  Period: 5 minutes

Conditions:
  Threshold type: Static
  Whenever CPUUtilization is...: Less
  than...: 30
```

* Click `Remove` on `Notification`.
* Click `Next`.

```yaml
Name and description:
  Alarm name: Workshop-CPU-Usage-Too-Small
  Alarm description: 
```

* Go to `EC2/Auto Scaling/Auto Scaling Group`.
* Open `Automatic scaling` tab.
* Click `Create dynamic scaling policy`

```yaml
Policy type: Simple scaling
Scaling policy name: Usage-too-big
CloudWatch alarm: Workshop-CPU-Usage-Too-Big
Take the action: Add 1 capacity units
And then wait: 300 seconds
```

* Click `Create dynamic scaling policy`

```yaml
Policy type: Simple scaling
Scaling policy name: Usage-too-small
CloudWatch alarm: Workshop-CPU-Usage-Too-Small
Take the action: Remove 1 capacity units
And then wait: 300 seconds
```

* Open `Details` tab.
* Click `Edit` in `Group Details` block. 

```yaml
Desired capacity: 1
Minimum capacity: 1
Maximum capacity: 2
```

#### Verify scaling

* Enter on instance using SSH.

```bash
sudo apt install stress
stress --cpu 1
```

* Wait until instance amount become 2.
* Verify that you are able to log in.

**NB: Page reload should cause redirect to log in page.**

#### Enable stickiness

* Go to `EC2/Load Balancing/Target Groups`.
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

* Verify that login currently works.

**NB: ALB have its own mechanism of stickiness, but it works with distribution across target groups. 
So you should use stickiness on target group level.**

## Store data in EFS

* Go to `EFS/File systems`
* Click `Create file system`

```yaml
General:
  Name: Workshop-Disk
  Availability and duration: Regional

  Automatic backups:
    Enable automatic backups: Yes

  Lifecycle management:
    Transition into IA: 30 days since last access
    Transition out of IA: On first access

  Performance mode: General Purpose
  Throughput mode: Bursting

  Encryption:
    Enable encryption of data at rest: Yes

Tags:
  Role: Workshop

Network:
  Virtual Private Cloud (VPC): default
  Mount Targets: <keep-it-in-all-az>
```

### Mount to instances

#### Allow accessing using IAM

* Go to `IAM/Access Management/Policies`.
* Click `Create Policy`

```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "Rule",
            "Effect": "Allow",
            "Action": [
                "elasticfilesystem:ClientMount",
                "elasticfilesystem:ClientWrite"
            ],
            "Resource": "arn:aws:elasticfilesystem:eu-west-1:669171167111:file-system/fs-0b797a9b7a4b06d1d"
        }
    ]
}
```

* Then continue configuration

```yaml
Tags:
    Role: Workshop

Name: Product-Service-EFS-Access
Description: Access to EFS of product service.
```

* Go to `IAM/Access Management/Roles`.
* Find `Product-Service-Instance`, and click on it.
* Click `Attach policies`.
* Find `Product-Service-Params-Load` and select it.
* Click `Attach policy` at the bottom of the screen.

#### Allow accessing using SG

TODO: Change to create specific SG in order to allow 

* Go to `EFS/File System`
* Click by name `Workshop-Disk`
* Open `Network` tab
* Copy value from `Security Group`
* Go to `EC2/Network & Security/Security Groups`
* Find & open security group by id used in EFS
* Click `Edit inbound rules`
* Ensure that rule exists

```yaml
Type: All Traffic
Protocol: All
Port range: All
Source: <id-of-product-service>
```

#### Test connection
On your instance execute

```shell
cd
sudo apt-get update
sudo apt-get -y install git binutils
git clone https://github.com/aws/efs-utils
cd efs-utils/
./build-deb.sh
sudo apt-get -y install ./build/amazon-efs-utils*deb

sudo mount -t efs <efs-io> /mnt/efs

dd if=/dev/zero of=testfile bs=1024 count=102400
```

#### Configure automatic

```shell
sudo nano /etc/fstab
```

```
file-system-id:/ /mnt/efs efs _netdev,noresvport,tls,iam 0 0
```

#### Create new version of AMI

TBD

`Workshop-Product-App-AMI-v02`