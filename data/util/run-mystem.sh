#!/usr/bin/env bash

mkdir -p temp
mkdir -p temp/lemma
mkdir -p temp/recip

doctor() {
	ltrname=`basename $1`
	echo $ltrname
	python3 util/strip-recip.py "$1" temp/recip/$ltrname
	mystem -d -c --format xml temp/recip/$ltrname temp/lemma/$ltrname

}

export -f doctor
<<<<<<< HEAD
find letters/ -type f -name '*' | parallel doctor
=======
find temp/clean -type f -name '*' | parallel doctor
>>>>>>> dbabad95cc54d59d8c911bb351e7692ba15715bc
