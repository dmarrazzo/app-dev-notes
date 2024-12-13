# CICD

## Continuous Integration

Reference technology: OpenShift Pipelines (Tekton)

* https://github.com/dmarrazzo/fraud-detection-app/blob/main/docs/pipelines.md
* https://www.redhat.com/en/blog/filtering-tekton-trigger-operations
* [Run Pipelines on code change with Triggers](https://blog.yongweilun.me/tekton-cicd-part-2-run-pipelines-on-code-change-with-triggers)

## Continuous Deployment

Reference technology: OpenShift GitOps (ArgoCD)

* https://developers.redhat.com/e-books/gitops-cookbook

The recommended workflow for implementing GitOps with Kubernetes manifests
is known as [trunk-based development](https://trunkbaseddevelopment.com/)

This method defines one branch as the
“trunk” and carries out development on each environment in a different short-lived
branch. When development is complete for that environment, the developer creates a
pull request for the branch to the trunk. Developers can also create a fork to work on an
environment, and then create a branch to merge the fork into the trunk.

