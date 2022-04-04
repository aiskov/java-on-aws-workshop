- [Hints for step 4](#hints-for-step-4)
  - [Use S3 to store files](#use-s3-to-store-files)
    - [Replace EFS store with S3](#replace-efs-store-with-s3)
      - [Create S3 bucket](#create-s3-bucket)
      - [Modify applicaion](#modify-applicaion)
  - [Use Redis to cache response](#use-redis-to-cache-response)
  - [Use Dynamo to keep session](#use-dynamo-to-keep-session)
    - [Remove stickiness](#remove-stickiness)
    - [Store session in Dynamo](#store-session-in-dynamo)
  - [Use SNS](#use-sns)
    - [Send notification about new files uploaded.](#send-notification-about-new-files-uploaded)
    - [Receive email notification.](#receive-email-notification)
  - [Use SQS](#use-sqs)
    - [Write request for image format to SQS](#write-request-for-image-format-to-sqs)
    - [Read and process request for image formatting from SQS](#read-and-process-request-for-image-formatting-from-sqs)

# Hints for step 4

## Use S3 to store files

### Replace EFS store with S3

#### Create S3 bucket

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

#### Modify application

* Check changes on `S3` branch.

#### Add access rights

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

#### Update application

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

TBD

## Use Dynamo to keep session

### Remove stickiness
TBD

### Store session in Dynamo
TBD

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
