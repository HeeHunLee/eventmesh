package org.apache.eventmesh.source.connector.rocketmq;

import io.cloudevents.CloudEvent;
import java.util.List;
import org.apache.eventmesh.client.tcp.EventMeshTCPClient;
import org.apache.eventmesh.client.tcp.EventMeshTCPClientFactory;
import org.apache.eventmesh.client.tcp.conf.EventMeshTCPClientConfig;
import org.apache.eventmesh.common.protocol.tcp.UserAgent;
import org.apache.eventmesh.connector.api.data.ConnectRecord;
import org.apache.eventmesh.source.connector.rocketmq.config.RocketMQSourceConfig;
import org.apache.eventmesh.source.connector.rocketmq.connector.RocketMQSourceConnector;

public class RocketMQSourceWorker {

    public static final String SOURCE_CONSUMER_GROUP = "DEFAULT-CONSUMER-GROUP";
    public static final String SOURCE_CONNECT_NAMESRVADDR = "127.0.0.1:9877";
    public static final String SOURCE_TOPIC = "TopicTest";

    public static final String DESTINATION = "SourceTopic";


    public static void main(String[] args) throws Exception {

        UserAgent userAgent = EventMeshTestUtils.generateClient1();
        EventMeshTCPClientConfig eventMeshTcpClientConfig = EventMeshTCPClientConfig.builder()
            .host("127.0.0.1")
            .port(10002)
            .userAgent(userAgent)
            .build();

        final EventMeshTCPClient<CloudEvent> client =
            EventMeshTCPClientFactory.createEventMeshTCPClient(eventMeshTcpClientConfig, CloudEvent.class);

        client.init();

        RocketMQSourceConnector rocketMQSourceConnector = new RocketMQSourceConnector();

        RocketMQSourceConfig rocketMQSourceConfig = new RocketMQSourceConfig();

        rocketMQSourceConfig.setSourceNameserver(SOURCE_CONNECT_NAMESRVADDR);
        rocketMQSourceConfig.setSourceTopic(SOURCE_TOPIC);
        rocketMQSourceConfig.setSourceGroup(SOURCE_CONSUMER_GROUP);

        rocketMQSourceConnector.init(rocketMQSourceConfig);

        rocketMQSourceConnector.start();

        while(true) {
            List<ConnectRecord> connectorRecordList = rocketMQSourceConnector.poll();
            for(ConnectRecord connectRecord : connectorRecordList) {
                // todo:connectorRecord 转换 cloudEvents
                CloudEvent event = EventMeshTestUtils.generateCloudEventV1(connectRecord.getExtension("topic"), connectRecord.getData().toString());
                client.publish(event, 3000);
                Thread.sleep(500);
            }
        }

    }

}
