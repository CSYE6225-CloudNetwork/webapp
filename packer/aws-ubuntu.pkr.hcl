packer {
  required_plugins {
    amazon = {
      version = ">= 1.2.8"
      source  = "github.com/hashicorp/amazon"
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
  #   default     = "us-west-2"
}

variable "profile" {
  description = "AWS CLI profile to use"
  type        = string
  #   default     = "dev"
}

variable "ssh_username" {
  description = "SSH username for the instance"
  type        = string
  #   default     = "ubuntu"
}

variable "USERNAME" {
  description = "MySQL Database username"
  type        = string
}

variable "MYSQL_PWD" {
  description = "MySQL Database password"
  type        = string
}


source "amazon-ebs" "ubuntu" {
  ami_name      = "base-ami-aws-${formatdate("YYYYMMDDHHmmss", timestamp())}"
  instance_type = var.instance_type
  region        = var.region
  profile       = var.profile # Ensure AWS CLI is configured for this profile

  source_ami_filter {
    filters = {
      name                = "ubuntu/images/*ubuntu-noble-24.04-amd64-server-*"
      root-device-type    = "ebs"
      virtualization-type = "hvm"
    }
    most_recent = true
    owners      = ["099720109477"] # Canonical's official AWS account ID
  }

  ssh_username = var.ssh_username
}

build {
  name = "learn-packer"
  sources = [
    "source.amazon-ebs.ubuntu"
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
}

