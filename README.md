# hands-on-operators
Repository for the workshop "hands on operators"

## Cluster erstellen
```bash
$ terraform apply
```

## Anmeldung am Cluster
```bash
aws eks --region $(terraform output -raw region) update-kubeconfig --name $(terraform output -raw cluster_name)
```

## Cluster zerstören
```bash
$ terraform destroy
```
