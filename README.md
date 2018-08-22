# pz-search-query
The pz-search-query is a service to accept queries to Piazza Elasticsearch instance for content discovery endpoints accept POST of Elasticsearch query language (DSL):

| Endpoint                                          | Type | Description                             |
|---------------------------------------------------|------|-----------------------------------------|
| http://pz-search-query/api/v1/data                | POST | returns "dataId" from matching records  |
| http://pz-search-query/api/v1/dslfordataresources | POST | returns full pzmetadataalias content from matching records as list of DataResource objects, presented in Postman as nicely formatted each being a JSON documents. |
| http://pz-search-query/api/v1/recordcount         | POST | returns count as number of records matching the input
query string (just the query portion of, e.g., full DSL JSON), example below. | 

```
{
     "match" : {
         "_all" : "kitten"
     }
}
```

***
## Requirements
Before building and/or running the pz-search-query service, please ensure that the following components are available and/or installed, as necessary:
- [Java](http://www.oracle.com/technetwork/java/javase/downloads/index.html) (JDK for building/developing, otherwise JRE is fine)
- [Maven (v3 or later)](https://maven.apache.org/install.html)
- [Git](https://git-scm.com/book/en/v2/Getting-Started-Installing-Git)
- [Eclipse](https://www.eclipse.org/downloads/), or any maven-supported IDE
- [ElasticSearch](https://www.elastic.co/)
- Access to Nexus is required to build

Ensure that the nexus url environment variable `ARTIFACT_STORAGE_URL` is set:

	$ export ARTIFACT_STORAGE_URL={Artifact Storage URL}

For additional details on prerequisites, please refer to the Piazza Developer's Guide [Core Overview](http://pz-docs.int.dev.east.paas.geointservices.io/devguide/02-pz-core/) or [Piazza Search Services](http://pz-docs.int.dev.east.paas.geointservices.io/devguide/12-pz-search-services/) sections. Also refer to the [prerequisites for using Piazza](http://pz-docs.int.dev.east.paas.geointservices.io/devguide/03-jobs/) section for additional details.

***
## Setup, Configuring, & Running
### Setup
Create the directory the repository must live in, and clone the git repository:

    $ mkdir -p {PROJECT_DIR}/src/github.com/venicegeo	
	$ cd {PROJECT_DIR}/src/github.com/venicegeo
    $ git clone git@github.com:venicegeo/pz-search-query.git
    $ cd pz-search-query

>__Note:__ In the above commands, replace {PROJECT_DIR} with the local directory path for where the project source is to be installed.

### Configuring
As noted in the Requirements section, to build and run this project, ElasticSearch is required. The `src/main/resources/application.properties` file controls URL information for ElasticSearch connection configurations.

To edit the port that the service is running on, edit the `server.port` property. <br/>
To edit the api basepath that the service endpoints are hosted on, edit the `api.basepath` property.

### Running
To build and run the search query locally, pz-search-query can be run using Eclipse any maven-supported IDE. Alternatively, pz-search-query can be run through command line interface (CLI), by navigating to the project directory and run:

	$ mvn clean install -U spring-boot:run

This will run a Tomcat server locally with the service running on port 8581 (unless port was modified per 'Configuring' section).

> __Note:__ This Maven build depends on having access to the `Piazza-Group` repository as defined in the `pom.xml` file. If your Maven configuration does not specify credentials to this Repository, this Maven build will fail.

### Running Unit Tests

To run the Search Query Controller unit tests from the main directory, run the following command:

	$ mvn test
