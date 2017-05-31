import Queue
import json
import math
import os
import re
import sys
import threading
import time
from pymongo import MongoClient

from nltk.stem.snowball import SnowballStemmer

reload(sys)
sys.setdefaultencoding('utf8')
# important tags - b, title, head
'''
path = os.getcwd()+"\\WEBPAGES_RAW"
for filename in os.listdir(path):
    if filename not in ("bookkeeping.json","bookkeeping.tsv"):
        for file in os.listdir(path+"\\"+filename):
            print file
'''
THREADCOUNT = 1


class myThread(threading.Thread):
    def __init__(self, threadID, q, book):
        threading.Thread.__init__(self)
        self.threadID = threadID
        self.diction = {}
        self.stemmer = SnowballStemmer("english")
        self.book_keeping = book
        self.indexed = False
        self.commonQueue = q
        self.totalCount = 0

    def run(self):
        print "Starting " + str(self.threadID) + " at time " + str(time.time())
        path = os.getcwd() + "\\corpus"
        global THREADCOUNT
        folder = self.threadID
        while folder <= 0:
            # print "Going Through folder " + str(folder)
            list = os.listdir(path + "\\" + str(folder))
            print list

            for file in list:
                print "Going Through file " + str(folder) + "/" + str(file)
                data = open(path + "\\" + str(folder) + "\\" + str(file))
                self.folderAndFile(path, folder, file)
                self.handle_data(data.read())
                if self.indexed:
                    self.totalCount += 1
                data.close()

            self.commonQueue.put(self.getDiction().copy())
            self.clear()
            print "I am ", self.threadID, " ADDING DICTION TO queue size ", self.commonQueue.qsize()
            folder += THREADCOUNT
        print "TOTAL FILES INDEXED = " + str(self.totalCount)

    def folderAndFile(self, path, folder, file):
        self.currentFolder = folder
        self.currentFile = file
        self.pathtoopen = path + "\\" + str(self.currentFolder) + "\\" + str(self.currentFile)
        self.posting = str(self.currentFolder) + "/" + str(self.currentFile)
        self.indexed = False
        self.anchorPostingStack = []

    def handle_data(self, data):
        if len(data) is 0:
            return

        pattern = re.compile('[^A-Za-z0-9]')
        data = pattern.sub(' ', data).split()

        data_iter = iter(data)
        for index, d in enumerate(data_iter):
            current_token = data[index].strip()
            #CAN INSERT CAPITAL INFLATE FREQUENCY LOGIC HERE
            if len(d) >= 2 and index + 1 != len(data):
                #The Token is not last in the list
                current_token = current_token.lower()
                key = format(self.stemmer.stem(current_token))
                self.addToken(key, self.posting)
                if (len(data[index + 1].strip()) >= 2):
                    next_token = data[index + 1].strip().lower()
                    ngram = key + " " + format(self.stemmer.stem(next_token))
                    self.addToken(ngram, self.posting)
            elif index + 1 == len(data):
                #The Token is last in the list
                current_token = current_token.lower()
                key = format(self.stemmer.stem(current_token))
                self.addToken(key, self.posting)

    def addToken(self, key, post):
        # key is your token
        # this is where we store information in our Index
        self.indexed = True
        if self.diction.get(key) is not None:
            #token already exists
            value = self.diction[key]
            if value.get(post) is not None:
                value[post] += 1
            else:
                value[post] = 1
                # print "Setting Value for ",d," as ",value[post]
        else:
            #create a record for that token
            value = {}
            value[post] = 1
            self.diction[key] = value
            # print "Setting Value for ", d, " as ", value[self.posting]

    def getDiction(self):
        return self.diction

    def clear(self):
        self.diction = {}


