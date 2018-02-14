package colloquy;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.flexible.precedence.PrecedenceQueryParser;
import org.apache.lucene.search.Query;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.InternalDateHistogram;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.sum.SumAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.tophits.TopHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.junit.Before;
import org.junit.Test;
import us.colloquy.model.IndexSearchResult;
import us.colloquy.model.Letter;
import us.colloquy.util.ElasticConnector;
import us.colloquy.util.PropertiesLoader;

import java.net.InetAddress;
import java.util.*;
import  org.joda.time.DateTime;

/**
 * Created by Peter Gershkovich on 12/24/17.
 */
public class ElasticSearch
{
    final Properties properties = new Properties();

    final Logger logger = Logger.getLogger(ElasticSearch.class.getPackage().getName());

    @Before
    public void setUp()
    {

        PropertiesLoader.loadProperties(properties, "properties.xml");

    }

    @Test
    public void testPrecedenceQuerySearch() throws Exception
    {
        //String searchString = "correlates AND (createdBy:Karin or createdBy:Zenta) ";

        String searchString = "Искусство";

        searchString = "добро*";

        Analyzer analyzer = new StandardAnalyzer();

        PrecedenceQueryParser luceneParser = new PrecedenceQueryParser(analyzer);

        luceneParser.setAllowLeadingWildcard(true);

        Query luceneQuery = luceneParser.parse(searchString, "");

        IndexSearchResult result = new IndexSearchResult();

        ElasticConnector ec = new ElasticConnector();

        String[] indices = new String[2];

        indices[0] = "tolstoy_letters";
        indices[1] = "tolstoy_diaries";


        ec.queryStringSearch(luceneQuery.toString(), properties, result, 0, indices);

        System.out.println("Number of results: " + result.getNumberOfResults());

        for (Letter letter : result.getLetters())
        {
            if (StringUtils.isNotEmpty(letter.getContent()))
            {
                System.out.println(letter.getContent());
            } else
            {
                System.out.println("Content empty ...");
            }
        }

    }


    @Test
    public void testQuerySearchFiltered() throws Exception
    {
        //String searchString = "correlates AND (createdBy:Karin or createdBy:Zenta) ";

        String searchString = "Искусство";

        searchString = "искусство";

        Analyzer analyzer = new StandardAnalyzer();

        PrecedenceQueryParser luceneParser = new PrecedenceQueryParser(analyzer);

        luceneParser.setAllowLeadingWildcard(true);

        Query luceneQuery = luceneParser.parse(searchString, "content");

        IndexSearchResult result = new IndexSearchResult();

        ElasticConnector ec = new ElasticConnector();

        Calendar start = Calendar.getInstance();

        start.set(1908,1,1);

        Calendar end = Calendar.getInstance();

        end.set(1910,1,1);


        String[] indices = new String[2];

        indices[0] = "tolstoy_letters";
        indices[1] = "tolstoy_diaries";

        ec.queryStringSearchFiltered(luceneQuery.toString(), properties, result,
                0, start.getTime().getTime(), end.getTime().getTime(), indices);

        System.out.println("Number of results: " + result.getNumberOfResults());

        for (Letter letter : result.getLetters())
        {
            if (StringUtils.isNotEmpty(letter.getContent()))
            {
                System.out.println(letter.getContent());
            } else
            {
                System.out.println("Content empty ...");
            }
        }

        System.out.println(result.getNumberOfResults());

    }

    @Test
    public void testStringQuerySearch() throws Exception
    {
        //String searchString = "correlates AND (createdBy:Karin or createdBy:Zenta) ";

        String searchString = "Искусство AND счастье";

        searchString = "добро*";

        //  searchString = "очень";


        String[] indices = new String[2];

        indices[0] = "tolstoy_letters";
        indices[1] = "tolstoy_diaries";

        IndexSearchResult result = new IndexSearchResult();

        ElasticConnector ec = new ElasticConnector();

        ec.queryStringSearch(searchString, properties, result, 0, indices);

        System.out.println("Number of results: " + result.getNumberOfResults());

        for (Letter letter : result.getLetters())
        {
            if (StringUtils.isNotEmpty(letter.getContent()))
            {
                System.out.println(letter.getContent());

            } else
            {
                System.out.println("Content empty ...");
            }

            for (String n : letter.getNotes())
            {
                System.out.println("note: " + n);
            }
        }
    }

