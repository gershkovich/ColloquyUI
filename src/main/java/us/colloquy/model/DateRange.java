package us.colloquy.model;

/**
 * Created by Peter Gershkovich on 12/31/17.
 */
public class DateRange
{
    //this is the date range (as long) that was selected by a user
    //intended to store in session for convenience
    private long start;
    private long end;

    public long getStart()
    {
        return start;
    }

    public void setStart(long start)
    {
        this.start = start;
    }

    public long getEnd()
    {
        return end;
    }

    public void setEnd(long end)
    {
        this.end = end;
    }
}
