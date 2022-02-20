- [Hints for step 2](#hints-for-step-2)
  - [Create RDS database - MySQL](#create-rds-database---mysql)
    - [Add tags](#add-tags)
  - [Create EC2 instance, install java.](#create-ec2-instance-install-java)
    - [Create security group](#create-security-group)
    - [Configure Instance details](#configure-instance-details)
    - [Add Storage](#add-storage)
    - [Add Tags](#add-tags-1)
    - [Configure Security Group](#configure-security-group)
    - [Configure SSH access](#configure-ssh-access)
    - [Verify access to EC2 instance](#verify-access-to-ec2-instance)
  - [Modify DB Security Role to allow access from ec2 instance to database](#modify-db-security-role-to-allow-access-from-ec2-instance-to-database)
    - [Allow connection](#allow-connection)
  - [Create schema & fill it with test data](#create-schema--fill-it-with-test-data)
    - [Connect from host to database](#connect-from-host-to-database)
    - [Create database](#create-database)
    - [Create application user](#create-application-user)
  - [Add additional disk to EC2 instance](#add-additional-disk-to-ec2-instance)
    - [Create EBS volume](#create-ebs-volume)
    - [Attach volume to the instance](#attach-volume-to-the-instance)
    - [Mount disk to instance](#mount-disk-to-instance)
  - [Upload Jar](#upload-jar)
  - [Start application](#start-application)
    - [Prepare java](#prepare-java)
    - [Configure application](#configure-application)
    - [Upload files](#upload-files)
    - [Run application](#run-application)
  - [Make service from application](#make-service-from-application)
    - [Start application after restarts](#start-application-after-restarts)
    - [Assign Elastic IP](#assign-elastic-ip)
    - [Verify installation](#verify-installation)

# Hints for step 2

## Create RDS database - MySQL
1. Go to AWS Console/Services/RDS
1. Left side panel Databases
1. Button Create Database

```yaml
Engine options:
  Engine type: MySQL
  Version: Newest one # tested with MySQL 8.0.26

# Generally database configuration depends on use case
# You may have database per service and environment - `products-service-prod`
# Or you may have one database server for your unit - `my-company` 
# then database server will hold many databases
DB instance identifier: workshop

# You need to have that credentials to configure database
Credentials:
  Master username: admin
  Master password: akyBULhfbzQpqUw-wuvwDL7D2MsCMi3e # Should be something secure
  Confirm password: akyBULhfbzQpqUw-wuvwDL7D2MsCMi3e

# Let's assume that we use in one country and clients active mostly in work hours
DB instance class:
  DB instance class (Group): Burstable classes (includes t classes) # Allows improving performance for while 
  DB instance class: db.t3.micro # Smallest one

Storage:
  # IO optimized allowed only when you allocate more than 100 GiB
  Storage Type: General Purpose SSD # Generally we do not expect that database will be bigger than several GiB
  Enable storage autoscaling: Yes # For production use it better to keep
  Maximum storage threshold: 21 # As smallest because we do not expect grows

Availability & durability:
  Multi-AZ deployment:
    Create a standby instance (recommended for production usage): Yes # Otherwise it will not have high-availability

Connectivety:
  # VPC is a network where we will be deployed
  Virtual private cloud: Default VPC (xxxxxxxxx) # just because it configured out of the box
  Subnet groups: default-vpc-xxxxxa # Should be same as initial deploy of application to reduce traffic and latency
  Public access: No # We wouldn't access outside our VPC
  
  Additional configuration: 
    Database port: 3306 # Keep default one

Database authentication:
  Database authentication options: Password authentication # We have no clients that supports IAM or Kerberos

Additional configuration:
  Initial database name: service_db # We want to initialize database
  
  Backup:
    Enable automated backups: Yes # For most cases it will be better to transfer responsibility for backups to the AWS 
    Backup retention period: 7days # For our demo case it will be ok
    Backup window: Select window
    Start time: 04:00 # We expect that our users not active at night
    Duration: 1h
    Copy tags to snapshots: Yes # Because we want to keep snapshots tag same as DB

  Encryption:
    Enable encryption: Yes # We would keep it secure 
    AWS KMS Key: (default) aws/rds # We wouldn't pay additional fees
  
  Monitoring:
    Granularity: 60 sec
    Monitoring Role: Default

  Log exports:
    Audit log: No
    Error log: No
    General log: No
    Slow query log: No
    
  Maintenance:
    Enable auto minor version upgrade: Yes
    Maintenance window:
      Select window: Yes
      Start day: Saturday
      Start time: 5:00 # After backup
      Duration: 1h
```

> Estimated monthly costs
> * DB instance 13.14 USD
> * Storage 5.06 USD
> * Total 15.68 USD

### Add tags
1. Go to AWS Console/Services/RDS
1. Left side panel Databases
1. Choose workshop database
1. Open Tags tab
1. Button add

## Create EC2 instance, install java.

### Create security group
* Go to `EC2/Network & Security/Security Groups`
* Click `Create Security Group`

```yaml
Security group name: Workshop-Product-App-SG
Description: Security group for workshop app ec2 instances
VPC: <vpc-id>

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
    Source: 0.0.0.0/0, ::/0
    Description: Application port

Outbound rules:
  - Type: All Traffic

Tags: 
  Role: Workshop
```

  
### Configure Instance details
* Go to AWS Console/Services/EC2
* Left side panel Instances
* Button: Launch instances
* Search: Ubuntu Server 20.04
* Choose option 64-bit (x86) under Select Button
* Click Select Button
* Choose t2.micro

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
```

### Add Storage
Keep as it is. 

8GiB should be enough.

### Add Tags
Add tag 

```yaml
Role: Workshop
```

### Configure Security Group

1. Choose `Assign a security group: Select an existing security group`
2. Select 

### Configure SSH access
After review when you click `Launch`. 

* Choose an existing key pair.
* Select a key pair - your key pair name
* Check "I acknowledge that I have access to the corresponding private key file..."

**TODO: Describe how to import own key.**

### Verify access to EC2 instance
Go to `EC2/Instances`

Choose in list your instance. In `Details` tab find `Public IPv4 address` and copy value.  

You should replace `<ec2-instance-pub-ip>` with your public ip value.

```shell
ssh ubuntu@<ec2-instance-pub-ip>
```

## Modify DB Security Role to allow access from ec2 instance to database

### Allow connection
Go to `RDS/Databases`. Click on your database name in order to show details.

In tab `Connectivity & security` find `VPC security groups` and click on group name.

You will be redirected to `EC2/Security Groups` with filter by your database 
security group name.

Click on security group name in order to show details.

Click on `Actions/Edit inbound rules`

You should add new line (Do not check existed line).

```yaml
Security group rule ID: '-'
Type: All trafic
Protocol: All
Port Range: All
Source: 
  Type: Custom
  Target: <security-group-of-your-instance>
```

In order to verify access you may use command:

```shell
ssh ubuntu@<ec2-instance-pub-ip> curl <endpoint-of-rds>:3306
```

Response should contains:

```log
curl: (1) Received HTTP/0.9 when not allowed
```

## Create schema & fill it with test data

### Connect from host to database
**NB: On that stage we will use our instance as bastion host.**

From `IntelliJ Idea/Database Tab/Create/Data Source/MySQL`

```yaml
Name: Workshop-MySQL
Comment: MySQL on AWS
  
General:
  Host: <endpoint-of-rds>
  Port: 3306
  User: <rds-admin-user>
  Password: <rds-admin-password>
  Save: Forever
  Database: <empty>

SSH/SSL: 
  Use SSH tunnel: Yes
  Tunnel Congiuration:
    Host: <ec2-instance-pub-ip>
    Port: 22
    Username: Ubuntu
    Local Port: <Dynamic>
    Authentication Type: Key Pair
    Private Key File: <path-to-private-key>
    Passphrase: <your-ssh-key-password>
      
  Use SSL: Yes
```

Click - `Test connection`.

### Create database
```sql
CREATE SCHEMA `service_db`;
```

Then execute file `env/data.sql` using right mouse button and option `Run 'data.sql'`.

As the target data source you should choose your workshop data source.

### Create application user
```sql
CREATE USER '<app-user-name>'@'%' IDENTIFIED BY '<app-user-password>';
GRANT ALL PRIVILEGES ON `service_db`.* TO  '<app-user-name>'@'%' 
```

## Add additional disk to EC2 instance

### Create EBS volume
Go to `EC2/Elastic Block Storage/Volumes` . You will see list where you already have EC2 instance volume.

Click `Create volume` button.

```yaml
Volume Type: General Purpose SSD (gp3) # It has lower price and more IOPS than gp2, and we have no requirements for 
                                       # high throughput or big volume size that will allow using cheaper HDD disks
Size (GiB): 1 # Let's use minimal value
IOPS: 3000 # Minimal (in gp2 it will be calculated)
Throughput (MiB/s): 125 # We have 
Availability Zone: <ec2-instance-availability-zone> # It should much because volumes located in particular AZ
Snapshot ID: Don`t create volume from a snapshot # It required copying from other volumes snapshots
Encryption:
    Encrypt this volume: Yes 
    KMS key: (default) aws/ebs # Because it provided for free 
```

After creation toy should wait some time until `Volume state` will be `Available`.

### Attach volume to the instance
* Go to `EC2/Instances` and open details of your workshop instance.
* Click on `Instance state` and choose `Stop instance` it could take several minutes.
* Go back to `EC2/Elastic Block Storage/Volumes` and select your new volume.
* Click `Actions/Attach volume` 

```yaml
Instance: <ec2-instance-id>
Device name: /dev/sdf # It should be valid linux device name, you should copy that name
```

Go to `EC2/Instances` and open details of your workshop instance.

Click on `Instance state` and choose `Start instance` it could take several minutes.

**NB: Update your instance ip address**

### Mount disk to instance
```shell
> ssh ubuntu@<ec2-instance-pub-ip>
> lsblk
```

Your disk will be probably at the end of the list with name `xvdf`, verify that it have no mount point and have correct 
size.

Then you should add prefix `/dev/` to that name and save it in workbook.

Create file system using:

```shell
> sudo mkfs -t xfs <ebs-drive-location> # Create file system and mount it
> sudo file -s <ebs-drive-location> # Verify that drive have FS
/dev/xvdf: SGI XFS filesystem data (blksz 4096, inosz 512, v2 dirs)
> sudo mkdir /var/product-files # Prepare directory to mount
> sudo mount <ebs-drive-location> /var/product-files # Try to mount
> sudo chgroup ubuntu /var/product-files # Transfer directory from root group to ubuntu
> sudo chmod 770 /var/product-files # Change access rights to allow full access to directory from owner group
> echo "Hi!" > /var/product-files/test.txt # Check your access rights by creation of test.txt file
```

Currently, you still have no configuration to mount that directory automatically on instance start.

Execute `lsblk` and copy UUID from your drive row 

```shell
> sudo lsblk -o +UUID 
```

Edit `/etc/fstab` and next line at the end replacing placeholder with your drive UUID.

```fstab
UUID=<volume-uuid>  /var/product-files  xfs  defaults,nofail  0  2 
```

Click on `Instance state` and choose `Reboot instance` it could take several minutes. Ip will be the same.

UI console may not show that state was changed, but you will receive message in console that connection was lost, 
something like:

```shell 
logout
Connection to <ec2-instance-pub-ip> closed.
```

Now you may verify that disk is still available after restart using

```shell
> ssh ubuntu@<ec2-instance-pub-ip> cat /var/product-files/test.txt
```

## Upload Jar
On instance

```shell
> sudo mkdir /opt/product-service
> sudo chgrp ubuntu /opt/product-service
> sudo chmod 770 /opt/product-service
```

On your host and project directory

```shell
> scp target/products-1.jar ubuntu@<ec2-instance-pub-ip>:/opt/product-service
> ssh ubuntu@<ec2-instance-pub-ip> 
```

## Start application

### Prepare java
On instance

```shell
> sudo apt install -y openjdk-17-jre-headless # Install java
> cd /opt/product-service # Go to app directory
> java -jar products-1.jar # Try to run application
```

Application should fail with: 

```log
Caused by: java.net.ConnectException: Connection refused
```

### Configure application
Create file `/opt/product-service/app.properties` and place to it

```properties
spring.datasource.url=jdbc:mysql://<rds-endpoint>:3306/service_db
spring.datasource.username=<rds-app-user>
spring.datasource.password=<rds-app-password>
    
app.files.location=/var/product-files/

logging.file.name=/var/log/product-service.log
```

### Upload files
On host from project directory:

```bash
> scp ./env/files/* ubuntu@<ec2-instance-pub-ip>:/var/product-files/ # Copy stored files
```

### Run application
On instance:

```bash
> cd /opt/product-service # Go to app directory
> java -jar products-1.jar \
  --spring.config.import=app.properties  # Run application
```

## Make service from application

### Start application after restarts
Create file `/etc/systemd/system/app-product.service`

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
ExecStart=java -jar products-1.jar --spring.config.import=app.properties
ExecStop=/bin/kill -15 $MAINPID

Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

Then execute:

```bash
> sudo systemctl daemon-reload # Reload needed to refresh configuration
> sudo systemctl enable app-product.service # It add application to autostart
> sudo systemctl start app-product.service # 
> sudo systemctl status app-product.service
```

### Assign Elastic IP
Go to `/EC2/Network & Security/Elastic IPs`. 

Click `Allocate Elastic IP address`

```yaml
Public IPv4 address pool:
    Amazon's pool of IPv4 addresses: Yes
Global static IP addresses: No
Tags:
  Role: Workshop
```

After save, you may choose your new Elastic IP address and then click `Actions/Associate Elastic IP address`.

```yaml
Resource type: Instance
Instance: <ec2-instance-id>
Private IP address: -
Reassociation:
    Allow this Elastic IP address to be reassociated: No
```

### Verify installation
Enter on yor page using your Elastic IP address `http://<elastic-ip>:8080`.

Try to restart server using start stop in order to lost your IP:

* Go to `EC2/Instances` and open details of your workshop instance.
* Click on `Instance state` and choose `Stop instance` it could take several minutes.
* Wait until instance stopped.
* Click on `Instance state` and choose `Start instance` it could take several minutes.

Wait for a while and refresh page `http://<elastic-ip>:8080`, it should work.

Try to kill process to verify restart. On instance:

```bash
> sudo systemctl status app-product.service
  app-product.service - Product service application
     Loaded: loaded (/etc/systemd/system/app-product.service; disabled; vendor preset: enabled)
     Active: active (running) since Fri 2021-12-03 09:56:51 UTC; 4s ago
   Main PID: 945 (java)
      Tasks: 14 (limit: 1147)
     Memory: 114.2M
     CGroup: /system.slice/app-product.service
             945 /usr/bin/java -jar products-1.jar --spring.config.import=app.properties
   
...
> sudo kill 945 # Kill process, it will simulate failure of java or application server
                # NB: Replace PID with those that you receive in status
```

Then check status you may take different responses, first option that service will be still disabled. Then wait for a 
while and retry.

```bash
> sudo systemctl status app-product.service
  app-product.service - Product service application
     Loaded: loaded (/etc/systemd/system/app-product.service; disabled; vendor preset: enabled)
     Active: activating (auto-restart) (Result: exit-code) since Fri 2021-12-03 09:57:21 UTC; 4s ago
    Process: 945 ExecStart=/usr/bin/java -jar products-1.jar --spring.config.import=app.properties (code=exited, status=143)
    Process: 977 ExecStop=/bin/kill -15 $MAINPID (code=exited, status=1/FAILURE)
   Main PID: 945 (code=exited, status=143)
   
...
```

Other option that you will receive status active but PID will be different as on example 
(first was 945 and new one is 983).

```bash
sudo systemctl status app-product.service
  app-product.service - Product service application
     Loaded: loaded (/etc/systemd/system/app-product.service; disabled; vendor preset: enabled)
     Active: active (running) since Fri 2021-12-03 09:57:31 UTC; 2s ago
   Main PID: 983 (java)
      Tasks: 14 (limit: 1147)
     Memory: 45.6M
     CGroup: /system.slice/app-product.service
             983 /usr/bin/java -jar products-1.jar --spring.config.import=app.properties
             
...
```
