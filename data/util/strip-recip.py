import sys
import json
import re

if len(sys.argv) < 3:
    print("./strip-recip.py [infile] [outfile]")
    exit()

in_fp = open(sys.argv[1], 'r')
out_fp = open(sys.argv[2], 'w')
recips_fp = open('util/recipients.json', 'r')

in_buff = in_fp.read()
recips = json.load(recips_fp)

def matchify(s):
	return re.compile(s.replace(' ', '\\\s+'))

recips = [matchify(r) for r in recips]

ltr_split = in_buff.split('\n')
for recip in recips:
	ltr_split[0] = recip.sub('', ltr_split[0])

out_fp.write('\n'.join(ltr_split))

in_fp.close()
out_fp.close()
recips_fp.close()
