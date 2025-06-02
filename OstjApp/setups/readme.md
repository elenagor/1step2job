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

### Run locally (Need to be performed only once for each environment)
Run Postges as Docker
```
$ docker pull pgvector/pgvector:pg17
$ docker run -itd -e POSTGRES_USER=ostjsvc -e POSTGRES_DB=ostjdb -e POSTGRES_PASSWORD=ostjsvc! -p 5432:5432 -v ./data:/<localpath>/data/ostj --name ostjdbv pgvector/pgvector:pg17
$ docker exec -it ostjdbv /bin/bash
```
**NOTE** The prompt will be changes it '#' as you will be working in Docker container console
```
# psql -U ostjsvc
# CREATE EXTENSION vector;
```

### Run Model
Run llama.cpp (assumes modle is downloaded at ~/projects/models/qwen3-8b/qwen3-8b.gguf, change the path is different)
```
$ llama-server -m  ~/projects/models/qwen3-8b/qwen3-8b.gguf -a qwen --port 8000 -ngl 99 -fa -sm layer --presence-penalty 1.5 -c 40960 -n 32768 --no-context-shift --no-webui --pooling mean
```

### Create/update Database
Navigate to OstjApp

After changing model in the OstjApp/OstjApi project (dotnet code) create a new database migration
```
$ dotnet ef migrations add <Name of The Migration>
```

When entity model has changed by:
- You after creating a new migrations above
- You pooled new migration from GIT after Somebody else had Entity Model changes
- Create a new database

```
$ dotnet ef database update
```

To delete database (NOTE: All data will be destroeyd)
```
$ dotnet ef database remove
```

If you want to drop all migrations history and start over
```
$ dotnet ef migrations remove
$ dotnet ef migrations add <Some initial name>
```
