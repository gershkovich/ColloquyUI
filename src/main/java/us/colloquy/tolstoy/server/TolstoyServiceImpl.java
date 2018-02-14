package us.colloquy.tolstoy.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.log4j.Logger;
import org.junit.Before;
import us.colloquy.model.DateRange;
import us.colloquy.model.IndexSearchResult;
import us.colloquy.model.Letter;
import us.colloquy.tolstoy.client.TolstoyService;
import us.colloquy.tolstoy.client.model.LetterDisplay;
import us.colloquy.tolstoy.client.model.SearchFacets;
import us.colloquy.tolstoy.client.model.ServerResponse;
import us.colloquy.util.Constants;
import us.colloquy.util.ElasticConnector;
import us.colloquy.util.Mailer;
import us.colloquy.util.PropertiesLoader;

import javax.servlet.http.HttpSession;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;


public class TolstoyServiceImpl extends RemoteServiceServlet implements TolstoyService
{

    final Properties properties = new Properties();

    final Logger logger = Logger.getLogger(TolstoyServiceImpl.class.getPackage().getName());


    @Before
    public void setUp()
    {

        PropertiesLoader.loadProperties(properties, "properties.xml");

    }

    // Implementation of sample interface method
    public String submitComments(String email, String msg)
    {
        try
        {

            if (EmailValidator.getInstance().isValid(email))
            {
                Mailer.sendFeedback(email, msg);
                return "success";
            } else
            {
                return "invalidEmail";
            }


        } catch (Throwable t)
        {

            t.printStackTrace();

            return "failure";
        }

    }

    @Override
    public ServerResponse getSelectedLetters(String searchString, SearchFacets searchFacets)
    {

        PropertiesLoader.loadProperties(properties, "properties.xml");

        ServerResponse sr = new ServerResponse();

        HttpSession session = getThreadLocalRequest().getSession();

        sr.setFeedback("Getting records");

        IndexSearchResult result = new IndexSearchResult();

        ElasticConnector ec = new ElasticConnector();

        String searchStringModified = "*";

        if (StringUtils.isNotEmpty(searchString))
        {
            searchStringModified = searchString;
        }

        DateRange dateRange = new DateRange();

        Object dateRangeObject = session.getAttribute(Constants.DATE_RANGE);

        if (dateRangeObject != null && dateRangeObject instanceof DateRange)
        {
            dateRange = (DateRange) dateRangeObject;

        }

        ec.queryStringSearchFiltered(searchStringModified, properties, result, 0,
                dateRange.getStart(), dateRange.getEnd(), searchFacets.getIndexesList().toArray(new String[0]));

        for (Letter letter : result.getLetters())
        {
            LetterDisplay ld = new LetterDisplay();

            if (StringUtils.isNotEmpty(letter.getEntry()))
            {
                ld.setContent(letter.getEntry());
                ld.setToWhoom("[Дневник]");

            } else
            {
                ld.setContent(letter.getContent());
                ld.setToWhoom(letter.getToWhom());
                ld.getNotes().addAll(letter.getNotes());
            }

            ld.setDate(letter.getDate());

            ld.setSource(letter.getSource());
            sr.getLetters().add(ld);
        }

        sr.setTotalNumberOfLetters(result.getNumberOfResults());

        return sr;
    }

    @Override
    public ServerResponse getSelectedLettersWithOffset(int totalLettersLoadedOnClient, String searchCriteria,  SearchFacets searchFacets)
    {
        PropertiesLoader.loadProperties(properties, "properties.xml");

        ServerResponse sr = new ServerResponse();

        HttpSession session = getThreadLocalRequest().getSession();

        DateRange dateRange = new DateRange();

        Object dateRangeObject = session.getAttribute(Constants.DATE_RANGE);

        if (dateRangeObject != null && dateRangeObject instanceof DateRange)
        {
            dateRange = (DateRange) dateRangeObject;

        }

        IndexSearchResult result = new IndexSearchResult();

        ElasticConnector ec = new ElasticConnector();

        String searchStringModified = "*";

        if (StringUtils.isNotEmpty(searchCriteria))
        {
            searchStringModified = searchCriteria;
        }

        ec.queryStringSearchFiltered(searchStringModified, properties, result, totalLettersLoadedOnClient,
                dateRange.getStart(), dateRange.getEnd(), searchFacets.getIndexesList().toArray(new String[0]));

        for (Letter letter : result.getLetters())
        {
            LetterDisplay ld = new LetterDisplay();


            if (StringUtils.isNotEmpty(letter.getEntry()))
            {
                ld.setContent(letter.getEntry());
                ld.setToWhoom("[Дневник]");

            } else
            {
                ld.setContent(letter.getContent());
                ld.setToWhoom(letter.getToWhom());
                ld.getNotes().addAll(letter.getNotes());
            }

            ld.setDate(letter.getDate());
            ld.setSource(letter.getSource());
            sr.getLetters().add(ld);

        }

        sr.setTotalNumberOfLetters(result.getNumberOfResults());

        return sr;
    }

    @Override
    public ServerResponse getDataForCharts(SearchFacets searchFacets)
    {
        ServerResponse sr = new ServerResponse();

        PropertiesLoader.loadProperties(properties, "properties.xml");

        sr.setFeedback("Getting records");

        IndexSearchResult result = new IndexSearchResult();

        ElasticConnector ec = new ElasticConnector();

        sr.setCsvLetterData(ec.getLetterHistogram(properties,  searchFacets.getIndexesList().toArray(new String[0])));

        return sr;
    }

    @Override
    public ServerResponse getLettersSubset(String range, String searchString,  SearchFacets searchFacets)
    {
        DateRange dateRange = new DateRange();

        long start = 0;
        long end = 0;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        HttpSession session = getThreadLocalRequest().getSession();

        if (StringUtils.isNotEmpty(range))
        {
            String[] startToEnd = range.split(":");

            if (startToEnd.length == 2)
            {
                try
                {
                    start = sdf.parse(startToEnd[0]).getTime();
                    end = sdf.parse(startToEnd[1]).getTime();

                    dateRange.setStart(start);
                    dateRange.setEnd(end);

                    session.setAttribute(Constants.DATE_RANGE, dateRange);


                } catch (ParseException e)
                {
                    e.printStackTrace();
                }

            }
        }

        PropertiesLoader.loadProperties(properties, "properties.xml");

        ServerResponse sr = new ServerResponse();

        sr.setFeedback("Getting records");

        IndexSearchResult result = new IndexSearchResult();

        ElasticConnector ec = new ElasticConnector();

        String searchStringModified = "*";

        if (StringUtils.isNotEmpty(searchString))
        {
            searchStringModified = searchString;
        }

        ec.queryStringSearchFiltered(searchStringModified, properties, result, 0, start, end,  searchFacets.getIndexesList().toArray(new String[0]));

        for (Letter letter : result.getLetters())
        {
            LetterDisplay ld = new LetterDisplay();

            if (StringUtils.isNotEmpty(letter.getEntry()))
            {
                ld.setContent(letter.getEntry());
                ld.setToWhoom("[Дневник]");

            } else
            {
                ld.setContent(letter.getContent());
                ld.setToWhoom(letter.getToWhom());
                ld.getNotes().addAll(letter.getNotes());
            }

            ld.setDate(letter.getDate());

            ld.setSource(letter.getSource());
            sr.getLetters().add(ld);
        }

        sr.setTotalNumberOfLetters(result.getNumberOfResults());

        return sr;
    }
}