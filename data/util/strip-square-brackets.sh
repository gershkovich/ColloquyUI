#!/usr/bin/env python

from os import listdir
from os.path import isfile, join, basename
from multiprocessing import Pool
import re
from sys import argv

if len(argv) < 4:
    print('./strip-square-brackets.sh [in_dir] [out dir] [num_procs]')
    exit()

#ideally would do more verification here
LTR_DIR = argv[1]
OUT_DIR = argv[2]
num_procs = int(argv[3])
BRACKET_REGEX = re.compile('\[([^\d]+?)\]')
BRACKET_REPL = r'\1'

letter_paths = [join(LTR_DIR, f) for f in listdir(LTR_DIR) if isfile(join(LTR_DIR, f))]

def strip_brackets(raw_path):
    raw_fp = open(raw_path, 'r')
    out_path = join(OUT_DIR, basename(raw_path))
    out_fp = open(out_path, 'w')
    raw_ltr = raw_fp.read()
    out_ltr = BRACKET_REGEX.sub(BRACKET_REPL, raw_ltr)
    out_fp.write(out_ltr)
    out_fp.close()
    raw_fp.close()

letter_paths = letter_paths[:10]
    
pool = Pool(num_procs)
pool.map(strip_brackets, letter_paths)
