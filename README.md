# pz-search-query
Service to accept queries to Piazza Elasticsearch instance for content discovery
endpoints accept POST of Elasticsearch query language (DSL):
http://pz-search-query/api/v1/data -  returns "dataId" from matching records
http://pz-search-query/api/v1/dslfordataresources -  returns full pzmetadata content from matching records as 
list of DataResource objects, presented in Postman as nicely formatted each being a JSON documents.

http://pz-search-query/api/v1/recordcount -  returns count as number of records matching the input
query string (just the query portion of, e.g., full DSL JSON)
```
{
     "match" : {
         "_all" : "kitten"
     }
}
```
For Piazza usage through Gateway-
Please reference Wiki page at:
https://github.com/venicegeo/venice/wiki/Pz-Search-Services

