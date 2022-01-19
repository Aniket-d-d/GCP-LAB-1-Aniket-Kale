import com.google.api.services.bigquery.model.TableRow;
import org.apache.beam.runners.dataflow.options.DataflowPipelineOptions;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;

public class MyPipeline {


    public interface MyOptions extends DataflowPipelineOptions {

        @Description("Subscription Topic")
        String gettopicName();
        void settopicName(String topicName);


    }


    public static void main(String[] args) {
        PipelineOptionsFactory.register(MyOptions.class);
        MyOptions options = PipelineOptionsFactory.fromArgs(args).withValidation().as(MyOptions.class);

        Pipeline pipeline = Pipeline.create(options);

        PCollection<String> pubsubmessage = pipeline.apply("Read PubSub Messgages", PubsubIO.readStrings().fromSubscription(options.gettopicName()));
        PCollection<TableRow> bqrow = pubsubmessage.apply(ParDo.of(new ConverttoStringBq()));

        bqrow.apply(BigQueryIO.writeTableRows().to("nttdata-c4e-bde.uc1_17.account")
                .withCreateDisposition(BigQueryIO.Write.CreateDisposition.CREATE_NEVER)
                .withWriteDisposition(BigQueryIO.Write.WriteDisposition.WRITE_APPEND));

        pipeline.run();
    }

    public static class ConverttoStringBq extends DoFn<String, TableRow> {
        @ProcessElement
        public void processing(ProcessContext processContext){
            TableRow tableRow = new TableRow().set("id", processContext.element())
                    .set("name", processContext.element())
                    .set("surname", processContext.element());
            processContext.output(tableRow);
        }
    }
}