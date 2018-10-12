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
import java.util.Calendar;
import java.util.List;
import java.util.Properties;


public class TolstoyServiceImpl extends RemoteServiceServlet implements TolstoyService
{

    private static final String SCATTER_PLOT_HEADER = "date,words,id,info\n";

    final Properties properties = new Properties();

    final Logger logger = Logger.getLogger(TolstoyServiceImpl.class.getPackage().getName());


    // Implementation of sample interface method
    public String submitComments(String email, String msg)
    {
        PropertiesLoader.loadProperties(properties, "properties.xml");

        try
        {

            if (EmailValidator.getInstance().isValid(email))
            {
                Mailer.sendFeedback(email, msg, properties);
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

        if (dateRangeObject instanceof DateRange)
        {
            dateRange = (DateRange) dateRangeObject;

        }

        ec.queryStringSearchFiltered(searchStringModified, properties, result, 0,
                dateRange.getStart(), dateRange.getEnd(), searchFacets.getIndexesList().toArray(new String[0]));


        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        if (StringUtils.isNotEmpty(searchString))
        {
            StringBuilder scatterPlotDataBuilder = new StringBuilder();

            scatterPlotDataBuilder.append(SCATTER_PLOT_HEADER);

            for (Letter letter : result.getLetters())
            {
                createServerResponse(letter, sdf, scatterPlotDataBuilder, sr);

            }


            sr.setSelectedStats(scatterPlotDataBuilder.toString());
        }

        sr.setTotalNumberOfLetters(result.getNumberOfResults());

        System.out.println("search filtered" + result.getNumberOfResults());

        return sr;
    }

    private void createServerResponse(Letter letter, SimpleDateFormat sdf, StringBuilder scatterPlotDataBuilder, ServerResponse sr)
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

        int numOfWords = 2; //just to be above the line

        String briefDescription = "";

        if (StringUtils.isNotEmpty(ld.getContent()))
        {
            String[] words = ld.getContent().split("\\s+");
            numOfWords = words.length;

            briefDescription = StringUtils.abbreviate(ld.getToWhoom() + ": " + ld.getContent(), 80).replaceAll("([,\n])", " ");
        }

        //let's make info line here

        setScatterPlotData(sdf.format(ld.getDate()), numOfWords, letter.getId(), briefDescription,  scatterPlotDataBuilder);

    }

    @Override
    public ServerResponse getSelectedLettersWithOffset(int totalLettersLoadedOnClient, String searchCriteria, SearchFacets searchFacets)
    {
        PropertiesLoader.loadProperties(properties, "properties.xml");

        ServerResponse sr = new ServerResponse();

        HttpSession session = getThreadLocalRequest().getSession();

        DateRange dateRange = new DateRange();

        Object dateRangeObject = session.getAttribute(Constants.DATE_RANGE);

        if (dateRangeObject instanceof DateRange)
        {
            dateRange = (DateRange) dateRangeObject;

        }

        IndexSearchResult result = new IndexSearchResult();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");


        ElasticConnector ec = new ElasticConnector();

        String searchStringModified = "*";

        if (StringUtils.isNotEmpty(searchCriteria))
        {
            searchStringModified = searchCriteria;
        }

        ec.queryStringSearchFiltered(searchStringModified, properties, result, totalLettersLoadedOnClient,
                dateRange.getStart(), dateRange.getEnd(), searchFacets.getIndexesList().toArray(new String[0]));

        if (StringUtils.isNotEmpty(searchCriteria))
        {
            StringBuilder scatterPlotDataBuilder = new StringBuilder();
            scatterPlotDataBuilder.append(SCATTER_PLOT_HEADER);

            for (Letter letter : result.getLetters())
            {
                createServerResponse(letter, sdf, scatterPlotDataBuilder, sr);

            }

            sr.setSelectedStats(scatterPlotDataBuilder.toString());
        }

        sr.setTotalNumberOfLetters(result.getNumberOfResults());

        System.out.println("search filtered offset " + result.getNumberOfResults());

        return sr;
    }

    private void setScatterPlotData(String formattedDate, int numOfWords, String briefDesc, String id,
                                    StringBuilder scatterPlotDataBuilder)
    {

        scatterPlotDataBuilder.append(formattedDate);
        scatterPlotDataBuilder.append(",");
        scatterPlotDataBuilder.append(numOfWords);
        scatterPlotDataBuilder.append(",");
        scatterPlotDataBuilder.append(briefDesc);
        scatterPlotDataBuilder.append(",");
        scatterPlotDataBuilder.append(id);
        scatterPlotDataBuilder.append("\n");

    }

    @Override
    public ServerResponse getDataForCharts(SearchFacets searchFacets)
    {
        ServerResponse sr = new ServerResponse();

        PropertiesLoader.loadProperties(properties, "properties.xml");

        sr.setFeedback("Getting records");

        IndexSearchResult result = new IndexSearchResult();

        ElasticConnector ec = new ElasticConnector();

        sr.setCsvLetterData(ec.getLetterHistogram(properties, searchFacets.getIndexesList().toArray(new String[0])));

        sr.setWorkEvents(events);

        return sr;
    }

    @Override
    public ServerResponse getLettersSubset(String range, String searchString, SearchFacets searchFacets)
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

        ec.queryStringSearchFiltered(searchStringModified, properties, result, 0, start, end, searchFacets.getIndexesList().toArray(new String[0]));


        StringBuilder scatterPlotDataBuilder = new StringBuilder();

        scatterPlotDataBuilder.append(SCATTER_PLOT_HEADER);

        for (Letter letter : result.getLetters())
        {
            createServerResponse(letter, sdf, scatterPlotDataBuilder, sr);

        }

        if (StringUtils.isNotEmpty(searchString))
        {
            sr.setSelectedStats(scatterPlotDataBuilder.toString());
        }

        sr.setTotalNumberOfLetters(result.getNumberOfResults());

        return sr;
    }

