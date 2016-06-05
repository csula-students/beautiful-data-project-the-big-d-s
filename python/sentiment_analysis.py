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
input_data = db.tweets

df = pd.DataFrame(list(input_data.find()))
c.close()

df['hashtags'] = ''
df['wordlist'] = ''
df['hc'] = np.nan
df['bs'] = np.nan
df['dt'] = np.nan
df['jk'] = np.nan
df['tweetsperday'] = 0
df['dateInt'] = (df.date.dt.year * 1000) + df.date.dt.dayofyear

cols = ['date', 'user','tweetsperday', 'text', 'wordlist', 'hashtags', 'hc', 'dt', 'bs', 'jk', 'dateInt' ]
df = df[cols]

df = df.sort_values(by = 'date', ascending = False)
df = df.reset_index(drop = True)


adjective_dictionary ={}
hashtags_dict = import_from_csv("hashtagdic.csv")

f1 = open("adjectivedict.csv")

for key, val in csv.reader(f1):
    adjective_dictionary[key] = float(val)

f1.close()
      
negation_list = ["not", "no", "dont"]   
candidate_dictionary = {"donald": "dt", "trump": "dt", "bernie": "bs", "sanders": "bs", "hillary": "hc", "clinton" : "hc",
                        "john" : "jk", "kasich" : "jk"}

for i in range(len(df)):
        
        if i % 1000000 == 0:
            print str(i) + " sentiments analyzed"
        
        sentence = df.at[i, 'text'].encode("utf-8")

        text =  main(sentence, adjective_dictionary, negation_list, candidate_dictionary)
        hashtag = check_hashtags(sentence, hashtags_dict)
        scoreDict = score(text, hashtag, adjective_dictionary, hashtags_dict)    

        if scoreDict['hc'] is 0 and scoreDict['dt'] is 0 and scoreDict['bs'] is 0 and scoreDict['jk'] is 0:
            continue
        else:
                df.set_value(i, 'wordlist', str(text))
                df.set_value(i, 'hashtags', str(hashtag))
                df.set_value(i, 'dt', scoreDict['dt'])
                df.set_value(i, 'hc', scoreDict['hc'])
                df.set_value(i, 'bs', scoreDict['bs'])
                df.set_value(i, 'jk', scoreDict['jk'])
# 85 sec per 1,000,000 tweets

df = df.dropna()
df = df.reset_index(drop = True)

spliterArray = df.dateInt.unique()
spliterArray

for date in spliterArray:
    
    dfsliced = df.loc[df.dateInt == date]

    userdict = {}

    for index, row in dfsliced.iterrows():

        user = row['user']
    
        if user not in userdict:
            userdict[user] = index, 1
        else:
            x, y = userdict[user]
            userdict[user] = x, (y + 1)
            df.set_value(index, 'dt', np.nan) 

    for user in userdict:

        df.set_value(userdict[user][0], 'tweetsperday', userdict[user][1])
        
    print str(date) + ' completed'

df = df.dropna()
df = df.reset_index(drop = True)

cols = ['date', 'user', 'tweetsperday', 'text', 'wordlist', 'hashtags', 'hc', 'dt', 'bs', 'jk']
df = df[cols]

df.tweetsperday = df.tweetsperday.astype(int)

es = Elasticsearch()

index_name = "tweets"
doc_type = "tweet"

max = len(df)

interval = 100000
count = 0
notDone = True

if es.indices.exists(index_name):
    
    print "index " + index_name + " exists"
else:
    print "index " + index_name + " did not exist, mapping required"

while notDone:

    bulk_data = []

    for i in range(interval * count , interval * (count + 1)):

        if i < max:
            tweet = {
                "_index": index_name,
                "_type": doc_type,
                "_source": df.iloc[i].to_dict()
            }
            bulk_data.append(tweet)
        else:
            notDone = False
            break
            
    count += 1
    
    helpers.bulk(es, bulk_data)
    
    #Every bulk is 100,000 tweets
    print("bulks done.... %s" % count)