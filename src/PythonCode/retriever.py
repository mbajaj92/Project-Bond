import re
from pymongo import MongoClient
import math
import os, json
from nltk.stem.snowball import SnowballStemmer

stemmer = SnowballStemmer("english")
client = MongoClient()
db = client.test
postings = db.postingsweek3
metadata = db.metadataweek3
dictforqueryterms = {}


def tokenize(query):
    pattern = re.compile('[^A-Za-z0-9]')
    wordstring = pattern.sub(' ', query)
    data = wordstring.split()
    data_iter = iter(data)
    listofquerysearch = set()
    for index, d in enumerate(data_iter):
        current_token = data[index].strip()
        if len(current_token) >= 2 and current_token.isupper():
            key = format(stemmer.stem(current_token))  # need to inflate factor for all uppercase
            listofquerysearch.add(str(key))
        if len(d) >= 2 and index + 1 != len(data):
            current_token = current_token.lower()
            key = format(stemmer.stem(current_token))
            listofquerysearch.add(str(key))
            if key in dictforqueryterms:
                cnt = dictforqueryterms[key]
                dictforqueryterms[key] = cnt + 1
            else:
                dictforqueryterms[key] = 1
            if (len(data[index + 1].strip()) >= 2):
                next_token = data[index + 1].strip().lower()
                key = key + " " + format(stemmer.stem(next_token))
                listofquerysearch.add(str(key))
                if key in dictforqueryterms:
                    cnt = dictforqueryterms[key]
                    dictforqueryterms[key] = cnt + 1
                else:
                    dictforqueryterms[key] = 1
        elif index + 1 == len(data):
            current_token = data[index].strip().lower()
            key = format(stemmer.stem(current_token))
            listofquerysearch.add(str(key))
            if key in dictforqueryterms:
                cnt = dictforqueryterms[key]
                dictforqueryterms[key] = cnt + 1
            else:
                dictforqueryterms[key] = 1
    return listofquerysearch


def get_tfidf(freqindoc, termfreq, totalCount, term_noofdocs):
    tf = float(freqindoc) / termfreq
    idf = float(totalCount) / term_noofdocs
    weight = math.log10(1 + tf) * math.log10(idf)
    return weight


def finddocs(query):
    list = tokenize(query.strip())
    scoreofdocuments = {}
    totalCountofdocs = metadata.find({"totalCount": {'$exists': True}})
    totalCount = totalCountofdocs.next()["totalCount"]
    finallist=[]
    indiction = {'$exists':True}
    for w in list:
        diction = {w:indiction}
        finallist.append(diction)
    dictiontoquery = {'$or': finallist}
    try :
        p = postings.find(dictiontoquery)
    except StopIteration:
        pass


    for foundvalue in p:
        try:
            for key in foundvalue:
                if key == '_id':
                    continue
                else:
                    print "key = " ,key
                    docs = foundvalue[key]
                    w = key
            docs=docs.split()
            retrieved_entry = metadata.find({w: {'$exists': True}})
            ret = retrieved_entry.next()[w]
            queryscoreforcurrentterm = get_tfidf(dictforqueryterms[w], int(ret[0]), int(totalCount), int(ret[1]))
            for doc in docs:
                l = doc.split("_")
                if len(l) is not 3:
                   continue
                #print l
                if l[0] in scoreofdocuments.keys():
                    v = scoreofdocuments[l[0]]
                else:
                    v = 0 #const1 *hub + const2 * aval + const3 *pagerank
                v += (queryscoreforcurrentterm * float(l[2]))
                scoreofdocuments[l[0]] = v
        except StopIteration:
            pass

    from heapq import nlargest
    sorted_scoreofdocs = nlargest(10, scoreofdocuments, key=scoreofdocuments.get)
    print sorted_scoreofdocs
    #sorted_scoreofdocs = sorted(scoreofdocuments.items(), key=operator.itemgetter(1), reverse=True)
    bookkeeping = open("corpus/bookkeeping.json")
    bookkeeping = json.loads(bookkeeping.read())
    index = 0
    returnstring = ""
    for entry in sorted_scoreofdocs:
        returnstring += bookkeeping[entry].strip() + " ||| "
        index += 1
        if (index == 10):
            break
    return returnstring

    # finddocs("information retrieval")
