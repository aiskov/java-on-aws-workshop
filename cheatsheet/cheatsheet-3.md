# Cheat sheet 3

## Locations

Security groups - `EC2/Network & Security/Security Groups`
Load balancer - `EC2/Load Balancing/Load Balancers`
S3 bucket - `S3/Buckets`
Param Store - `Systems Manager/Application Management/Parameter Store`
EBS Snapshots - `EC2/Elastic Block Storage/Snapshots`
EC2 Instances - `EC2/Instances`
Launch template - `EC2/Launch Templates`
Target Groups - `EC2/Load Balancing/Target Groups`
Auto Scaling Groups - `EC2/Auto Scaling/Auto Scaling Groups`
Alarms - `CloudWatch/Alarms`
Policies - `IAM/Access Management/Policies`
Roles - `IAM/Access Management/Roles`
EFS - `EFS/File systems`

## Tools

### AWS

```bash
sudo apt install -y awscli # Install AWS command line client

aws s3 cp ./target/products-1.jar s3://<artifact-store-bucket>/ # Upload to bucket
aws s3 cp s3://<artifact-store-bucket>/products-1.jar . # Download from bucket
aws --region eu-west-1 ssm get-parameters-by-path --path /config/product-service_prod # Get param for env
aws --region eu-west-1 ssm get-parameters-by-path --path /config/product-service # Get default param
```

### Stress tools

```bash
sudo apt install stress # Install stress tools
stress --cpu 1 # Load one processor of server, nb: may cause connection lost
```

### Linux services

```bash
sudo systemctl daemon-reload # Reload needed to refresh configuration
sudo systemctl enable app-product.service # Add application to autostart
sudo systemctl start app-product.service # Start application
sudo systemctl status app-product.service # Current status of application
sudo systemctl restart app-product.service # Restart application
```

### Linux mount

Mount EFS 

```shell
sudo apt-get update
sudo apt-get -y install git binutils
git clone https://github.com/aws/efs-utils
cd efs-utils/
./build-deb.sh
sudo apt-get -y install ./build/amazon-efs-utils*deb

sudo mount -t efs <efs-io> /mnt/efs

dd if=/dev/zero of=testfile bs=1024 count=102400
```

#### FSTAB - mount on startup 

```shell
sudo nano /etc/fstab
```

```
<efs-id>:/ /mnt/efs efs _netdev,noresvport,tls,iam 0 0
```

### Java

```bash
# Build application
mvn clean package
```

```bash
# Run application
java -jar products-1.jar --spring.config.import=app.properties # Run using properties
java -jar products-1.jar --spring.profiles.active=prod # Run using provider
```

```xml
<!-- Dependencies required for param store -->
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
```

```yaml
# How to enable context refresh using actuator 
management:
  endpoints:
    web:
      exposure:
        include: env, refresh
```

```bash
# Call to refresh
curl -X POST http://127.0.0.1:8888/actuator/refresh
```
**NB:** *You may need to add `--add-opens java.base/java.lang=ALL-UNNAMED` to the 
java parameters because of bug in the AWS library, otherwise you could get 
`InaccessibleObjectException`*