'''
class Parser(book):
    def __init__(self, book):
        # HTMLParser.__init__(self)
        Parser.__init__(self);
        self.diction = {}
        self.stemmer = SnowballStemmer("english")
        self.indexed = False
        self.reverse_book_keeping = self.reverseBook(self.book_keeping)

    def reverseBook(self, book):
        value = {}
        for key in book.keys():
            value[book[key]] = key
        return value

    def folderAndFile(self, path, folder, file):
        self.currentFolder = folder
        self.currentFile = file
        self.pathtoopen = path + "\\" + str(self.currentFolder) + "\\" + str(self.currentFile)
        self.posting = str(self.currentFolder) + "/" + str(self.currentFile)
        self.indexed = False
        self.anchorPostingStack = []

    def print_diction(self):
        for k, v in self.diction.iteritems():
            print k, v

    def handle_data(self, data):
        if len(data) is 0 or len(self.stackList) == 0:
            return

        pattern = re.compile('[^A-Za-z0-9]')
        data = pattern.sub(' ', data).split()
        self.process_dataimpl(data, self.posting)

        if self.stackList[-1] == "a" and len(self.anchorPostingStack) > 0:
            parent = self.graphNodeDiction[self.posting]
            if self.graphNodeDiction.get(self.anchorPostingStack[-1]) is not None:
                child = self.graphNodeDiction[self.anchorPostingStack[-1]]
            else:
                child = GraphNode(self.anchorPostingStack[-1])
                self.graphNodeDiction[self.anchorPostingStack[-1]] = child
            self.graphNodeSet.add(self.anchorPostingStack[-1])
            parent.addChild(child)
            child.addParent(parent)
            self.process_dataimpl(data, self.anchorPostingStack[-1], factor * 4)
            self.anchorPostingStack.pop()
            index = self.get_Last_Index("a")
            self.stackList = self.stackList[:index] + self.stackList[index + 1:]

    def addToken(self, key, post, factor):

        # key is your token
        # this is where we store information in our Index
        self.indexed = True
        if self.diction.get(key) is not None:
            value = self.diction[key]
            if value.get(post) is not None:
                value[post] += factor
            else:
                value[post] = factor
                # print "Setting Value for ",d," as ",value[post]
        else:
            value = {}
            value[post] = factor
            self.diction[key] = value
            # print "Setting Value for ", d, " as ", value[self.posting]

    def process_dataimpl(self, data, post, factor):
        data_iter = iter(data)
        for index, d in enumerate(data_iter):
            current_token = data[index].strip()
            if len(current_token) >= 2 and current_token.isupper():
                key = format(self.stemmer.stem(current_token))
                self.addToken(key, post, factor * 3)
            if len(d) >= 2 and index + 1 != len(data):
                #2 gram
                current_token = current_token.lower()
                key = format(self.stemmer.stem(current_token))
                self.addToken(key, post, factor)
                if (len(data[index + 1].strip()) >= 2):
                    next_token = data[index + 1].strip().lower()
                    ngram = key + " " + format(self.stemmer.stem(next_token))
                    self.addToken(ngram, post, factor)
            elif index + 1 == len(data):
                current_token = data[index].strip().lower()
                key = format(self.stemmer.stem(current_token))
                self.addToken(key, post, factor)

    def compute_factor(self):
        factor = 1.0;
        for s in self.stackList:
            if self.inflating_factor.get(s) is not None:
                factor *= self.inflating_factor.get(s)
        return factor;

    def get_Last_Index(self, tag):
        if tag in self.stackList:
            return len(self.stackList) - 1 - self.stackList[::-1].index(tag);
        else:
            return -1

    def getDiction(self):
        return self.diction

    def clear(self):
        for key in self.diction.keys():
            value = self.diction[key]
            value.clear()
        self.diction.clear()
        self.diction = {}

'''

def get_tfidf(freqindoc, termfreq, totalCount, term_noofdocs):
    tf = float(freqindoc) / termfreq
    idf = float(totalCount) / term_noofdocs
    weight = math.log10(1 + tf) * math.log10(idf)
    # print "TF IDF IS ", weight
    return weight


client = MongoClient()
q = Queue.Queue()
threadlist = []
book = open(os.getcwd() + "\\corpus\\book_keeping.json").read()
book = json.loads(book)
for i in range(0, THREADCOUNT):
    thread1 = myThread(i, q, book)
    threadlist.append(thread1)
    thread1.start()
    time.sleep(10)

totalCount = 0
for i in range(0, THREADCOUNT):
    threadlist[i].join()
    totalCount += threadlist[i].totalCount

db = client.test
postings = db.finalPostings
metadata = db.finalMetaData
postings.drop()
metadata.drop()
index = {}
freq = {}
'''
freq -
key : list
abc : []
abc : [45,2]
45 - overall freqeuncy - totalFreq
2 - number of documents that contain this token - docCount

'''
for diction in list(q.queue):
    for token in diction.keys():
        if freq.get(token) is None:
            cnt = 0
            cntdocuments = 0
        else:
            l = freq[token]
            cnt = l[0]
            cntdocuments = l[1]
        tokenDiction = diction[token]
        for posting in tokenDiction.keys():
            cnt += tokenDiction[posting]
            cntdocuments += 1
        val = [cnt, cntdocuments]
        freq[token] = val
for diction in list(q.queue):
    for token in diction.keys():
        if index.get(token) is None:
            value = ""
        else:
            value = index[token]
        tokenDiction = diction[token]
        for posting in tokenDiction.keys():
            if freq[token][1] == totalCount:
                pass
            else:
                if posting in value:
                    position = value.index(posting)
                    left = value[0:position]
                    entry = value[position:]
                    position = entry.find(' ')
                    right = ""
                    if position is not -1:
                        right = entry[position:]
                        right = right.strip()
                        entry = entry[0:position]

                    oldFreq = float(entry.split('_')[1])
                    temp = tokenDiction[posting] + oldFreq
                    tfidf = get_tfidf(temp, freq[token][0], totalCount, freq[token][1])
                    value = left + " " + posting + "_" + str(temp) + "_" + str(
                        tfidf) + " " + right
                else:
                    tfidf = get_tfidf(tokenDiction[posting], freq[token][0], totalCount, freq[token][1])
                    value += " " + posting + "_" + str(tokenDiction[posting]) + "_" + str(tfidf)
        index[token] = value

print "inserting postings total key size = " + str(len(index))
for key in index:
    postings.insert({key: index[key]})
print "inserting metadata total key size = " + str(len(freq))
for key in freq:
    metadata.insert({key: freq[key]})
metadata.insert({"totalCount": totalCount})

print "Done"