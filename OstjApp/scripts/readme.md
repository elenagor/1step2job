## Building and deployng OstjApps

### Build

### Publish as docker image
**_NOTE:_** You should have docker installed to build and publish docker images

To build services run navigate to Solution root (OstjApp)
```
cd OstjApi
dotnet publish --os linux --arch x64 /t:PublishContainer
```

### Run as docker image
To run OstjApi docker image run 
```
docker -e ASPNETCORE_HTTP_PORTS=80 \
    -e ASPNETCORE_ENVIRONMENT=Production \
    -e ASPNETCORE_ConnectionStrings__DefaultConnection="Host=localhost;Port=5432;Database=ostjdb;Username=ostjsvc;Password=ostjsvc!" \
    -e ASPNETCORE_Email__Username="<SMPT_USER_NAME>" \
    -e ASPNETCORE_Email__Password="<SMPT_USER_PASSWORD>" \
    -p 80:80 \
    --name ostjapi \
    ostjapi
```

### Run locally