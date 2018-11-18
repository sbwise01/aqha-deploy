# aqha-deploy
Application deployment utility to perform highly available blue green deployments of ASG based apps on AWS

## Desired minimum functionality
1. Application resource
  1. Launch Template
  1. Autoscaling Group
1. Injectible security groups for instances
1. Self join ELB Classic / ALB
1. Injectible Health check / Availability check
1. Deployment strategies
   1. Initial
   1. Destroy
   1. Blue Green
   1. Rolling
   1. Downtime
1. True HA for Blue Green
   1. Tests of health check succeed prior to swapping out ASGs in load balancers
   1. Full connection drain wait
1. Injectible user data
   1.  Support for chef-solo instance bootstraping including S3 config data object creation
1. Cookbook for chef-solo instance bootstrap utility
1. Support startup and shutdown hooks
1. Configuration generation utility
   1.  Use of defaults/overrides for multi-application / multi-stack deployment management
1. Instance Profile
1. KMS key grants for S3 config data object
1. Auto-renewing assume role for deployment
