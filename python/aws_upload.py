import pandas as pd
from elasticsearch import Elasticsearch, RequestsHttpConnection, helpers
from elasticsearch.client import IndicesClient
from requests_aws4auth import AWS4Auth

#local es
es = Elasticsearch()

# used to create mapping for aws
# es_index = IndicesClient(es)
# es_index.get_mapping('tweets')

gen = helpers.scan(client = es, index = "tweets", doc_type = "tweet")


source = []
for x in gen:
    source.append(x['_source'])

df = pd.DataFrame.from_dict(data = source,  dtype = str)

df.date = pd.to_datetime(df.date)
df.hc = df.hc.astype(float)
df.jk = df.jk.astype(float)
df.dt = df.dt.astype(float)
df.bs = df.bs.astype(float)
df.tweetsperday = df.tweetsperday.astype(int) 

df = df.sort_values('date', ascending = True)
df = df.reset_index(drop = True)

# had to ignore non ascii symbols due to issues with aws parsing

for i in range(len(df)):
    df.set_value(i, 'text', df.at[i, 'text'].encode("ascii","ignore"))
    df.set_value(i, 'user', df.at[i, 'user'].encode("ascii","ignore"))
    df.set_value(i, 'wordlist', df.at[i, 'wordlist'].encode("ascii","ignore"))
    df.set_value(i, 'hashtags', df.at[i, 'hashtags'].encode("ascii","ignore"))

index_name = "tweets"
doc_type = "tweet"
body_mapping = {
    u'mappings': {
        u'tweet': {
            u'properties': {
                u'bs': {u'type': u'double'},     
                u'date': {u'type': u'date'},
                u'dt': {u'type': u'double'},
                u'hashtags': {u'type': u'string'},
                u'hc': {u'type': u'double'},
                u'jk': {u'type': u'double'},
                u'text': {u'type': u'string'},
                u'tweetsperday': {u'type': u'integer'},
                u'user': {u'index': u'not_analyzed', u'type': u'string'},
                u'wordlist': {u'type': u'string'}
            }
        }
    }
}

ACCESSKEY = ACCESSKEY_HIDDEN
SECRETKEY = SECRETKEY_HIDDEN
host = ELASTICSEARCH_ENDPOINT

awsauth = AWS4Auth(ACCESSKEY, SECRETKEY, 'us-west-2', 'es')

es = Elasticsearch(
    hosts=[{'host': host, 'port': 443}],
    http_auth=awsauth,
    use_ssl=True,
    verify_certs=True,
    connection_class=RequestsHttpConnection
)

es_index = IndicesClient(es)

max = len(df)

interval = 100000
count = 0
notDone = True

if es_index.exists(index = index_name):
    print 'deleting ' + index_name
    es_index.delete(index = index_name)
    print 'creating ' + index_name
    es_index.create(index = index_name, body = body_mapping)
else:
    print 'creating ' + index_name
    es_index.create(index = index_name, body = body_mapping)
    
print "created " + index_name

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