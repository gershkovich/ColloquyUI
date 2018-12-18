#!/usr/bin/env bash

#SBATCH --time=00:10:00
#SBATCH --job-name=""
#SBATCH --cluster=smp
#SBATCH --partition=smp
#SBATCH --nodes=1
#SBATCH --ntasks-per-node=10
#SBATCH --ntasks=10
#SBATCH --output=log.txt
#SBATCH --mail-type=ALL
#SBATCH --mail-user=richardbfulop@gmail.com

python ./util/get-named-entities.py 10
