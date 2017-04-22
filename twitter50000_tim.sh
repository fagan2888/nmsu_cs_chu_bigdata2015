#PBS -l nodes=1:ppn=48
#PBS -l walltime=2400:00:00

DATA_PATH='put_data_path_here'
DATE='put_date_here'
ZNUM=10

 
for NUM_THREAD in 2 4 6 8 10 12 14 16 18 20
do
    SAMPLER_ID='twitter50000'-'z'$ZNUM-'cim'-'thread'-$NUM_THREAD-$DATE
    java -Xmx220G -ea -cp infdetection.jar sampling.MainInfDetection -chainNum 2 -graphfile $DATA_PATH'/cite.txt' -datafile $DATA_PATH'/inf_data.txt' -samplerId $SAMPLER_ID -znum $ZNUM  -burnin 10 -numIter 100 -duplicate yes -concurrent y -numThread $NUM_THREAD -checkConsistence n -debug n -printThread y > ./log/$SAMPLER_ID.log 2>&1
done

# fix graph size, change ZNUM
for DATA_SIZE in 50000
do
    DATA_PATH='data/twitter'$DATA_SIZE'/'
    NUM_THREAD=20
    for ZNUM in 10 20 30 40 50 #60 70 80 90 100
    do
        SAMPLER_ID='twitter'$DATA_SIZE'-z'$ZNUM'-cim-serial-'$DATE
        java -Xmx220G -ea -cp infdetection.jar sampling.MainInfDetection -chainNum 2 -graphfile $DATA_PATH'/cite.txt' -datafile $DATA_PATH'/inf_data.txt' -samplerId $SAMPLER_ID -znum $ZNUM  -burnin 10 -numIter 100 -duplicate yes -concurrent n -checkConsistence n -debug n -printThread y -access iterator > ./log/$SAMPLER_ID.log 2>&1  

        SAMPLER_ID='twitter'$DATA_SIZE'-z'$ZNUM'-cim-parallel-'$DATE
        java -Xmx220G -ea -cp infdetection.jar sampling.MainInfDetection -chainNum 2 -graphfile $DATA_PATH'/cite.txt' -datafile $DATA_PATH'/inf_data.txt' -samplerId $SAMPLER_ID-iterator -znum $ZNUM  -burnin 10 -numIter 100 -duplicate yes  -concurrent y -numThread $NUM_THREAD -checkConsistence n -access iterator -model oaim  > log/$SAMPLER_ID.log  2>&1 
    done
done
