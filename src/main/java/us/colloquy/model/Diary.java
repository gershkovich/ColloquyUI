package us.colloquy.model;

import java.util.Date;

/**
 * Created by Peter Gershkovich on 2/11/18.
 */
public class Diary
{
    private Date date;
    private String place;
    private String entry;
    private String note;

    public Date getDate()
    {
        return date;
    }

    public void setDate(Date date)
    {
        this.date = date;
    }

    public String getPlace()
    {
        return place;
    }

    public void setPlace(String place)
    {
        this.place = place;
    }

    public String getEntry()
    {
        return entry;
    }

    public void setEntry(String entry)
    {
        this.entry = entry;
    }

    public String getNote()
    {
        return note;
    }

    public void setNote(String note)
    {
        this.note = note;
    }
}