    private void mapExemplar(SearchHit hit, String content)
    {

        //  Map<String, Object> fildsMap = hit.getSource();

        ///  content = (( String )fildsMap.getOrDefault("type", ""));
//        exemplar.setSignedOut(( boolean )fildsMap.getOrDefault("signedOut", false));
//        exemplar.setProtein(( String )fildsMap.getOrDefault("protein", ""));
//        exemplar.setCdna(( String )fildsMap.getOrDefault("cdna", ""));
//        exemplar.setDx(( String )fildsMap.getOrDefault("dx", ""));
//        exemplar.setHistology(( String )fildsMap.getOrDefault("histology", ""));
//        exemplar.setSite(( String )fildsMap.getOrDefault("site", ""));
//
//
//        long createdDateLong = (long) fildsMap.getOrDefault("dateCreated", 0);
//        if (createdDateLong > 0)
//            exemplar.setDateCreated(new Date( createdDateLong));
//
//        long updateDateLong = (long) fildsMap.getOrDefault("dateUpdated", 0);
//        if (updateDateLong > 0)
//            exemplar.setDateUpdated(new Date( updateDateLong));
//
//        exemplar.setCreatedBy(( String )fildsMap.getOrDefault("createdBy", ""));
//
//        exemplar.setUpdatedBy(( String )fildsMap.getOrDefault("updatedBy", ""));
//
//
//        exemplar.setFusionLabel(( String )fildsMap.getOrDefault("fusion", ""));
//        exemplar.setSource(( String )fildsMap.getOrDefault("source", ""));
//        exemplar.setGeneSymbol(( String )fildsMap.getOrDefault("geneSymbol", ""));
//        exemplar.setGeneName(( String )fildsMap.getOrDefault("geneName", ""));
    }


    @Test
    public void testCustomSearch() throws Exception
    {
        //this search is assembled by system parameters

        Settings settings = Settings.builder()
                .put("cluster.name", properties.getProperty("elastic_cluster_name")).build();


        try (TransportClient elasticClient = new PreBuiltTransportClient(settings).
                addTransportAddress(new TransportAddress(InetAddress.getByName(properties.getProperty("elastic_ip_address")), 9300)))
        {

            SearchRequestBuilder rb = elasticClient.prepareSearch("exemplar_index")
                    .setTypes("downstream")
                    .setSearchType(SearchType.DFS_QUERY_THEN_FETCH);

            BoolQueryBuilder queryBuilder;

            queryBuilder = QueryBuilders.boolQuery()
                    //						.mustNot(QueryBuilders.matchQuery("additionalNote", "Terminated"))
                    .must(QueryBuilders.matchQuery("additionalNote", "EGFR"))
                    .should(QueryBuilders.boolQuery()
                            .should(QueryBuilders.matchQuery("geneSymbol", "EGFR"))
                            .should(QueryBuilders.matchQuery("protein", "prot"))
                            .should(QueryBuilders.matchQuery("dx", "dx")));

            rb.setQuery(queryBuilder);             // Query


            SearchResponse response = rb.setSize(100)
                    .setScroll(TimeValue.timeValueMinutes(2))
                    .execute()
                    .actionGet();

            SearchHits hits = response.getHits();

//            List<Exemplar> exemplarList = new ArrayList<>();
//
//
//            for ( SearchHit hit : hits )
//            {
//                Exemplar exemplar = new Exemplar();
//
//                mapExemplar(hit, exemplar);
//
//                exemplarList.add(exemplar);
//            }
//
//            for ( Exemplar ex : exemplarList)
//            {
//                System.out.println(ex.toString());
//            }


        } catch (Throwable throwable)
        {
            throwable.printStackTrace();
        }


    }

    @Test
    public void testAggregation()

    {

        Settings settings = Settings.builder()
                .put("cluster.name", properties.getProperty("elastic_cluster_name")).build();


        try (TransportClient elasticClient = new PreBuiltTransportClient(settings).
                addTransportAddress(new TransportAddress(InetAddress.getByName(properties.getProperty("elastic_ip_address")), 9300)))
        {

            SearchResponse sr = elasticClient.prepareSearch("tolstoy_letters")
                    .addAggregation(
                            AggregationBuilders.dateHistogram("day").field("date")
                                            .dateHistogramInterval(DateHistogramInterval.DAY)
                    )
                    .execute().actionGet();


            InternalDateHistogram agg = sr.getAggregations().get("day");


            String stop = "";

// For each entry
            for (InternalDateHistogram.Bucket entry : agg.getBuckets()) {
                DateTime key = (DateTime)entry.getKey();                    // bucket key

                long docCount = entry.getDocCount();            // Doc count

                System.out.println(key + " - " + docCount);
            }



        } catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    @Test
    public void getLettersHistogram()
    {

        String[] indices = new String[2];

        indices[0] = "tolstoy_letters";
        indices[1] = "tolstoy_diaries";

        ElasticConnector ec = new ElasticConnector();
        System.out.println(ec.getLetterHistogram(properties, indices));
    }
}
