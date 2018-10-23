package us.colloquy.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.histogram.ParsedDateHistogram;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.joda.time.DateTime;
import us.colloquy.model.IndexSearchResult;
import us.colloquy.model.Letter;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Peter Gershkovich on 12/24/17.
 */
public class ElasticConnector
{

    public void queryStringSearch(String searchString, Properties properties, IndexSearchResult result, int fromInt, String[] indices)
    {


        boolean useHighlighting = false;

        try (RestHighLevelClient elasticClient = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("localhost", 9200, "http"),
                        new HttpHost("localhost", 9201, "http"))))
        {

            String filtered = searchString.replaceAll("\\sИ\\s", " AND ")
                    .replaceAll("\\sИЛИ\\s", " OR ");

            String highlightedText = "highlightedText";

            HighlightBuilder hb = new HighlightBuilder();
            hb.field("content").field("notes").field("entry").forceSource(true).numOfFragments(0).preTags("<span class=\"" + highlightedText + "\">").postTags("</span>");


            SearchRequest searchRequest = new SearchRequest(indices);

            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

            if (StringUtils.isNotEmpty(searchString) && searchString.length() > 2)
            {
                useHighlighting = true;

                searchSourceBuilder.highlighter(hb); //add HighlightBuilder
            }

            searchSourceBuilder.query(QueryBuilders.queryStringQuery(searchString));
            searchSourceBuilder.sort(new FieldSortBuilder("date").order(SortOrder.ASC));

            searchRequest.source(searchSourceBuilder);

            searchSourceBuilder.size(100);

            searchSourceBuilder.from(fromInt);

            SearchResponse searchResponse = elasticClient.search(searchRequest, RequestOptions.DEFAULT);

            SearchHits hits = searchResponse.getHits();

            result.setNumberOfResults(hits.getTotalHits());

            for (SearchHit hit : hits)
            {

                Letter letter = new Letter();

                if (useHighlighting)
                {
                    Map<String, HighlightField> highlightFields = hit.getHighlightFields();

                    if (highlightFields.containsKey("content"))
                    {
                        //should be only one fragment
                        letter.setContent(highlightFields.get("content").getFragments()[0].string());

                    }

                    if (highlightFields.containsKey("notes"))
                    {
                        //more than one fragment since can contain many notes
                        //  System.out.println(highlightFields.get("notes").getFragments().length);

                        for (Text fr : highlightFields.get("notes").getFragments())
                        {
                            letter.getNotes().add(fr.string());
                        }
                    }

                    if (highlightFields.containsKey("entry"))
                    {
                        letter.setContent(highlightFields.get("entry").getFragments()[0].string());
                    }

                } else
                {

                    Map<String, Object> fieldsMap = hit.getSourceAsMap();

                    if (fieldsMap.containsKey("content"))
                    {
                        letter.setContent(((String) fieldsMap.getOrDefault("content", "")));


                    } else if (fieldsMap.containsKey("entry"))
                    {
                        letter.setContent(((String) fieldsMap.getOrDefault("entry", "")));
                    }
                }

                result.getLetters().add(letter);
            }


        } catch (Throwable throwable)
        {
            throwable.printStackTrace();
        }
    }

    public String getLetterHistogram(Properties properties, String[] indices)
    {

        StringBuilder sb = new StringBuilder();

        sb.append("date");
        sb.append(",");
        sb.append("letters");

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        try (RestHighLevelClient elasticClient = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost(properties.getProperty("elastic_ip_address"), 9200, "http"),
                        new HttpHost(properties.getProperty("elastic_ip_address"), 9201, "http"))))
        {

            SearchRequest searchRequest = new SearchRequest(indices);

            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

            searchSourceBuilder.query(QueryBuilders.matchAllQuery());

            DateHistogramAggregationBuilder dhb = AggregationBuilders.dateHistogram("day").field("date")
                    .dateHistogramInterval(DateHistogramInterval.DAY);

            searchSourceBuilder.aggregation(dhb);

            searchRequest.source(searchSourceBuilder);

            SearchResponse searchResponse = elasticClient.search(searchRequest, RequestOptions.DEFAULT);

            Aggregations aggregations = searchResponse.getAggregations();

            ParsedDateHistogram agg = aggregations.get("day");

            // For each entry
            for (Histogram.Bucket entry : agg.getBuckets())
            {
                DateTime day = (DateTime) entry.getKey();                    // bucket key

                long docCount = entry.getDocCount();            // Doc count

                if (docCount > 0)
                {

                    sb.append("\n");

                    sb.append(formatter.format(day.toDate()));
                    sb.append(",");
                    sb.append(docCount);
                }
            }


        } catch (Exception e)
        {
            e.printStackTrace();
        }

        return sb.toString();

    }

    private SearchResponse prepareIndex(TransportClient elasticClient, String[] indices)
    {
        if (indices.length == 1 && StringUtils.isNotEmpty(indices[0]))
        {
            return elasticClient.prepareSearch(indices[0])
                    .addAggregation(
                            AggregationBuilders.dateHistogram("day").field("date")
                                    .dateHistogramInterval(DateHistogramInterval.DAY)
                    )
                    .execute().actionGet();
        } else if (indices.length == 2 && StringUtils.isNotEmpty(indices[0]) && StringUtils.isNotEmpty(indices[1]))
        {
            return elasticClient.prepareSearch(indices[0], indices[1])
                    .addAggregation(
                            AggregationBuilders.dateHistogram("day").field("date")
                                    .dateHistogramInterval(DateHistogramInterval.DAY)
                    )
                    .execute().actionGet();

        } else
        {

            return elasticClient.prepareSearch()  //use default
                    .addAggregation(
                            AggregationBuilders.dateHistogram("day").field("date")
                                    .dateHistogramInterval(DateHistogramInterval.DAY)
                    )
                    .execute().actionGet();
        }
    }


    public void queryStringSearchFiltered(String searchString, Properties properties,
                                          IndexSearchResult result, int fromInt, long start, long end, String[] indices)
    {
        boolean useHighlighting = false;

        try (RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("localhost", 9200, "http"),
                        new HttpHost("localhost", 9201, "http"))))
        {

            SearchRequest searchRequest = new SearchRequest(indices);

            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();


            String filtered = searchString.replaceAll("\\sИ\\s", " AND ")
                    .replaceAll("\\sИЛИ\\s", " OR ");

            String highlightedText = "highlightedText";

            HighlightBuilder hb = new HighlightBuilder();
            hb.field("content").field("notes").field("toWhom").field("place").field("entry").forceSource(true).numOfFragments(0).preTags("<span class=\"" + highlightedText + "\">").postTags("</span>");


            searchSourceBuilder.query(QueryBuilders.queryStringQuery(filtered));

            searchSourceBuilder.sort(new FieldSortBuilder("date").order(SortOrder.ASC));
            // searchSourceBuilder.sort("date", SortOrder.ASC);

            searchRequest.source(searchSourceBuilder);

            searchSourceBuilder.size(10);

            System.out.println("From: " + fromInt);

            searchSourceBuilder.from(fromInt);


            if (start != 0 || end != 0)
            {
                searchSourceBuilder.postFilter(QueryBuilders.rangeQuery("date")
                        .from(start).to(end));
            }

            if (StringUtils.isNotEmpty(searchString) && searchString.length() > 2)
            {
                useHighlighting = true;
                searchSourceBuilder.highlighter(hb); //add HighlightBuilder
            }

            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

            SearchHits hits = searchResponse.getHits();


            System.out.println("total hits" + searchResponse.getHits().getHits().length);


            result.setNumberOfResults(hits.getTotalHits());

            Calendar cal = Calendar.getInstance();

            for (SearchHit hit : hits)
            {
                Letter letter = new Letter();

                Map<String, Object> fieldsMap = hit.getSourceAsMap();

                if (useHighlighting)
                {
                    Map<String, HighlightField> highlightFields = hit.getHighlightFields();


                    if (highlightFields.containsKey("content"))
                    {
                        //should be only one fragment todo make it based on user's preferences
                        letter.setContent(highlightFields.get("content").getFragments()[0].string());

                    } else
                    {

                        letter.setContent(((String) fieldsMap.getOrDefault("content", "")));

                    }

                    if (highlightFields.containsKey("entry"))
                    {
                        //should be only one fragment todo make it based on user's preferences
                        letter.setEntry(highlightFields.get("entry").getFragments()[0].string());

                    } else
                    {

                        letter.setEntry(((String) fieldsMap.getOrDefault("entry", "")));

                    }

                    if (highlightFields.containsKey("notes"))
                    {
                        //more than one fragment since can contain many notes
                        for (Text fr : highlightFields.get("notes").getFragments())
                        {
                            letter.getNotes().add(fr.string());
                        }
                    } else
                    {
                        List<String> notes = (List) fieldsMap.getOrDefault("notes", new ArrayList<String>());

                        for (String note : notes)
                        {
                            letter.getNotes().add(note);
                        }
                    }


                } else
                {

                    if (fieldsMap.containsKey("content"))
                    {
                        letter.setContent(((String) fieldsMap.getOrDefault("content", "")));

                    }

                    if (fieldsMap.containsKey("entry"))
                    {
                        letter.setEntry(((String) fieldsMap.getOrDefault("entry", "")));

                    }

                    if (fieldsMap.containsKey("notes"))
                    {
                        List<String> notes = (List) fieldsMap.getOrDefault("notes", new ArrayList<String>());

                        for (String note : notes)
                        {
                            letter.getNotes().add(note);
                        }
                    }
                }

                if (fieldsMap.containsKey("date"))
                {
                    long date = (Long) fieldsMap.get("date");

                    cal.setTimeInMillis(date);

                    letter.setDate(cal.getTime());
                }

                if (fieldsMap.containsKey("souce"))
                {
                    letter.setSource(((String) fieldsMap.getOrDefault("souce", "")));
                }

                if (fieldsMap.containsKey("source"))
                {
                    letter.setSource(((String) fieldsMap.getOrDefault("source", "")));
                }

                if (fieldsMap.containsKey("id"))
                {
                    letter.setId(((String) fieldsMap.getOrDefault("id", "")));
                }


                if (fieldsMap.containsKey("toWhom"))
                {
                    letter.setToWhom(((String) fieldsMap.getOrDefault("toWhom", "")));
                }

                result.getLetters().add(letter);
            }


        } catch (Throwable throwable)
        {
            throwable.printStackTrace();
        }
    }


    public void queryAllEvents(Properties properties, IndexSearchResult result)
    {


        try (RestHighLevelClient elasticClient = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("localhost", 9200, "http"),
                        new HttpHost("localhost", 9201, "http"))))
        {

            SearchRequest searchRequest = new SearchRequest("tolstoy_composition");

            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

            searchSourceBuilder.query(QueryBuilders.matchAllQuery());

            searchRequest.source(searchSourceBuilder);

            searchSourceBuilder.size(1000);

            SearchResponse searchResponse = elasticClient.search(searchRequest, RequestOptions.DEFAULT);

            SearchHits hits = searchResponse.getHits();

            result.setNumberOfResults(hits.getTotalHits());

            for (SearchHit hit : hits)
            {
                Map<String, Object> fieldsMap = hit.getSourceAsMap();

                for (String key : fieldsMap.keySet())
                {
                    System.out.println(fieldsMap.get(key));

                    List<Map<String, Object>> activities = (List) fieldsMap.get("activityList");

                    for (Map<String, Object> map : activities)
                    {
                        for (String k : map.keySet())
                        {
                            System.out.println(map.get(k));
                        }
                    }
                }
            }

        } catch (Throwable throwable)
        {
            throwable.printStackTrace();
        }
    }

}
