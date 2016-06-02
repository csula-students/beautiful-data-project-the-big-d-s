# -*- coding: utf-8 -*-

import pandas as pd
from pymongo import MongoClient
from elasticsearch import Elasticsearch
from elasticsearch import helpers
from string import punctuation
import numpy as np
import csv

def import_from_csv(file):
    tempDictionary = {}
    for key,val in csv.reader(open(file)):
        tempDictionary[key] = val
        
    return tempDictionary

def check_hashtags(sentence, hashtags_dictionary):
    parsed_word_list = sentence.split()
    existing_hashtags = []
    
    for word in parsed_word_list:
        if word in hashtags_dictionary:
            existing_hashtags.append(word)

    return existing_hashtags

def main(sentenceIn, adjective_dictionary, negation_list, candidate_dictionary):
    #sentence = "Today I think that Donald Trump is great and I don't like Hillary Clinton. Yay Bernie!"
    sentence = sentenceIn
    
    modified_name_list = replace_candidate_names(sentence.translate(None, punctuation), candidate_dictionary)
    modified_pronoun_list = replace_pronouns(modified_name_list)
    
    distance_list = distance_calculator(modified_pronoun_list, candidate_dictionary, negation_list, adjective_dictionary)
    
    return distance_list
    
def replace_candidate_names(sentence, candidates):
    parsed_word_list = sentence.split()
    new_word_list = []
    
    for word in parsed_word_list:
        if word.lower() == "Donald".lower() or word.lower() == "Trump".lower():
            new_word_list.append(candidates["donald"])
        elif word.lower() == "Hillary".lower() or word.lower() == "Clinton".lower():
             new_word_list.append(candidates["clinton"])
        elif word.lower() == "Bernie".lower() or word.lower() == "Sanders".lower():
             new_word_list.append(candidates["bernie"])
        elif word.lower() == "John".lower() or word.lower() == "Kasich".lower():
             new_word_list.append(candidates["john"])
        elif word.lower() == "bs":
             new_word_list.append("bullshit")
        elif word.lower() == "jk":
             new_word_list.append("justkidding")
        else:
            new_word_list.append(word.lower())
 
    return new_word_list

def replace_pronouns(in_list):
    pronouns = ['he', 'his', 'him', 'hes']
    fpronouns = ['she', 'her', 'shes']
    candidates = ['dt', 'bs', 'jk']
    cand = "null"
    hc = False
    for word in in_list:
        if word == "hc":
            hc = True
    for word in in_list:
        if word in candidates:
            cand = word
        elif word in pronouns:
            in_list[in_list.index(word)] = cand
        elif word in fpronouns:
            if hc == True:
                in_list[in_list.index(word)] = 'hc'
        
    
    return in_list


def distance_calculator(word_list, modified_dict, negation_list, sentiment_dictionary):
    return word_list
  



def score(tweetl, hl, senti_dict, hashtags_dict):
    candidates = ['dt', 'bs', 'hc', 'jk']
    candidate_p  = []
    negation_p = []
    templist = find_positions(tweetl, candidates,senti_dict)
    candidate_p = templist[0]
    negation_p = templist[1]
    senti_p = templist[2]

    result = {"dt": 0, "bs": 0, "hc": 0, "jk": 0}
    if len(candidate_p) != 0 and len(senti_p) != 0:
        for sent in senti_p:
            
            #print tweetl[sent]
            
            negation = False
            
            #check for negation
            for neg in negation_p:
                if sent - 3 <= neg <= sent:
                    negation = True

            selected = candidate_p[0]
            difference = len(tweetl)
            
            #assign scores
            for cand in candidate_p:
                curd = abs(sent - cand)
                if difference > curd:
                    selected = cand
                    difference = curd
                elif curd == difference:
                    score = senti_dict[tweetl[sent]]
                    if negation:
                        score = - score
                    result[tweetl[selected]] += score
                    result[tweetl[cand]] += score
                    #print tweetl[selected] + " and " + tweetl[cand] + " receives score of " + str(score)
                    break
                elif difference < curd:
                    score = senti_dict[tweetl[sent]]
                    if negation:
                        score = - score
                    #print tweetl[selected] + " receives score of " + str(score)
                    result[tweetl[selected]] += score
                    break
                    
        for hashtag in hl:
            scorelist = hashtags_dict[hashtag].split()
            result[scorelist[0]] += float(scorelist[1])
    return result

