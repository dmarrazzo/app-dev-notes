
```sh
cd word-gen
mvn clean package
oc new-build --strategy docker --binary --name=word-gen
oc start-build word-gen --from-dir . --follow
oc patch imagestream word-gen --type merge -p '{"spec":{"lookupPolicy":{"local":true}}}'
oc tag word-gen:latest word-gen:1.0
cd ..
```


```sh
cd word-count
mvn clean package
oc new-build --strategy docker --binary --name=word-count
oc start-build word-count --from-dir . --follow
oc patch imagestream word-count --type merge -p '{"spec":{"lookupPolicy":{"local":true}}}'
oc tag word-count:latest word-count:1.0
cd ..
```
