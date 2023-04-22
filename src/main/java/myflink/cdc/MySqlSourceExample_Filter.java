package myflink.cdc;

import com.ververica.cdc.connectors.mysql.source.MySqlSource;
import com.ververica.cdc.debezium.JsonDebeziumDeserializationSchema;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class MySqlSourceExample_Filter {
    public static void main(String[] args) throws Exception {

        MySqlSource<String> mySqlSource = MySqlSource.<String>builder()
                .hostname("127.0.0.1")
                .port(3306)
                .includeSchemaChanges(true)
                .databaseList("faker") // set captured database
                .tableList("faker.word") // set captured table
                .username("root")
                .password("root")
                .deserializer(new JsonDebeziumDeserializationSchema()) // converts SourceRecord to JSON String
                .build();

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        env.enableCheckpointing(3000);

        DataStream<String> stream = env.fromSource(mySqlSource, WatermarkStrategy.noWatermarks(), "MySQL Source");
        DataStream<String> words = stream.filter(new FilterFunction<String>() {
            @Override
            public boolean filter(String value) throws Exception {
                return value != " ";
            }
        });
        words.print();
        env.execute();
    }
}