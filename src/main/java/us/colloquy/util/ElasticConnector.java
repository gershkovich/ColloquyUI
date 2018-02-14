package us.colloquy.util;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.InternalDateHistogram;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import us.colloquy.model.Diary;
import us.colloquy.model.IndexSearchResult;
import us.colloquy.model.Letter;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Peter Gershkovich on 12/24/17.
 */
public class ElasticConnector
{

    public void queryStringSearch(String searchString, Properties properties, IndexSearchResult result, int fromInt, String[] indices)
    {
        Settings settings = Settings.builder()
                .put("cluster.name", properties.getProperty("elastic_cluster_name")).build();


        boolean useHighlighting = false;

        try (TransportClient elasticClient = new PreBuiltTransportClient(settings).
                addTransportAddress(new TransportAddress(InetAddress.getByName(properties.getProperty("elastic_ip_address")), 9300)))
        {

            String filtered = searchString.replaceAll("\\sИ\\s", " AND ")
                    .replaceAll("\\sИЛИ\\s", " OR ");

            String highlightedText = "highlightedText";

            HighlightBuilder hb = new HighlightBuilder();
            hb.field("content").field("notes").field("entry").forceSource(true).numOfFragments(0).preTags("<span class=\"" + highlightedText + "\">").postTags("</span>");

            SearchRequestBuilder searchRequestBuilder = getSearchRequestBuilder(elasticClient, filtered, indices);

            if (StringUtils.isNotEmpty(searchString) && searchString.length() > 2)
            {
                useHighlighting = true;
                searchRequestBuilder = searchRequestBuilder.highlighter(hb); //add HighlightBuilder
            }

            SearchResponse response =
                    searchRequestBuilder.setSize(100).setFrom(fromInt).execute().actionGet();

            SearchHits hits = response.getHits();

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
                        System.out.println(highlightFields.get("notes").getFragments().length);

                        for (Text fr : highlightFields.get("notes").getFragments())
                        {
                            letter.getNotes().add(fr.string());
                        }
                    }

                    if (highlightFields.containsKey("entry"))
                    {
                        //more than one fragment since can contain many notes
                        System.out.println(highlightFields.get("entry").getFragments().length);

                        for (Text fr : highlightFields.get("entry").getFragments())
                        {
                            letter.setContent(highlightFields.get("entry").getFragments()[0].string());
                        }
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


        Settings settings = Settings.builder()
                .put("cluster.name", properties.getProperty("elastic_cluster_name")).build();


        try (TransportClient elasticClient = new PreBuiltTransportClient(settings).
                addTransportAddress(new TransportAddress(InetAddress.getByName(properties.getProperty("elastic_ip_address")), 9300)))
        {

            SearchResponse sr = prepareIndex(elasticClient, indices);

            InternalDateHistogram agg = sr.getAggregations().get("day");

            String stop = "";

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
// For each entry
            for (InternalDateHistogram.Bucket entry : agg.getBuckets())
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
        Settings settings = Settings.builder()
                .put("cluster.name", properties.getProperty("elastic_cluster_name")).build();


        boolean useHighlighting = false;

        try (TransportClient elasticClient = new PreBuiltTransportClient(settings).
                addTransportAddress(new TransportAddress(InetAddress.getByName(properties.getProperty("elastic_ip_address")), 9300)))
        {

            String filtered = searchString.replaceAll("\\sИ\\s", " AND ")
                    .replaceAll("\\sИЛИ\\s", " OR ");

            String highlightedText = "highlightedText";

            HighlightBuilder hb = new HighlightBuilder();
            hb.field("content").field("notes").field("toWhom").field("place").field("entry").forceSource(true).numOfFragments(0).preTags("<span class=\"" + highlightedText + "\">").postTags("</span>");

            SearchRequestBuilder searchRequestBuilder = getSearchRequestBuilder(elasticClient, filtered, indices);

            if (start != 0 || end != 0)
            {
                searchRequestBuilder = searchRequestBuilder
                        .setPostFilter(QueryBuilders.rangeQuery("date")
                                .from(start).to(end));
            }

            if (StringUtils.isNotEmpty(searchString) && searchString.length() > 2)
            {
                useHighlighting = true;
                searchRequestBuilder = searchRequestBuilder.highlighter(hb); //add HighlightBuilder
            }

            SearchResponse response =
                    searchRequestBuilder.setSize(100).setFrom(fromInt).execute().actionGet();

            SearchHits hits = response.getHits();

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

    private SearchRequestBuilder getSearchRequestBuilder(TransportClient elasticClient, String filtered, String[] indices)
    {
        if (indices.length == 1 && StringUtils.isNotEmpty(indices[0]))
        {
            return elasticClient.prepareSearch(indices[0])
                    .setQuery(QueryBuilders.queryStringQuery(filtered));

        } else if (indices.length == 2 && StringUtils.isNotEmpty(indices[0]) && StringUtils.isNotEmpty(indices[1]))
        {
            return elasticClient.prepareSearch(indices[0], indices[1])
                    .setQuery(QueryBuilders.queryStringQuery(filtered));

        } else
        {

            return elasticClient.prepareSearch()
                    .setQuery(QueryBuilders.queryStringQuery(filtered));
        }


    }

}
