import org.apache.beam.runners.dataflow.options.DataflowPipelineOptions;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.PipelineResult;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.JsonToRow;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.*;
import org.apache.beam.vendor.grpc.v1p36p0.com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyPipeline {

    private static final Logger logs = LoggerFactory.getLogger(MyPipeline.class);

    private static final TupleTag<TableSchema> Valid_Messages = new TupleTag<TableSchema>() {};
    private static final TupleTag<String> Invalid_Messages = new TupleTag<String>(){};


    public interface Options extends DataflowPipelineOptions {
        @Description("PubSub Subscription")
        String getSubTopic();
        void setSubTopic(String subTopic);

        @Description("DLQ topic")
        String getDlqTopic();
        void setDlqTopic(String dlqTopic);

    }

    public static void main(String[] args) {

        PipelineOptionsFactory.register(Options.class);
        Options options = PipelineOptionsFactory.fromArgs(args).withValidation().as(Options.class);
        run(options);

    }

    static class validJson extends DoFn<String,TableSchema> {
        @ProcessElement
        public void processElement(@Element String json,ProcessContext processContext)throws Exception{
            try {
                Gson gson = new Gson();
                TableSchema table = gson.fromJson(json, TableSchema.class);
                processContext.output(Valid_Messages,table);
            }catch(Exception e){
                e.printStackTrace();
                processContext.output(Invalid_Messages,json);
            }
        }
    }

    // Schema for Conversion (JSON to Row)
    public static final Schema rawSchema = Schema
            .builder()
            .addInt32Field("id")
            .addStringField("name")
            .addStringField("surname")
            .build();

    public static PipelineResult run(Options options) {

        Pipeline pipeline = Pipeline.create(options);


        PCollectionTuple pubsubMessage = pipeline
                .apply("Read Message From PS-Subscription", PubsubIO.readStrings().fromSubscription(options.getSubTopic()))

                .apply("Validator", ParDo.of(new validJson()).withOutputTags(Valid_Messages, TupleTagList.of(Invalid_Messages)));


        PCollection<TableSchema> validData = pubsubMessage.get(Valid_Messages);
        PCollection<String> invalidData = pubsubMessage.get(Invalid_Messages);

        validData.apply("Gson to Json Convertor",ParDo.of(new DoFn<TableSchema, String>() {
                    @ProcessElement
                    public void convert(ProcessContext context){
                        Gson g = new Gson();
                        String gsonString = g.toJson(context.element());
                        context.output(gsonString);
                    }
                })).apply("Json To Row Convertor", JsonToRow.withSchema(rawSchema)).
                        apply("Write Message To BigQuery Table", BigQueryIO.<Row>write().to("uc1_17.account")
                        .useBeamSchema()
                        .withCreateDisposition(BigQueryIO.Write.CreateDisposition.CREATE_NEVER)
                        .withWriteDisposition(BigQueryIO.Write.WriteDisposition.WRITE_APPEND));

        invalidData.apply("Send Invalid Message To DLQ", PubsubIO.writeStrings().to(options.getDlqTopic()));

        return pipeline.run();

    }
}
