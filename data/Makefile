.PHONY: brackets lemma preimport import topics

topics: topics/web.mallet
	./util/mallet-run.sh

import: out/*
	./util/mallet-import.sh

preimport: temp/lemma/*
	./util/clean-mallet-input.sh

lemma: temp/clean/*
	./util/run-mystem.sh	

brackets: letters/*
	./util/strip-square-brackets.sh letters temp/clean 5


