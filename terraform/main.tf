provider "google" {
  project     = var.project
  region      = var.region
}

terraform {
  required_providers {
    google = "0.20.0"
  }
  required_version = ">= 1.0"
}
