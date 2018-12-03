package us.colloquy.model;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Peter Gershkovich on 11/29/18.
 */
public class Work
{
    private String originalTitle;
    private Map<String, String> title = new LinkedHashMap<>();
    private List<Event> events = new LinkedList<>();

    public String getOriginalTitle()
    {
        return originalTitle;
    }

    public void setOriginalTitle(String originalTitle)
    {
        this.originalTitle = originalTitle;
    }

    public Map<String, String> getTitle()
    {
        return title;
    }

    public void setTitle(Map<String, String> title)
    {
        this.title = title;
    }

    public List<Event> getEvents()
    {
        return events;
    }

    public void setEvents(List<Event> events)
    {
        this.events = events;
    }
}
