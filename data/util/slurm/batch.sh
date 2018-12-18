#!/usr/bin/env bash

#SBATCH --time=01:00:00
#SBATCH --job-name=""
#SBATCH --cluster=smp
#SBATCH --partition=smp
#SBATCH --nodes=1
#SBATCH --ntasks-per-node=10
#SBATCH --ntasks=10
#SBATCH --output=log.txt
#SBATCH --mail-type=ALL
#SBATCH --mail-user=richardbfulop@gmail.com

export PATH=$PATH:"~/local/bin/"
module load java
./util/strip-square-brackets.sh letters temp/clean 10
./util/run-mystem.sh
./util/clean-mallet-input.sh
./util/mallet-import.sh
