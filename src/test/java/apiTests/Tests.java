package apiTests;

import api.model.engine.Contributors;
import api.model.engine.Owner;
import api.model.engine.Repo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

import static util.ProjectConstans.ORG;

public class Tests {

    String token = "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";
    String baseUrl = "https://api.github.com";
    Response response;
    Repo[] repoList;
    List<Repo> listSorted;


    private static final Logger LOG = Logger.getLogger(Tests.class);


    /*
       1. Find the top 3 most popular projects/repos based on stars, forks, and watchers; one list
           for each metric
     */
    @Test
    public void getListPopularProjects()throws JsonProcessingException {
        RestAssured.baseURI = baseUrl;
        RequestSpecification request = RestAssured.given();

        request.header("Accept", "application/vnd.github.v3+json");

        response = request.get("/orgs/"+ORG+"/repos?page=2&per_page=100");

        Assert.assertEquals(response.getStatusCode(), 200);

        String jsonString = response.asString();

        ObjectMapper mapper = new ObjectMapper();
        repoList = mapper.readValue(jsonString, Repo[].class);
        listSorted= Arrays.asList(repoList);

        listSorted.sort(Comparator.comparing(Repo::getStargazers_count));


        LOG.info("##### Top 3 most popular projects/repos - Stars #####");
        LOG.info("1. "+ listSorted.get(listSorted.size()-1).getName()+" ("+listSorted.get(listSorted.size()-1).getStargazers_count()+")");
        LOG.info("2. "+ listSorted.get(listSorted.size()-2).getName()+" ("+listSorted.get(listSorted.size()-2).getStargazers_count()+")");
        LOG.info("3. "+ listSorted.get(listSorted.size()-3).getName()+" ("+listSorted.get(listSorted.size()-3).getStargazers_count()+")");

        listSorted.sort(Comparator.comparing(Repo::getForks));


        LOG.info("##### Top 3 most popular projects/repos - Forks #####");
        LOG.info("1. "+ listSorted.get(listSorted.size()-1).getName()+" ("+listSorted.get(listSorted.size()-1).getForks()+")");
        LOG.info("2. "+ listSorted.get(listSorted.size()-2).getName()+" ("+listSorted.get(listSorted.size()-2).getForks()+")");
        LOG.info("3. "+ listSorted.get(listSorted.size()-3).getName()+" ("+listSorted.get(listSorted.size()-3).getForks()+")");

        listSorted.sort(Comparator.comparing(Repo::getWatchers));


        LOG.info("##### Top 3 most popular projects/repos - Watchers #####");
        LOG.info("1. "+ listSorted.get(listSorted.size()-1).getName()+" ("+listSorted.get(listSorted.size()-1).getWatchers()+")");
        LOG.info("2. "+ listSorted.get(listSorted.size()-2).getName()+" ("+listSorted.get(listSorted.size()-2).getWatchers()+")");
        LOG.info("3. "+ listSorted.get(listSorted.size()-3).getName()+" ("+listSorted.get(listSorted.size()-3).getWatchers()+")");

    }
    /*
        2. Find all unique contributors across all repos in that organization
     */

    @Test
    public void getAllUniqueContributors()throws JsonProcessingException {
        Owner[] ownerList;
        List<Owner> ownerListCollection;
        ArrayList<String> uniqueContributors = new ArrayList<>();
        RestAssured.baseURI = baseUrl;
        RequestSpecification request = RestAssured.given();

        request.header("Accept", "application/vnd.github.v3+json")
                .header("Authorization", "Bearer " + token);

        response = request.get("/orgs/"+ORG+"/repos?page=2&per_page=100");

        Assert.assertEquals(response.getStatusCode(), 200);

        String jsonString = response.asString();

        ObjectMapper mapper = new ObjectMapper();
        repoList = mapper.readValue(jsonString, Repo[].class);
        listSorted= Arrays.asList(repoList);

        for (Repo obj:
                listSorted) {
            Response responseReq2 = request.get("/repos/"+ORG+"/"+obj.getName()+"/contributors");
            Assert.assertEquals(responseReq2.getStatusCode(), 200);
            jsonString = responseReq2.asString();
            mapper = new ObjectMapper();
            ownerList = mapper.readValue(jsonString, Owner[].class);
            ownerListCollection= Arrays.asList(ownerList);
            if(ownerListCollection.size()==1){
                uniqueContributors.add(ownerListCollection.get(0).getLogin());
            }

        }

        LOG.info("##### Unique contributors #####");
        int i = 1;
        for(String s:
                uniqueContributors){
            LOG.info(i+". "+s);
            i++;
        }

    }

    /*
        3. Find the top 3 contributors in that organization based on addition/deletions and number
        of commits; one list for each metric
     */
    @Test
    public void getListContributors()throws JsonProcessingException {
        ArrayList<Contributors> topContributors = new ArrayList<>();
        List<Contributors> contributorsListCollection;
        Contributors[] contributorsList;
        RestAssured.baseURI = baseUrl;
        RequestSpecification request = RestAssured.given();

        request.header("Accept", "application/vnd.github.v3+json")
                .header("Authorization", "Bearer " + token);

        response = request.get("/orgs/"+ORG+"/repos?page=2&per_page=100");

        Assert.assertEquals(response.getStatusCode(), 200);

        String jsonString = response.asString();

        ObjectMapper mapper = new ObjectMapper();
        repoList = mapper.readValue(jsonString, Repo[].class);
        listSorted= Arrays.asList(repoList);

        for (Repo obj:
                listSorted) {
            response = request.get("/repos/"+ORG+"/"+obj.getName()+"/contributors?q=contributions&order=desc&page=2&per_page=100");
            Assert.assertEquals(response.getStatusCode(), 200);
            jsonString = response.asString();
            mapper = new ObjectMapper();
            contributorsList = mapper.readValue(jsonString, Contributors[].class);
            contributorsListCollection= Arrays.asList(contributorsList);
            topContributors.addAll(contributorsListCollection);
        }
        topContributors.sort(Comparator.comparing(Contributors::getContributions));


        LOG.info("##### Top 3 contributors #####");
        LOG.info("1. "+ topContributors.get(topContributors.size()-1).getLogin()+" ("+topContributors.get(topContributors.size()-1).getContributions()+")");
        LOG.info("2. "+ topContributors.get(topContributors.size()-2).getLogin()+" ("+topContributors.get(topContributors.size()-3).getContributions()+")");
        LOG.info("3. "+ topContributors.get(topContributors.size()-3).getLogin()+" ("+topContributors.get(topContributors.size()-3).getContributions()+")");

    }
}
