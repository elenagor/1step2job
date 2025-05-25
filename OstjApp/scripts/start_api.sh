docker -e ASPNETCORE_HTTP_PORTS=80 \
    -e ASPNETCORE_ENVIRONMENT=Production \
    -e ASPNETCORE_ConnectionStrings__DefaultConnection="Host=localhost;Port=5433;Database=ostjdb;Username=ostjsvc;Password=ostjsvc!" \
    -e ASPNETCORE_Email__Username="yuri.ackerman@1setp2job.ai" \
    -e ASPNETCORE_Email__Password="oH94E%W$FD*NrxQObB3@TgB@" \
    -p 80:80 \
    --name ostjapi \
    ostjapi