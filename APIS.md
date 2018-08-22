# Running the API's

* [sbr-api](https://github.com/ONSdigital/sbr-api):

```shell
sbt "api/run -Dhttp.port=9002"
```

* [sbr-control-api](https://github.com/ONSdigital/sbr-control-api):

```shell
sbt "api/run -Dsbr.hbase.inmemory=true -Dhttp.port=9001"
```

* [sbr-admin-data-api](https://github.com/ONSdigital/sbr-admin-data-api):

```shell
sbt "api/run -Dsource=hbaseInMemory -Dsbr.hbase.inmemory=true -Dhttp.port=9003"
```

* [business-index-api](https://github.com/ONSdigital/business-index-api):

```shell
elasticsearch
sbt "api/run -Denvironment=local"
```