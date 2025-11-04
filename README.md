


# 실행 순서

Zipkin 서버를 도커로 실행하려면 다음 명령어를 사용하세요:
```shell
docker run -d -p 9411:9411 openzipkin/zipkin
```

Redis 서버를 도커로 실행하려면 다음 명령어를 사용하세요:
```shell
cd redis # 필요 시
docker-compose up -d
cd .. # 필요 시
```


그런 다음, 각 마이크로서비스를 다음 순서로 실행하세요:
1. Config 서비스
2. Eureka 서비스
3. Gateway 서비스
4. 나머지 마이크로서비스들 (user, product, order, payment)

<img width="1048" height="855" alt="image" src="https://github.com/user-attachments/assets/716fa5e1-d611-497f-a674-e7279585e141" />

<img width="1043" height="702" alt="image" src="https://github.com/user-attachments/assets/c8c7f5ba-b1be-4ad5-8dc3-3e15e03b616d" />

<img width="1051" height="682" alt="image" src="https://github.com/user-attachments/assets/97feb33a-3b9d-4ad8-bfc8-cb686a52e3e9" />

<img width="1057" height="568" alt="image" src="https://github.com/user-attachments/assets/034a3e86-6b6f-40bf-90b8-b200c2b2aa9f" />



