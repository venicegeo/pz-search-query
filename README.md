# pz-search-query
Service to accept queries to Piazza Elasticsearch instance for content discovery
endpoints accept POST of Elasticsearch query language (DSL):
http://pz-search-query.cf.piazzageo.io/api/v1/data -  returns "dataId" from matching records
http://pz-search-query.cf.piazzageo.io/api/v1/datafull -  returns full pzmetadata content from matching records as list of strings, 
each being a JSON document of the matching record.
http://pz-search-query.cf.piazzageo.io/api/v1/dslfordataresources -  returns full pzmetadata content from matching records as 
list of DataResource objects, presented in Postman as nicely formatted each being a JSON documents.

