Code repository for paper:
Chuan Hu, Huiping Cao: Discovering Time-evolving Influence from Dynamic Heterogeneous Graphs, 2015 IEEE International Conference on Big Data (IEEE BigData 2015) the first Interna- tional Workshop on Mining Big Data in Social Networks (MBD-SONET).

Data link: ask for download link. Chuan Hu: chuanhu90@gmail.com

Command usage:
java -cp infdetection.jar sampling.MainInfDetection 
-model <model: oaim, laim or cim> 
-chainNum <# of chains> 
-graphfile <path to graph file>
-datafile <path to object profile data file>
-samplerId <sampler id>
-znum <# of topics>
-anum <# of latent aspects>
-burnin <# of burnin iterations>
-numIter <# of total iterations>
-concurrent <y or n. indicate parallel Gibbs sampling or not> 
-numThread <# of threads>
-checkConsistence <y or n. whether check consistence of counts. mainly used in debug> 
-debug <y or n. debug on sampling process>
-printThread <y or n. whether print thread running time details>
-access <iterator or index>

Example shell files:
1. twitter50000_tim.sh: run TIM model on Twitter50000 data set
2. dblp_whole.sh: run TIM model on DBLP data set
