# Rundown of `ci.yaml` workflow
Before running jobs, define event which will trigger workflow and set some env variables.

## Trivy job
1. Clone repository
2. Run Trivy to scan our GitHub repo
3. Build an image from Dockerfile
4. Scan for image vulnerabilities

trivy.yaml can be added to the root directory as its config file.
trivy-secret.yaml can be added to the root directory to write custom rules, enable, allow or disable built in rules.
More info: https://github.com/aquasecurity/trivy-action, https://aquasecurity.github.io/trivy/v0.29.2/docs/

## Hadolint job
1. Clone the repository
2. Run hadolint

Uses .hadolint.yaml (from root directory) as its config file. The default Docker image location is root. More info: https://github.com/hadolint/hadolint-action

## TFlint job
1. Clone the repository
2. Setup TFLint
3. Init TFlint
    - this step just installs plugins like GCP, AWS, Microsoft Azure or even our custom-made plugins
4. Run TFlint

Uses .tflint.hcl (from root directory) as its config file. The default plugin is for GCP but AWS and Microsoft Azure plugins can be added. More info: https://github.com/terraform-linters/setup-tflint

## Lint job
1. Set up GO
2. Clone the repository
3. Go mod tidy
4. Run go linter using a premade GH action

Uses .golangci.yaml (from root directory) as its config file.

## Editorconfig job
1. Clone the repository
2. Install Editorconfig and run it

Uses .editorconfig (from root directory) as its config file.

## Go test job
1. Set up GO
2. Clone the repository
3. Go mod tidy
4. Run go tests

## Licenses check
1. Clone repository
2. Set up GO
3. Install go-licenses
4. Run go-licenses

This step prints out a comma-separated report (CSV) listing the libraries used by a binary/package, the URL where their licenses can be viewed and the type of license.

## Sonarcloud check
1. Clone repository
2. Run sonarcloud check

## Check version file job
This step needs Lint job, Editorconfig job, Go test job, Licenses check, and Sonarcloud check to succeed to execute
1. Clone the repository
2. Get changed files using a premade GH action
3. Check if .ver file is on the list of changed files (if it's not, fail the workflow)

## Upload docker image job
This step needs Check version file job to succeed to execute "so we don't override an already existing Docker image"
1. Clone the repository
2. Get the version from .ver file
3. Get the pull request number
4. Login to private repository (Nexus)
5. Build a docker image (uses Dockerfile from the root directory)
6. Tag the image
7. Push the image to private repository

## Upload zip job
This step needs Check version file job to succeed to execute so we don't override an already existing zip artifact
1. Clone the repository
2. Get the version from .ver file
3. Get the pull request number
4. Set extension for url (where to upload the zip)
5. Zip and upload to private repository (Nexus)
