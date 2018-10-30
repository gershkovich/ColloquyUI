from natasha import PersonExtractor
from os.path import isfile, join, basename
from os import listdir
import json
from multiprocessing import Pool
import sys
import codecs

def process_ltr(ltr):
    names = []
    extractor = PersonExtractor()
    with codecs.open(ltr, mode='r', encoding='utf-8') as letter_fp:
        contents = letter_fp.read()
        matches = extractor(contents)
        for m in matches:
            names.append({"name": m.fact.as_json, 
                          "filename": basename(ltr)})
    return names

ltr_root = 'letters'
letters = [join(ltr_root, f) for f in listdir(ltr_root) if isfile(join(ltr_root, f))]

if len(sys.argv) < 2:
	print("./get-named-entites [num-procs]")
	exit()

num_procs = int(sys.argv[1])
if num_procs > 0:
    pool = Pool(num_procs)
    all_names = pool.map(process_ltr, letters)
else:
    all_names = [process_ltr(ltr_path) for ltr_path in letters] 

with codecs.open('util/names.json', mode='w', encoding='utf-8') as out_fp:
    json.dump(all_names, out_fp, ensure_ascii=False)
