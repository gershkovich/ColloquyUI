mkdir -p out

flatten() {
	ltrname=`basename $1`
	echo $ltrname
	java -jar ~/oxygen/lib/saxon9ee.jar -s:$1 -o:out/$ltrname -xsl:gen-mallet-input.xslt
}
export -f flatten;

find temp/lemma/ -type f -name '*' | parallel flatten;
