# Cheat sheet 2

## Locations

SQL databases - `RDS/Databases`

VM's - `EC2/Instances/Instances`
Assignable IPs - `EC2/Network & Security/Elastic IPs`
Firewalls - `EC2/Network & Security/Security Groups`
Disks - `EC2/Elastic Block Storage/Volumes`

## Tools

### Java

```bash
sudo apt install -y openjdk-17-jre-headless # Install java
java -jar products-1.jar # Run application
java -jar products-1.jar --spring.config.import=app.properties  # Run application with properties
```

### SSH

```shell
ssh ubuntu@<ec2-instance-ip> # Connect to instance
ssh ubuntu@<ec2-instance-pub-ip> cat /var/product-files/test.txt # Execute operation on server, read file
scp ./env/files/* ubuntu@<ec2-instance-ip>:/var/product-files/ # Upload files
```

### Linux files access

```shell
sudo mkdir /opt/product-service # Create dir (in restricted directory)
sudo chgrp ubuntu /opt/product-service # Change owner of group
sudo chmod 770 /opt/product-service # Change access rights to allow anything to user
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

```bash
sudo lsblk
sudo lsblk -o +UUID 
```

```shell
sudo mkfs -t xfs <ebs-drive-location> # Create file system
sudo file -s <ebs-drive-location> # Verify that drive have FS
sudo mount <ebs-drive-location> /var/product-files # Mount
```

#### FSTAB mount on start

Edit `/etc/fstab`

```fstab
UUID=<volume-uuid>  /var/product-files  xfs  defaults,nofail  0  2 
```

### MySQL

Execute operation on server, check mysql connection
```bash
ssh ubuntu@<ec2-instance-pub-ip> curl <endpoint-of-rds>:3306
```

Create database:

```sql
CREATE SCHEMA `service_db`;
```

Create user and password:

```sql
CREATE USER '<app-user-name>'@'%' IDENTIFIED BY '<app-user-password>';
GRANT ALL PRIVILEGES ON `service_db`.* TO  '<app-user-name>'@'%';
```