def find_positions(tweetl, candidates,senti_dict):
    negation_list = ["not", "no", "dont"]  
    candidate_p  = []
    negation_p = []
    senti_p = []
    result = []
    index = -1
    for word in tweetl:
        index += 1
        if word in candidates:
            candidate_p.append(index)
        if word in negation_list:
            negation_p.append(index)
        if word in senti_dict:
            senti_p.append(index)
            
            
    result.append(candidate_p)
    result.append(negation_p)
    result.append(senti_p)
    return result
    
c = MongoClient()
db = c.twitter
input_data = db.tweets23

data = pd.DataFrame(list(input_data.find()))
c.close()

data.index = data['date']
data = data.sort_values(by = 'date', ascending = True)

data['hashtags'] = str(np.nan)
data['wordlist'] = str(np.nan)
data['hc'] = np.nan
data['bs'] = np.nan
data['dt'] = np.nan
data['jk'] = np.nan

cols = ['date', 'user', 'text', 'wordlist', 'hashtags', 'hc', 'dt', 'bs', 'jk']
data = data[cols]

adjective_dictionary ={}
hashtags_dict = import_from_csv("hashtagdic.csv")

f1 = open("adjectivedict.csv")

for key, val in csv.reader(f1):
    adjective_dictionary[key] = float(val)

f1.close()
    
    
negation_list = ["not", "no", "dont"]   
candidate_dictionary = {"donald": "dt", "trump": "dt", "bernie": "bs", "sanders": "bs", "hillary": "hc", "clinton" : "hc",
                        "john" : "jk", "kasich" : "jk"}
                        
count = 0

for i in range(len(data)):   
    sentence = data.iloc[i]['text'].encode("utf-8")
    
    text =  main(sentence, adjective_dictionary, negation_list, candidate_dictionary)
    hashtag = check_hashtags(sentence, hashtags_dict)
    scoreDict = score(text, hashtag, adjective_dictionary, hashtags_dict)
        
    count += 1
    if count % 100000 == 0:
        print count
    
    if scoreDict['hc'] is 0 and scoreDict['dt'] is 0 and scoreDict['bs'] is 0 and scoreDict['jk'] is 0:
        continue
    else:

        data.set_value(data.iloc[i]['date'], 'hc', scoreDict['hc'])
        data.set_value(data.iloc[i]['date'], 'dt', scoreDict['dt'])
        data.set_value(data.iloc[i]['date'], 'bs', scoreDict['bs'])
        data.set_value(data.iloc[i]['date'], 'jk', scoreDict['jk'])
        
data = data.dropna()

data['wordlist'] = data['wordlist'].astype(str)
data['hashtags'] = data['hashtags'].astype(str)

es = Elasticsearch()

index_name = "tweets"
doc_type = "tweet"

max = len(data)

interval = 100000
count = 0
notDone = True

if es.indices.exists(index_name):
    
    print "indices " + index_name + " exists"
else:
    print "indices " + index_name + " did not exist, mapping required"

while notDone:

    bulk_data = []

    for i in range(interval * count , interval * (count + 1)):

        if i < max:
            tweet = {
                "_index": index_name,
                "_type": doc_type,
                "_source": data.iloc[i].to_dict()
            }
            bulk_data.append(tweet)
        else:
            notDone = False
            break
            
    count += 1
    
    helpers.bulk(es, bulk_data)
    
    #Every bulk is 100,000 tweets
    print("bulks done.... %s" % count)