export LAB_ID=17


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
--gcpTempLocation=gs://c4e-uc1-dataflow-temp-$LAB_ID/temp \https://github.com/Aniket-d-d/GCP-LABS-Aniket-Kale/blob/main/GCP-Lab%201/readme.txt
--stagingLocation=gs://c4e-uc1-dataflow-temp-$LAB_ID/staging \
--subnetwork=regions/europe-west4/subnetworks/subnet-uc1-$LAB_ID \
--streaming"
