package us.colloquy.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Peter Gershkovich on 9/9/18.
 */
public class Event
{
    private Map<String, String> event = new LinkedHashMap<>();
    private String start;
    private String end;
    private Map<String, String>  precision = new LinkedHashMap<>();
    private Map<String, String>  comment = new LinkedHashMap<>();


    public String getStart()
    {
        return start;
    }

    public void setStart(String start)
    {
        this.start = start;
    }

    public String getEnd()
    {
        return end;
    }

    public void setEnd(String end)
    {
        this.end = end;
    }

    public Map<String, String> getEvent()
    {
        return event;
    }

    public void setEvent(Map<String, String> event)
    {
        this.event = event;
    }

    public Map<String, String> getPrecision()
    {
        return precision;
    }

    public void setPrecision(Map<String, String> precision)
    {
        this.precision = precision;
    }

    public Map<String, String> getComment()
    {
        return comment;
    }

    public void setComment(Map<String, String> comment)
    {
        this.comment = comment;
    }
}