    String events = "[\n" +
            "  {\n" +
            "    \"oritinalTitle\": \"Детство\",\n" +
            "    \"translation\": {\n" +
            "      \"en\": \"Childhood\"\n" +
            "    },\n" +
            "    \"events\": [\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1852-06-01T15:00:36.264Z\",\n" +
            "        \"end\": \"1852-06-30T15:00:36.265Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1851-01-18T15:00:36.231Z\",\n" +
            "        \"end\": \"1852-07-03T15:00:36.231Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"oritinalTitle\": \"Вторая русская книга для чтения\",\n" +
            "    \"translation\": {\n" +
            "      \"en\": \"Second Russian Book for Reading \"\n" +
            "    },\n" +
            "    \"events\": [\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1874-12-15T15:00:36.338Z\",\n" +
            "        \"end\": \"1875-01-15T15:00:36.339Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1875-06-19T15:00:36.339Z\",\n" +
            "        \"end\": \"1875-06-20T04:56:02.000Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"oritinalTitle\": \"Четвертая русская книга для чтения\",\n" +
            "    \"translation\": {\n" +
            "      \"en\": \"Fourth Russian Book for Reading \"\n" +
            "    },\n" +
            "    \"events\": [\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1874-12-15T15:00:36.340Z\",\n" +
            "        \"end\": \"1875-01-15T15:00:36.340Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1875-06-19T15:00:36.340Z\",\n" +
            "        \"end\": \"1875-06-20T04:56:02.000Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"oritinalTitle\": \"И свет во тьме светит\",\n" +
            "    \"translation\": {\n" +
            "      \"en\": \"The Light Shines in Darkness\"\n" +
            "    },\n" +
            "    \"events\": [\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1896-01-09T15:00:36.346Z\",\n" +
            "        \"end\": \"1896-03-20T15:00:36.346Z\",\n" +
            "        \"comment\": \"Writes, revises first draft, continues to work intermittently after this date\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1911-06-01T15:00:36.347Z\",\n" +
            "        \"end\": \"1911-06-30T15:00:36.347Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"oritinalTitle\": \"Петр Хлебник\",\n" +
            "    \"translation\": {\n" +
            "      \"en\": \"Peter the Baker\"\n" +
            "    },\n" +
            "    \"events\": [\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1884-01-30T15:00:36.358Z\",\n" +
            "        \"end\": \"1884-01-31T05:00:00.000Z\",\n" +
            "        \"comment\": \"unfinished\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1894-07-15T15:00:36.358Z\",\n" +
            "        \"end\": \"1894-07-16T05:00:00.000Z\",\n" +
            "        \"comment\": \"unfinished\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1918-06-01T14:00:36.358Z\",\n" +
            "        \"end\": \"1918-06-30T14:00:36.358Z\",\n" +
            "        \"comment\": \"unfinished\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"oritinalTitle\": \"Холстомер\",\n" +
            "    \"translation\": {\n" +
            "      \"en\": \"Kholstomer \"\n" +
            "    },\n" +
            "    \"events\": [\n" +
            "      {\n" +
            "        \"event_title\": \"conception\",\n" +
            "        \"start\": \"1856-05-31T15:00:36.358Z\",\n" +
            "        \"end\": \"1856-06-01T04:56:02.000Z\",\n" +
            "        \"comment\": \"Tolstoy wants to write the story of a horse\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1863-03-03T15:00:36.358Z\",\n" +
            "        \"end\": \"1864-01-15T15:00:36.358Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1885-06-01T15:00:36.359Z\",\n" +
            "        \"end\": \"1885-06-30T15:00:36.359Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"oritinalTitle\": \"Три дня в деревне\",\n" +
            "    \"translation\": {\n" +
            "      \"en\": \"Three Days in the Village \"\n" +
            "    },\n" +
            "    \"events\": [\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1909-10-23T05:00:00.000Z\",\n" +
            "        \"end\": \"1910-01-14T05:00:00.000Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1910-09-15T05:00:00.000Z\",\n" +
            "        \"end\": \"1910-09-15T05:00:00.000Z\",\n" +
            "        \"comment\": \"Published in Vestnik Evropy\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"oritinalTitle\": \"Отрочества\",\n" +
            "    \"translation\": {\n" +
            "      \"en\": \"Boyhood \"\n" +
            "    },\n" +
            "    \"events\": [\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1852-11-29T15:00:36.272Z\",\n" +
            "        \"end\": \"1854-03-14T15:00:36.273Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1854-06-01T15:00:36.273Z\",\n" +
            "        \"end\": \"1854-06-30T15:00:36.273Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"oritinalTitle\": \"Записки маркёра\",\n" +
            "    \"translation\": {\n" +
            "      \"en\": \"A Billiard-Marker's Notes\"\n" +
            "    },\n" +
            "    \"events\": [\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1853-09-13T15:00:36.277Z\",\n" +
            "        \"end\": \"1854-02-15T15:00:36.277Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1855-06-01T15:00:36.277Z\",\n" +
            "        \"end\": \"1855-06-30T15:00:36.277Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"oritinalTitle\": \"Севастополь в августе 1855 года\",\n" +
            "    \"translation\": {\n" +
            "      \"en\": \"Sevastopol in August 1855\"\n" +
            "    },\n" +
            "    \"events\": [\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1855-09-19T15:00:36.289Z\",\n" +
            "        \"end\": \"1855-12-27T15:00:36.289Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1856-06-01T15:00:36.290Z\",\n" +
            "        \"end\": \"1856-06-30T15:00:36.290Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"oritinalTitle\": \"Из кавказских воспоминаний. Разжалованный\",\n" +
            "    \"translation\": {\n" +
            "      \"en\": \"From Memoirs of the Caucasus. Disranked.  \"\n" +
            "    },\n" +
            "    \"events\": [\n" +
            "      {\n" +
            "        \"event_title\": \"conception\",\n" +
            "        \"start\": \"1853-12-15T15:00:36.294Z\",\n" +
            "        \"end\": \"1853-12-16T04:56:02.000Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1856-06-01T15:00:36.295Z\",\n" +
            "        \"end\": \"1856-06-30T15:00:36.295Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1856-11-01T15:00:36.294Z\",\n" +
            "        \"end\": \"1856-11-30T15:00:36.294Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"oritinalTitle\": \"Утро помещика\",\n" +
            "    \"translation\": {\n" +
            "      \"en\": \"A Landlord's Morning\"\n" +
            "    },\n" +
            "    \"events\": [\n" +
            "      {\n" +
            "        \"event_title\": \"conception\",\n" +
            "        \"start\": \"1852-06-15T15:00:36.295Z\",\n" +
            "        \"end\": \"1852-08-15T15:00:36.295Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1856-06-01T15:00:36.297Z\",\n" +
            "        \"end\": \"1856-06-30T15:00:36.297Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1852-08-15T15:00:36.297Z\",\n" +
            "        \"end\": \"1856-11-29T15:00:36.297Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"oritinalTitle\": \"Семейное счастие\",\n" +
            "    \"translation\": {\n" +
            "      \"en\": \"Family Happiness\"\n" +
            "    },\n" +
            "    \"events\": [\n" +
            "      {\n" +
            "        \"event_title\": \"conception\",\n" +
            "        \"start\": \"1857-08-16T15:00:36.301Z\",\n" +
            "        \"end\": \"1857-08-17T04:56:02.000Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1858-04-09T15:00:36.301Z\",\n" +
            "        \"end\": \"1858-10-30T15:00:36.301Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1859-06-01T15:00:36.302Z\",\n" +
            "        \"end\": \"1859-06-30T15:00:36.302Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"oritinalTitle\": \"Идиллия: Оно заработки хорошо, да и грех бывает от того\",\n" +
            "    \"translation\": {\n" +
            "      \"en\": \"Idilliya: Ono zarabotki khorosho, da i grekh byvaet ot togo\"\n" +
            "    },\n" +
            "    \"events\": [\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1860-05-25T15:00:36.307Z\",\n" +
            "        \"end\": \"1860-10-28T15:00:36.307Z\",\n" +
            "        \"comment\": \"Unfinished\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1911-06-01T15:00:36.308Z\",\n" +
            "        \"end\": \"1911-06-30T15:00:36.308Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"oritinalTitle\": \"Сто лет\",\n" +
            "    \"translation\": {\n" +
            "      \"en\": \"One Hundred Years\"\n" +
            "    },\n" +
            "    \"events\": [\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1879-06-01T15:00:36.343Z\",\n" +
            "        \"end\": \"1879-06-30T15:00:36.343Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"oritinalTitle\": \"Записки сумасшедшего\",\n" +
            "    \"translation\": {\n" +
            "      \"en\": \"Memoirs of a Madman \"\n" +
            "    },\n" +
            "    \"events\": [\n" +
            "      {\n" +
            "        \"event_title\": \"conception\",\n" +
            "        \"start\": \"1884-04-11T15:00:36.359Z\",\n" +
            "        \"end\": \"1884-04-12T05:00:00.000Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1884-06-01T15:00:36.359Z\",\n" +
            "        \"end\": \"1886-06-30T15:00:36.359Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1912-06-01T15:00:36.360Z\",\n" +
            "        \"end\": \"1912-06-30T15:00:36.360Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"oritinalTitle\": \" Три сына\",\n" +
            "    \"translation\": {\n" +
            "      \"en\": \"Three  Sons\"\n" +
            "    },\n" +
            "    \"events\": [\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1887-06-01T15:00:36.362Z\",\n" +
            "        \"end\": \"1887-06-30T15:00:36.362Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1889-06-01T15:00:36.362Z\",\n" +
            "        \"end\": \"1889-06-30T15:00:36.362Z\",\n" +
            "        \"comment\": \"First published in Tsvetnik\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"oritinalTitle\": \"Послесловие к «Крейцеровой сонате»\",\n" +
            "    \"translation\": {\n" +
            "      \"en\": \"Afterword to The Kreutzer Sonata\"\n" +
            "    },\n" +
            "    \"events\": [\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1889-10-31T15:00:36.366Z\",\n" +
            "        \"end\": \"1890-04-25T15:00:36.366Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"oritinalTitle\": \"Дорого стоит\",\n" +
            "    \"translation\": {\n" +
            "      \"en\": \"Too Dear \"\n" +
            "    },\n" +
            "    \"events\": [\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1890-10-20T15:00:36.368Z\",\n" +
            "        \"end\": \"1890-10-29T15:00:36.368Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1899-06-01T15:00:36.368Z\",\n" +
            "        \"end\": \"1899-06-30T15:00:36.368Z\",\n" +
            "        \"comment\": \"First published by V.G. Chertkov's press in England\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"oritinalTitle\": \"Сон молодого царя\",\n" +
            "    \"translation\": {\n" +
            "      \"en\": \"Dream of the Young Tsar\"\n" +
            "    },\n" +
            "    \"events\": [\n" +
            "      {\n" +
            "        \"event_title\": \"draft\",\n" +
            "        \"start\": \"1894-12-15T15:00:36.369Z\",\n" +
            "        \"end\": \"1894-12-16T05:00:00.000Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1912-06-01T15:00:36.369Z\",\n" +
            "        \"end\": \"1912-06-30T15:00:36.369Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"oritinalTitle\": \"Труд, смерть и болезнь\",\n" +
            "    \"translation\": {\n" +
            "      \"en\": \"Labor, death and illness\"\n" +
            "    },\n" +
            "    \"events\": [\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1903-06-01T15:00:36.383Z\",\n" +
            "        \"end\": \"1903-06-30T15:00:36.383Z\",\n" +
            "        \"comment\": \"First published in Yiddish in Gilf. Literaturnyy sbornik s illyustratsiyami   and then in Russian\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1903-07-23T05:00:00.000Z\",\n" +
            "        \"end\": \"1903-08-20T05:00:00.000Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"oritinalTitle\": \"Ассирийский царь Асархадон\",\n" +
            "    \"translation\": {\n" +
            "      \"en\": \"The Assyrian Tsar Asarkhadon\"\n" +
            "    },\n" +
            "    \"events\": [\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1903-06-01T15:00:36.389Z\",\n" +
            "        \"end\": \"1903-06-30T15:00:36.389Z\",\n" +
            "        \"comment\": \"First published in Yiddish in Gilf. Literaturnyy sbornik s illyustratsiyami   and then in Russian\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1903-07-21T05:00:00.000Z\",\n" +
            "        \"end\": \"1903-08-20T05:00:00.000Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"oritinalTitle\": \"Алеша Горшок\",\n" +
            "    \"translation\": {\n" +
            "      \"en\": \"Alyosha the Pot\"\n" +
            "    },\n" +
            "    \"events\": [\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1905-02-24T05:00:00.000Z\",\n" +
            "        \"end\": \"1905-02-28T05:00:00.000Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1911-06-01T15:00:36.397Z\",\n" +
            "        \"end\": \"1911-06-30T15:00:36.397Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"oritinalTitle\": \"Что я видел во сне…\",\n" +
            "    \"translation\": {\n" +
            "      \"en\": \"What I Saw in my Dream\"\n" +
            "    },\n" +
            "    \"events\": [\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1906-11-13T05:00:00.000Z\",\n" +
            "        \"end\": \"1906-11-13T05:00:00.000Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1911-06-01T15:00:36.398Z\",\n" +
            "        \"end\": \"1911-06-30T15:00:36.398Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"oritinalTitle\": \"Песни на деревне\",\n" +
            "    \"translation\": {\n" +
            "      \"en\": \"Songs at the Village\"\n" +
            "    },\n" +
            "    \"events\": [\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1909-10-22T05:00:00.000Z\",\n" +
            "        \"end\": \"1909-11-08T05:00:00.000Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1910-06-01T15:00:36.401Z\",\n" +
            "        \"end\": \"1910-06-30T15:00:36.401Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"oritinalTitle\": \"Рубка леса\",\n" +
            "    \"translation\": {\n" +
            "      \"en\": \"The Wood-Felling\"\n" +
            "    },\n" +
            "    \"events\": [\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1854-07-01T15:00:36.285Z\",\n" +
            "        \"end\": \"1854-09-01T15:00:36.285Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1855-06-01T15:00:36.286Z\",\n" +
            "        \"end\": \"1855-06-30T15:00:36.286Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1855-06-01T15:00:36.285Z\",\n" +
            "        \"end\": \"1855-07-18T15:00:36.285Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"oritinalTitle\": \"Севастополь в декабре месяце\",\n" +
            "    \"translation\": {\n" +
            "      \"en\": \"Sevastopol in December\"\n" +
            "    },\n" +
            "    \"events\": [\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1855-03-15T15:00:36.286Z\",\n" +
            "        \"end\": \"1855-04-25T15:00:36.286Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1855-06-01T15:00:36.287Z\",\n" +
            "        \"end\": \"1855-06-30T15:00:36.287Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"oritinalTitle\": \"Два гусара\",\n" +
            "    \"translation\": {\n" +
            "      \"en\": \"Two Hussars\"\n" +
            "    },\n" +
            "    \"events\": [\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1856-03-12T15:00:36.293Z\",\n" +
            "        \"end\": \"1856-04-14T15:00:36.293Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1856-06-01T15:00:36.293Z\",\n" +
            "        \"end\": \"1856-06-30T15:00:36.293Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"oritinalTitle\": \"Три смерти\",\n" +
            "    \"translation\": {\n" +
            "      \"en\": \"Three Deaths\"\n" +
            "    },\n" +
            "    \"events\": [\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1858-01-15T15:00:36.300Z\",\n" +
            "        \"end\": \"1858-01-24T15:00:36.300Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1859-06-01T15:00:36.300Z\",\n" +
            "        \"end\": \"1859-06-30T15:00:36.300Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"oritinalTitle\": \"Азбука \",\n" +
            "    \"translation\": {\n" +
            "      \"en\": \"Primer\"\n" +
            "    },\n" +
            "    \"events\": [\n" +
            "      {\n" +
            "        \"event_title\": \"conception\",\n" +
            "        \"start\": \"1868-01-01T15:00:36.336Z\",\n" +
            "        \"end\": \"1868-01-02T04:56:02.000Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1872-06-01T15:00:36.337Z\",\n" +
            "        \"end\": \"1872-06-30T15:00:36.337Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1871-09-15T15:00:36.336Z\",\n" +
            "        \"end\": \"1872-10-15T15:00:36.336Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"oritinalTitle\": \"Третья русская книга для чтения\",\n" +
            "    \"translation\": {\n" +
            "      \"en\": \"Third Russian Book for Reading \"\n" +
            "    },\n" +
            "    \"events\": [\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1874-12-15T15:00:36.339Z\",\n" +
            "        \"end\": \"1875-01-15T15:00:36.339Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1875-06-19T15:00:36.339Z\",\n" +
            "        \"end\": \"1875-06-20T04:56:02.000Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"oritinalTitle\": \"[Роман о времени Петра I]\",\n" +
            "    \"translation\": {\n" +
            "      \"en\": \"Novel about the time of Peter I\"\n" +
            "    },\n" +
            "    \"events\": [\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1870-02-15T15:00:36.341Z\",\n" +
            "        \"end\": \"1870-06-15T15:00:36.341Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1872-09-15T15:00:36.341Z\",\n" +
            "        \"end\": \"1873-03-19T15:00:36.341Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"oritinalTitle\": \"Декабристы\",\n" +
            "    \"translation\": {\n" +
            "      \"en\": \"Decemberists\"\n" +
            "    },\n" +
            "    \"events\": [\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1863-09-15T15:00:36.342Z\",\n" +
            "        \"end\": \"1863-12-15T15:00:36.342Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1877-12-15T15:00:36.342Z\",\n" +
            "        \"end\": \"1878-01-15T15:00:36.342Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1878-03-15T15:00:36.342Z\",\n" +
            "        \"end\": \"1878-09-15T15:00:36.342Z\",\n" +
            "        \"comment\": \"Research, meeting with families of Decembrists\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1878-10-15T15:00:36.343Z\",\n" +
            "        \"end\": \"1879-01-31T15:00:36.343Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1884-11-15T15:00:36.343Z\",\n" +
            "        \"end\": \"1884-11-16T05:00:00.000Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"oritinalTitle\": \"Власть тьмы\",\n" +
            "    \"translation\": {\n" +
            "      \"en\": \"The Power of Darkness \"\n" +
            "    },\n" +
            "    \"events\": [\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1886-08-15T15:00:36.344Z\",\n" +
            "        \"end\": \"1886-12-15T15:00:36.344Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1887-02-15T15:00:36.344Z\",\n" +
            "        \"end\": \"1887-02-16T05:00:00.000Z\",\n" +
            "        \"comment\": \"Published in Posrednik\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"oritinalTitle\": \"Плоды просвещения\",\n" +
            "    \"translation\": {\n" +
            "      \"en\": \"The Fruits of Enlightenment: A Comedy in Four Acts \"\n" +
            "    },\n" +
            "    \"events\": [\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1886-09-15T15:00:36.345Z\",\n" +
            "        \"end\": \"1886-12-15T15:00:36.345Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1889-03-25T15:00:36.345Z\",\n" +
            "        \"end\": \"1889-07-24T15:00:36.345Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1889-12-22T15:00:36.346Z\",\n" +
            "        \"end\": \"1890-04-15T15:00:36.346Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1891-06-15T15:00:36.346Z\",\n" +
            "        \"end\": \"1891-06-16T15:00:36.346Z\",\n" +
            "        \"comment\": \"First published in V pamyat' Yur'eva. Sbornik, izdannyy druz'yami pokoynogo\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"oritinalTitle\": \"Окончание малороссийской легенды «Сорок лет», изданной Костомаровым в 1881 г.\",\n" +
            "    \"translation\": {\n" +
            "      \"en\": \"Okonchanie malorossiyskoy legendy «Sorok let», izdannoy Kostomarovym v 1881 g.\"\n" +
            "    },\n" +
            "    \"events\": [\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1886-01-15T15:00:36.361Z\",\n" +
            "        \"end\": \"1886-04-15T15:00:36.361Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1899-03-05T15:00:36.362Z\",\n" +
            "        \"end\": \"1899-03-06T05:00:00.000Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"oritinalTitle\": \"Франсуаза\",\n" +
            "    \"translation\": {\n" +
            "      \"en\": \"Françoise\"\n" +
            "    },\n" +
            "    \"events\": [\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1890-10-19T15:00:36.367Z\",\n" +
            "        \"end\": \"1890-11-01T15:00:36.368Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1891-02-05T15:00:36.368Z\",\n" +
            "        \"end\": \"1891-02-06T05:00:00.000Z\",\n" +
            "        \"comment\": \"Published in Novoe vremia without attribution to Tolstoy\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"oritinalTitle\": \"Хозяин и работник\",\n" +
            "    \"translation\": {\n" +
            "      \"en\": \"Master and Man \"\n" +
            "    },\n" +
            "    \"events\": [\n" +
            "      {\n" +
            "        \"event_title\": \"conception\",\n" +
            "        \"start\": \"1892-12-15T15:00:36.371Z\",\n" +
            "        \"end\": \"1892-12-16T05:00:00.000Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1894-09-06T15:00:36.371Z\",\n" +
            "        \"end\": \"1894-09-13T15:00:36.371Z\",\n" +
            "        \"comment\": \"First rough draft\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1894-12-25T15:00:36.371Z\",\n" +
            "        \"end\": \"1895-01-14T15:00:36.371Z\",\n" +
            "        \"comment\": \"Revision\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1895-03-05T15:00:36.371Z\",\n" +
            "        \"end\": \"1895-03-06T05:00:00.000Z\",\n" +
            "        \"comment\": \"Published in the March book of Severnii Vestnik\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"oritinalTitle\": \"Разрушение ада и восстановление его\",\n" +
            "    \"translation\": {\n" +
            "      \"en\": \"The Destruction and Restoration of Hell \"\n" +
            "    },\n" +
            "    \"events\": [\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1902-11-01T05:00:00.000Z\",\n" +
            "        \"end\": \"1902-12-31T05:00:00.000Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1903-06-01T15:00:36.377Z\",\n" +
            "        \"end\": \"1903-06-30T15:00:36.377Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"oritinalTitle\": \"После бала\",\n" +
            "    \"translation\": {\n" +
            "      \"en\": \"After the Ball\"\n" +
            "    },\n" +
            "    \"events\": [\n" +
            "      {\n" +
            "        \"event_title\": \"conception\",\n" +
            "        \"start\": \"1903-06-09T05:00:00.000Z\",\n" +
            "        \"end\": \"1903-06-10T05:00:00.000Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1903-08-09T05:00:00.000Z\",\n" +
            "        \"end\": \"1903-08-20T05:00:00.000Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1911-06-01T15:00:36.380Z\",\n" +
            "        \"end\": \"1911-06-30T15:00:36.380Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"oritinalTitle\": \"Мать\",\n" +
            "    \"translation\": {\n" +
            "      \"en\": \"Mother \"\n" +
            "    },\n" +
            "    \"events\": [\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1891-04-15T15:00:36.403Z\",\n" +
            "        \"end\": \"1891-05-15T15:00:36.403Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"oritinalTitle\": \"Люцерн\",\n" +
            "    \"translation\": {\n" +
            "      \"en\": \"Lucerne\"\n" +
            "    },\n" +
            "    \"events\": [\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1857-06-01T15:00:36.298Z\",\n" +
            "        \"end\": \"1857-06-30T15:00:36.298Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1857-07-07T15:00:36.298Z\",\n" +
            "        \"end\": \"1857-07-11T15:00:36.298Z\",\n" +
            "        \"comment\": \"Written in Switzerland\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"oritinalTitle\": \"Казаки\",\n" +
            "    \"translation\": {\n" +
            "      \"en\": \"The Cossacks\"\n" +
            "    },\n" +
            "    \"events\": [\n" +
            "      {\n" +
            "        \"event_title\": \"conception\",\n" +
            "        \"start\": \"1852-05-10T15:00:36.302Z\",\n" +
            "        \"end\": \"1853-10-21T15:00:36.303Z\",\n" +
            "        \"comment\": \"First conceived in verse\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1853-08-28T15:00:36.304Z\",\n" +
            "        \"end\": \"1854-01-01T15:00:36.304Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1857-05-15T15:00:36.304Z\",\n" +
            "        \"end\": \"1858-12-31T15:00:36.304Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1863-12-01T15:00:36.305Z\",\n" +
            "        \"end\": \"1864-01-01T15:00:36.305Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1864-06-01T15:00:36.305Z\",\n" +
            "        \"end\": \"1864-06-30T15:00:36.305Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"oritinalTitle\": \"Анна Каренина \",\n" +
            "    \"translation\": {\n" +
            "      \"en\": \"Anna Karenina \"\n" +
            "    },\n" +
            "    \"events\": [\n" +
            "      {\n" +
            "        \"event_title\": \"conception\",\n" +
            "        \"start\": \"1870-02-24T15:00:36.327Z\",\n" +
            "        \"end\": \"1870-02-25T04:56:02.000Z\",\n" +
            "        \"comment\": \"Conception\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1873-03-19T15:00:36.327Z\",\n" +
            "        \"end\": \"1873-05-17T15:00:36.327Z\",\n" +
            "        \"comment\": \"Work on first full draft\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1873-09-23T15:00:36.328Z\",\n" +
            "        \"end\": \"1873-10-16T15:00:36.328Z\",\n" +
            "        \"comment\": \"Work on first full draft\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"stops work\",\n" +
            "        \"start\": \"1873-10-16T15:00:36.333Z\",\n" +
            "        \"end\": \"1873-10-17T04:56:02.000Z\",\n" +
            "        \"comment\": \"Want to give up writing Anna Karenina\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1873-12-13T15:00:36.328Z\",\n" +
            "        \"end\": \"1874-02-11T15:00:36.328Z\",\n" +
            "        \"comment\": \"Work on first full draft\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1874-03-01T15:00:36.333Z\",\n" +
            "        \"end\": \"1874-03-02T04:56:02.000Z\",\n" +
            "        \"comment\": \"Submits first part of Anna Karenina to M.N Katkov's press for publication as a separate edition\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"stops publication\",\n" +
            "        \"start\": \"1874-06-20T15:00:36.333Z\",\n" +
            "        \"end\": \"1874-06-21T04:56:02.000Z\",\n" +
            "        \"comment\": \"Stops first attempt to publish Anna Karenina\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1874-09-15T15:00:36.334Z\",\n" +
            "        \"end\": \"1874-09-16T04:56:02.000Z\",\n" +
            "        \"comment\": \"Promises to publish Anna Karenina serially in Russkii Vestnik\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1874-11-01T15:00:36.334Z\",\n" +
            "        \"end\": \"1874-11-02T04:56:02.000Z\",\n" +
            "        \"comment\": \"Payment for the novel settled with Russkii Vestnik\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1874-12-12T15:00:36.334Z\",\n" +
            "        \"end\": \"1874-12-13T04:56:02.000Z\",\n" +
            "        \"comment\": \"Commits to publishing serially with Russkii Vestnik\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1874-12-12T15:00:36.330Z\",\n" +
            "        \"end\": \"1874-12-21T15:00:36.330Z\",\n" +
            "        \"comment\": \"Revises beginning of Anna Karenina for Russkii Vestnik  (parts I, II, 10 chapters of part III)\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1875-03-22T15:00:36.331Z\",\n" +
            "        \"end\": \"1875-05-15T15:00:36.331Z\",\n" +
            "        \"comment\": \"Revises parts I and II for Russkii Vestnik\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1875-12-12T15:00:36.331Z\",\n" +
            "        \"end\": \"1876-08-15T15:00:36.331Z\",\n" +
            "        \"comment\": \"Revises parts III,IV,V for Russkii Vestnik\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1876-11-10T15:00:36.331Z\",\n" +
            "        \"end\": \"1877-01-15T15:00:36.332Z\",\n" +
            "        \"comment\": \"Revises parts V, VI for Russkii Vestnik\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1875-01-15T15:00:36.334Z\",\n" +
            "        \"end\": \"1877-04-15T15:00:36.335Z\",\n" +
            "        \"comment\": \"Anna Karenina published in Russkii Vestnik\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1877-03-15T15:00:36.332Z\",\n" +
            "        \"end\": \"1877-04-22T15:00:36.332Z\",\n" +
            "        \"comment\": \"Revises part VII and epilogue (which became part VIII) for Russkii Vestnik\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1877-05-15T15:00:36.332Z\",\n" +
            "        \"end\": \"1877-06-15T15:00:36.332Z\",\n" +
            "        \"comment\": \"Work on part VIII for separate edition\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1877-06-15T15:00:36.335Z\",\n" +
            "        \"end\": \"1877-06-16T04:56:02.000Z\",\n" +
            "        \"comment\": \"Part VIII of Anna Karenina published as separate edition\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1877-06-15T15:00:36.332Z\",\n" +
            "        \"end\": \"1877-08-15T15:00:36.332Z\",\n" +
            "        \"comment\": \"Prepares first separate edition\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1878-01-15T15:00:36.335Z\",\n" +
            "        \"end\": \"1878-01-16T04:56:02.000Z\",\n" +
            "        \"comment\": \"Anna Karenina first published as separate edition\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"oritinalTitle\": \"Рассказы из «Новой азбуки»\",\n" +
            "    \"translation\": {\n" +
            "      \"en\": \"Stories from \\\"New Primer\\\" \"\n" +
            "    },\n" +
            "    \"events\": [\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1874-11-15T15:00:36.337Z\",\n" +
            "        \"end\": \"1875-01-20T15:00:36.337Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1875-06-19T15:00:36.337Z\",\n" +
            "        \"end\": \"1875-06-20T04:56:02.000Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"oritinalTitle\": \"[Князь Федор Щетинин]\",\n" +
            "    \"translation\": {\n" +
            "      \"en\": \"Prince Fyodor Shchetinin\"\n" +
            "    },\n" +
            "    \"events\": [\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1877-06-01T15:00:36.341Z\",\n" +
            "        \"end\": \"1877-06-30T15:00:36.341Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"oritinalTitle\": \"Живой труп\",\n" +
            "    \"translation\": {\n" +
            "      \"en\": \"The Living Corpse \"\n" +
            "    },\n" +
            "    \"events\": [\n" +
            "      {\n" +
            "        \"event_title\": \"conception\",\n" +
            "        \"start\": \"1897-12-28T15:00:36.347Z\",\n" +
            "        \"end\": \"1897-12-29T05:00:00.000Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1900-01-27T05:00:00.000Z\",\n" +
            "        \"end\": \"1900-02-18T05:00:00.000Z\",\n" +
            "        \"comment\": \"Plans drama, research\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1900-05-01T05:00:00.000Z\",\n" +
            "        \"end\": \"1900-11-28T05:00:00.000Z\",\n" +
            "        \"comment\": \"Though Tolstoy continued to intermittently consider returning to the work, it was left unfinished\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1911-09-23T05:00:00.000Z\",\n" +
            "        \"end\": \"1911-09-23T05:00:00.000Z\",\n" +
            "        \"comment\": \"Published in Russkoe slovo\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"oritinalTitle\": \"От ней все качества\",\n" +
            "    \"translation\": {\n" +
            "      \"en\": \"All Qualities are from Her\"\n" +
            "    },\n" +
            "    \"events\": [\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1910-03-29T05:00:00.000Z\",\n" +
            "        \"end\": \"1910-07-01T15:00:36.357Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"oritinalTitle\": \"Суратская кофейная\",\n" +
            "    \"translation\": {\n" +
            "      \"en\": \"The Coffee-House of Surrat\"\n" +
            "    },\n" +
            "    \"events\": [\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1887-01-15T15:00:36.363Z\",\n" +
            "        \"end\": \"1887-01-23T15:00:36.363Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1893-01-15T15:00:36.363Z\",\n" +
            "        \"end\": \"1893-01-16T05:00:00.000Z\",\n" +
            "        \"comment\": \"published in  Severnii Vestnik No. 1\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"oritinalTitle\": \"Крейцерова соната\",\n" +
            "    \"translation\": {\n" +
            "      \"en\": \"The Kreutzer Sonata\"\n" +
            "    },\n" +
            "    \"events\": [\n" +
            "      {\n" +
            "        \"event_title\": \"conception\",\n" +
            "        \"start\": \"1879-06-01T15:00:36.363Z\",\n" +
            "        \"end\": \"1879-06-30T15:00:36.363Z\",\n" +
            "        \"comment\": \"Begins but doesn't finish \\\"Wife's Murderer\\\", which dealt with the theme later developed in The Kreutzer Sonata\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1887-06-21T15:00:36.364Z\",\n" +
            "        \"end\": \"1888-06-15T15:00:36.364Z\",\n" +
            "        \"comment\": \"Work on first, second, and third drafts of The Kreutzer Sonata\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1889-04-15T15:00:36.364Z\",\n" +
            "        \"end\": \"1889-08-29T15:00:36.364Z\",\n" +
            "        \"comment\": \"Work on fourth through seventh draft of The Kreutzer Sonata\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1889-09-17T15:00:36.364Z\",\n" +
            "        \"end\": \"1889-10-18T15:00:36.364Z\",\n" +
            "        \"comment\": \"Work on the eight draft of The Kreutzer Sonata (the version that began to circulate among readers via hand, lithograph, and hectograph copies)\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1889-11-02T15:00:36.364Z\",\n" +
            "        \"end\": \"1889-12-09T15:00:36.364Z\",\n" +
            "        \"comment\": \"Final draft of The Kreutzer Sonata\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1890-06-01T15:00:36.365Z\",\n" +
            "        \"end\": \"1890-06-30T15:00:36.365Z\",\n" +
            "        \"comment\": \"The Kreutzer Sonata is  published in Berlin on the basis of hand copies of the eighth draft\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1890-06-01T15:00:36.365Z\",\n" +
            "        \"end\": \"1890-06-30T15:00:36.365Z\",\n" +
            "        \"comment\": \"The Kreutzer Sonata along with the \\\"Afterword to The Kreutzer Sonata\\\" published in Geneva\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1891-04-13T15:00:36.365Z\",\n" +
            "        \"end\": \"1891-04-14T05:00:00.000Z\",\n" +
            "        \"comment\": \"Sofia Tolstoy is granted an audience with Alexander III who gives her permission to publish the previously censored Kreutzer Sonata\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1891-06-15T15:00:36.366Z\",\n" +
            "        \"end\": \"1891-06-16T05:00:00.000Z\",\n" +
            "        \"comment\": \"The Kreutzer Sonata is first published in Russia\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"oritinalTitle\": \"Дьявол\",\n" +
            "    \"translation\": {\n" +
            "      \"en\": \"The Devil \"\n" +
            "    },\n" +
            "    \"events\": [\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1889-11-10T15:00:36.366Z\",\n" +
            "        \"end\": \"1889-11-24T15:00:36.366Z\",\n" +
            "        \"comment\": \"First draft written in two weeks, between the eight and ninth drafts of The Kreutzer Sonata; initially titled \\\"Istoriia Frederiksa\\\"\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1909-02-19T05:00:00.000Z\",\n" +
            "        \"end\": \"1909-06-15T05:00:00.000Z\",\n" +
            "        \"comment\": \"Tolstoy revises his draft, titles the story \\\"The Devil\\\"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1911-06-01T15:00:36.367Z\",\n" +
            "        \"end\": \"1911-06-30T15:00:36.367Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"oritinalTitle\": \"Три притчи\",\n" +
            "    \"translation\": {\n" +
            "      \"en\": \"Three Parables \"\n" +
            "    },\n" +
            "    \"events\": [\n" +
            "      {\n" +
            "        \"event_title\": \"conception\",\n" +
            "        \"start\": \"1893-11-01T15:00:36.369Z\",\n" +
            "        \"end\": \"1893-12-31T15:00:36.370Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1894-10-12T15:00:36.370Z\",\n" +
            "        \"end\": \"1894-12-31T15:00:36.370Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1895-02-15T15:00:36.370Z\",\n" +
            "        \"end\": \"1895-02-17T15:00:36.370Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1895-06-01T15:00:36.370Z\",\n" +
            "        \"end\": \"1895-06-30T15:00:36.370Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"oritinalTitle\": \"Воскресение\",\n" +
            "    \"translation\": {\n" +
            "      \"en\": \"Resurrection \"\n" +
            "    },\n" +
            "    \"events\": [\n" +
            "      {\n" +
            "        \"event_title\": \"conception\",\n" +
            "        \"start\": \"1887-06-15T15:00:36.373Z\",\n" +
            "        \"end\": \"1887-06-16T05:00:00.000Z\",\n" +
            "        \"comment\": \"Inspired by an incident related by A.F. Koni\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"conception\",\n" +
            "        \"start\": \"1888-04-12T15:00:36.373Z\",\n" +
            "        \"end\": \"1888-06-01T15:00:36.373Z\",\n" +
            "        \"comment\": \"Asks and receives A.F. Koni's permission to use the story for a literary work\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1889-12-26T15:00:36.374Z\",\n" +
            "        \"end\": \"1890-06-15T15:00:36.374Z\",\n" +
            "        \"comment\": \"Begins to write Resurrection\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1890-12-15T15:00:36.374Z\",\n" +
            "        \"end\": \"1891-01-25T15:00:36.374Z\",\n" +
            "        \"comment\": \"Contemplates how to proceed with Resurrection\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1891-06-09T15:00:36.374Z\",\n" +
            "        \"end\": \"1891-06-10T15:00:36.374Z\",\n" +
            "        \"comment\": \"Briefly returns to Resurrection\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1895-05-15T15:00:36.374Z\",\n" +
            "        \"end\": \"1895-08-09T15:00:36.374Z\",\n" +
            "        \"comment\": \"Returns to work on Resurrection\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1898-11-15T15:00:36.375Z\",\n" +
            "        \"end\": \"1898-11-16T05:00:00.000Z\",\n" +
            "        \"comment\": \"Decides to publish Resurrection with A.F. Marks in the journal Niva\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1898-07-15T15:00:36.374Z\",\n" +
            "        \"end\": \"1899-01-12T15:00:36.374Z\",\n" +
            "        \"comment\": \"Returns to work on Resurrection. Wishes to Doukhobours\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1898-10-22T15:00:36.375Z\",\n" +
            "        \"end\": \"1899-01-23T15:00:36.375Z\",\n" +
            "        \"comment\": \"Send manuscript to Niva for typesetting, text of Resurrection assembled\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1899-03-13T15:00:36.375Z\",\n" +
            "        \"end\": \"1899-03-14T05:00:00.000Z\",\n" +
            "        \"comment\": \"Publiction begins in Niva\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1899-01-12T15:00:36.375Z\",\n" +
            "        \"end\": \"1899-12-15T15:00:36.375Z\",\n" +
            "        \"comment\": \"Major revision at the proofs stage\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1900-06-23T05:00:00.000Z\",\n" +
            "        \"end\": \"1900-06-23T05:00:00.000Z\",\n" +
            "        \"comment\": \"Tolstoy considers writing a sequel to Resurrection\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1900-06-01T15:00:36.375Z\",\n" +
            "        \"end\": \"1900-06-30T15:00:36.376Z\",\n" +
            "        \"comment\": \"Resurrection appears in two separate editions published y A.F. Marks\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1899-06-01T15:00:36.376Z\",\n" +
            "        \"end\": \"1900-06-30T15:00:36.376Z\",\n" +
            "        \"comment\": \"Simultaneous serial publication in Russian by Svobodnoe slovo, in French in Echo de Paris, in German in Deutsche Verlagsanstalt, in English in The Cosmopolitan Magazine and The Clarion\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"oritinalTitle\": \"Фальшивый купон\",\n" +
            "    \"translation\": {\n" +
            "      \"en\": \"The Forged Coupon\"\n" +
            "    },\n" +
            "    \"events\": [\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1898-01-01T15:00:36.395Z\",\n" +
            "        \"end\": \"1898-06-12T15:00:36.395Z\",\n" +
            "        \"comment\": \"Writes part of the novella\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1902-10-06T05:00:00.000Z\",\n" +
            "        \"end\": \"1902-11-01T05:00:00.000Z\",\n" +
            "        \"comment\": \"Revises, continues to write\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1903-12-25T05:00:00.000Z\",\n" +
            "        \"end\": \"1904-02-04T05:00:00.000Z\",\n" +
            "        \"comment\": \"Continues to write, does not consider work finished\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1911-06-01T15:00:36.397Z\",\n" +
            "        \"end\": \"1911-06-30T15:00:36.397Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"oritinalTitle\": \"Разговор с прохожим\",\n" +
            "    \"translation\": {\n" +
            "      \"en\": \"A Conversation with Passersby\"\n" +
            "    },\n" +
            "    \"events\": [\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1909-09-09T05:00:00.000Z\",\n" +
            "        \"end\": \"1909-09-21T05:00:00.000Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1910-06-01T15:00:36.399Z\",\n" +
            "        \"end\": \"1910-06-30T15:00:36.399Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"oritinalTitle\": \"Проезжий и крестьянин\",\n" +
            "    \"translation\": {\n" +
            "      \"en\": \"The Traveler and the Peasant\"\n" +
            "    },\n" +
            "    \"events\": [\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1909-09-11T05:00:00.000Z\",\n" +
            "        \"end\": \"1909-10-04T05:00:00.000Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1917-05-10T05:00:00.000Z\",\n" +
            "        \"end\": \"1917-05-10T05:00:00.000Z\",\n" +
            "        \"comment\": \"Published in Utro Rossii No. 116\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"oritinalTitle\": \"История вчерашнего дня\",\n" +
            "    \"translation\": {\n" +
            "      \"en\": \"A History of Yesterday\"\n" +
            "    },\n" +
            "    \"events\": [\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1851-03-15T15:00:36.268Z\",\n" +
            "        \"end\": \"1851-03-16T04:56:02.000Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1928-06-01T14:00:36.271Z\",\n" +
            "        \"end\": \"1928-06-30T14:00:36.271Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"oritinalTitle\": \"Юность\",\n" +
            "    \"translation\": {\n" +
            "      \"en\": \"Youth \"\n" +
            "    },\n" +
            "    \"events\": [\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1855-03-12T15:00:36.274Z\",\n" +
            "        \"end\": \"1856-09-24T15:00:36.274Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1857-06-01T15:00:36.274Z\",\n" +
            "        \"end\": \"1857-06-30T15:00:36.274Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"oritinalTitle\": \"Набег\",\n" +
            "    \"translation\": {\n" +
            "      \"en\": \"The Raid\"\n" +
            "    },\n" +
            "    \"events\": [\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1852-06-15T15:00:36.275Z\",\n" +
            "        \"end\": \"1852-12-24T15:00:36.275Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1853-06-01T15:00:36.276Z\",\n" +
            "        \"end\": \"1853-06-30T15:00:36.276Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"oritinalTitle\": \"Севастополь в мае\",\n" +
            "    \"translation\": {\n" +
            "      \"en\": \"Sevastopol in May \"\n" +
            "    },\n" +
            "    \"events\": [\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1855-06-01T15:00:36.288Z\",\n" +
            "        \"end\": \"1855-06-30T15:00:36.288Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1855-07-18T15:00:36.287Z\",\n" +
            "        \"end\": \"1855-07-26T15:00:36.287Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"oritinalTitle\": \"Метель\",\n" +
            "    \"translation\": {\n" +
            "      \"en\": \"The Snowstorm\"\n" +
            "    },\n" +
            "    \"events\": [\n" +
            "      {\n" +
            "        \"event_title\": \"conception\",\n" +
            "        \"start\": \"1854-01-24T15:00:36.291Z\",\n" +
            "        \"end\": \"1854-01-25T04:56:02.000Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1856-01-15T15:00:36.292Z\",\n" +
            "        \"end\": \"1856-02-12T15:00:36.292Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1856-06-01T15:00:36.292Z\",\n" +
            "        \"end\": \"1856-06-30T15:00:36.292Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"oritinalTitle\": \"Альберт\",\n" +
            "    \"translation\": {\n" +
            "      \"en\": \"Albert \"\n" +
            "    },\n" +
            "    \"events\": [\n" +
            "      {\n" +
            "        \"event_title\": \"conception\",\n" +
            "        \"start\": \"1857-01-07T15:00:36.299Z\",\n" +
            "        \"end\": \"1857-01-08T04:56:02.000Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1857-01-07T15:00:36.299Z\",\n" +
            "        \"end\": \"1857-03-17T15:00:36.299Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1858-06-01T15:00:36.299Z\",\n" +
            "        \"end\": \"1858-06-30T15:00:36.300Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"oritinalTitle\": \"Поликушка\",\n" +
            "    \"translation\": {\n" +
            "      \"en\": \"Polikushka\"\n" +
            "    },\n" +
            "    \"events\": [\n" +
            "      {\n" +
            "        \"event_title\": \"conception\",\n" +
            "        \"start\": \"1861-03-15T15:00:36.305Z\",\n" +
            "        \"end\": \"1861-03-16T04:56:02.000Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1861-05-06T15:00:36.306Z\",\n" +
            "        \"end\": \"1862-10-15T15:00:36.306Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1863-06-01T15:00:36.306Z\",\n" +
            "        \"end\": \"1863-06-30T15:00:36.306Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"oritinalTitle\": \"Тихон и Маланья\",\n" +
            "    \"translation\": {\n" +
            "      \"en\": \"Tikhon and Malania\"\n" +
            "    },\n" +
            "    \"events\": [\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1860-11-15T15:00:36.306Z\",\n" +
            "        \"end\": \"1862-12-15T15:00:36.307Z\",\n" +
            "        \"comment\": \"Unfinished\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1911-06-01T15:00:36.307Z\",\n" +
            "        \"end\": \"1911-06-30T15:00:36.307Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"oritinalTitle\": \"Война и мир \",\n" +
            "    \"translation\": {\n" +
            "      \"en\": \"War and Peace \"\n" +
            "    },\n" +
            "    \"events\": [\n" +
            "      {\n" +
            "        \"event_title\": \"conception\",\n" +
            "        \"start\": \"1863-10-15T15:00:36.308Z\",\n" +
            "        \"end\": \"1864-02-15T15:00:36.308Z\",\n" +
            "        \"comment\": \"Initially conceived as a novel about the year 1812\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1864-12-01T15:00:36.321Z\",\n" +
            "        \"end\": \"1864-12-02T04:56:02.000Z\",\n" +
            "        \"comment\": \"Decision to publish novel in Russkii Vestnik\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1864-02-15T15:00:36.321Z\",\n" +
            "        \"end\": \"1865-01-03T15:00:36.322Z\",\n" +
            "        \"comment\": \"Part I completed, sent to Russkii Vestnik\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1865-02-06T15:00:36.322Z\",\n" +
            "        \"end\": \"1866-05-20T15:00:36.322Z\",\n" +
            "        \"comment\": \"First two volumes of the novel published serially in Russkii Vestnik under the title The Year 1805\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1866-09-15T15:00:36.322Z\",\n" +
            "        \"end\": \"1866-12-15T15:00:36.322Z\",\n" +
            "        \"comment\": \"Finished first complete draft of the novel\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1867-01-15T15:00:36.323Z\",\n" +
            "        \"end\": \"1867-01-16T04:56:02.000Z\",\n" +
            "        \"comment\": \"Declared intention to publish the whole novel as a separate edition\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1867-03-19T15:00:36.323Z\",\n" +
            "        \"end\": \"1867-03-25T15:00:36.323Z\",\n" +
            "        \"comment\": \"Calls novel War and Peace for the first time\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1867-06-21T15:00:36.323Z\",\n" +
            "        \"end\": \"1867-06-22T04:56:02.000Z\",\n" +
            "        \"comment\": \"Finalizes plans to publish War and Peace with F.F. Ris' press with P.I. Bartenev serving as editor\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1867-09-15T15:00:36.324Z\",\n" +
            "        \"end\": \"1867-09-30T15:00:36.324Z\",\n" +
            "        \"comment\": \"Revises, corrects proofs for volumes I,II,III of first edition\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1868-01-01T15:00:36.324Z\",\n" +
            "        \"end\": \"1868-03-15T15:00:36.324Z\",\n" +
            "        \"comment\": \"Revises volume IV, composes and revises volume V\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"stops work\",\n" +
            "        \"start\": \"1868-06-01T15:00:36.325Z\",\n" +
            "        \"end\": \"1868-07-30T15:00:36.325Z\",\n" +
            "        \"comment\": \"Has a difficult time writing\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1869-01-15T15:00:36.325Z\",\n" +
            "        \"end\": \"1869-01-16T04:56:02.000Z\",\n" +
            "        \"comment\": \"Both parts of volume VI completed, as is part of the epilogue\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1869-06-01T15:00:36.325Z\",\n" +
            "        \"end\": \"1869-09-15T15:00:36.325Z\",\n" +
            "        \"comment\": \"Intensive work on the epilogue\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1869-12-12T15:00:36.326Z\",\n" +
            "        \"end\": \"1869-12-13T04:56:02.000Z\",\n" +
            "        \"comment\": \"Moskovskii Vedomosti announces the sale of 1st edition (fourth volume) and 2nd edition of War and Peace\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1873-05-01T15:00:36.327Z\",\n" +
            "        \"end\": \"1873-06-30T15:00:36.327Z\",\n" +
            "        \"comment\": \"Works on third edition of War and Peace while also composing Anna Karenina\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"oritinalTitle\": \"Первая русская книга для чтения\",\n" +
            "    \"translation\": {\n" +
            "      \"en\": \"First Russian Book for Reading \"\n" +
            "    },\n" +
            "    \"events\": [\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1874-12-15T15:00:36.338Z\",\n" +
            "        \"end\": \"1875-01-15T15:00:36.338Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1875-06-19T15:00:36.338Z\",\n" +
            "        \"end\": \"1875-06-20T04:56:02.000Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"oritinalTitle\": \"Первый винокур, или Как чертенок краюшку заслужил\",\n" +
            "    \"translation\": {\n" +
            "      \"en\": \"The First Distiller \"\n" +
            "    },\n" +
            "    \"events\": [\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1886-02-15T15:00:36.344Z\",\n" +
            "        \"end\": \"1886-03-15T15:00:36.344Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1887-06-01T15:00:36.344Z\",\n" +
            "        \"end\": \"1887-06-30T15:00:36.345Z\",\n" +
            "        \"comment\": \"Published in Posrednik\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"oritinalTitle\": \"Зараженное семейство\",\n" +
            "    \"translation\": {\n" +
            "      \"en\": \"An Infected Family\"\n" +
            "    },\n" +
            "    \"events\": [\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1863-12-15T15:00:36.357Z\",\n" +
            "        \"end\": \"1864-02-15T15:00:36.357Z\",\n" +
            "        \"comment\": \"unfinished\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"oritinalTitle\": \"Смерть Ивана Ильича\",\n" +
            "    \"translation\": {\n" +
            "      \"en\": \"The Death of Ivan Ilych \"\n" +
            "    },\n" +
            "    \"events\": [\n" +
            "      {\n" +
            "        \"event_title\": \"conception\",\n" +
            "        \"start\": \"1881-07-02T15:00:36.360Z\",\n" +
            "        \"end\": \"1881-07-03T04:56:02.000Z\",\n" +
            "        \"comment\": \"Death of Ivan Ilich Mechnikov, inspiration for Tolstoy's character\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"conception\",\n" +
            "        \"start\": \"1884-04-27T15:00:36.360Z\",\n" +
            "        \"end\": \"1884-04-28T05:00:00.000Z\",\n" +
            "        \"comment\": \"Tolstoy writes that he wants to start and finish a story about the death of a judge\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1884-04-30T15:00:36.360Z\",\n" +
            "        \"end\": \"1884-12-04T15:00:36.361Z\",\n" +
            "        \"comment\": \"Intermittent work\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1885-08-20T15:00:36.361Z\",\n" +
            "        \"end\": \"1886-03-25T15:00:36.361Z\",\n" +
            "        \"comment\": \"Intensive period of work and preparation for publication\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1886-06-01T15:00:36.361Z\",\n" +
            "        \"end\": \"1886-06-30T15:00:36.361Z\",\n" +
            "        \"comment\": \"First published in Sofia Tolstoy's Sochineniia grafa L. N. Tolstogo\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"oritinalTitle\": \"Карма\",\n" +
            "    \"translation\": {\n" +
            "      \"en\": \"Karma\"\n" +
            "    },\n" +
            "    \"events\": [\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1894-11-17T15:00:36.369Z\",\n" +
            "        \"end\": \"1894-11-18T05:00:00.000Z\",\n" +
            "        \"comment\": \"published in Severnii Vestnik\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"oritinalTitle\": \"Отец Сергий\",\n" +
            "    \"translation\": {\n" +
            "      \"en\": \"Father Sergius \"\n" +
            "    },\n" +
            "    \"events\": [\n" +
            "      {\n" +
            "        \"event_title\": \"conception\",\n" +
            "        \"start\": \"1889-12-15T15:00:36.372Z\",\n" +
            "        \"end\": \"1890-01-15T15:00:36.372Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1890-02-01T15:00:36.372Z\",\n" +
            "        \"end\": \"1890-03-03T15:00:36.372Z\",\n" +
            "        \"comment\": \"Research, trip to Optina Monastery\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1890-03-01T15:00:36.372Z\",\n" +
            "        \"end\": \"1890-05-31T15:00:36.372Z\",\n" +
            "        \"comment\": \"First draft\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1891-06-15T15:00:36.372Z\",\n" +
            "        \"end\": \"1891-09-15T15:00:36.372Z\",\n" +
            "        \"comment\": \"Work on revising the first draft\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1898-06-12T15:00:36.373Z\",\n" +
            "        \"end\": \"1898-08-31T15:00:36.373Z\",\n" +
            "        \"comment\": \"Final revision; even after this revision Tolstoy considered the story unfinished\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1911-06-01T15:00:36.373Z\",\n" +
            "        \"end\": \"1911-06-30T15:00:36.373Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"oritinalTitle\": \"Три вопроса\",\n" +
            "    \"translation\": {\n" +
            "      \"en\": \"Three Questions\"\n" +
            "    },\n" +
            "    \"events\": [\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1903-06-01T15:00:36.388Z\",\n" +
            "        \"end\": \"1903-06-30T15:00:36.389Z\",\n" +
            "        \"comment\": \"First published in Yiddish in Gilf. Literaturnyy sbornik s illyustratsiyami   and then in Russian\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1903-07-22T05:00:00.000Z\",\n" +
            "        \"end\": \"1903-08-20T05:00:00.000Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"oritinalTitle\": \"Хаджи-Мурат\",\n" +
            "    \"translation\": {\n" +
            "      \"en\": \"Hadji Murat\"\n" +
            "    },\n" +
            "    \"events\": [\n" +
            "      {\n" +
            "        \"event_title\": \"conception\",\n" +
            "        \"start\": \"1851-12-23T15:00:36.390Z\",\n" +
            "        \"end\": \"1851-12-24T04:56:02.000Z\",\n" +
            "        \"comment\": \"Tolstoy writes about his negative opinion of the historical Hadji Murat\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"conception\",\n" +
            "        \"start\": \"1875-06-15T15:00:36.390Z\",\n" +
            "        \"end\": \"1875-06-16T04:56:02.000Z\",\n" +
            "        \"comment\": \"Interested in Caucasian Folklore\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"conception\",\n" +
            "        \"start\": \"1896-07-19T15:00:36.390Z\",\n" +
            "        \"end\": \"1896-07-20T05:00:00.000Z\",\n" +
            "        \"comment\": \"Tolstoy sees the lone flower called Tatarin on a walk and is reminded of Hadji Murat\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1896-08-10T15:00:36.391Z\",\n" +
            "        \"end\": \"1896-10-23T15:00:36.391Z\",\n" +
            "        \"comment\": \"Work on first draft, entitled \\\"Repei\\\"\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"research\",\n" +
            "        \"start\": \"1896-12-12T15:00:36.391Z\",\n" +
            "        \"end\": \"1897-09-15T15:00:36.391Z\",\n" +
            "        \"comment\": \"Requests materials on Hadji Murat from St. Petersburg\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1897-10-16T15:00:36.391Z\",\n" +
            "        \"end\": \"1898-05-04T15:00:36.391Z\",\n" +
            "        \"comment\": \"Revises, expands, and rewrites first draft, titling the project Hadji Murat\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1901-02-03T05:00:00.000Z\",\n" +
            "        \"end\": \"1901-06-22T05:00:00.000Z\",\n" +
            "        \"comment\": \"Adds the history of Hadji Murat to the initial drafts\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1902-06-24T05:00:00.000Z\",\n" +
            "        \"end\": \"1902-10-09T05:00:00.000Z\",\n" +
            "        \"comment\": \"Completes the manuscript of Hadji Murat but refuses to publish it\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1912-06-01T15:00:36.395Z\",\n" +
            "        \"end\": \"1912-06-30T15:00:36.395Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"approximate\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"oritinalTitle\": \"Волк\",\n" +
            "    \"translation\": {\n" +
            "      \"en\": \"Wolf\"\n" +
            "    },\n" +
            "    \"events\": [\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1908-07-19T05:00:00.000Z\",\n" +
            "        \"end\": \"1908-07-19T05:00:00.000Z\",\n" +
            "        \"comment\": \"Dictated story into a phonograph\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1909-02-23T05:00:00.000Z\",\n" +
            "        \"end\": \"1909-02-23T05:00:00.000Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  {\n" +
            "    \"oritinalTitle\": \"Благодарная почва\",\n" +
            "    \"translation\": {\n" +
            "      \"en\": \"Fertile Soil  \"\n" +
            "    },\n" +
            "    \"events\": [\n" +
            "      {\n" +
            "        \"event_title\": \"work\",\n" +
            "        \"start\": \"1910-06-21T05:00:00.000Z\",\n" +
            "        \"end\": \"1910-07-09T05:00:00.000Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"event_title\": \"publication\",\n" +
            "        \"start\": \"1910-07-24T05:00:00.000Z\",\n" +
            "        \"end\": \"1910-07-24T05:00:00.000Z\",\n" +
            "        \"comment\": \"\",\n" +
            "        \"precision\": \"accurate\"\n" +
            "      }\n" +
            "    ]\n" +
            "  }\n" +
            "]";
}