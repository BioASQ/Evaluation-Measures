This directory contains EvalMeasuresBioASQ Java project. 

This project includes evaluation Java Source Package for BioASQ flat evaluation measures. 
converters Source Package also included, for conversion of files in integer format needed for both flat and hierarchical measures. 

Libraries used by EvalMeasuresBioASQ are located in \BioASQEvaluation\dist\lib

Instructions for BioASQ evaluation measures
-----------------------------------------------

Task A
---------

1. Before running the measures the results of the system and the golden standard results need to be mapped to the integer-based format:

java -Xmx10G -cp $CLASSPATH:./flat/BioASQEvaluation/dist/BioASQEvaluation.jar converters.MapMeshResults mesh/mapping.txt system_A_results.txt system_A_results_mapped.txt

where file system_A_results.txt contains a line (with labels seperated by space) for each test instance e.g.:
D05632 D04322
D033321 D98766 D98765
...

A file named system_A_results_mapped.txt will be created containing the coressponding integer labels e.g.:
45 67
23 90 89
...

The same procedure is repeated for the file with the true labels.

2. For running the flat measures the following command is invoked:

java -Xmx10G -cp $CLASSPATH:./flat/BioASQEvaluation/dist/BioASQEvaluation.jar evaluation.Evaluator true_labels_mapped.txt system_A_results_mapped.txt

The program will print to the standard output the following numbers: accuracy EbP EbR EbF MaP MaR MaF MiP MiR MiF


3. For running the hierarchical measures:
./hierarchical/bin/HEMKit ./mesh/mesh_hier_int.txt true_labels_mapped.txt system_A_results_mapped.txt 4 5

will result to the following output: hP hR hF LCA-P LCA-R LCA-F



Task B
---------

1. For running the measures for Task B, phase A the following command is invoked:

java -Xmx10G -cp $CLASSPATH:./flat/BioASQEvaluation/dist/BioASQEvaluation.jar evaluation.EvaluatorTask1b -phaseA -e 3 golden_file.json system_response.json

2. For running the measures for Task B, phase B the following command is invoked:

java -Xmx10G -cp $CLASSPATH:./flat/BioASQEvaluation/dist/BioASQEvaluation.jar evaluation.EvaluatorTask1b -phaseB -e 3 golden_file.json system_response.json