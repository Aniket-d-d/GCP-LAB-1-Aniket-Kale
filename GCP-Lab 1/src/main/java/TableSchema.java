import org.apache.beam.sdk.schemas.JavaFieldSchema;
import org.apache.beam.sdk.schemas.annotations.DefaultSchema;
import org.apache.beam.sdk.schemas.annotations.SchemaCreate;


@DefaultSchema(JavaFieldSchema.class)
public class TableSchema {

    @SchemaCreate
    public TableSchema(int new_id , String new_name , String new_surname){
    }
}
