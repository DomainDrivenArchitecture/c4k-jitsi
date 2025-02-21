# Setup 
## Infrastructure on Hetzner / Aws

For a setup on hetzner / aws we use terraform.

```
resource "aws_s3_bucket" "backup" {
  bucket = "backup"
  acl    = "private"

  versioning {
    enabled = false
  }
  tags = {
    name        = "backup"
    Description = "bucket for backups in stage: ${var.stage}"
  }
}

resource "hcloud_server" "jitsi_2025_02" {
  name        = "the name"
  image       = "ubuntu-24.04"
  server_type = "cx32"
  location    = "fsn1"
  ssh_keys    = ...

  lifecycle {
    ignore_changes        = [ssh_keys]
  }
}

resource "aws_route53_record" "v4" {
  for_each ["jitsi", "stun.jitsi", "excalidraw.jitsi", "etherpad.jitsi"]
  zone_id = the_dns_zone
  name    = each.key
  type    = "A"
  ttl     = "300"
  records = [hcloud_server.jitsi_2025_01.ipv4_address]
}

output "ipv4" {
  value = hcloud_server.jitsi_2025_01.ipv4_address
}

```

## k8s minicluster

For k8s installation we use our [provs](https://repo.prod.meissa.de/meissa/provs) with the following configuation:


```
{:fqdn "fqdn-from-above"
 :node {:ipv4 "ip-from-above"}
 :certmanager {:email "admin-email" :letsencryptEndpoint "prod}}
```

## kubectl apply c4k-jitsi

The last step for applying the jitsi deployment is

```
c4k-jitsi config.edn auth.edn | kubectl apply -f -
```

with the following config.edn:

```
{:fqdn "fqdn-from-above"
 :issuer "prod" }
```
