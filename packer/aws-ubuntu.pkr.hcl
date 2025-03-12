packer {
  required_plugins {
    amazon = {
      version = ">= 1.2.8"
      source  = "github.com/hashicorp/amazon"
    }
    googlecompute = {
      version = ">= 1.0.0"
      source  = "github.com/hashicorp/googlecompute"
    }
  }
}

variable "instance_type" {
            description = "Type of instance to be used"
  type        = string
}

variable "region" {
  description = "AWS region to deploy the instance"
  type        = string
}

variable "AWS_DEMO_ACCOUNT_ID" {
  description = "AWS Demo account ID"
  type        = string
}

variable "ssh_username" {
  description = "SSH username for the instance"
  type        = string
}

variable "USERNAME" {
  description = "MySQL Database username"
  type        = string
}

variable "MYSQL_PWD" {
  description = "MySQL Database password"
  type        = string
}

# variable "profile" {
#   description = "aws profile name"
#   type        = string
# }

// GCP specific variables
variable "gcp_project_id" {
  description = "GCP Project ID"
  type        = string
}

variable "gcp_zone" {
  description = "GCP Zone to deploy the instance"
  type        = string
  default     = "us-central1-a"
}

variable "gcp_machine_type" {
  description = "GCP Machine type"
  type        = string
  default     = "e2-medium"
}

// New variable for machine image name
variable "gcp_machine_image_name" {
  description = "Name for the GCP machine image"
  type        = string
  default     = "app-machine-image"
}

locals {
  timestamp      = formatdate("YYYYMMDDHHmmss", timestamp())
  gcp_image_name = "base-image-gcp-${local.timestamp}"
}

// AWS Source
source "amazon-ebs" "ubuntu" {
  ami_name      = "base-ami-aws-${local.timestamp}"
  instance_type = var.instance_type
  region        = var.region

  ami_users = [var.AWS_DEMO_ACCOUNT_ID]
  #   profile       = var.profile
  source_ami_filter {
    filters = {
      name                = "ubuntu/images/*ubuntu-noble-24.04-amd64-server-*"
      root-device-type    = "ebs"
      virtualization-type = "hvm"
    }
    most_recent = true
    owners      = ["099720109477"] // Canonical's official AWS account ID
  }
  ssh_username = var.ssh_username
}

// GCP Source
source "googlecompute" "ubuntu" {
  project_id              = var.gcp_project_id
  source_image_family     = "ubuntu-minimal-2404-lts-amd64"
  zone                    = var.gcp_zone
  image_name              = local.gcp_image_name
  image_description       = "Custom image for application"
  ssh_username            = var.ssh_username
  machine_type            = var.gcp_machine_type
  image_storage_locations = ["us"]
  disk_size               = 20
}

build {
  name = "learn-packer"
  sources = [
    "source.amazon-ebs.ubuntu",
    "source.googlecompute.ubuntu"
  ]

  provisioner "file" {
    source      = "../webapp/src/main/resources/setupscript.sh"
    destination = "/tmp/setupscript.sh"
  }

  provisioner "file" {
    source      = "../webapp/target/webapp-0.0.1-SNAPSHOT.jar"
    destination = "/tmp/"
  }

  provisioner "shell" {
    inline = [
      "sudo apt-get update -y && sudo apt-get install -y dos2unix",
      "dos2unix /tmp/setupscript.sh",
      "chmod +x /tmp/setupscript.sh",
      "sudo /tmp/setupscript.sh /tmp/webapp-0.0.1-SNAPSHOT.jar ${var.USERNAME} ${var.MYSQL_PWD}"
    ]
  }

  // Post-processor to create a GCP machine image from the disk image
  post-processor "shell-local" {
    // Only run this for the GCP build
    only = ["googlecompute.ubuntu"]

    inline = [
      // Create a temporary VM from the disk image
      "echo 'Creating temporary VM from disk image...'",
      "gcloud compute instances create temp-vm-for-machine-image --project=${var.gcp_project_id} --zone=${var.gcp_zone} --machine-type=${var.gcp_machine_type} --image=${local.gcp_image_name} --image-project=${var.gcp_project_id} || echo 'Failed to create VM, continuing...'",

    ]
  }

  post-processor "shell-local" {
    // Only run this for the GCP build
    only = ["googlecompute.ubuntu"]

    inline = [

      // Create a machine image from the VM
      "echo 'Creating machine image from VM...'",
      "gcloud compute machine-images create ${var.gcp_machine_image_name}-${local.timestamp} --project=${var.gcp_project_id} --source-instance=temp-vm-for-machine-image --source-instance-zone=${var.gcp_zone}|| echo 'Failed to create MI, continuing...'",
    ]
  }

  post-processor "shell-local" {
    // Only run this for the GCP build
    only = ["googlecompute.ubuntu"]

    inline = [
      // Delete the temporary VM
      "echo 'Cleaning up temporary VM...'",
      "gcloud compute instances delete temp-vm-for-machine-image --project=${var.gcp_project_id} --zone=${var.gcp_zone} --quiet || (echo 'Failed to delete VM, continuing...' && exit 0)"
    ]
  }
}
