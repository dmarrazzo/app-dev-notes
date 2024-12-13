# Knative

https://access.redhat.com/documentation/en-us/openshift_container_platform/4.9/html/serverless/install

```sh
oc get knativeserving.operator.knative.dev/knative-serving -n knative-serving --template='{{range .status.conditions}}{{printf "%s=%s\n" .type .status}}{{end}}'

oc get pods -n knative-serving
```

14x2 = 28 pods

```sh
oc get pods -n knative-serving-ingress
```

4 pods

```sh
oc get knativeeventing.operator.knative.dev/knative-eventing \
  -n knative-eventing \
  --template='{{range .status.conditions}}{{printf "%s=%s\n" .type .status}}{{end}}'

oc get pods -n knative-eventing
```

16x2 = 32


## Deploy funqy


In this example we'll experience the Knative Broker and Trigger pattern:

https://redhat-developer-demos.github.io/knative-tutorial/knative-tutorial/eventing/index.html
https://quarkus.io/guides/funqy-knative-events



- Build image:

  ```sh
  oc new-build --strategy docker --binary --name=funqy-knative-events-quickstart -l app=funqy-knative-events-quickstart

  mkdir build
  mv target build
  cp src/main/docker/Dockerfile.jvm build/Dockerfile
  cd build/
  oc start-build funqy-knative-events-quickstart --from-dir . --follow

  Successfully pushed image-registry.openshift-image-registry.svc:5000/test/funqy-knative-events-quickstart@sha256:23b237f8aa21d7b618c7d0f2209df431a2310755d967b4b663f2b24f3e2d66f7
  ```

- Set up the broker in our namespace

  ```sh
  kn broker create default
  ```

- Update `funqy-service.yaml` to match the image name.

- Deploy the knative service:

  ```sh
  oc apply -f ./src/main/k8s/funqy-service.yaml
  ```

- Deploy the triggers

  ```sh
  oc apply -f ./src/main/k8s/defaultChain-trigger.yaml
  oc apply -f ./src/main/k8s/annotatedChain-trigger.yaml
  oc apply -f ./src/main/k8s/configChain-trigger.yaml
  oc apply -f ./src/main/k8s/lastChainLink-trigger.yaml
  ```

- Watch log:

  ```sh
  stern funq --container user-container
  ```

- In parallel call the service:

  - Deploy a Fedora image and execute a bash shell:
  
    ```sh
    oc apply -f src/main/k8s/curler.yaml
    oc exec -it curler -- /bin/bash
    ```

  - In the container issue the following curl command:

    ```sh
    curl -v  http://broker-ingress.knative-eventing.svc.cluster.local/test/default \
    -X POST \
    -H "Ce-Id: 1234" -H "Ce-Specversion: 1.0" \
    -H "Ce-Type: defaultChain" -H "Ce-Source: curl" \
    -H "Content-Type: application/json" \
    -d '"Start"'
    ```


CHAT 
-----------------------------

