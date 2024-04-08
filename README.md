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
## Workshop Users
User werden über die Oberfläche angelegt. Die Schlüssel werden entsprechend notiert und den Teilnehmern zur Verfügung gestellt

## Konfiguration
### Aws cli
`~/.aws/credentials`
```
[workshop-<YOUR_NUMBER>]
aws_access_key_id = <YOUR_KEY_ID>
aws_secret_access_key = <YOUR_ACCESS_KEY>
```

```bash
aws eks --region $(terraform output -raw region) update-kubeconfig \
    --name $(terraform output -raw cluster_name) --profile workshop --alias workshop-teilnehmer
```



## User Berechtigen
Nachdem der cluster erstellt wurde, müssen die User der configMap hinzugefügt werden.

```bash
kubectl edit -n kube-system configmap/aws-auth
```

hier müssen dann alle User hinzugefügt werden

```yaml
apiVersion: v1
data:
  
  mapRoles: |
    .
    .
    .
  # this is the part where every user has to be added  
  mapUsers: |
    - userarn: arn:aws:iam::878321264576:user/workshop-1
      username: workshop
      groups:
      - workshop
kind: ConfigMap
metadata:
  creationTimestamp: "2024-04-03T11:58:04Z"
  name: aws-auth
```


## Todos

- [ ] User anlegen
- [ ] onboarding testen
- [ ] operator vorbereiten
- [ ] aufgabenstellung definieren
- [ ] lösung vorbereiten
