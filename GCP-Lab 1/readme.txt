
export PROJECT_ID=$(gcloud config get-value project)
export REGION='europe-west4'
export PIPELINE_FOLDER=gs://${PROJECT_ID}
export MAIN_CLASS_NAME=gcp_lab1.PubSubToBigQuery
export RUNNER=DataflowRunner
export LAB_ID=17
export TOPIC=projects/${PROJECT_ID}/topics/uc1-input-topic-17

mvn compile exec:java \
-Dexec.mainClass=MyPipeline \
-Dexec.cleanupDaemonThreads=false \
-Dexec.args=" \
--subTopic=projects/nttdata-c4e-bde/subscriptions/uc1-input-topic-sub-17 \
--dlqTopic=projects/nttdata-c4e-bde/topics/uc1-dlq-topic-17 \
--runner=DataflowRunner \
--project=nttdata-c4e-bde \
--jobName=usecase1-labid-$LAB_ID \
--region=europe-west4 \
--serviceAccount=c4e-uc1-sa-$LAB_ID@nttdata-c4e-bde.iam.gserviceaccount.com \
--maxNumWorkers=1 \
--workerMachineType=n1-standard-1 \
--gcpTempLocation=gs://c4e-uc1-dataflow-temp-$LAB_ID/temp \
--stagingLocation=gs://c4e-uc1-dataflow-temp-$LAB_ID/staging \
--subnetwork=regions/europe-west4/subnetworks/subnet-uc1-$LAB_ID \
--streaming"
