#!/usr/bin/env bash

mkdir -p temp
mkdir -p temp/lemma
mkdir -p temp/recip

doctor() {
	ltrname=`basename $1`
	echo $ltrname
	python util/strip-recip.py "$1" temp/recip/$ltrname
	mystem -d -c --format xml temp/recip/$ltrname temp/lemma/$ltrname

}

export -f doctor
find temp/clean -type f -name '*' | parallel doctor
