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

