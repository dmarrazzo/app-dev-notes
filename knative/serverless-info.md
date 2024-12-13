Now Available: OpenShift Serverless 1.23
========================================

TL;DR: OpenShift Serverless 1.23  is now Generally Available as of June 30 2022! In this release we have included Knative 1.2.  Notable features are documentation for unmanaged add-on Serverless on ROSA, Serverless integration with Distributed tracing. More monitoring dashboards.  s2i build strategy in addition to Buildpacks for building Knative Functions, Use of OpenShift Internal ImageRegistry for deploying Knative Functions. 

-----------------------------

On behalf of the OpenShift Serverless team, we'd like to announce the latest release of OpenShift Serverless, version 1.23 ! This release is using upstream Knative version 1.2 and targets OpenShift Container Platform 4.10 EUS, 4.9, 4.8 EUS, 4.7 and 4.6 EUS.  
Red Hat OpenShift Serverless 1.23 is based on the open-source project Knative version 1.2. This release includes Serverless on ROSA as unmanaged Add-on documentation. Serverless integration with Distributed tracing and other document improvements. 
This release also includes upgraded Technology Preview of Knative Kafka Broker and Knative Kafa Sink.  Now you can create a Kafka Broker that uses an externally managed Kafka topic . Addition of monitoring dashboards for  visualizing Kafka Broker/Trigger and KafkaSink for Observability and ease of debugging. 
List of exposed dashboards:
Event Count (avg/sec, over 1m window)
Success Rate (2xx Event, fraction rate, over 1m window)
Event Count by Event Type (avg/sec, over 1m window)
Event Count by Response Code Class (avg/sec, over 1m window)
Failure Rate (non-2xx Event, fraction rate, over 1m window)
Event Dispatch Latency (ms)
For Serverless Functions/Knative Functions Tech Preview, s2i as a build strategy has been added, in addition to CNCF Buildpacks.
Please see our release notes for more details and more features for this and previous releases. 
I would like to extend my sincere thanks and gratitude to the whole Serverless team that spans multiple business units and across companies,  for making the release possible. 
We are always looking for feedback and you can reach us 
Mailing lists: serverless-interest or serverless-dev 
Like Slack? #forum-serverless. 
Feedback on docs: Now you create issues for our documentation using the Open an issue icon on the top right corner.

FAQs and Appendix
What is OpenShift Serverless?
OpenShift Serverless lets you increase your cluster density through dynamic scaling based on demand as well as provides the capability to build event-driven applications that can connect to and from several systems running on-premises, on the cloud, and inside or outside of Kubernetes. OpenShift Serverless comes with 1 click install experience and it is ready for production use. It also offers Eventing infrastructure for your Event Driven solutions and Serverless Functions/Knative Functions as Technology Preview. 

P.S: Short 5 min introduction video on Knative courtesy of Stelios Kousouris : Serverless like Pizza delivery, it is efficient
Serverless is also available on Developer Sandbox for a quick spin . :) 

What are the Event Sources provided with Serverless 1.23?
Serverless 1.23  includes Kafka Channel/Event Source alongside other built-in Event Sources, such as ,  (Kubernetes APIs,  Ping, and ContainerSource) . In addition several event sources, powered by Camel-K (GA) and community, such as AWS S3, AWS Kinesis, AWS SQS, AWS SNS, Elasticsearch, Salesforce, MongoDB, Database (PostgreSQL, MySQL, SQL Server) to name the few. 
We have added Kafka Broker as a Technology Preview feature.

What  Architectures are supported for Serverless ?
Red Hat OpenShift Serverless offers multi-arch support and  is available as a no-charge add-on to the Red Hat OpenShift Container Platform. All the tested Cloud Provider(s) and Infrastructure can be found at our Supported Configuration page. Red Hat OpenShift Serverless is also  available on Developer Sandbox. Developer Sandbox is a sandbox OpenShift environment for accessing Red Hat products and technologies easily.

What are Serverless Functions/Knative Functions?
Technology Preview of Serverless functions/Knative Functions is a programming model that reduces programming complexities to make it attractive to even non developers, such as Data Scientist and Content Developers.  Serverless functions are shipped bundled with OpenShift Serverless and  offer local developer experience using Docker/Podman. In-cluster build using OpenShift Pipelines. We are striving to complete the Serverless offering with all the necessary constructs developers need to build modern cloud-native applications.  OpenShift Serverless functions OOTB runtimes including Quarkus, Node.js, Python, Go and Spring Boot, TypeScript, Rust. It offers multiple build strategies, s2i and CNCF Buildpacks. These functions can be invoked by plain HTTP Requests as well aWe are striving to complete the Serverless offering with all the necessary constructs developers need to build modern cloud-native applicationss Cloud Events by leveraging the Eventing component. Please find our Serverless functions documentation here
What is the difference between Serverless Containers and Serverless functions?
OpenShift Serverless is a deployment platform that runs your app containers and functions in Serverless manner, dynamically scaling up and down. We call this Serverless with flexibility



It's super easy to get started! 




What are the common OpenShift UseCases?


Where do I find more information or FAQ’s for OpenShift Serverless?
OpenShift Serverless Internal Internal FAQ. 
For more information:
Website - OpenShift.com/serverless
Product Documentation
About Serverless
OpenShift Serverless Release Notes
Quickstart for Quarkus Serverless Function on Dev Sandbox
451 Research Report on OpenShift Serverless
Knative Cookbook (Free E-book)
Knative Tutorial by Red Hat Developers
Katacoda Labs 
Getting Started with OpenShift Serverless 
Serverless Camel-K
Serverless Eventing with Camel-K
[video] Getting Started with Camel K - API and Knative 
[video] Event Streaming with Kafka on Knative Eventing 
[Article] Serverless Java Functions on OpenShift
[Article] Send S3 data to Telegram with Red Hat OpenShift Serverless Functions
[Article] Faster web deployment with Python serverless functions
[Article]Create your first OpenShift Serverless function
[Article]Write a Quarkus function in two steps on OpenShift Serverless
[Article]Node.js circuit breakers for serverless functions
[Article]Node.js serverless functions on Red Hat OpenShift, Part 1: Logging
[Article]Node.js serverless functions on Red Hat OpenShift, Part 2: Debugging locally
[Article] Node.js serverless functions on Red Hat OpenShift, Part 3: Debugging on a cluster
[video] Quarkus Serverless function on OpenShift Serverless
[video] Node.js Serverless function on OpenShift Serverless
[video] Golang Serverless function on OpenShift Serverless
[video] Introduction to Eventing
[video] OpenShift Serverless demo using OpenShift Pipelines, including Triggers, and Revisions.



#Feedback from field

