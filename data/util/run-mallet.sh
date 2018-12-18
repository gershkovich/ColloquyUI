#!/bin/bash

~/mallet/bin/mallet train-topics --input web.mallet --num-topics 80 --output-state topics/topic-state.gz --num-iterations 200  --xml-topic-report topics/topic-report.xml --output-doc-topics topics/topic-docs.txt
