bootstrap server = dm-bus-cbv--pes-dmrg---ut-a.bf2.kafka.rhcloud.com:443
client ID = f0e42480-47d4-47b6-ad03-b7c6c77702e4
client secret = N2E2ymfpWRQRxJ3iK425lzeJcdLOB23u

token endpoint= https://sso.redhat.com/auth/realms/redhat-external/protocol/openid-connect/token


./bin/kafka-console-producer.sh --broker-list 52.73.117.146:9092,34.196.146.135:9092,18.204.0.246:9092 --producer.config producer.properties --topic my-topic