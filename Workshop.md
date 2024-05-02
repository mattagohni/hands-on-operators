## install operator-sdk
download latest sdk from https://github.com/operator-framework/operator-sdk/releases/ for your platform
e.g. linux-vdi `operator-sdk_linux_amd64`

for mac use `v1.32.0` and do not use `brew`

## initialize project with operator-sdk
```bash
$ operator-sdk init --plugins quarkus --domain mattagohni.de --project-version 3
```

```bash
$ operator-sdk create api --group=k8s --version=v1 --kind=MessageServer --namespaced --plugins quarkus
```

## create namespace
```bash
$ kubectl create namespace hello-cloudland
```

## start operator
```bash
$ mvn quarkus:dev
```

## create test entry
```bash
$ kubectl apply -f message-server.yaml --namespace=hello-cloudland
```

## port-forward
```bash
$ kubectl port-forward svc/test-message-server 4711:80
```
