


# 실행 순서

Zipkin 서버를 도커로 실행하려면 다음 명령어를 사용하세요:
```shell
docker run -d -p 9411:9411 openzipkin/zipkin
```

그런 다음, 각 마이크로서비스를 다음 순서로 실행하세요:
1. Config 서비스
2. Eureka 서비스
3. Gateway 서비스
4. 나머지 마이크로서비스들 (user, product, order, payment)