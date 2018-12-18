from sklearn.feature_extraction.text import CountVectorizer
from json import dump

def get_top_n_words(corpus, n=200):
    vec = CountVectorizer(input='filename', strip_accents='unicode').fit(corpus)
    bag_of_words = vec.transform(corpus)
    sum_words = bag_of_words.sum(axis=0) 
    words_freq = [(word, sum_words[0, idx]) for word, idx in vec.vocabulary_.items()]
    words_freq = sorted(words_freq, key = lambda x: x[1], reverse=True)
    return words_freq[:n]

from os import listdir
from os.path import isfile, join
p = 'out'
onlyfiles = [join(p, f) for f in listdir(p) if isfile(join(p, f))]
top_words = get_top_n_words(onlyfiles, 400)
letters = ['й','ц','у','к','е','н','г','ш','щ','з','х','ъ','ф','ы',
           'в','а','п','о','л','д','ж','э','я','ч','с','м','и','т','ь','б','ю','ё']

with open("stoplists/stoplist.txt", "w") as out_fp:
    for letter in letters:
        out_fp.write(letter + ' ')
    #concat stoplists/ru.txt to stoplists/stoplist.txt
    with open('stoplists/ru.txt', 'r') as ru_fp:
        out_fp.write('\n')
        out_fp.write(ru_fp.read())
