FROM openjdk:8-jdk-alpine

RUN apk add --update --no-cache bash sudo tar

RUN addgroup -S oraman && adduser -S oraman -G oraman \
 && mkdir -p /opt 

ADD target/oraman-*-dist.tgz /opt/
RUN chown -R oraman:oraman /opt/oraman

USER oraman

# Inicialização e portas
CMD /opt/oraman/oraman run
EXPOSE 8080
