# Work notes - For Lab

```yaml
VPC:
  Name: # Example: default
  Id: # Example: vpc-43995c3a

  Subnets:
    - Name: # Example: public-1
      Id: # Example: subnet-0650fcf6f33288da7
      Availablity Zone: # Example: eu-west-1a

    - Name: # Example: public-2
      Id: # Example: subnet-4b592911
      Availablity Zone: # Example: eu-west-1b

    - Name: # Example: public-3
      Id: 
      Availablity Zone:

RDS: 
  Endpoint: # Example: workshop.cdbp96gigslf.eu-west-1.rds.amazonaws.com

  Admin User:
    User: # Example: admin
    Password: # Example akyBULhfbzQpqUw-wuvwDL7D2MsCMi3e
  Application User:
    User: # Example: product_app
    Password: # Example: 4yRuVLm7UPvbbNu----CPBX36RXCXHsUNnY3

EC2:
  Security Group: 
    Id: # Example sg-0571ab8cd4eaed1d9
    Name: # Example Workshop-Product-App-SG
    
  Instances:
    - Id: # Example: i-0b86ff997a0f08909
      Public IP: # Example: 34.244.26.32
      Availability Zone: # Example: eu-west-1a

ElasticIp:
    - # Example: 18.202.142.131

EBS:
  - Id: # Example: vol-0252afdc6e6876b01
    Location: # Example: /dev/xvdf
    Uuid: # Example: b89e8beb-e65b-48af-8a75-c1e9d789d801

ELB: 
  Host: # Example: http://workshop-lb-42913877.eu-west-1.elb.amazonaws.com/
  Name: # Example Workshop-LB
  Security Group:
    Id: # Example: sg-1f0a255d
    Name: # Workshop-LB-SG

S3:
  - Name: # Example artifact-store-c1e9d789d801
    ARN: # arn:aws:s3:::artifact-store-c1e9d789d801
    URI: # Example s3://artifact-store-c1e9d789d801

EFS: 
  Id: # Example fs-0b797a9b7a4b06d1d
  Security Group: # Example sg-081df38df2ffecf9e
```

## User data draft

```bash
#!/bin/bash
aws s3 cp s3://artifact-store-c1e9d789d801/products-1.jar /opt/product-service/
systemctl restart app-product.service
```