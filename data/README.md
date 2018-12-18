# Processing Tolstoy's Letters
## How to run it
1. Remove the recipient and Tolstoy's signature by running ./util/strip-recip.py
2. Lemmatize the output by running ./util/run-mystem.sh
3. Generate a stop list based on the output by running ./util/gen-stoplist.py
4. Import your data into a Mallet web with ./util/mallet-import.sh
5. Run Mallet topic modeling on the data using ./util/run-mallet.sh

## How it works

1. We remove the recipient line because we don't want the recipient to be included in the topics. 
We don't want topics that are centered around a person or a family, because these aren't getting to the 
gist of our research questions, "Who did Tolstoy talk to? And about what?" In letters where Tolstoy 
wrote to his wife, Anna Tolstaya, her name shouldn't be coming up in the topic contents. We also remove 
Tolstoy's signature during this step because Lev Tolstoy and his many signatures aren't relevant topics 
either.

2. We lemmatize the output to reduce all the different cases of a word down to its base form. This way, if 
Tolstoy uses "бога" "богу" или "бог", all of these are considered to be "бог" for the topic model's sake.
This usually improves topic model results and is a customary step in topic modelling.

3. We generate a stop list based on the contents of the letters themselves. Our script generates a stop list based on the 400 most common lemmas. We then concatentate our custom list to a stock list for Russian, French, German, and English languages. I also manually added a few elements to the stock list, including the months of the year.

4. Then we import our data into a Mallet web, and run Mallet. I tried 50, 60, 70, 80, 90, and 100 topics, and found that 80 topics yielded the best results. (todo: look at some statistics for the project)

## Todo
- investigate the rectangle brackets in the letters, e.g. in letters/90-22. Should we replace regex '\[(w+)\]' with '\1'?
- write get-named-entities.py