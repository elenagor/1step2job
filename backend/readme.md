### Get project from GIT repository
cd <YourWorkingFolder>/1step2job/backend 
mvn clean package -DskipTests  -f pom.xml

### Map local ips to defined domains
/etc/hosts:
192.168.1.183 kfk.1step2job.ai db.1step2job.ai
72.65.240.221	llm.1step2job.ai

### Download Kafka Docker image and run as container:
```
https://kafka.apache.org/downloads
```
docker run -d  \
  -p 9092:9092 \
  --name kafka-local \
  -e KAFKA_NODE_ID=1 \
  -e KAFKA_PROCESS_ROLES=broker,controller \
  -e KAFKA_LISTENERS=PLAINTEXT://:9092,CONTROLLER://:9093 \
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://kfk.1step2job.ai:9092 \
  -e KAFKA_CONTROLLER_LISTENER_NAMES=CONTROLLER \
  -e KAFKA_LISTENER_SECURITY_PROTOCOL_MAP=CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT \
  -e KAFKA_CONTROLLER_QUORUM_VOTERS=1@localhost:9093 \
  -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 \
  -e KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR=1 \
  -e KAFKA_TRANSACTION_STATE_LOG_MIN_ISR=1 \
  -e KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS=0 \
  -e KAFKA_NUM_PARTITIONS=3 \
  -e KAFKA_LOG_DIRS=/var/lib/kafka/data \
  apache/kafka:4.0.0

### Or Run kafka as service in terminal
```
cd <KafkaBinDir> or <KafkaBinWindowsDir>
./kafka-server-start.sh ../../config/server.properties
```

### Test kafka service:
./kafka-topics.sh --bootstrap-server kfk.1step2job.ai:9092 --list
### Create topics in kafka:
./kafka-topics.sh --bootstrap-server kfk.1step2job.ai:9092 --create --topic ostj_person_jobs
./kafka-topics.sh --bootstrap-server kfk.1step2job.ai:9092 --create --topic ostj_user_resumes
### Test kafka topics are created
./kafka-topics.sh --bootstrap-server kfk.1step2job.ai:9092 --list


### For test pjmatcher kafka listener
```
./kafka-console-producer.sh --bootstrap-server kfk.1step2job.ai:9092 --topic ostj_user_resumes
./kafka-console-producer.sh --topic ostj_person_jobs --bootstrap-server kfk.1step2job.ai:9092 --property "parse.key=true" --property "key.separator=="
```
{ "resumeFilePath":"data/Person.txt", "jdFilePath":"data/JD_Match1.txt","promptFilePath":"prompt.txt"}
{ "resumeFilePath":"data/Person.txt", "jdFilePath":"","promptFilePath":"prompt_get_info.txt"}
{ "PersonId":"1", "jdFilePath":"","promptFilePath":"prompt_get_info.txt"}
{ "PersonId":"1", "JobId":"1","promptFilePath":"prompt.txt"}
{ "PersonId":"1", "JobId":"1","PromptId":"1"}
{ "PersonId":"1","ProfileId":"1", "JobId":"1","PromptId":"1"}
{ "PositionId":"1"}

### For development
docker build . -t ostj/pjmapper -f ./stream-personjob/Dockerfile
docker build . -t ostj/pjmatcher -f ./resume-matching/Dockerfile

docker run --name pjmatcher ostj/pjmatcher:latest
docker run --name pjmapper ostj/pjmapper:latest
