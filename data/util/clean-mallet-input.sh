mkdir -p out

flatten() {
	ltrname=`basename $1`
	echo $ltrname
	saxon -s:$1 -o:out/$ltrname -xsl:util/gen-mallet-input.xslt
}
export -f flatten;

find temp/lemma/ -type f -name '*' | parallel flatten;
