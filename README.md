# Automated Assignment of Human Readable Descriptions (AHRD)

Short descriptions in sequence databases are useful to quickly gain insight into important information about a sequence, for example in search results. We developed a new program called “Automatic assignment of Human Readable Descriptions” (AHRD) with the aim to select descriptions and Gene Ontology terms that are concise, informative and precise. AHRD outperforms competing methods and can overcome problems caused by wrong annotations, lack of similar sequences and partial alignments.

## Table of contents

* [1 Getting started](#1-getting-started)
    * [1.1 Requirements](#1-1-requirements)
    * [1.2 Installation](#1-2-installation)
        * [1.2.1 Get AHRD](#1-2-1-get-ahrd)
        * [1.2.2 Build the executable jar](#1-2-2-build-the-executable-jar)
* [2. Usage](#2-usage)
    * [2.1 AHRD example usages](#2-1-ahrd-example-usages)
    * [2.2 Input](#2-2-input)
        * [2.2.1 Required input data](#2-2-1-required-input-data)
        * [2.2.2 Optional input data](#2-2-2-optional-input-data)
        * [2.2.3 Required config files](#2-2-3-required-config-files)
            * [2.2.3.1 Test custom blacklists and filters](#2-2-3-1-test-custom-blacklists-and-filters)
    * [2.3 Batcher](#2-3-batcher)
    * [2.4. Output](#2-4-output)
        * [2.4.1 Tab-Delimited Table](#2-4-1-tab-delimited-table)
        * [2.4.2 Fasta-Format](#2-4-2-fasta-format)
    * [2.5 AHRD run using BLASTX results](#2-5-ahrd-run-using-blastx-results)
    * [2.6 Computing F-Scores for selected parameter sets (AHRD-Evaluator)](#2-6-computing-f-scores-for-selected-parameter-sets-ahrd-evaluator)
        * [2.6.1 Computing Gene Ontology Annotation F-Scores](#2-6-1-computing-gene-ontology-annotation-f-scores)
    * [2.7 Parameter Optimization](#2-7-parameter-optimization)
        * [2.7.1 Parameter Optimization via Genetic Algorithm](#2-7-1-parameter-optimization-via-genetic-algorithm)
        * [2.7.2 Parameter Optimization via Simulated Annealing](#2-7-2-parameter-optimization-via-simulated-annealing)
        * [2.7.3 Optimization in parallel (Trainer-Batcher)](#2-7-3-optimization-in-parallel-trainer-batcher)
* [3 Algorithm](#3-algorithm)
    * [3.1 Pseudo-Code](#3-1-pseudo-code)
    * [3.2 Used Formulae and Parameters](#3-2-used-formulae-and-parameters)
    * [3.3 Parameters](#3-3-parameters)
        * [3.3.1 Parameters controlling the parsing of tabular sequence similarity search result tables (legacy BLAST, BLAST+, and BLAT)](#3-3-1-parameters-controlling-the-parsing-of-tabular-sequence-similarity-search-result-tables-legacy-blast-blast-and-blat)
        * [3.3.2 Parameters controlling Gene Ontology term annotations](#3-3-2-parameters-controlling-gene-ontology-term-annotations)
            * [3.3.2.1 Prefer reference proteins as candidates that have GO Term annotations](#3-3-2-1-prefer-reference-proteins-as-candidates-that-have-go-term-annotations)
            * [3.3.2.2 Custom reference Gene Ontology annotations (non UniprotKB GOA)](#3-3-2-2-custom-reference-gene-ontology-annotations-non-uniprotkb-goa)
* [4 Testing](#4-testing)
* [5 License](#5-license)
* [6 Authors](#6-authors)
* [7 References](#7-references)


## 1 Getting started

### 1.1 Requirements

AHRD is a Java-Program which requires ``Java 1.7`` or higher and ``ant``.


### 1.2 Installation

#### 1.2.1 Get AHRD

Copy (clone) AHRD to your computer using git via command-line, then change into AHRD's directory, and finally use the latest stable version:

<code>git clone https://github.com/groupschoof/AHRD.git

cd AHRD

git checkout tags/v3.3.3</code>

Alternativelly without using ``git``, you can download AHRD version ``v3.3.3`` ("zip":https://github.com/groupschoof/AHRD/archive/v3.3.3.zip or "tar.gz":https://github.com/groupschoof/AHRD/archive/v3.3.3.tar.gz) and extract it.

#### 1.2.2 Build the executable jar

Running

<code>ant dist</code>

will create the executable JAR-File: ``./dist/ahrd.jar``

## 2 Usage

All AHRD-Inputs are passed to AHRD in a single YML-File.  See ``./ahrd_example_input.yml`` for details.  (About YAML-Format see <a href="http://en.wikipedia.org/wiki/YAML">Wikipedia/YAML</a>)

Basically AHRD needs a FASTA-File of amino acid sequences and different files containing the results from the respective BLAST searches, in our example we searched three databases: Uniprot/trEMBL, Uniprot/Swissprot and TAIR10. Note, that AHRD is generic and can make use of any number of different Blast databases that do not necessarily have to be the above ones. If e.g. annotating genes from a fungal genome searching yeast databases might be more recommendable than using TAIR (_Arabidopsis thaliana_).

All parameters can be set manually, or the default ones can be used as given in the example input file ``ahrd_example_input.yml`` (see sections "2.1":#21-ahrd-example-usages and "3.2":#32-used-formulae-and-parameters for more details).

In order to parallelize the protein function annotation processes,  AHRD can be run on batches of recommended size between 1,000 to 2,000 proteins.  If you want to annotate very large protein sets or have low memory capacities use the included Batcher to split your input-data into Batches of appropriate size (see section "2.3":#23-batcher). _Note:_ As of Java 7 or higher AHRD is quite fast and batching might no longer be necessary.

### 2.1 AHRD example usages

There are _two_ template AHRD input files provided that you should use according to your use case. All example input files are stored in ``./test/resources`` and are named ``ahrd_example_input*.yml``. You can run AHRD on any of these use cases with <code>java -Xmx2g -jar ./dist/ahrd.jar your_use_case_file.yml</code>

| *Use Case* | *Template File* |
| ---------- | --------------- |
| Annotate your Query proteins with Human Readable Descriptions (HRD) | ``./test/resources/ahrd_example_input.yml`` |
| Annotate your Query proteins with HRD _and_ Gene Ontology (GO) terms | ``./test/resources/ahrd_example_input_go_prediction.yml`` |

### 2.2 Input

Example files for all input files can be found under ``./test/resources/``. _NOTE:_ Only files containing ``example`` in their filename should be used as template input files. Other YAML files are used for testing purposes.

#### 2.2.1 Required input data

1. Protein sequences in fasta format
2. Sequence Similarity Search (``blastp`` or ``blat``) results in tabular format

(If you run AHRD in batches the blast search results need to be batched in the same way as the fasta files.)

Recommended Sequence Similarity Search:

For your query proteins you should start independent BLAST searches e.g.  in the three different databases mentioned above:

<code>
blastp -outfmt 6 -query query_sequences_AA.fasta -db uniprot_swissprot.fasta -out query_vs_swissprot.txt
</code>

#### 2.2.2 Optional input data

If you want AHRD to predict your query protein's functions with Gene Ontology (GO) terms, you need to provide the GO annotations of reference proteins. See section "3.3.2":#332-parameters-controlling-gene-ontology-term-annotations for more details.  

#### 2.2.3 Required config files

1. Input yml with all pathes and parameters according to your needs (see ahrd_example_input.yml and section Parameters)
1. Blacklists and filters (they can either be used as provided or can be adapted to your needs and databases). Each of these files contains a list of valid Java regular expressions, one per line. For details on Java regular expressions please refer to http://docs.oracle.com/javase/tutorial/essential/regex/. In the YAML file, the blacklists can be put at the upper most level in order to apply for all blast search results, or can be provided for each blast search result individually. The description filter files have to be specified for each blast search result separately.
    - **Description blacklist** (Argument ``blacklist: ./test/resources/blacklist_descline.txt``) - Any Blast-Hit's description matching one of the regular expressions in this file will be ignored.
    - **Description filter** for each single blast database (Argument ``filter: ./test/resources/filter_descline_sprot.txt``) - Any part of a Blast-Hit description that matches any one of the regular expressions in this file will be deleted from the description.
    - **Token blacklist** (Argument ``token_blacklist: ./test/resources/blacklist_token.txt``) - Blast-Hit's descriptions are composed of words. Any such word matching any one of the regular expressions in this file will be ignored by AHRD's scoring, albeit it will not be deleted from the description and thus might still be seen in the final output.

##### 2.2.3.1 Test custom blacklists and filters

As explained in 2.2.3 AHRD makes use of blacklists and filters provided as Java regular expressions. You can test your own custom blacklists and filters:

1. Put the strings representing Blast-Hit descriptions or words contained in them in the file ``./test/resources/regex_list.txt``. Note, that each line is interpreted as a single entry.
2. Put the Java regular expressions you want to test in file ``./test/resources/match_list.txt``, using one regular expression per line.
3. Execute ``ant test.regexs`` and study the output.

Example Output for test string "activity", and regular expressions "(?i)interacting", and "(?i)activity" applied in serial:

<code>[junit] activity

[junit] (?i)interacting -> activity

[junit] (?i)activity -> </code>

The above example demonstrates how the first regular expression does not match anything in the test string "activity", but after matching it against the second regular expression nothing remains, because the matched substring has been filtered out. As you can see, this test applies all provided regular expression _in order of appearance_ and shows what _remains_ of the provided test string after filtering with the provided regular expressions.


### 2.3 Batcher

AHRD provides a function to generate several input.yml files from large datasets, consisting of _batches_ of query proteins. For each of these batches the user is expected to provide the batch's query proteins in FASTA format, and one Blast result file for each database searched. The AHRD batcher will then generate a unique input.yml file and entry in a batch shell script to execute AHRD on the respective batches in parallel for example on a compute cluster using a batch-system like LSF. We recommend this for very large datasets (more than a genome) or computers with low RAM.

To generate the mentioned input.yml files and batcher shell script that can subsequently be used to start AHRD in parallel use the batcher function as follows:
<code>java -cp ./dist/ahrd.jar ahrd.controller.Batcher ./batcher_input_example.yml</code>
You will have to edit ``./batcher_input_example.yml`` and provide the following arguments. Note, that in the mentioned directories each file will be interpreted as belonging to one unique Batch, if and only if they have identical file names. 

1. ``shell_script:`` Path to the shell-script file which later contains all the statements to invoke AHRD on each supplied batch in parallel.
1. ``ahrd_call: "java -Xmx2048m -jar ./dist/ahrd.jar #batch#"`` This line will be the basis of executing AHRD on each supplied batch. The line _must_ contain ``#batch#`` wich will be replaced with the input.yml files. To use a batch system like LSF this line has to be modified to something like ``bsub -q normal 'java -Xmx2048m -jar ./dist/ahrd.jar #batch#'``
1. ``proteins_dir:`` The path to the directory the query protein batches are stored in.
1. ``batch_ymls_dir:`` The path to the directory the AHRD-Batcher should store the generated input.yml files in.
1. ``dir:`` Each database entry requires this argument, the path to the directory each batch's blast result file from searches in the corresponding Blast-database is located.
1. ``output_dir:`` The directory each AHRD run should create a subdirectory with the output for the processed batch.

_Batch-Name requirement:_ All above explained files belonging to the same Batch _must_ have the same name. This name must start with alpha-numeric characters and may finish with digits indicating the Batch's number. File extensions are allowed to be varying. 

### 2.4 Output

AHRD supports two different formats. The default one is a tab-delimited table.
The other is FASTA-Format.

#### 2.4.1 Tab-Delimited Table

AHRD writes out a CSV table with the following columns:
1. Protein-Accesion -- The Query Protein's Accession
1. Blast-Hit-Accession -- The Accession of the Protein the assigned description was taken from.
1. AHRD-Quality-Code -- explained below
1. Human-Readable-Description -- The assigned HRD
1. Gene-Ontology-ID -- If AHRD was requested to generate Gene-Ontology-Annotations, they are appended here.

AHRD's quality-code consists of a three character string, where each character is either <strong>'*'</strong> if the respective criteria is met or *'-'* otherwise. Their meaning is explained in the following table:

| Position | Criteria |
| -------- | -------- |
| 1 | Bit score of the blast result is >50 and e-value is <e-10 |
| 2 | Overlap of the blast result is >60% |
| 3 | Top token score of assigned HRD is >0.5 |

#### 2.4.2 Fasta-Format

To set AHRD to write its output in FASTA-Format set the following switch in the input.yml:

<code>
output_fasta: true
</code>

AHRD will write a valid FASTA-File of your query-proteins where the Header will be composed of the same parts as above, but here separated by whitespaces.

### 2.5 AHRD run using BLASTX results

In order to run AHRD on BLASTX results instead of BLASTP results you have to modify the following parameters in the ahrd_example_input.yml:

<code>token_score_bit_score_weight: 0.5

token_score_database_score_weight: 0.3

token_score_overlap_score_weight: 0.2</code>

Since the algorithm is based on protein sequences and the BLASTX searches are based on nucleotide sequence there will be a problem calculating the overlap score of the blast result.  To overcome this problem the token_score_overlap_score_weight has to be set to 0.0. Therefore the other two scores have to be raised. These three parameters have to sum up to 1. The resulting parameter configuration could look like this: 
<code>token_score_bit_score_weight: 0.6

token_score_database_score_weight: 0.4

token_score_overlap_score_weight: 0.0</code>

### 2.6 Computing F-Scores for selected parameter sets (AHRD-Evaluator)

Having different parameter sets AHRD enables you to compute their performance in terms of F-Scores for each protein in comparison to a ground truth. Optionally you can also revise the theorectically maximum attainable F-Score and see how well the best Hits from each sequence similarity search perform. In order to do so, use the Evaluator function:

<code>java -cp ./dist/ahrd.jar ahrd.controller.Evaluator evaluator_example_hrd.yml</code> 

See ``./test/resources/evaluator_example_hrd.yml`` for more details.

Parameters specific to the Evaluator function are:
1. ``ground_truth_fasta: path_to_your_ground_truth.fasta``  _Required_ parameter pointing to the fasta formatted file containing the ground truth proteins _including_ the ground truth Human Readable Descriptions. (See ``./test/resources/ground_truth.fasta`` for details.)
1. ``find_highest_possible_evaluation_score: false`` Set to ``true`` if you want to see how the best fitting candidate description among the sequence similarity search hits performs. This gives you the theoretical best possible performance that can be achieved with the provided sequence similarity search results. In addition to the F-score itself the accession and description of the best performing candidate protein is written to the output as well. 
1. ``write_scores_to_output: false`` Set to ``true`` if you want to see all internal intermediate scores: Token-Scores, Lexical-Scores, and Description-Scores. Use with extreme caution, because this option is meant for developers _only_.
1. ``write_best_blast_hits_to_output: false`` Set to ``true`` if you want to see the respective best sequence similarity search Hits and their performances.
1. ``f_measure_beta_parameter: 1.0`` This parameter is also used by AHRD-Trainer. See section "2.7":#27-parameter-optimization for details.
1. ``write_fscore_details_to_output: false`` Set to ``true`` to add Precision (ie. positive predictive value) and Recall (ie. true positive rate) to the output of all F-Scores.
1. ``competitors:`` AHRDs' annotation performance can be compared to one or more of its competitors. Each competitor has to be given a name that will be used to identify it in the output (eg. "``blast2go:``").
1. ``descriptions: path_to_competitor_descriptions.tsv``  A competitors description annotations have to be supplied in a tab delimited file of two columns (1: short protein accession, 2: description). The file should not have a column name header. Each protein accession should be present only once (one description per protein). All supplied protein accessions have to be present in the ``proteins_fasta`` file too.
  
#### 2.6.1 Computing Gene Ontology Annotation F-Scores

In addition to description annotations, Gene Ontology term annotations can be evaluated as well (see section "3.3.2":#332-parameters-controlling-gene-ontology-term-annotations for details on enabling the annoation with Gene Ontology terms). 

See ``./test/resources/evaluator_example_hrd_and_go.yml`` for more details.

1. ``ground_truth_go_annotations: path_to_your_ground_truth_go_annotations.goa`` Enables the calculation of F-scores based on the comparison of AHRD-annotated Gene Ontology term versus a ground truth of Genen Ontotoly annotations (goa file). The ground truth annotations have to be provided in tab delimited file containing only two columns (1: short protein accession, 2: GO-term accession). The goa file should not contain column names and every provided protein short accession has to be present in the ``proteins_fasta`` file.
1. ``find_highest_possible_go_score: false:`` Set to ``true`` to include the F-scores of the best fitting candidate Gene Ontology annotations from the sequence similarity search results. 
1. ``simple_GO_f1_scores: false`` Set to ``true`` to enable the calculation and output of an F-score based on 'simple' cardinality of Gene Ontology annotations in comparison to the ``ground_truth_go_annotations``. If the evaluation of Genen Ontoloy annotations is triggered because ``ground_truth_go_annotations`` are supplied and none of the other go-based scores (``ancestry_GO_f1_scores`` or ``semsim_GO_f1_scores``) are set to ``true``, ``simple_GO_f1_scores`` will be set to ``true`` automatically (if not explicitly set to ``false``).    
1. ``ancestry_GO_f1_scores: false`` Set to ``true`` to obtain an F-score based on the cardinality of Gene Ontology annotations extended by their ancestry (Adds all term down towards the ontologies root).  
1. ``semsim_GO_f1_scores: false`` Set to ``true`` to calculate an F-score based on the semantic similarity between the Gene Ontology annotation AHRD made and the ground truth GO annotation. The semantic similarity of GO term sets is derived from their individual information content which in turn is calculated based on the term annotation frequencies in Swiss Prot.  
``go_db_path: AHRD_dir/data`` Directory to save a copy of Swiss Prot, the Gene Ontology and data derived from them. This data is stored as a serialized java object in a file named ``accGoDb.ser``. If the ``go_db_path`` option is unspecified the ``data`` subdirectory in the AHRD directory is used. If the directory does not contain a previously created ``accGoDb.ser`` file, AHRD downloads a copy of Swissprot (``uniprot_sprot.dat.gz``: 520Mb) and the Gene Ontology  (``go_daily-termdb-tables.tar.gz``: 12Mb). AHRD then derives the mapping of each Gene Ontology term to all its ancestors from the Gene Ontology and the annotation frequency of Gene Ontology terms from Swiss Prot. Once the ``accGoDb.ser`` file has been created this process does only have to be repeated if the information becomes out dated and you wish to renew the file. To trigger a recalculation using newer versions of Swiss Prot or Gene Ontology simply delete the ``uniprot_sprot.dat.gz``, ``go_daily-termdb-tables.tar.gz`` and ``accGoDb.ser`` files and run an AHRD evaluation including Gene Ontology annotations again. 

### 2.7 Parameter Optimization

If you have a ground truth of well annotated proteins (at least 1000) from an organism closely related to the proteins you want to annotate, you can optimize AHRD's parameters to reproduce protein function predictions more alike to your ground truth.

Two approaches for parameter optimization can be chosen from:
1. a genetic algorithm
1. simulated annealing

Basically the input file is almost identical to the AHRD evaluator run. The following additional parameters are available to modify the behavior of _both_ approaches; all numeric values given show the _default_ used for the respective parameter:

1. ``path_log: your_log_file.tsv`` For the genetic algorithm: Stores the best parameters the evaluation found in each successive generation, their F1-Score, the difference in score to the best of the last generation and the method leading the particular parameter set (<b>random</b>ly generated, <b>mutation</b> of a parameter set, <b>recombination</b> of two parameter sets, <b>seed</b> parameter set the algorithm was started with). For the simmulated annealing: Stores the parameters evaluated in any optimization iteration, their F1-Score, and the difference in score to the currently accepted parameter set.
1. ``f_measure_beta_parameter: 1.0`` The scaling factor used to compute the F-Score of an assigned HRD when comparing it with the reference. See <a href="https://en.wikipedia.org/wiki/F1_score">Wikipedia/F1_score</a> for details.
1. ``mutator_mean: 0.25``  Mutate a randomly selected parameter by value gaussian normal distributed with this mean 
1. ``mutator_deviation: 0.15``  Mutate a randomly selected parameter by value gaussian normal distributed with this standard deviation

Both approaches either optimize AHRD's parameters according to its description annotation performance _or_ its Gene Ontology term annotation performance.
Optimization according to Gene Ontology based F-scores is automatically triggered if at least one valid ``gene_ontology_reference:`` file is provided _and_ a valid ``ground_truth_go_annotations:`` file is provided (section: "2.7":#261-computing-gene-ontology-annotation-f-Scores).
Then AHRD uses the highest complexity GO based F-score it is requested to calculate (in order of increasing complexity: ``simple_GO_f1_scores:``, ``ancestry_GO_f1_scores:`` and ``semsim_GO_f1_scores:``).
 
#### 2.7.1 Parameter Optimization via Genetic Algorithm

To start the genetic algorithm approach:
<code>java -cp dist/ahrd.jar ahrd.controller.GeneticTrainer trainer_example_input.yml</code>

_Note:_ See ``./test/resources/genetic_algorithm_trainer_example_input.yml`` for details.

Parameters specific to the genetic algorithm; all numeric values given show the _default_ used for the respective parameter:
1. ``number_of_generations: 100`` The number of generations the parameter populations are allowed to evolve over.
1. ``population_size: 200`` The number of parameter sets in each generation.

#### 2.7.2 Parameter Optimization via Simulated Annealing

Alternatively a simulated annealing approach can be used:
<code>java -cp dist/ahrd.jar ahrd.controller.SimulatedAnnealingTrainer trainer_example_input.yml</code>

_Note:_ See ``./test/resources/simmulated_annealing_trainer_example_input.yml`` for details.

1. ``temperature: 75000``  Temperature used in _simulated annealing_ 
1. ``cool_down_by: 1``  Dimish temperature by this value after each simulated annealing iteration
1. ``optimization_acceptance_probability_scaling_factor: 2500000000.0``  Used to scale the probability of accepting a currently evaluated parameter set. ``P(Accept) := exp(-delta_scores*scaling_factor/current-temperature)``
1. ``remember_simulated_annealing_path: false``  Set to ``true``, if you want the optimization to remember already visited parameter sets and their performance. This increases memory usage but improves final results.  

#### 2.7.3 Optimization in parallel (Trainer-Batcher)

Simulated Annealing is a time and resource consuming process. Because AHRD uses quite a number of parameters, the search space is accordingly large. Hence it is recommendable to start independend simulated annealing runs in parallel. Do this using 

<code>java -cp ./dist/ahrd.jar ahrd.controller.TrainerBatcher ./test/resources/trainer_batcher_example.yml</code>

The following parameters are specific to the Trainer-Batcher:

1. ``shell_script: ./start_ahrd_batched.sh`` The path to the shell script you will use to start the simulated annealing jobs in parallel.
1. ``ahrd_call: "java -Xmx2048m -cp ./dist/ahrd.jar ahrd.controller.Trainer #batch#"`` Defines a line in the resulting shell script, requires the presence of ``#batch#`` which will be replaced with the respective generated Trainer inputs. This line can be used to submit the parallel optimizations into a job queue.
1. ``no_start_positions_in_parameter_space: 1024``  Defines how many parallel optimization runs you want to start. 
1. ``batch_ymls_dir: ./trainer_batch_ymls``  Path to the directory in which to store the generated Trainer input files. 
1. ``output_dir: ./trainer_results``  Path to the directory in which to store each Trainer run's output(s). 

_Note_, that the Trainer-Batcher works very much like the AHRD-Batcher (section "2.3":#23-batcher). Particularly you need to respect the _batch-name requirements_ explained there.

The above Trainer-Batcher example input shows how to automatically generated a desired number of input files with different starting points in parameter space. These input files can then directly be used with the above documented AHRD Trainer (section "2.7":#27-parameter-optimization). 

## 3 Algorithm

Based on e-values the 200 best scoring blast results are chosen from each database-search (e.g. Swissprot, TAIR, trEMBL). For all resulting candidate description lines a score is calculated using a lexical approach. First each description line is passed through two regular expression filters. The first filter discards any matching description line in order to ignore descriptions like e.g. 'Whole genome shotgun sequence', while the second filter tailors the description lines deleting matching parts, in order to discard e.g. the trailing Species-Descriptions 'OS=Arabidopsis thaliana [...]". In the second step of the scoring each description line is split into single tokens, which are passed through a blacklist filter, ignoring all matching tokens in terms of score. Tokens are sequences of characters with a collective meaning. For each token a score is calculated from three single scores with different weights, the bit score, the database score and the overlap score. The bit score is provided within the blast result. The database score is a fixed score for each blast database, based on the description quality of the database. The overlap score reflects the overlap of the query and subject sequence. In the second step the sum of all token scores from a description line is divided by a correction factor that avoids the scoring system from being biased towards longer or shorter description lines. From this ranking now the best scoring description line can be chosen. In the last step a domain name provided by InterProScan results, if available, is extracted and appended to the best scoring description line for each uncharacterized protein. In the end for each uncharacterized protein a description line is selected that comes from a high-scoring BLAST match, that contains words occurring frequently in the descriptions of highest scoring BLAST matches and that does not contain meaningless "fill words". Each HRD line will contain an evaluation section that reflects the significance of the assigned human readable description.  

### 3.1 Pseudo-Code

1. Choose best scoring blast results, 200 from each database searched
1. Filter description lines of above blast-results using regular expressions:
    - Reject those matched by any regex given in e.g. ./test/resources/blacklist_descline.txt,
    - Delete those parts of each description line, matching any regex in e.g. ./test/resources/filter_descline_sprot.txt. 
1. Divide each description line into tokens (characters of collective meaning)
    - In terms of score ignore any tokens matching regexs given e.g. in ./test/resources/blacklist_token.txt.
1. Token score (calculated from: bitscore, database weight, overlap score)
1. Lexical score (calculated from: Token score, High score factor, Pattern factor, Correction factor)
1. Description score (calculated from: Lexical score and Blast score)
1. Choose best scoring description line

### 3.2 Used Formulae and Parameters

![Used Formulae and Parameters](https://raw.githubusercontent.com/groupschoof/AHRD/master/images/formulae.jpg)

### 3.3 Parameters

Above formulae use the following parameters as given in *./ahrd_example_input.yml*. These parameters can either be used as provided or can be adapted to your needs.

The weights in the above formulae are

<code>token_score_bit_score_weight: 0.468

token_score_database_score_weight: 0.2098

token_score_overlap_score_weight: 0.3221 </code>

and Blast-Database specific, for example for UniprotKB/Swissprot:

<code>weight: 653

description_score_bit_score_weight: 2.717061 </code>

#### 3.3.1 Parameters controlling the parsing of tabular sequence similarity search result tables (legacy BLAST, BLAST+, and BLAT)

AHRD has been designed to work with tabular sequence similarity search results in tabular format. By default it parses the tabular "Blast 8" output:

| Sequence Similarity Search Tool | recommended output format (``command line switch``) |
| --------------------------------| ------------------------------------------------------ |
| Blast+ | ``-outfmt 6`` |
| legacy Blast | ``-m 8`` |
| Blat | ``-out=blast8`` |

The following paramters can be set optionally, if your tabular sequence similarity search result deviates from the above "Blast 8" format. See example file ``./test/resources/ahrd_input_seq_sim_table.yml`` for more details.

| Optional Parameter | example | meaning of parameter |
| ------------------ | ------- | -------------------- |
| seq_sim_search_table_comment_line_regex | ``"#"`` | single character that starts a to be ignored comment line |
| seq_sim_search_table_sep | ``"\t"`` | single character that separates columns |
| seq_sim_search_table_query_col | ``10`` | number of column holding the query protein's accession |
| seq_sim_search_table_subject_col | ``11`` | number of column holding the Hit protein's accession |
| seq_sim_search_table_query_start_col | ``16`` | number of column holding the query's start amino acid position in the local alignment |
| seq_sim_search_table_query_end_col | ``17`` | number of column holding the query's end amino acid position in the local alignment |
| seq_sim_search_table_subject_start_col | ``18`` | number of column holding the Hit's start amino acid position in the local alignment |
| seq_sim_search_table_subject_end_col | ``19`` | number of column holding the Hit's end amino acid position in the local alignment |
| seq_sim_search_table_e_value_col | ``20`` | number of column holding the Hit's E-Value |
| seq_sim_search_table_bit_score_col | ``21`` | number of column holding the Hit's Bit-Score |

_NOTE:_ All above column numbers start counting with zero, i.e. the first column has number 0.

#### 3.3.2 Parameters controlling Gene Ontology term annotations

AHRD is capable of annotating the Query proteins with Gene Ontology (GO) terms. It does so, by transferring the reference GO terms found in the Blast Hit AHRD selects as source of the resulting HRD. To be able to pass these reference GO terms AHRD needs a reference GO annotation file (GOA). By default AHRD expects this GOA file to be in the standard Uniprot format. You can download the latest GOA file from the "Uniprot server":http://ftp.ebi.ac.uk/pub/databases/GO/goa/UNIPROT/. To obtain GO annotations for all UniprotKB proteins download file ``goa_uniprot_all.gaf.gz`` (last visit Feb 16th 2017)

To have AHRD annotate your proteins with GO terms, you just need to provide the optional parameter ``gene_ontology_reference``. See example file ``./test/resources/ahrd_input_seq_sim_table_go_prediction.yml`` for more details.

##### 3.3.2.1 Prefer reference proteins as candidates that have GO Term annotations

The parameter ``prefer_reference_with_go_annos: true`` is highly recommended when aiming to annotate your query proteins with GO Terms. If this parameter is set to true only those candidate references are considered that also have GO Term annotations. However, if you put more emphasis on Human Readable Descriptions and are prepared to accept a couple of your queries to not get any GO Term predictions you can switch this off with ``prefer_reference_with_go_annos: false`` or just omit the parameter as by default it is set to ``false``.  

##### 3.3.2.2 Custom reference Gene Ontology annotations (non UniprotKB GOA)

Unfortunately UniprotKB GOA files use short protein accessions like ``W9QFR0``, while the UniprotKB FASTA databases use the long protein accessions with pipes like this ``tr|W9QFR0|W9QFR0_9ROSA``. In order to enable AHRD to match the correct short and long accessions, and thus the database GO annotations it uses regular expressions to parse both the long accessions and the GOA files. By default AHRD is setup to handle Uniprot formats, but you can provide custom regular expressions for your own custom GOA files:

1. Set the regular expression to extract short protein accessions mapped to GO terms from the database GOA using ``gene_ontology_reference_regex:``. For example for TAIR10 ("ATH_GO_GOSLIM.txt":https://www.arabidopsis.org/download_files/GO_and_PO_Annotations/Gene_Ontology_Annotations/ATH_GO_GOSLIM.txt.gz, use ``gene_ontology_reference_regex: "^AT[1-5CM]G\\d{5}\\t\\S+\\t(?<shortAccession>AT[1-5CM]G\\d{5})(\\.\\d{1,2})?\\t[^\\t]+\\t[^\\t]+\\t(?<goTerm>GO:\\d{7})\\t"`` (The default is: ``gene_ontology_reference_regex: ^UniProtKB\\s+(?<shortAccession>\\S+)\\s+\\S+\\s+(?<goTerm>GO:\\d{7})``)
1. Set the Blast database specific regular expression, used to extract the short accessions from long ones with ``short_accession_regex:`` For example for TAIR10, use ``short_accession_regex: "^(?<shortAccession>[^\\.]+)(\\.\\d+)?$"``. (The default is: ``"^[^|]+\\|(?<shortAccession>[^|]+)"``)

_Note:_ You must provide the above named match groups ``shortAccession`` and ``goTerm``, respectively.

## 4 Testing

If you want to run the complete JUnit Test-Suite execute: <code>ant</code>

If you want to execute a test run on example proteins use: <code>ant test.run</code>

## 5 License

See attached file LICENSE.txt for details.

## 6 Authors

Dr. Asis Hallab, Kathrin Klee, Florian Boecker, Dr. Sri Girish, and Prof. Dr. Heiko Schoof

INRES Crop Bioinformatics
University of Bonn
Katzenburgweg 2
53115 Bonn
Germany

## 7 References

High quality genome projects that used AHRD:

<p><sup>1</sup> Young, Nevin D., Frédéric Debellé, Giles E. D. Oldroyd, Rene Geurts, Steven B. Cannon, Michael K. Udvardi, Vagner A. Benedito, et al. “The Medicago Genome Provides Insight into the Evolution of Rhizobial Symbioses.” Nature 480, no. 7378 (December 22, 2011): 520–24. doi:10.1038/nature10625.</p> 

<p><sup>2</sup> The Tomato Genome Consortium. “The Tomato Genome Sequence Provides Insights into Fleshy Fruit Evolution.” Nature 485, no. 7400 (May 31, 2012): 635–41. doi:10.1038/nature11119.</p>

<p><sup>3</sup> International Wheat Genome Sequencing Consortium (IWGSC). “A Chromosome-Based Draft Sequence of the Hexaploid Bread Wheat (Triticum Aestivum) Genome.” Science (New York, N.Y.) 345, no. 6194 (July 18, 2014): 1251788. doi:10.1126/science.1251788.</p>

<p><sup>4</sup> International Barley Genome Sequencing Consortium, Klaus F. X. Mayer, Robbie Waugh, John W. S. Brown, Alan Schulman, Peter Langridge, Matthias Platzer, et al. “A Physical, Genetic and Functional Sequence Assembly of the Barley Genome.” Nature 491, no. 7426 (November 29, 2012): 711–16. doi:10.1038/nature11543.</p>

<p><sup>5</sup> Wang, W., G. Haberer, H. Gundlach, C. Gläßer, T. Nussbaumer, M. C. Luo, A. Lomsadze, et al. “The Spirodela Polyrhiza Genome Reveals Insights into Its Neotenous Reduction Fast Growth and Aquatic Lifestyle.” Nature Communications 5 (2014): 3311. doi:10.1038/ncomms4311.</p>

<p><sup>6</sup> Guo, Shaogui, Jianguo Zhang, Honghe Sun, Jerome Salse, William J. Lucas, Haiying Zhang, Yi Zheng, et al. “The Draft Genome of Watermelon (Citrullus Lanatus) and Resequencing of 20 Diverse Accessions.” Nature Genetics 45, no. 1 (January 2013): 51–58. doi:10.1038/ng.2470.</p>

<p>AHRD was applied on all plant genomes present in the public database *PlantsDB*:</p>

<p><sup>7</sup> Spannagl, Manuel, Thomas Nussbaumer, Kai C. Bader, Mihaela M. Martis, Michael Seidel, Karl G. Kugler, Heidrun Gundlach, and Klaus F. X. Mayer. “PGSB PlantsDB: Updates to the Database Framework for Comparative Plant Genome Research.” Nucleic Acids Research 44, no. D1 (January 4, 2016): D1141–47. doi:10.1093/nar/gkv1130.</p>