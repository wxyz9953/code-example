package example;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.alicloud.openservices.tablestore.SyncClient;
import com.alicloud.openservices.tablestore.model.GetRowResponse;
import com.alicloud.openservices.tablestore.model.GetRowRequest;
import com.alicloud.openservices.tablestore.model.PrimaryKey;
import com.alicloud.openservices.tablestore.model.PrimaryKeyBuilder;
import com.alicloud.openservices.tablestore.model.PrimaryKeyValue;
import com.alicloud.openservices.tablestore.model.Row;
import com.alicloud.openservices.tablestore.model.SingleRowQueryCriteria;
import com.aliyun.fc.runtime.Context;
import com.aliyun.fc.runtime.StreamRequestHandler;
import com.aliyun.fc.runtime.FunctionInitializer;
import com.aliyun.fc.runtime.PreStopHandler;

public class App implements StreamRequestHandler, FunctionInitializer, PreStopHandler {

    private SyncClient client = null;

    @Override
    public void initialize(Context context) {
        String endpoint = System.getenv("ENDPOINT"), instanceName = System.getenv("INSTANCE_NAME");
        String accessKeyId = System.getenv("ACCESS_KEY"), accessKeySecret = System.getenv("ACCESS_KEY_SECRET");
        client = new SyncClient(endpoint, accessKeyId, accessKeySecret, instanceName);
    }

    @Override
    public void preStop(Context context) {
        if(client != null) client.shutdown();
    }

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
        PrimaryKey primaryKey = PrimaryKeyBuilder.createPrimaryKeyBuilder()
        .addPrimaryKeyColumn("region", PrimaryKeyValue.fromString("abc"))
        .addPrimaryKeyColumn("id", PrimaryKeyValue.fromLong(1))
        .build();

        // 读取一行数据，设置数据表名称。
        SingleRowQueryCriteria criteria = new SingleRowQueryCriteria("fc_test", primaryKey);
        // 设置读取最新版本
        criteria.setMaxVersions(1);
        GetRowResponse getRowResponse = client.getRow(new GetRowRequest(criteria));
        Row row = getRowResponse.getRow();
        outputStream.write(row.toString().getBytes());
        outputStream.flush();
    }
}
