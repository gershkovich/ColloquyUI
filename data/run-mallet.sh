#!/bin/bash

~/mallet/bin/mallet train-topics --input web.mallet --num-topics 60 --output-state topics/topic-state.gz --num-iterations 100  --xml-topic-report topics/topic-report.xml --output-doc-topics topics/topic-docs.txt
