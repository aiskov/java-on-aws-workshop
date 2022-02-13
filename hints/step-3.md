# Hints for step 2

- [Hints for step 2](#hints-for-step-2)
  - [Create load balancer](#create-load-balancer)
    - [Security group for load balancer](#security-group-for-load-balancer)
    - [Target for load balancer](#target-for-load-balancer)
    - [Load balancer configuration](#load-balancer-configuration)
    - [Allow connection only from load balancer](#allow-connection-only-from-load-balancer)
  - [Store artifacts in S3](#store-artifacts-in-s3)
    - [Save artifact in S3](#save-artifact-in-s3)
      - [Upload using command line](#upload-using-command-line)
    - [Download artifact from S3](#download-artifact-from-s3)
      - [Install client](#install-client)
      - [Configure Access](#configure-access)
  - [Externalize application configuration](#externalize-application-configuration)
    - [Create properties](#create-properties)
    - [Adopt application to connect](#adopt-application-to-connect)
    - [Verify that properties loaded from AWS](#verify-that-properties-loaded-from-aws)
    - [Refresh properties without restart](#refresh-properties-without-restart)
    - [Stop using app.properties on server](#stop-using-appproperties-on-server)
  - [Automate node configuration](#automate-node-configuration)
    - [Create AMI](#create-ami)
    - [Add User Data](#add-user-data)
  - [Store data in EFS](#store-data-in-efs)
  - [Configure multiple instance](#configure-multiple-instance)
  - [Configure auto scaling](#configure-auto-scaling)

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
* Click `Actions` button and choose `Edit inbound rules`
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

### Adopt application to connect

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
* Change server.port to `9999`

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