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

### Download and build model
It is recommended to install new UV python package manager from [here](https://docs.astral.sh/uv/#highlights). Follow instructions for you OS.

When UV is installed create Python Virtual Environment. Commands below assume that location for Virtual Environemnts is exists and located in ~/.local/venvs (you can have any other location depending on your needs and OS)
```
uv venv --python 3.12 --seed ~/.local/venvs/llm
```
The command above will download python3.12 and create virtual environment

To activate VENV 
- On Mac/Linux
```
source ~/.local/venvs/llm/bin/activate
```
- On Windows
```
%HOME%\.local\venvs\llm\bin\acitvate.bat
```

Install Huggignface tools
```
uv pip install "huggingface-hub[cli]"
```

Download model (assuming Qwen-4b). List of models you can find [Qwen models](https://huggingface.co/collections/Qwen/qwen3-67dd247413f0e2e4f653967f)
Create a folder to store model files (hereafter ~/projects/models/qwen3-4b)
```
huggingface-cli download "Qwen/Qwen3-4B" --local-dir ~/projects/models/qwen3-4b
```

Convert downloaded model to GGUF format comatible with llama.cpp (this assuems you have llama.cpp installed on you machine).
Change directory to location of llama.cpp/bin (e.g. if llama.cpp is installed on Mac with brew, then it would be /opt/homebrew/Cellar/llama.cpp/5310/bin).
```
cd /opt/homebrew/Cellar/llama.cpp/5310/bin
python convert_hf_to_gguf.py ~/projects/models/qwen3-4b --outfile ~/projects/models/qwen3-4b/qwen3-4b.gguf
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
$ dotnet ef migrations add <Name of The Migration (sort of comment)>
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
$ dotnet ef database drop
```

If you want to drop all migrations history and start over
```
$ dotnet ef migrations remove
$ dotnet ef migrations add <Some initial name>
```
