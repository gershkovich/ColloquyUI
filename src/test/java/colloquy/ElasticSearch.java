package colloquy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.flexible.precedence.PrecedenceQueryParser;
import org.apache.lucene.search.Query;
import org.elasticsearch.search.SearchHit;
import org.junit.Before;
import org.junit.Test;
import us.colloquy.model.IndexSearchResult;
import us.colloquy.model.Letter;
import us.colloquy.model.Work;
import us.colloquy.util.ElasticConnector;
import us.colloquy.util.PropertiesLoader;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

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

        System.out.println("Number of results: " + result.getNumberOfResults());


        //1583 results

    }


    @Test
    public void testHistogram()
    {
        String[] indices = new String[2];

        indices[0] = "tolstoy_letters";
        indices[1] = "tolstoy_diaries";

        ElasticConnector ec = new ElasticConnector();

        System.out.println(ec.getLetterHistogram(properties, indices));

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

        start.set(1908, 1, 1);

        Calendar end = Calendar.getInstance();

        end.set(1910, 1, 1);


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


    @Test
    public void testAllEventSearch() throws Exception
    {
        ElasticConnector ec = new ElasticConnector();

        List<Work> tolstoyWorks = new LinkedList<>();

        ec.queryAllEvents(properties, tolstoyWorks);

        System.out.println("Number of results: " + tolstoyWorks.size());

        //get json document
        ObjectWriter ow = new com.fasterxml.jackson.databind.ObjectMapper().writer().withDefaultPrettyPrinter();

        String json = ow.writeValueAsString(tolstoyWorks);
        System.out.println(json);

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
    public void getLettersHistogram()
    {

        String[] indices = new String[2];

        indices[0] = "tolstoy_letters";
        indices[1] = "tolstoy_diaries";

        ElasticConnector ec = new ElasticConnector();
        System.out.println(ec.getLetterHistogram(properties, indices));
    }
}
