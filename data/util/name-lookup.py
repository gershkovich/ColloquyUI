from urllib.request import urlopen
from urllib.parse import urlencode

QUERY_ENDPOINT = "https://query.wikidata.org/bigdata/namespace/wdq/sparql"
country = "wd:Q34266"
person_name = "лев николаевич толстой"
query = """SELECT DISTINCT ?person 
WHERE 
{
     ?person wdt:P31 wd:Q5 .
     ?person wdt:P27 %s .
     ?person rdfs:label ?personLabel .
     FILTER(CONTAINS(LCASE(?personLabel), "%s"@ru)).
}
""" % (country, person_name)
query_dict = {"query": query}
query_obj = bytes(urlencode(query_dict, encoding='utf-8'), encoding='utf-8')
url = QUERY_ENDPOINT
contents = urlopen(url, data=query_obj).read()
print(str(contents, encoding='utf-8'))
