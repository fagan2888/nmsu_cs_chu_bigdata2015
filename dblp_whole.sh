#PBS -l nodes=1:ppn=44
#PBS -l walltime=240:00:00

DATA_PATH='put_your_data_path_here'
ZNUM=100
DATE='put_date_here'

for NUM_THREAD in 20
do
    SAMPLER_ID='dblp-whole-z100-thread'$NUM_THREAD 
    java -Xmx40G -cp infdetection.jar sampling.MainInfDetection -chainNum 2 -graphfile $DATA_PATH'cite.txt' -datafile $DATA_PATH'inf_data.txt'  -samplerId $SAMPLER_ID -znum $ZNUM  -burnin 100 -duplicate yes  -concurrent y -numThread $NUM_THREAD -printThread y -checkConsistence n -seq y -debug y > ./log/$SAMPLER_ID-$DATE.log
# 
done
