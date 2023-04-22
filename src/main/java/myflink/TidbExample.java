package myflink;

import com.ververica.cdc.connectors.shaded.com.google.common.collect.ImmutableMap;
import com.ververica.cdc.connectors.tidb.TDBSourceOptions;
import com.ververica.cdc.connectors.tidb.TiDBSource;
import com.ververica.cdc.connectors.tidb.TiKVChangeEventDeserializationSchema;
import com.ververica.cdc.connectors.tidb.TiKVSnapshotEventDeserializationSchema;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.util.Collector;
import org.tikv.kvproto.Cdcpb;
import org.tikv.kvproto.Kvrpcpb;

public class TidbExample {
    public static void main(String[] args) throws Exception {

        SourceFunction<String> tidbSource =
                TiDBSource.<String>builder()
                        .database("faker") // set captured database
                        .tableName("book")// set captured table
                        .tiConf(TDBSourceOptions.getTiConfiguration(
                                "119.29.221.226：3390",
                                ImmutableMap.of(
                                        "user", "root",
                                        "password", "ddf@root321"
                                )
                        ))
                        .snapshotEventDeserializer(
                                new TiKVSnapshotEventDeserializationSchema<String>() {
                                    @Override
                                    public void deserialize(
                                            Kvrpcpb.KvPair record, Collector<String> out)
                                            throws Exception {
                                        out.collect(record.toString());
                                    }

                                    @Override
                                    public TypeInformation<String> getProducedType() {
                                        return BasicTypeInfo.STRING_TYPE_INFO;
                                    }
                                })
                        .changeEventDeserializer(
                                new TiKVChangeEventDeserializationSchema<String>() {
                                    @Override
                                    public void deserialize(
                                            Cdcpb.Event.Row record, Collector<String> out)
                                            throws Exception {
                                        out.collect(record.toString());
                                    }

                                    @Override
                                    public TypeInformation<String> getProducedType() {
                                        return BasicTypeInfo.STRING_TYPE_INFO;
                                    }
                                })
                        .build();

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

// enable checkpoint
        env.enableCheckpointing(3000);
        env.addSource(tidbSource)
                .returns(String.class) // explicitly specify the produced type
                .print()
                .setParallelism(1);

        env.execute("Print TiDB Snapshot + Binlog");
    }
}