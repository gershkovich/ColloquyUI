from natasha import NamesExtractor

with open("letters/") as letter:
    extractor = NamesExtractor()
    matches = extractor()