Greg Autric4:02 PM
Hi Every body from Paris ;-) 
Efstathios Rouvas4:02 PM
Hi from Scotland !
Karsten Gresch4:03 PM
Hi all, hi Stathis!
Efstathios Rouvas4:03 PM
Hi Karsten, nice talking to you again!
Ivo Bek4:04 PM
https://docs.google.com/document/d/13sBDy1sW3aS1WXmv--A0RynUiLnHuEJ-AudaRlR2zgE/edit#
Karsten Gresch4:04 PM
:) thanks, glad to see you! Looking forward to joining a serverless wf training with you in the hopefully not too far futue... :D
Efstathios Rouvas4:05 PM
+100
Alessandro Lazarotti4:15 PM
we use JQ for data manipulation. This blog entry shows how to use JQ expressions to work with workflows data: https://blog.kie.org/2022/04/serverless-workflow-expressions.html - for more complex use cases as Ivo mentioned, we integrate with Camel-k
David Williams4:31 PM
Yeah, I was worried that Odin, the winky pirate, was transferring to IBM.  We should give him a red hat now.
Paulo Menon4:37 PM
@Paul Brown this is a getting started page, is also in the slide deck
https://kiegroup.github.io/kogito-docs/serverlessworkflow/main/getting-started/create-your-first-workflow-service.html
Ricardo Zanini4:37 PM
these guides are a working in progress, please bear this in mind
we are adding a lot more content as we go
Paul Brown4:38 PM
thank you
Helber Belmiro4:39 PM
There's also this blog post: https://blog.kie.org/2022/05/getting-started-with-service-calls-and-serverless-workflow.html
Ricardo Zanini4:39 PM
regarding the states/constructs and overall DSL, see: https://kiegroup.github.io/kogito-docs/serverlessworkflow/main/getting-started/cncf-serverless-workflow-specification-support.html
this is the current state of our implementation of the spec
there, you will find a link to each definition in the DSL
you can open issues in https://github.com/kiegroup/kogito-docs if you see any problems or have questions regarding the guides we are working on
Alessandro Lazarotti4:41 PM
@Karsten "Will the Kogito team stay with Red Hat?" - Kogito will be a shared community with IBM, so they will also contribute in the project. But yes, mostly of current engineers involved with Serverless Workflow will stay at Red Hat
Karsten Gresch4:44 PM
@Alessandro - that's wonderful news! Thanks!!!
David Williams4:44 PM
In the machine learning / data engineering pipeline world, most tools have you define pipeline orchestrations in code / yaml.  This offers some advantages in the two stage dev lifecycle and the side-by-side code/flow diagram.
Stephen Nimmo4:44 PM
This is pretty neat!
Alessandro Lazarotti4:44 PM
"Will there be any UI component available (human based tasks as single deployable services)?" - we will have UI components, but focused on the workflow authoring and runtime information. Human based tasks is not the target for  the serverless workflow effort
Karsten Gresch4:45 PM
Thanks!
Love it anyway ;)
Ricardo Zanini4:45 PM
there is a slightly complex use case here: https://github.com/kiegroup/kogito-examples/tree/main/serverless-workflow-examples/serverless-workflow-newsletter-subscription
Anurag Saran4:48 PM
Please share the source page where the recording link will be posted.
Paulo Menon4:50 PM
I will send an email with the link for the recording soon it's uploaded to our cop call share drive
Karsten Gresch4:51 PM
Thanks Paolo! The slides might also be interesting... :)
Paulo Menon4:52 PM
Yes sure
Alessandro Lazarotti4:53 PM
"once GA where this offering will eventually land for customers to consume? Maybe as part of RH Openshift Servless offering?" -> yes, in fact it will be part of Openshift Serverless offer even during devpreview/tech preview phase
Ricardo Zanini4:53 PM
If you have questions trying the examples, please mark me in the CoP chat room
Or any questions related to the guides as well :)
Eder Ignatowicz4:54 PM
The chat room for COP is the same?
BA COP?
Efstathios Rouvas4:54 PM
Exciting stuff !!
Ricardo Zanini4:54 PM
IDK, CoP guys, you tell me :D
David Williams4:54 PM
Time to rename the BA COP, I think.
Ricardo Zanini4:54 PM
+1
Karsten Gresch4:55 PM
+1
Helber Belmiro4:56 PM
@Ricardo can you send the link to CoP chat?
or anyone else :)
Efstathios Rouvas4:56 PM
https://mail.google.com/chat/u/0/#chat/space/AAAA7TmhXNU
^^ link to existing BA CoP gchat
Helber Belmiro4:57 PM
thank you
Rafael Soares4:58 PM
Thanks, @Ivo
Efstathios Rouvas4:58 PM
Smart CoP ?
Kris Verlaenen4:58 PM
https://docs.google.com/document/d/19B3to_3_ZNiVYn0GADdqaFIyj0metP238TE97tGDw4o
Rafael Soares4:58 PM
PAMless CoP :-)
Karsten Gresch4:59 PM
:D
Ivo Bek4:59 PM
https://docs.google.com/presentation/d/1ZCOy_uHrDMK7y97fKm-4XMYpDbF0q7JuikZ1GxdLjkc/edit#slide=id.g6b619a1e04_0_2548
link to slide deck ^^^
Efstathios Rouvas4:59 PM
+1
Gbenga Taylor4:59 PM
HA @Rafael
Paulo Menon4:59 PM
slide deck https://docs.google.com/presentation/d/1ZCOy_uHrDMK7y97fKm-4XMYpDbF0q7JuikZ1GxdLjkc/edit#slide=id.g131bc55b47f_0_0
You4:59 PM
when stateful will be supported?
Efstathios Rouvas5:00 PM
thank you all, really good stuff
Kris Verlaenen5:00 PM
Stateful is part of DP1
Ricardo Zanini5:00 PM
+1
David van Balen5:00 PM
Thanks, Kris
Karsten Gresch5:00 PM
Really great stuff, thank you very much for this session!!! 