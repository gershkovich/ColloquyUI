package colloquy;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Before;
import org.junit.Test;
import us.colloquy.model.events.Event;
import us.colloquy.util.PropertiesLoader;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

/**
 * Created by Peter Gershkovich on 1/15/18.
 */
public class TestLoadEvents
{
    final Properties properties = new Properties();

    final Logger logger = Logger.getLogger(ElasticSearch.class.getPackage().getName());

    @Before
    public void setUp()
    {

        PropertiesLoader.loadProperties(properties, "properties.xml");

    }

    @Test
    public void loadEventsFromExcel()
    {

        Map<String, Event> literaryWorkEvents = new TreeMap<>();


        File file = new File("files/Hadji_Murat_BTAB_7.31.xlsx");

        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");

        SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");

        try (Workbook wb = new XSSFWorkbook(new FileInputStream(file)))
        {
            Sheet panelSheet = null;

            for (int i = 0; i < wb.getNumberOfSheets(); i++)
            {
                Sheet sheet = wb.getSheetAt(i);

                if ("Writing History".equalsIgnoreCase(sheet.getSheetName().trim()))
                {
                    panelSheet = sheet;

                }
            }

            if (panelSheet != null)
            {
                for (Row row : panelSheet)
                {
                    if (row.getRowNum() > 0)
                    {
                        Event event = new Event();

                        for (Cell cell : row)
                        {
                            switch (cell.getColumnIndex())
                            {
                                case 0:
                                    String eventName = cell.toString();

                                    event.setName(eventName);
                                    break;

                                case 1:
                                    String eventType = cell.toString();

                                    event.setType(eventType);

                                    break;

                                case 2:
                                    String group = cell.toString();

                                    event.setGroup(group);

                                    break;

                                case 3:

                                    if (cell.getCellTypeEnum() == CellType.NUMERIC)
                                    {
                                        System.out.println("Row No.: " + row.getRowNum() + " " +
                                                cell.getNumericCellValue());

                                        if (HSSFDateUtil.isCellDateFormatted(cell))
                                        {
                                            System.out.println("Row No.: " + row.getRowNum() + " " +
                                                    cell.getDateCellValue());

                                            event.setStart(cell.getDateCellValue());
                                        }
                                    } else
                                    {
                                        event.setStart(df.parse(cell.getStringCellValue()));
                                    }

                                    break;

                                default:

                                    break;

                            }

                        }
//



                        if (StringUtils.isNotEmpty(event.getName()))
                        {
                            event.setId(event.getName() + "-" + sdf.format(event.getStart()));
                            literaryWorkEvents.put(event.getId(), event);


                        }
                    }
                }
            }


            for (String key : literaryWorkEvents.keySet())
            {
                System.out.println(key);

            }


        } catch (Throwable throwable)
        {

            throwable.printStackTrace();
        }

    }


}

