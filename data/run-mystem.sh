#!/usr/bin/env bash

mkdir -p temp
mkdir -p temp/lemma
for ltr in letters/*
do
	ltrname=`basename $ltr`
	echo $ltrname
	mystem -d -c --format xml $ltr temp/lemma/$ltrname
done;

